package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for executing common SQL queries against the database.
 * Uses MetadataService to obtain table and column information.
 * @author Gabe Luciano
 */
public class QueryManager {
    /**
     * Specification for a parameterized WHERE clause:
     * - clause: the SQL fragment (e.g. "col1 LIKE ? OR col2 LIKE ?")
     * - param: the value to bind to each placeholder
     * - copies: number of times the param should be bound
     */
    private record QuerySpecification(String clause, String param, int copies) { }

    /// JDBC Connection used for all queries.
    private final Connection connection;

    /// Service for retrieving table and column metadata.
    private final MetadataService metadata;

    /**
     * Constructs a QueryManager using the singleton ConnectionManager and a fresh MetadataService.
     */
    public QueryManager() {
        this.connection = ConnectionManager.getInstance().getConnection();
        this.metadata = new MetadataService();
    }

    /**
     * Executes the SELECT and returns each row as a Map.
     */
    private List<Map<String, Object>> runQuery(String tableName, QuerySpecification spec) {

        List<Map<String, Object>> results = new ArrayList<>();
        String where = (spec == null) ? "" : " WHERE" + spec.clause;
        String sql   = "SELECT * FROM " + formatString(tableName) + where;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (spec != null) {
                for (int i = 1; i <= spec.copies; i++) {
                    ps.setString(i, spec.param);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<ColumnData> cols = metadata.getColumns(tableName);
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    for (ColumnData c : cols) {
                        map.put(c.getName(), rs.getString(c.getName()));
                    }
                    results.add(map);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    /**
     * Retrieves all rows from the specified table.
     *
     * @param tableName the name of the table to query
     * @return a list of rows, each represented as a map from column name to value
     */
    public List<Map<String, Object>> selectAll(String tableName) {
        return runQuery(tableName, null);
    }

    /**
     * Searches text columns of the specified table for the given query string.
     * Only columns whose type name contains CHAR, TEXT, or MEMO are included.
     *
     * @param tableName   the name of the table to search
     * @param queryString the substring to search for in text columns
     * @return a list of matching rows, each as a map from column to value
     */
    public List<Map<String, Object>> search(String tableName, String queryString) {

        List<ColumnData> columns = metadata.getColumns(tableName);
        List<String> textCols = new ArrayList<>();

        for (ColumnData c : columns) {
            if (c.getType().toUpperCase().matches(".*(CHAR|TEXT|MEMO).*")) {
                textCols.add(c.getName());
            }
        }

        if (textCols.isEmpty()) return List.of();

        String where = textCols.stream()
                .map(c -> formatString(c) + " LIKE ?")
                .collect(Collectors.joining(" OR "));
        QuerySpecification spec = new QuerySpecification(where, "%" + queryString + "%", textCols.size());

        return runQuery(tableName, spec);
    }

    /**
     * Updates a single row in the given table.
     * Uses the first primary-key column to identify the row.
     *
     * @param tableName the name of the table to update
     * @param rows      a map of column names to new values
     * @param pkValue   the primary-key value identifying which row to update
     */
    public void updateRow(String tableName, Map<String, Object> rows, Object pkValue) {
        ColumnData pk = metadata.getColumns(tableName).stream()
                .filter(ColumnData::isPrimaryKey)
                .findFirst()
                .orElseThrow();

        List<String> upCols = rows.keySet().stream()
                .filter(c -> !c.equals(pk.getName()))
                .toList();

        String set = upCols.stream()
                .map(c -> formatString(c) + "=?")
                .collect(Collectors.joining(", "));

        String sql = "UPDATE " + formatString(tableName) + " SET " + set +
                     " WHERE " + formatString(pk.getName()) + "=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int i = 1;
            for (String c : upCols) {
                ps.setObject(i++, rows.get(c));
            }
            ps.setObject(i, pkValue);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Formats a raw identifier (table or column name) by surrounding it with
     * brackets and escaping any closing bracket within.
     *
     * @param identifier the raw table or column name
     * @return the safely quoted identifier for use in SQL
     */
    private static String formatString(String identifier) {
        return "[" + identifier.replace("]", "]]") + "]";
    }

    /**
     * Inserts a single row; columns map may omit AUTOINCREMENT primary key.
     *
     * @param table  the table to insert into
     * @param values column → value map (nulls allowed)
     */
    public void insertRow(String table, Map<String, Object> values) {

        if (values.isEmpty()) return;

        List<String> cols = new ArrayList<>(values.keySet());
        String colSql = cols.stream().map(QueryManager::formatString).collect(Collectors.joining(", "));
        String marks  = cols.stream().map(c -> "?").collect(Collectors.joining(", "));

        String sql = "INSERT INTO " + formatString(table) +
                     " (" + colSql + ") VALUES (" + marks + ")";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int i = 1;
            for (String c : cols) {
                Object v = values.get(c);
                if (v == null) {
                    ps.setNull(i++, Types.VARCHAR);   // avoid NPE from UCanAccess when setting null
                } else {
                    ps.setObject(i++, v);
                }
            }
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}

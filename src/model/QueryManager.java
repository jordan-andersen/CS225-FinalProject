package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
     * Constructs a QueryService using the singleton ConnectionManager and a fresh MetadataService.
     */
    public QueryManager() {
        this.connection = ConnectionManager.getInstance().getConnection();
        this.metadata = new MetadataService();
    }

    private List<Map<String, Object>> runQuery(String tableName, QuerySpecification querySpecification) {

        List<Map<String, Object>> results = new ArrayList<>();
        String where;
        String statementString;

        if (querySpecification == null) {
            where = "";
        } else {
            where = "WHERE" + querySpecification.clause;
        }

        statementString = "SELECT * FROM " + formatString(tableName) + where;

        try(PreparedStatement preparedStatement = connection.prepareStatement(statementString)) {
            if (querySpecification != null) {
                for (int i = 0; i < querySpecification.copies; i++) {
                    preparedStatement.setString(i, querySpecification.param);
                }
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
               List<ColumnData> columns = metadata.getColumns(tableName);
               while (resultSet.next()) {
                   Map<String, Object> rowMap = new HashMap<>();
                   for (ColumnData column : columns) {
                       rowMap.put(column.getName(), resultSet.getString(column.getName()));
                   }
                   results.add(rowMap);
               }
            }
        }
        catch (SQLException e) {
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
        List<String> textColumns = new ArrayList<>();
        List<Map<String, Object>> results = new ArrayList<>();

        for (ColumnData column : columns) {
            if (column.getName().equalsIgnoreCase("TEXT") ||
                    column.getName().equalsIgnoreCase("MEMO") ||
                    column.getName().equalsIgnoreCase("CHAR")) {
                textColumns.add(column.getName());
            }
        }

        if (textColumns.isEmpty()) {
            return results;
        }
        String where = "";
        for (String column : textColumns) {
           where += formatString(column) + " LIKE ? OR ";
        }
        QuerySpecification spec = new QuerySpecification(where, "%" + queryString + "%", textColumns.size());

        return runQuery(tableName, spec);
    }

    /**
     * Updates a single row in the given table.
     * Uses the first primary-key column to identify the row.
     *
     * @param tableName the name of the table to update
     * @param rows      a map of column names to new values (must include primary key)
     * @param dataValue  the value of the primary key identifying which row to update
     */
    public void updateRow(String tableName, Map<String, Object> rows, Object dataValue) {
        ColumnData columnData = metadata.getColumns(tableName).stream()
                .filter(ColumnData::isPrimaryKey)  // Filters based on if the ColumnInfo's primaryKey attribute is true.
                .findFirst()
                .orElseThrow();

        List<String> upCols = rows.keySet().stream()
                .filter(c -> !c.equals(columnData.getName()))
                .toList();

        String set = upCols.stream()
                .map(c -> formatString(c) + "=?")
                .collect(Collectors.joining(", "));

        String statementString = "UPDATE " + formatString(tableName) + " SET " + set + " WHERE " + formatString(columnData.getName()) + "=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(statementString)) {
            int i = 1;
            for (String c : upCols) {
                preparedStatement.setObject(i++, rows.get(c));
            }
            preparedStatement.setObject(i, dataValue);
            preparedStatement.executeUpdate();
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
}

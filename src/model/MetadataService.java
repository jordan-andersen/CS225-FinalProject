package model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides methods for retrieving database metadata such as table names and column definitions.
 *
 * @author David H., Jordan A.
 */

public class MetadataService {
    /// JDBC connection used to obtain metadata.
    private final Connection connection;

    /**
     * Constructs a {@link MetadataService} using the singleton {@link ConnectionManager}.
     */
    public MetadataService() {
        this.connection = ConnectionManager.getInstance().getConnection();
    }

    /**
     * Retrieves the names of all user tables in the database,
     * excluding system tables whose names start with "MSys".
     *
     * @return a list of table names that do not start with "MSys"
     * @throws RuntimeException if a database access error occurs
     */
    public List<String> listTables() {
        List<String> tableList = new ArrayList<>();

        try (ResultSet rs = connection.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");

                //This will ignore tables that start with "MSys"
                if (!tableName.startsWith("MSys")) {
                    tableList.add(tableName);
                }
            }
        } catch (SQLException e) {
            //This catch will wrap and throw any SQL exceptions that are left as unchecked runtime errors
            throw new RuntimeException("Error retrieving table list", e);
        }

        return tableList;
    }

    /**
     * Retrieves metadata about the columns of the specified table.
     * Each ColumnData contains the column name, data type, and a flag
     * indicating whether it is part of the primary key.
     *
     * @param tableName the name of the table whose column metadata is retrieved
     * @return a list of ColumnData for each column in the given table
     * @throws RuntimeException if a database access error occurs
     */
    public List<ColumnData> getColumns(String tableName) {
        List<ColumnData> columns = new ArrayList<>();
        Set<String> primaryKeys = new HashSet<>();

        try {
            try (ResultSet pkRs = connection.getMetaData().getPrimaryKeys(null, null, tableName)) {
                while (pkRs.next()) {
                    primaryKeys.add(pkRs.getString("COLUMN_NAME"));
                }
            }

            try (ResultSet colRs = connection.getMetaData().getColumns(null, null, tableName, "%")) {
                while (colRs.next()) {
                    String colName = colRs.getString("COLUMN_NAME");
                    String colType = colRs.getString("TYPE_NAME");
                    boolean isPk = primaryKeys.contains(colName);

                    //Construct and store column metadata object
                    ColumnData columnData = new ColumnData(colName, colType, isPk);
                    columns.add(columnData);
                }
            }
        } catch (SQLException e) {
            //This catch will wrap and throw any SQL exceptions that are left as unchecked runtime errors
            throw new RuntimeException("Error retrieving column metadata", e);
        }

        return columns;
    }

}

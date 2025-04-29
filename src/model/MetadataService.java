package model;

import java.sql.Connection;
import java.util.List;

/**
 * Provides methods for retrieving database metadata such as table names and column definitions.
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
        /*
         - Create an empty list called tableList
         - Call connection.getMetaData().getTables(null, null, "%", ["TABLE"]) to get all table metadata
         - For each row in the ResultSet:
            - Read the TABLE_NAME column into tableName
            - If tableName does NOT start with "MSys":
                -Add tableName to tableList
         - Return tableList
         */
        return null;
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
        /*
         - Create an empty list called columns
         - Create an empty set called primaryKeys

         - Call connection.getMetaData().getPrimaryKeys(null, null, tableName)
            - For each row in the ResultSet:
                - Read the COLUMN_NAME column and add it to primaryKeys

         - Call connection.getMetaData().getColumns(null, null, tableName, "%")
            - For each row in the ResultSet:
                - Read COLUMN_NAME into colName
                - Read TYPE_NAME into colType
                - Determine isPk = primaryKeys.contains(colName)
                - Create a new ColumnData(colName, colType, isPk)
                - Add this ColumnData to columns
         - Return columns
         */
        return null;
    }

}

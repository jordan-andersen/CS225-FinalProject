package model;

/**
 * Represents metadata for a database table column.
 * Contains the column name, its SQL data type, and a flag indicating
 * whether it is part of the table's primary key.
 *
 * @author --
 */
public record ColumnData(
        /// The name of the column.
        String name,

        /// The SQL type name of the column (e.g., VARCHAR, INTEGER).
        String type,

        /// True if this column is part of the primary key; false otherwise.
        boolean isPrimaryKey) { }

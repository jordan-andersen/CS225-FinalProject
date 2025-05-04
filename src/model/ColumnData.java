package model;

/**
 * Represents metadata for a database table column.
 * Contains the column name, its SQL data type, and a flag indicating
 * whether it is part of the table's primary key.
 *
 * @author -- Brad Jn-Baptiste
 */
public class ColumnData{
        /// The name of the column.
        String name;

        /// The SQL type name of the column (e.g., VARCHAR, INTEGER).
        String type;

        /// True if this column is part of the primary key; false otherwise.
        boolean isPrimaryKey;

public ColumnData(String name, String type, boolean isPrimaryKey){
        this.name = name;
        this.type = type;
        this.isPrimaryKey = isPrimaryKey;
}

public String getName(){
        return name;
}
public void setName(String name){
        this.name = name;
}
public String getType(){
        return type;
}
public void setType(){
        this.type = type;
}
public boolean isPrimaryKey(){
        return isPrimaryKey;
}
public void setPrimaryKey(boolean isPrimaryKey){
        isPrimaryKey = primaryKey;
}

// the isPrimaryKey ? is if-else statement
@Override
public String toString(){
        return name + " (" + type + ") " + (isPrimaryKey ? " Primary Key: " : "");
        }
}

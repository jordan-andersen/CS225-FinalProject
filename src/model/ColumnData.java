package model;

import java.util.Objects;

/**
 * Represents metadata for a database table column.
 * Contains the column name, its SQL data type, and a flag indicating
 * whether it is part of the table's primary key.
 *
 * @author -- Brad Jn-Baptiste
 */
public class ColumnData {
        /// The name of the column.
        private final String name;

        /// The SQL type name of the column (e.g., VARCHAR, INTEGER).
        private final String type;

        /// True if this column is part of the primary key; false otherwise.
        private final boolean isPrimaryKey;

        public ColumnData(String name, String type, boolean isPrimaryKey) {
                this.name = name;
                this.type = type;
                this.isPrimaryKey = isPrimaryKey;
        }

        public String getName() {
                return name;
        }

        public String getType() {
                return type;
        }

        public boolean isPrimaryKey() {
                return isPrimaryKey;
        }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                ColumnData that = (ColumnData) o;
                return isPrimaryKey() == that.isPrimaryKey() && Objects.equals(getName(), that.getName()) && Objects.equals(getType(), that.getType());
        }

        @Override
        public int hashCode() {
                return Objects.hash(getName(), getType(), isPrimaryKey());
        }

        @Override
        public String toString() {
                return "ColumnData{" +
                        "name='" + name + '\'' +
                        ", type='" + type + '\'' +
                        ", isPrimaryKey=" + isPrimaryKey +
                        '}';
        }
}
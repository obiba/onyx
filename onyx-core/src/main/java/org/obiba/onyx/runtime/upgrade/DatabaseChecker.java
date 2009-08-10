/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.runtime.upgrade;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

public class DatabaseChecker {

  private JdbcTemplate jdbcTemplate;

  public DatabaseChecker(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Indicates whether the specified table exists.
   * 
   * @param tableName the table name
   * @return <code>true</code> if the table exists
   */
  public boolean isTableExists(final String tableName) {
    boolean tablePresent = false;

    try {
      tablePresent = (Boolean) JdbcUtils.extractDatabaseMetaData(jdbcTemplate.getDataSource(), new DatabaseMetaDataCallback() {
        public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
          return dbmd.getTables(null, null, tableName, null).next();
        }
      });
    } catch(MetaDataAccessException ex) {
      throw new RuntimeException(ex);
    }

    return tablePresent;
  }

  /**
   * Indicates whether the specified table contains the specified column.
   * 
   * @param tableName the table name
   * @param columnName the column name
   * @return <code>true</code> if the column exists in the table
   */
  public boolean hasColumn(final String tableName, final String columnName) {
    boolean columnPresent = false;

    try {
      columnPresent = (Boolean) JdbcUtils.extractDatabaseMetaData(jdbcTemplate.getDataSource(), new DatabaseMetaDataCallback() {
        public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
          return dbmd.getColumns(null, null, tableName, columnName).next();
        }
      });
    } catch(MetaDataAccessException ex) {
      throw new RuntimeException(ex);
    }

    return columnPresent;
  }
}

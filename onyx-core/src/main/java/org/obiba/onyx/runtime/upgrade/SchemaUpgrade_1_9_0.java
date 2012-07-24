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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import liquibase.change.Change;
import liquibase.change.DropForeignKeyConstraintChange;
import liquibase.database.Database;
import liquibase.database.sql.visitor.SqlVisitor;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.ForeignKey;
import liquibase.exception.JDBCException;

import org.obiba.runtime.upgrade.support.LiquiBaseUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaUpgrade_1_9_0 extends LiquiBaseUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(SchemaUpgrade_1_9_0.class);

  private static final String[] TABLES = { "action", "questionnaire_participant", "instrument_run", "measure", "experimental_condition" };

  private Database database;

  //
  // LiquiBaseUpgradeStep Methods
  //

  @Override
  public void applyChanges() {
    this.database = getDatabase();
    try {
      List<SqlVisitor> visitors = new ArrayList<SqlVisitor>();
      for(Change change : getChanges()) {
        change.executeStatements(database, visitors);
      }
    } catch(Exception ex) {
      throw new RuntimeException("Could not apply change to the database", ex);
    }
  }

  protected List<Change> getChanges() {
    List<Change> changes = new ArrayList<Change>();

    // Get a snapshot of the current database.
    DatabaseSnapshot databaseSnapshot = null;
    try {
      databaseSnapshot = database.createDatabaseSnapshot(null, null);

    } catch(JDBCException ex) {
      throw new RuntimeException("Could not create a database snapshot", ex);
    }

    Map<String, String> fkMap = new HashMap<String, String>();
    for(ForeignKey fk : databaseSnapshot.getForeignKeys()) {
      if(fk.getForeignKeyColumns().equals("user_id")) {
        fkMap.put(fk.getForeignKeyTable().getName(), fk.getName());
      }
    }

    // Drop user_id constraints and columns
    for(String table : TABLES) {
      if(fkMap.containsKey(table)) {
        log.debug("Dropping foreign key {} from table {}", new Object[] { fkMap.get(table), table });
        changes.add(createDropForeignKeyConstraintChange(table, fkMap.get(table)));
        log.debug("Dropping user_id column of table {}", table);
        changes.add(createDropColumnChange(databaseSnapshot, table, "user_id"));
      }
    }

    return changes;
  }

  private DropForeignKeyConstraintChange createDropForeignKeyConstraintChange(String table, String constraint) {
    DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
    change.setBaseTableName(table);
    change.setConstraintName(constraint);
    return change;
  }

}
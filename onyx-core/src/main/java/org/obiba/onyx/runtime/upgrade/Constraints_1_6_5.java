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
import java.util.List;

import liquibase.change.AddUniqueConstraintChange;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.exception.JDBCException;

import org.obiba.runtime.upgrade.support.LiquiBaseUpgradeStep;

public class Constraints_1_6_5 extends LiquiBaseUpgradeStep {
  //
  // LiquiBaseUpgradeStep Methods
  //

  protected List<Change> getChanges() {
    List<Change> changes = new ArrayList<Change>();

    // Get a snapshot of the current database.
    DatabaseSnapshot databaseSnapshot = null;
    try {
      Database database = getDatabase();
      databaseSnapshot = database.createDatabaseSnapshot(null, null);
    } catch(JDBCException ex) {
      throw new RuntimeException("Could not create a database snapshot", ex);
    }

    // participant_tube_registration
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "participant_tube_registration", "tube_set_name"));
    // Seems like Hibernate uses the first column's name as the constraint's name (it will append _2, _3 when multiple
    // constraints on the same column exists)
    changes.add(createUniqueConstraintChange(databaseSnapshot, "participant_tube_registration", "interview_id", "interview_id", "tube_set_name"));
    return changes;
  }

  /**
   * 
   * @param databaseSnapshot
   * @param table the table to change
   * @param name the name of the constraint.
   * @param columns the column(s) part of the constraint
   * @return
   */
  public AddUniqueConstraintChange createUniqueConstraintChange(DatabaseSnapshot databaseSnapshot, String table, String name, String... columns) {
    AddUniqueConstraintChange change = new AddUniqueConstraintChange();
    change.setTableName(table);
    change.setConstraintName(name);

    StringBuilder sb = new StringBuilder();
    for(String column : columns) {
      if(sb.length() > 0) sb.append(',');
      sb.append(column);
    }
    change.setColumnNames(sb.toString());
    return change;
  }
}
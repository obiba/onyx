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

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.exception.JDBCException;

import org.obiba.runtime.upgrade.support.LiquiBaseUpgradeStep;

public class SchemaUpgrade2_1_6_0 extends LiquiBaseUpgradeStep {
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

    // Drop participant exported and export_date columns (ONYX-1037).
    changes.add(createDropColumnChange(databaseSnapshot, "participant", "exported"));
    changes.add(createDropColumnChange(databaseSnapshot, "participant", "export_date"));

    return changes;
  }
}
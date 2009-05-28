/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.runtime.upgrade.support;

import org.obiba.onyx.runtime.upgrade.AbstractDatabaseUpgradeStep;
import org.obiba.runtime.Version;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.jdbc.SimpleJdbcTestUtils;

public class SqlScriptUpgradeStep extends AbstractDatabaseUpgradeStep {

  private Resource script;

  public Resource getScript() {
    return script;
  }

  public void setScript(Resource script) {
    this.script = script;
  }

  public void execute(Version currentVersion, SimpleJdbcTemplate template) {
    try {
      SimpleJdbcTestUtils.executeSqlScript(template, script, false);
    } catch(DataAccessException ex) {
      throw new RuntimeException("There was an error while executing the upgrade sql script: " + ex);
    }
  }
}

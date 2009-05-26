/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.runtime.upgrade.impl;

import java.io.IOException;

import org.obiba.onyx.runtime.upgrade.AbstractDatabaseUpgradeStep;
import org.obiba.onyx.util.FileUtil;
import org.obiba.runtime.Version;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

public class SqlScriptUpgradeStep extends AbstractDatabaseUpgradeStep {

  private Resource script;

  public Resource getScript() {
    return script;
  }

  public void setScript(Resource script) {
    this.script = script;
  }

  public void execute(Version currentVersion, JdbcTemplate template) {
    try {
      String scriptString = FileUtil.readString(script.getInputStream());
      template.execute(scriptString);
    } catch(IOException ex) {
      throw new RuntimeException("Error in reading sql script: " + ex);
    }
  }
}

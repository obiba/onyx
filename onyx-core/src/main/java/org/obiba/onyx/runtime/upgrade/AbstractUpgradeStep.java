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

import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.UpgradeStep;

public abstract class AbstractUpgradeStep implements UpgradeStep {

  private String description;

  private Version appliesTo;

  public Version getAppliesTo() {
    return appliesTo;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setAppliesTo(Version appliesTo) {
    this.appliesTo = appliesTo;
  }

}

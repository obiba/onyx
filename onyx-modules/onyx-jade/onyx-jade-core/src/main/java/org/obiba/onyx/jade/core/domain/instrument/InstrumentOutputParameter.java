/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.instrument;

public class InstrumentOutputParameter extends InstrumentParameter {

  private static final long serialVersionUID = 1070862021923112847L;

  // ignored when instrument type is itself not repeatable
  private Boolean repeatable;

  public InstrumentOutputParameter() {
    super();
  }

  public void setRepeatable(Boolean repeatable) {
    this.repeatable = repeatable;
  }

  public Boolean getRepeatable() {
    return repeatable;
  }

}

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

import org.obiba.onyx.jade.core.service.InputSourceVisitor;

public class FixedSource extends InputSource {

  private static final long serialVersionUID = -55114802454360982L;

  private String value;

  public FixedSource() {
    super();
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public void accept(InputSourceVisitor visitor) {
    visitor.visit(this);
  }

}

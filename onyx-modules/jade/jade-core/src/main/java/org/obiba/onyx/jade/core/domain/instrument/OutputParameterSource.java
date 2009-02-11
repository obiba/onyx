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

public class OutputParameterSource extends InputSource {

  private static final long serialVersionUID = 79789789454360982L;

  private String instrumentType;

  private String parameterName;

  public OutputParameterSource() {
    super();
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  public String getInstrumentType() {
    return instrumentType;
  }

  public void setInstrumentType(String instrumentType) {
    this.instrumentType = instrumentType;
  }

  public String getParameterName() {
    return parameterName;
  }

  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  @Override
  public void accept(InputSourceVisitor visitor) {
    visitor.visit(this);
  }

}

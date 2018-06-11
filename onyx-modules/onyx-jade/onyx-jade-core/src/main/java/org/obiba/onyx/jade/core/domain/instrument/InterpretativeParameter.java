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

import org.obiba.onyx.util.data.DataType;

public class InterpretativeParameter extends InstrumentParameter {

  private static final long serialVersionUID = -5035544856948727535L;

  public static final String YES = "Yes";

  public static final String NO = "No";

  private ParticipantInteractionType type;

  public InterpretativeParameter() {
    super();
    setDataType(DataType.TEXT);
    setCaptureMethod(InstrumentParameterCaptureMethod.MANUAL);
  }

  public ParticipantInteractionType getType() {
    return type;
  }

  public void setType(ParticipantInteractionType type) {
    this.type = type;
  }

}

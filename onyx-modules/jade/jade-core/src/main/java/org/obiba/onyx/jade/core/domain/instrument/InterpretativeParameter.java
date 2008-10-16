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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.obiba.onyx.util.data.DataType;

@Entity
@DiscriminatorValue("InterpretativeParameter")
public class InterpretativeParameter extends InstrumentParameter {

  private static final long serialVersionUID = -5035544856948727535L;

  @Enumerated(EnumType.STRING)
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

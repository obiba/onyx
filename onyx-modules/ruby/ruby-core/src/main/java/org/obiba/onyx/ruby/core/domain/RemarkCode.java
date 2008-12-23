/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.obiba.core.domain.AbstractEntity;

@Entity
public class RemarkCode extends AbstractEntity {

  private static final long serialVersionUID = -5200773148463248159L;

  private String code;

  @ManyToOne
  @JoinColumn(name = "registered_participant_tube_id")
  private RegisteredParticipantTube registeredParticipantTube;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public RegisteredParticipantTube getRegisteredParticipantTube() {
    return registeredParticipantTube;
  }

  public void setRegisteredParticipantTube(RegisteredParticipantTube registeredParticipantTube) {
    this.registeredParticipantTube = registeredParticipantTube;
  }

  public RemarkCode() {
  }

  public RemarkCode(String code) {
    this.code = code;
  }

}

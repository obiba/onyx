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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.service.UserSessionService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.NoSuchMessageException;

@Entity
public class ContraIndication extends AbstractEntity {

  private static final long serialVersionUID = 13324234234234L;

  @Transient
  private transient ApplicationContext context;

  @Transient
  private transient UserSessionService userSessionService;
  
  @ManyToOne
  @JoinColumn(name = "instrument_id")
  private Instrument instrument;

  @Column(length = 200)
  @Index(name = "name_index")
  private String name;

  @Column(length = 200)
  private String description;

  @Enumerated(EnumType.STRING)
  private ParticipantInteractionType type;
  
  public void setApplicationContext(ApplicationContext context) {
    this.context = context;
  }

  public void setUserSessionService(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
  }
  
  public Instrument getInstrument() {
    return instrument;
  }

  public void setInstrument(Instrument instrument) {
    this.instrument = instrument;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    String retVal = description;

    if(context != null && userSessionService != null) {
      try {
        retVal = context.getMessage(description, null, userSessionService.getLocale());
      } catch(NoSuchMessageException ex) {
        ; // return non-localized description
      }
    }

    return retVal;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ParticipantInteractionType getType() {
    return type;
  }

  public void setType(ParticipantInteractionType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return getName();
  }

}

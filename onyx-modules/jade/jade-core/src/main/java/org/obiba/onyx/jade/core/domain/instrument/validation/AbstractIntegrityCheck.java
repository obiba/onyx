/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.instrument.validation;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.springframework.context.ApplicationContext;

@Entity(name = "IntegrityCheck")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "integrity_check_type", discriminatorType = DiscriminatorType.STRING, length = 100)
public abstract class AbstractIntegrityCheck extends AbstractEntity implements IntegrityCheck {

  @Enumerated(EnumType.STRING)
  private IntegrityCheckType type;

  @ManyToOne
  @JoinColumn(name = "instrument_parameter_id")
  private InstrumentParameter targetParameter;

  private String customizedDescription;

  @Transient
  protected transient ApplicationContext context;

  @Transient
  protected transient UserSessionService userSessionService;

  public void setApplicationContext(ApplicationContext context) {
    this.context = context;
  }

  public void setUserSessionService(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
  }

  public void setType(IntegrityCheckType type) {
    this.type = type;
  }

  public IntegrityCheckType getType() {
    if(type == null) {
      type = IntegrityCheckType.ERROR;
    }

    return type;
  }

  public void setTargetParameter(InstrumentParameter targetParameter) {
    this.targetParameter = targetParameter;
  }

  public InstrumentParameter getTargetParameter() {
    return targetParameter;
  }

  public void setCustomizedDescription(String customizedDescription) {
    this.customizedDescription = customizedDescription;
  }

  public String getCustomizedDescription() {
    return customizedDescription;
  }

  //
  // IntegrityCheck Methods
  //

  public abstract boolean checkParameterValue(Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService);

  public String getDescription(ActiveInstrumentRunService activeRunService) {
    String retVal = getClass().getSimpleName();

    if(context != null && userSessionService != null) {
      retVal = context.getMessage(getDescriptionKey(activeRunService), getDescriptionArgs(activeRunService), userSessionService.getLocale());
    }

    return retVal;
  }

  protected String getDescriptionKey(ActiveInstrumentRunService activeRunService) {
    String descriptionKey = getCustomizedDescription();

    if(descriptionKey == null) {
      descriptionKey = getClass().getSimpleName();
    }

    return descriptionKey;
  }

  protected abstract Object[] getDescriptionArgs(ActiveInstrumentRunService activeRunService);
}

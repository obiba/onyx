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

import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

public abstract class AbstractIntegrityCheck implements IntegrityCheck {

  private IntegrityCheckType type;

  private String customizedDescription;

  public void setType(IntegrityCheckType type) {
    this.type = type;
  }

  public IntegrityCheckType getType() {
    if(type == null) {
      type = IntegrityCheckType.ERROR;
    }

    return type;
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

  public abstract boolean checkParameterValue(InstrumentParameter checkedParameter, Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService);

  public MessageSourceResolvable getDescription(InstrumentParameter checkedParameter, ActiveInstrumentRunService activeRunService) {
    String[] codes = new String[] { getDescriptionKey(activeRunService) };
    return new DefaultMessageSourceResolvable(codes, getDescriptionArgs(checkedParameter, activeRunService));
  }

  protected String getDescriptionKey(ActiveInstrumentRunService activeRunService) {
    String descriptionKey = getCustomizedDescription();

    if(descriptionKey == null) {
      descriptionKey = getClass().getSimpleName();
    }

    return descriptionKey;
  }

  protected abstract Object[] getDescriptionArgs(InstrumentParameter checkedParameter, ActiveInstrumentRunService activeRunService);
}

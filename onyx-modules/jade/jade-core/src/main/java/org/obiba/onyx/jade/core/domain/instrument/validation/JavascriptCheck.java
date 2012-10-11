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

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.js.JavascriptValueSource;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.type.BooleanType;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.magma.DataValueConverter;
import org.obiba.onyx.magma.MagmaInstanceProvider;
import org.obiba.onyx.util.data.Data;

/**
 *
 */
public class JavascriptCheck extends AbstractIntegrityCheck {

  private static final long serialVersionUID = -8460705928474147080L;

  private String script;

  private transient MagmaInstanceProvider magmaInstanceProvider;

  @Override
  public boolean checkParameterValue(InstrumentParameter checkedParameter, Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService) {
    Value value = getValue(activeRunService.getParticipant(), checkedParameter, paramData);
    return value.isNull() ? false : (Boolean) value.getValue();
  }

  private Value getValue(Participant participant, InstrumentParameter checkedParameter, Data paramData) {
    VariableEntity entity = magmaInstanceProvider.newParticipantEntity(participant);
    ValueTable valueTable = magmaInstanceProvider.getParticipantsTable();
    if(valueTable.hasValueSet(entity)) {
      ValueSet valueSet = valueTable.getValueSet(entity);
      return getSource(checkedParameter, paramData).getValue(valueSet);
    }
    return BooleanType.get().nullValue();
  }

  public void setMagmaInstanceProvider(MagmaInstanceProvider magmaInstanceProvider) {
    this.magmaInstanceProvider = magmaInstanceProvider;
  }

  private ValueSource getSource(InstrumentParameter checkedParameter, Data paramData) {
    Value val = DataValueConverter.dataToValue(paramData);
    String newScript = script.replace("$('" + checkedParameter.getCode() + "')", "newValue('" + val.toString() + "','" + val.getValueType().getName() + "')");

    JavascriptValueSource source = new JavascriptValueSource(BooleanType.get(), newScript);
    Initialisables.initialise(source);
    return source;
  }

  @Override
  protected Object[] getDescriptionArgs(InstrumentParameter checkedParameter, ActiveInstrumentRunService activeRunService) {
    return new Object[] { checkedParameter.getLabel(), script };
  }

}

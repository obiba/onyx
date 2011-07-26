/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.data;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.js.JavascriptValueSource;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.magma.DataValueConverter;
import org.obiba.onyx.magma.MagmaInstanceProvider;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get data from a variable.
 */
public class JavascriptDataSource implements IDataSource {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(JavascriptDataSource.class);

  private static final String PARTICIPANT_TABLE_NAME = "Participants";

  private static final String PARTICIPANT_ENTITY_TYPE = "Participant";

  private String valueType;

  private String script;

  private String unit;

  private boolean sequence;

  private transient MagmaInstanceProvider magmaInstanceProvider;

  private transient ValueSource source;

  public JavascriptDataSource(String script) {
    this.script = script;
  }

  public String getScript() {
    return script;
  }

  @Override
  public Data getData(Participant participant) {
    log.debug("Resolving script: '{}'", script);
    if(participant == null) return null;

    Value value = getValue(participant);

    return DataValueConverter.valueToData(value);
  }

  private Value getValue(Participant participant) {
    // Get the stage's ValueTable.
    ValueTable onyxParticipantTable = magmaInstanceProvider.getValueTable(PARTICIPANT_TABLE_NAME);

    // Get the currently interviewed participant's ValueSet.
    VariableEntity entity = new VariableEntityBean(PARTICIPANT_ENTITY_TYPE, participant.getBarcode());
    ValueSet valueSet = onyxParticipantTable.getValueSet(entity);

    return getSource().getValue(valueSet);
  }

  public boolean isSequence() {
    return sequence;
  }

  public void setSequence(boolean sequence) {
    this.sequence = sequence;
  }

  @Override
  public String getUnit() {
    return unit;
  }

  @Override
  public String toString() {
    return script;
  }

  private ValueSource getSource() {
    if(source == null) {
      source = new JavascriptValueSource(ValueType.Factory.forName(valueType), script) {
        protected boolean isSequence() {
          return sequence;
        };
      };
      Initialisables.initialise(source);
    }
    return source;
  }

  public void setMagmaInstanceProvider(MagmaInstanceProvider magmaInstanceProvider) {
    this.magmaInstanceProvider = magmaInstanceProvider;
  }

}

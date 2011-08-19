/*******************************************************************************
 * Copyright 2011(c) OBiBa. All rights reserved.
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
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.magma.DataValueConverter;
import org.obiba.onyx.magma.MagmaInstanceProvider;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@code IDataSource} that executes a {@code Magma JavaScript} script to produce a {@code Data}
 * instance.
 */
public class JavascriptDataSource implements IDataSource {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(JavascriptDataSource.class);

  private String valueType;

  private String script;

  private String unit;

  private boolean sequence;

  private transient MagmaInstanceProvider magmaInstanceProvider;

  private transient ValueSource source;

  public JavascriptDataSource(String script, String valueType) {
    this.script = script;
    this.valueType = valueType;
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
    VariableEntity entity = magmaInstanceProvider.newParticipantEntity(participant.getBarcode());
    ValueTable valueTable = magmaInstanceProvider.getParticipantsTable();
    ValueSet valueSet = valueTable.getValueSet(entity);
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

  public void setValueType(String valueType) {
    this.valueType = valueType;
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

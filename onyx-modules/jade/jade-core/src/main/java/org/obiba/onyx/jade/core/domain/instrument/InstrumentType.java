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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.data.TypeConverterDataSource;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.DataType;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

public class InstrumentType implements Serializable {

  private static final long serialVersionUID = 23414234L;

  private String name;

  private String description;

  private IDataSource expectedMeasureCount;

  private List<InstrumentParameter> instrumentParameters;

  private List<Contraindication> contraindications;

  private boolean allowPartial;

  // Allows overriding values from defaults.properties
  private Properties properties;

  public InstrumentType() {
  }

  public InstrumentType(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public IDataSource getExpectedMeasureCount() {
    return expectedMeasureCount;
  }

  public void setExpectedMeasureCount(IDataSource expectedMeasureCount) {
    this.expectedMeasureCount = expectedMeasureCount;
  }

  public MessageSourceResolvable getInstructions() {
    return new DefaultMessageSourceResolvable(new String[] { name + ".instructions" }, "");
  }

  /**
   * Get the count of repeatable measures for the given participant, default count is 1.
   * @param participant
   * @return
   */
  public int getExpectedMeasureCount(Participant participant) {
    if(expectedMeasureCount == null) {
      return 1;
    } else {
      TypeConverterDataSource conv = new TypeConverterDataSource(expectedMeasureCount, DataType.INTEGER);
      String count = conv.getData(participant).getValueAsString();
      if(count == null) {
        return 1;
      } else {
        return Integer.parseInt(count);
      }
    }
  }

  public List<Contraindication> getContraindications() {
    return contraindications != null ? contraindications : (contraindications = new ArrayList<Contraindication>());
  }

  /**
   * 
   * @param ci
   * @return this for chaining
   */
  public InstrumentType addContraindication(Contraindication ci) {
    if(ci != null) {
      getContraindications().add(ci);
    }
    return this;
  }

  public List<InstrumentParameter> getInstrumentParameters() {
    return instrumentParameters != null ? instrumentParameters : (instrumentParameters = new ArrayList<InstrumentParameter>());
  }

  /**
   * Returns true if at least one {@link InstrumentOutputParameter} may be captured manually. This indicates that
   * measurements for this instrument may be captured either manually or automatically.
   * @return True if manual capture is permitted.
   */
  public boolean hasManualCaptureOutputParameters() {
    for(InstrumentOutputParameter parameter : getOutputParameters()) {
      if(isManualCapture(parameter)) return true;
    }
    return false;
  }

  /**
   * Returns a list of {@link InstrumentOutputParameter}s that permit manual capture. Normally these parameters would be
   * captured automatically, but if required they may also be captured manually.
   * @return A list of output parameters that permit manual capture.
   */
  public List<InstrumentOutputParameter> getManualCaptureOutputParameters() {
    List<InstrumentOutputParameter> outputParameters = new ArrayList<InstrumentOutputParameter>();

    for(InstrumentOutputParameter parameter : getOutputParameters()) {
      if(isManualCapture(parameter)) {
        outputParameters.add(parameter);
      }
    }

    return outputParameters;
  }

  /**
   * Check if the given parameter is to be captured manually.
   * @param parameter
   * @return
   */
  private boolean isManualCapture(InstrumentOutputParameter parameter) {
    return (InstrumentParameterCaptureMethod.AUTOMATIC.equals(parameter.getCaptureMethod()) && parameter.isManualCaptureAllowed()) //
        || (InstrumentParameterCaptureMethod.MANUAL.equals(parameter.getCaptureMethod()) && isRepeatable(parameter));
  }

  /**
   * 
   * @param parameter
   * @return this for chaining
   */
  public InstrumentType addInstrumentParameter(InstrumentParameter parameter) {
    if(parameter != null) {
      getInstrumentParameters().add(parameter);
    }
    return this;
  }

  /**
   * Get the instrument parameter searching by the code, and if not found, searching by the vendor name.
   * @param name
   * @return null if not found.
   */
  public InstrumentParameter getInstrumentParameter(String name) {
    return getInstrumentParameter(getInstrumentParameters(), name);
  }

  /**
   * Get the instrument parameter of a specific type searching by the code, and if not found, searching by the vendor
   * name.
   * @param <T>
   * @param parameterType
   * @param name
   * @return
   */
  public <T extends InstrumentParameter> T getInstrumentParameter(Class<T> parameterType, String name) {
    return getInstrumentParameter(getInstrumentParameters(parameterType), name);
  }

  private <T extends InstrumentParameter> T getInstrumentParameter(List<T> parameters, String name) {
    // Lookup the parameter using it's code
    T parameter = null;
    for(T param : parameters) {
      if(name.equals(param.getCode()) == true) {
        parameter = param;
        break;
      }
    }

    if(parameter == null) {
      // Lookup the parameter using it's vendor name
      for(T param : parameters) {
        if(name.equals(param.getVendorName()) == true) {
          parameter = param;
          break;
        }
      }
    }

    return parameter;
  }

  /**
   * Returns all of the <code>InstrumentType</code>'s parameters of the specified type.
   * @param <T>
   * @param parameterType type of parameter to return (i.e., the class)
   * @return
   */
  @SuppressWarnings("unchecked")
  public <T extends InstrumentParameter> List<T> getInstrumentParameters(Class<T> parameterType) {
    List<T> parameters = new ArrayList<T>();

    for(InstrumentParameter parameter : getInstrumentParameters()) {
      if(parameterType.isInstance(parameter)) {
        parameters.add((T) parameter);
      }
    }

    return parameters;
  }

  /**
   * Returns all of the <code>InstrumentType</code>'s parameters of the specified type and having, or not having, a data
   * source.
   * 
   * @param parameterType type of parameter to return (i.e., the class)
   * @param hasDataSource indicates whether the parameters returned should include those with or without a data source
   * @return parameters of the specified type with or without a data source, as specified (or an empty list, if none)
   */
  @SuppressWarnings("unchecked")
  public <T extends InstrumentParameter> List<T> getInstrumentParameters(Class<T> parameterType, boolean hasDataSource) {
    List<T> parameters = new ArrayList<T>();

    for(InstrumentParameter parameter : getInstrumentParameters(parameterType)) {
      if(!(parameter.getDataSource() != null ^ hasDataSource)) {
        parameters.add((T) parameter);
      }
    }

    return parameters;
  }

  public boolean hasInterpretativeParameter(ParticipantInteractionType type) {
    return !getInterpretativeParameters(type).isEmpty();
  }

  public List<InterpretativeParameter> getInterpretativeParameters(ParticipantInteractionType type) {
    List<InterpretativeParameter> interpretativeParameters = new ArrayList<InterpretativeParameter>();

    for(InstrumentParameter parameter : getInstrumentParameters()) {
      if(parameter instanceof InterpretativeParameter) {
        InterpretativeParameter interpretativeParameter = (InterpretativeParameter) parameter;

        if(type == null || interpretativeParameter.getType().equals(type)) {
          interpretativeParameters.add(interpretativeParameter);
        }
      }
    }

    return interpretativeParameters;
  }

  public boolean hasInterpretativeParameter() {
    return !getInterpretativeParameters(null).isEmpty();
  }

  public List<InterpretativeParameter> getInterpretativeParameters() {
    return getInterpretativeParameters(null);
  }

  public boolean hasInputParameter(boolean readOnly) {
    return !getInputParameters(readOnly).isEmpty();
  }

  public List<InstrumentInputParameter> getInputParameters(boolean readOnly) {
    return getInstrumentParameters(InstrumentInputParameter.class, readOnly);
  }

  public boolean hasInputParameter(InstrumentParameterCaptureMethod captureMethod) {
    return !getInputParameters(captureMethod).isEmpty();
  }

  public List<InstrumentInputParameter> getInputParameters(InstrumentParameterCaptureMethod captureMethod) {
    List<InstrumentInputParameter> inputParameters = new ArrayList<InstrumentInputParameter>();

    for(InstrumentParameter parameter : getInstrumentParameters()) {
      if(parameter instanceof InstrumentInputParameter) {
        InstrumentInputParameter inputParameter = (InstrumentInputParameter) parameter;
        InstrumentParameterCaptureMethod inputParameterCaptureMethod = inputParameter.getCaptureMethod();

        if(inputParameterCaptureMethod.equals(captureMethod)) {
          inputParameters.add(inputParameter);
        }
      }
    }

    return inputParameters;
  }

  public boolean hasInputParameter() {
    return !getInputParameters().isEmpty();
  }

  public List<InstrumentInputParameter> getInputParameters() {
    List<InstrumentInputParameter> inputParameters = new ArrayList<InstrumentInputParameter>();

    for(InstrumentParameter parameter : getInstrumentParameters()) {
      if(parameter instanceof InstrumentInputParameter) {
        InstrumentInputParameter inputParameter = (InstrumentInputParameter) parameter;
        inputParameters.add(inputParameter);
      }
    }

    return inputParameters;
  }

  public boolean hasOutputParameter(InstrumentParameterCaptureMethod captureMethod) {
    return !getOutputParameters(captureMethod).isEmpty();
  }

  public List<InstrumentOutputParameter> getOutputParameters(InstrumentParameterCaptureMethod captureMethod) {
    List<InstrumentOutputParameter> outputParameters = new ArrayList<InstrumentOutputParameter>();

    for(InstrumentOutputParameter parameter : getOutputParameters()) {
      if(parameter.getCaptureMethod().equals(captureMethod)) {
        outputParameters.add(parameter);
      }
    }

    return outputParameters;
  }

  public boolean hasOutputParameter(boolean automatic) {
    return !getOutputParameters(automatic).isEmpty();
  }

  public List<InstrumentOutputParameter> getOutputParameters(boolean automatic) {
    List<InstrumentOutputParameter> outputParameters = new ArrayList<InstrumentOutputParameter>();

    if(automatic) {
      outputParameters = getOutputParameters(InstrumentParameterCaptureMethod.AUTOMATIC);
    } else {
      for(InstrumentParameterCaptureMethod captureMethod : InstrumentParameterCaptureMethod.values()) {
        if(!captureMethod.equals(InstrumentParameterCaptureMethod.AUTOMATIC)) {
          outputParameters.addAll(getOutputParameters(captureMethod));
        }
      }
    }

    return outputParameters;
  }

  public boolean hasOutputParameter() {
    return !getOutputParameters().isEmpty();
  }

  public List<InstrumentOutputParameter> getOutputParameters() {
    List<InstrumentOutputParameter> outputParameters = new ArrayList<InstrumentOutputParameter>();

    for(InstrumentParameter parameter : getInstrumentParameters()) {
      if(parameter instanceof InstrumentOutputParameter) {
        InstrumentOutputParameter outputParameter = (InstrumentOutputParameter) parameter;
        outputParameters.add(outputParameter);
      }
    }

    return outputParameters;
  }

  /**
   * Shall we expect data from a remote instrument application ?
   * @param instrument
   * @return
   */
  public boolean isInteractive() {
    return !getOutputParameters(InstrumentParameterCaptureMethod.AUTOMATIC).isEmpty();
  }

  public Contraindication getContraindication(String contraindicationCode) {
    for(Contraindication ci : getContraindications()) {
      if(ci.getCode().equals(contraindicationCode)) return ci;
    }
    return null;
  }

  /**
   * An instrument is repeatable when the expected measure count datasource is defined.
   * @return
   */
  public boolean isRepeatable() {
    return expectedMeasureCount != null;
  }

  /**
   * Repeatable parameters are not computed output parameters in an instrument type that is repeatable. In addition to
   * that, this kind of parameters can be specifically specified as not being repeatable.
   * @param instrumentParameter
   * @return
   */
  public boolean isRepeatable(InstrumentParameter instrumentParameter) {
    if(isRepeatable() //
        && instrumentParameter instanceof InstrumentOutputParameter //
        && !instrumentParameter.getCaptureMethod().equals(InstrumentParameterCaptureMethod.COMPUTED)) {
      InstrumentOutputParameter outputParam = (InstrumentOutputParameter) instrumentParameter;
      Boolean repeatable = outputParam.getRepeatable();
      return repeatable == null ? true : repeatable;
    }
    return false;
  }

  @Override
  public String toString() {
    return name;
  }

  public boolean isAllowPartial() {
    return allowPartial;
  }

  public void setAllowPartial(boolean allowPartial) {
    this.allowPartial = allowPartial;
  }

  // Setter is not defined due to issues with autowiring (several beans of type Properties exist in the context)
  public Properties getProperties() {
    return properties;
  }

}

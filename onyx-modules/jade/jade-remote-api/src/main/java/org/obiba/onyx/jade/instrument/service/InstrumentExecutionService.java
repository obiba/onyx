package org.obiba.onyx.jade.instrument.service;

import java.util.Date;
import java.util.Map;

import org.obiba.onyx.util.data.Data;

/**
 * Defines the interface through which code running on local stations may communicate with the server.
 * <p>
 * This interface must be kept simple and should not exchange persisted objects, unless these have no relationship to
 * any other persisted entity.
 * 
 * @author plaflamm
 * 
 */
public interface InstrumentExecutionService {
  
  /**
   * Returns the name of the instrument's operator.
   * 
   * @return the instrument's operator name.
   */
  public String getInstrumentOperator();

  public String getParticipantID();
  
  public String getParticipantLastName();
  
  public String getParticipantFirstName();
  
  public Date getParticipantBirthDate();
  
  public String getParticipantGender();

  public Map<String, Data> getInputParametersValue(String... parameters);
  
  public Data getInputParameterValue(String parameter);

  public void addOutputParameterValues(Map<String, Data> values);

  public void addOutputParameterValue(String name, Data value);
  
  public void instrumentRunnerError(Exception error);

}

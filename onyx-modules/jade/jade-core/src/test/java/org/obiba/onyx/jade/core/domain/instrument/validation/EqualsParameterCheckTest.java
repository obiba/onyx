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

import static org.easymock.EasyMock.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

public class EqualsParameterCheckTest {

  private EqualsParameterCheck equalsParameterCheck;

  private ParticipantInterview interview;
  
  private InstrumentRun instrumentRun;
  
  private InstrumentType instrumentType;
  
  private Instrument instrument;
  
  private InstrumentParameter checkedParameter;
  
  private InstrumentParameter otherParameter;

  private InstrumentRunService instrumentRunServiceMock;
  
  private ActiveInstrumentRunService activeInstrumentRunServiceMock;
  
  @Before
  public void setUp() {
    equalsParameterCheck = new EqualsParameterCheck();

    interview = new ParticipantInterview();
    
    instrumentType = new InstrumentType();
    
    instrument = new Instrument();
    instrument.setInstrumentType(instrumentType);
    
    instrumentRun = new InstrumentRun();
    instrumentRun.setParticipantInterview(interview);
    instrumentRun.setInstrument(instrument);
    
    checkedParameter = new InstrumentOutputParameter();
    checkedParameter.setName("checkedParamName");
    
    otherParameter = new InstrumentInputParameter();
    otherParameter.setName("otherParamName");
    
    equalsParameterCheck.setTargetParameter(checkedParameter);
    equalsParameterCheck.setParameter(otherParameter);
    
    instrumentRunServiceMock = createMock(InstrumentRunService.class);
    
    activeInstrumentRunServiceMock = createMock(ActiveInstrumentRunService.class);
  }
  
  /**
   * Tests equal DataType.INTEGER parameters. 
   */
  @Test
  public void testIntegerParametersEqual() {
    checkedParameter.setDataType(DataType.INTEGER);
    otherParameter.setDataType(DataType.INTEGER);

    // Initialize checked parameter's run value.
    Data checkedData = DataBuilder.buildInteger(100l);
    
    // Initialize other parameter's run value.
    Data otherData = DataBuilder.buildInteger(100l);
    
    InstrumentRunValue otherRunValue = new InstrumentRunValue();
    otherRunValue.setInstrumentParameter(otherParameter);
    otherRunValue.setData(otherData);

    expect(activeInstrumentRunServiceMock.getInputInstrumentRunValue(otherParameter.getName())).andReturn(otherRunValue);   

    replay(activeInstrumentRunServiceMock);
        
    Assert.assertTrue(equalsParameterCheck.checkParameterValue(checkedData, instrumentRunServiceMock, activeInstrumentRunServiceMock));
    
    verify(activeInstrumentRunServiceMock);
  }
  
  /**
   * Tests unequal DataType.INTEGER parameters. 
   */
  @Test
  public void testIntegerParametersUnequal() {
    checkedParameter.setDataType(DataType.INTEGER);
    otherParameter.setDataType(DataType.INTEGER);

    // Initialize checked parameter's run value.
    Data checkedData = DataBuilder.buildInteger(100l);
    
    // Initialize other parameter's run value.
    Data otherData = DataBuilder.buildInteger(200l);
    
    InstrumentRunValue otherRunValue = new InstrumentRunValue();
    otherRunValue.setInstrumentParameter(otherParameter);
    otherRunValue.setData(otherData);

    expect(activeInstrumentRunServiceMock.getInputInstrumentRunValue(otherParameter.getName())).andReturn(otherRunValue);
   
    replay(activeInstrumentRunServiceMock);
        
    Assert.assertFalse(equalsParameterCheck.checkParameterValue(checkedData, instrumentRunServiceMock, activeInstrumentRunServiceMock));
    
    verify(activeInstrumentRunServiceMock);
  }
}

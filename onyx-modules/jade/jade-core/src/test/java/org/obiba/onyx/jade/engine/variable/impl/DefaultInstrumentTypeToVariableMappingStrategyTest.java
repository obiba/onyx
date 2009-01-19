/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.engine.variable.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import junit.framework.Assert;

import org.apache.wicket.spring.test.ApplicationContextMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.util.VariableStreamer;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.jade.engine.variable.IInstrumentTypeToVariableMappingStrategy;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class DefaultInstrumentTypeToVariableMappingStrategyTest {

  private static final Logger log = LoggerFactory.getLogger(DefaultInstrumentTypeToVariableMappingStrategyTest.class);

  private InstrumentRunService instrumentRunServiceMock;

  private InstrumentService instrumentServiceMock;

  private IInstrumentTypeToVariableMappingStrategy instrumentTypeToVariableMappingStrategy;

  private InstrumentType instrumentType;

  private InstrumentParameter inputParam;

  private InstrumentParameter outputParam;

  private InstrumentParameter interParam;

  @Before
  public void setUp() {
    ApplicationContextMock mockCtx = new ApplicationContextMock();
    instrumentRunServiceMock = createMock(InstrumentRunService.class);
    instrumentServiceMock = createMock(InstrumentService.class);
    mockCtx.putBean("instrumentRunService", instrumentRunServiceMock);
    mockCtx.putBean("instrumentService", instrumentServiceMock);

    instrumentTypeToVariableMappingStrategy = new DefaultInstrumentTypeToVariableMappingStrategy();
    ((DefaultInstrumentTypeToVariableMappingStrategy) instrumentTypeToVariableMappingStrategy).setInstrumentRunService(instrumentRunServiceMock);
    ((DefaultInstrumentTypeToVariableMappingStrategy) instrumentTypeToVariableMappingStrategy).setInstrumentService(instrumentServiceMock);

    instrumentType = createInstrumentType();
  }

  @Test
  public void testVariable() {
    Variable root = createInstrumentTypeVariable();

    Assert.assertEquals(1, root.getVariables().size());
    Variable var = root.getVariable(instrumentType.getName());
    Assert.assertNotNull(var);
    Assert.assertEquals(4, var.getVariables().size());

    Variable subVar = var.getVariable(DefaultInstrumentTypeToVariableMappingStrategy.INPUT);
    Assert.assertNotNull(subVar);

    subVar = var.getVariable(DefaultInstrumentTypeToVariableMappingStrategy.OUTPUT);
    Assert.assertNotNull(subVar);

    subVar = var.getVariable(DefaultInstrumentTypeToVariableMappingStrategy.INTERPRETIVE);
    Assert.assertNotNull(subVar);

    subVar = var.getVariable(DefaultInstrumentTypeToVariableMappingStrategy.INSTRUMENT_RUN);
    Assert.assertNotNull(subVar);
    Assert.assertEquals(6, subVar.getVariables().size());

    Variable subSubVar = subVar.getVariable(DefaultInstrumentTypeToVariableMappingStrategy.CONTRAINDICATION);
    Assert.assertNotNull(subSubVar);

    subSubVar = subVar.getVariable(DefaultInstrumentTypeToVariableMappingStrategy.INSTRUMENT);
    Assert.assertNotNull(subSubVar);
  }

  @Test
  public void testInstrumentInputParameter() {
    Variable root = createInstrumentTypeVariable();
    log.info(VariableStreamer.toXML(root));

    Variable variable = root.getVariable(instrumentType.getName()).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.INPUT).getVariable("INPUT_PARAM");
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    InstrumentRunValue runValue = new InstrumentRunValue();
    runValue.setInstrumentParameter(inputParam);
    runValue.setData(DataBuilder.buildText("coucou"));

    expect(instrumentServiceMock.getInstrumentType(instrumentType.getName())).andReturn(instrumentType);
    expect(instrumentRunServiceMock.findInstrumentRunValue(participant, instrumentType, "INPUT_PARAM")).andReturn(runValue);
    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);

    Data data = instrumentTypeToVariableMappingStrategy.getData(participant, variable);

    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertNotNull(data);
    Assert.assertEquals(DataType.TEXT, data.getType());
    Assert.assertEquals("coucou", data.getValue());
  }

  @Test
  public void testInstrumentOutputParameter() {
    Variable root = createInstrumentTypeVariable();
    log.info(VariableStreamer.toXML(root));

    Variable variable = root.getVariable(instrumentType.getName()).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.OUTPUT).getVariable("OUTPUT_PARAM");
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    InstrumentRunValue runValue = new InstrumentRunValue();
    runValue.setInstrumentParameter(outputParam);
    runValue.setData(DataBuilder.buildInteger(123l));

    expect(instrumentServiceMock.getInstrumentType(instrumentType.getName())).andReturn(instrumentType);
    expect(instrumentRunServiceMock.findInstrumentRunValue(participant, instrumentType, "OUTPUT_PARAM")).andReturn(runValue);
    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);

    Data data = instrumentTypeToVariableMappingStrategy.getData(participant, variable);

    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertNotNull(data);
    Assert.assertEquals(DataType.INTEGER, data.getType());
    Assert.assertEquals(123l, data.getValue());
  }

  @Test
  public void testInstrumentInterpretiveParameter() {
    Variable root = createInstrumentTypeVariable();
    log.info(VariableStreamer.toXML(root));

    Variable variable = root.getVariable(instrumentType.getName()).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.INTERPRETIVE).getVariable("INTERPRETIVE_PARAM");
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    InstrumentRunValue runValue = new InstrumentRunValue();
    runValue.setInstrumentParameter(interParam);
    runValue.setData(DataBuilder.buildText("coucou"));

    expect(instrumentServiceMock.getInstrumentType(instrumentType.getName())).andReturn(instrumentType);
    expect(instrumentRunServiceMock.findInstrumentRunValue(participant, instrumentType, "INTERPRETIVE_PARAM")).andReturn(runValue);
    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);

    Data data = instrumentTypeToVariableMappingStrategy.getData(participant, variable);

    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertNotNull(data);
    Assert.assertEquals(DataType.TEXT, data.getType());
    Assert.assertEquals("coucou", data.getValue());
  }

  @Test
  public void testInstrumentRun() {
    Variable root = createInstrumentTypeVariable();
    log.info(VariableStreamer.toXML(root));

    Variable variable = root.getVariable(instrumentType.getName()).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.INSTRUMENT_RUN).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.USER);
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    InstrumentRun run = new InstrumentRun();
    User user = new User();
    user.setLogin("toto");
    run.setUser(user);

    expect(instrumentServiceMock.getInstrumentType(instrumentType.getName())).andReturn(instrumentType);
    expect(instrumentRunServiceMock.getLastCompletedInstrumentRun(participant, instrumentType)).andReturn(run);
    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);

    Data data = instrumentTypeToVariableMappingStrategy.getData(participant, variable);

    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertNotNull(data);
    Assert.assertEquals(DataType.TEXT, data.getType());
    Assert.assertEquals("toto", data.getValue());
  }

  @Test
  public void testInstrument() {
    Variable root = createInstrumentTypeVariable();
    log.info(VariableStreamer.toXML(root));

    Variable variable = root.getVariable(instrumentType.getName()).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.INSTRUMENT_RUN).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.INSTRUMENT).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.BARCODE);
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    InstrumentRun run = new InstrumentRun();
    run.setInstrument(instrumentType.getInstruments().get(0));

    expect(instrumentServiceMock.getInstrumentType(instrumentType.getName())).andReturn(instrumentType);
    expect(instrumentRunServiceMock.getLastCompletedInstrumentRun(participant, instrumentType)).andReturn(run);
    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);

    Data data = instrumentTypeToVariableMappingStrategy.getData(participant, variable);

    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertNotNull(data);
    Assert.assertEquals(DataType.TEXT, data.getType());
    Assert.assertEquals("123", data.getValue());
  }

  @Test
  public void testContraindication() {
    Variable root = createInstrumentTypeVariable();
    log.info(VariableStreamer.toXML(root));

    Variable variable = root.getVariable(instrumentType.getName()).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.INSTRUMENT_RUN).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.CONTRAINDICATION).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.CONTRAINDICATION_CODE);
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    InstrumentRun run = new InstrumentRun();
    run.setInstrumentType(instrumentType);
    run.setContraindication(instrumentType.getContraindications().get(0));

    expect(instrumentServiceMock.getInstrumentType(instrumentType.getName())).andReturn(instrumentType);
    expect(instrumentRunServiceMock.getLastCompletedInstrumentRun(participant, instrumentType)).andReturn(run);
    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);

    Data data = instrumentTypeToVariableMappingStrategy.getData(participant, variable);

    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertNotNull(data);
    Assert.assertEquals(DataType.TEXT, data.getType());
    Assert.assertEquals("CI", data.getValue());
  }

  @Test
  public void testNoContraindication() {
    Variable root = createInstrumentTypeVariable();
    log.info(VariableStreamer.toXML(root));

    Variable variable = root.getVariable(instrumentType.getName()).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.INSTRUMENT_RUN).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.CONTRAINDICATION).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.CONTRAINDICATION_CODE);
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    InstrumentRun run = new InstrumentRun();
    run.setInstrumentType(instrumentType);

    expect(instrumentServiceMock.getInstrumentType(instrumentType.getName())).andReturn(instrumentType);
    expect(instrumentRunServiceMock.getLastCompletedInstrumentRun(participant, instrumentType)).andReturn(run);
    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);

    Data data = instrumentTypeToVariableMappingStrategy.getData(participant, variable);

    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertNull(data);
  }

  private Variable createInstrumentTypeVariable() {
    Variable variable = new Variable("Root");
    variable.addVariable(instrumentTypeToVariableMappingStrategy.getVariable(instrumentType));
    return variable;
  }

  /**
   * @return
   */
  private InstrumentType createInstrumentType() {
    InstrumentType type = new InstrumentType();
    type.setName("InstrumentType");

    type.addContraindication(new Contraindication("CI", Contraindication.Type.ASKED));

    Instrument instrument = new Instrument();
    instrument.setBarcode("123");
    instrument.setName("Instrument");
    instrument.setSerialNumber("321");
    instrument.setStatus(InstrumentStatus.ACTIVE);
    instrument.setVendor("Cag");
    type.addInstrument(instrument);

    inputParam = new InstrumentInputParameter();
    inputParam.setCode("INPUT_PARAM");
    inputParam.setDataType(DataType.TEXT);
    type.addInstrumentParameter(inputParam);

    InstrumentParameter anotherInputParam = new InstrumentInputParameter();
    anotherInputParam.setCode("INPUT_PARAM2");
    anotherInputParam.setDataType(DataType.TEXT);
    type.addInstrumentParameter(anotherInputParam);

    outputParam = new InstrumentOutputParameter();
    outputParam.setCode("OUTPUT_PARAM");
    outputParam.setDataType(DataType.INTEGER);
    type.addInstrumentParameter(outputParam);

    interParam = new InterpretativeParameter();
    interParam.setCode("INTERPRETIVE_PARAM");
    interParam.setDataType(DataType.TEXT);
    type.addInstrumentParameter(interParam);

    return type;
  }

}

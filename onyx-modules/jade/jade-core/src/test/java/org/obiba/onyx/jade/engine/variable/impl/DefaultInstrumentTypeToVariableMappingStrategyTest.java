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
import org.obiba.onyx.core.data.FixedDataSource;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.impl.DefaultVariablePathNamingStrategy;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
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

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultInstrumentTypeToVariableMappingStrategyTest.class);

  private InstrumentRunService instrumentRunServiceMock;

  private InstrumentService instrumentServiceMock;

  private IInstrumentTypeToVariableMappingStrategy instrumentTypeToVariableMappingStrategy;

  private InstrumentType instrumentType;

  private InstrumentType repeatableInstrumentType;

  private InstrumentParameter inputParam;

  private InstrumentParameter outputParam;

  private InstrumentParameter interParam;

  IVariablePathNamingStrategy variablePathNamingStrategy;

  private InstrumentRun instrumentRun;

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

    repeatableInstrumentType = createRepeatableInstrumentType();

    variablePathNamingStrategy = new DefaultVariablePathNamingStrategy();

    // Create a COMPLETED InstrumentRun.
    instrumentRun = new InstrumentRun();
    instrumentRun.setStatus(InstrumentRunStatus.COMPLETED);
  }

  @Test
  public void testVariable() {
    Variable root = createInstrumentTypeVariable(instrumentType);
    // log.info(VariableStreamer.toXML(root));

    Assert.assertEquals(1, root.getVariables().size());
    Variable var = root.getVariable(instrumentType.getName());
    Assert.assertNotNull(var);
    Assert.assertEquals(5, var.getVariables().size());

    Variable subVar = var.getVariable(DefaultInstrumentTypeToVariableMappingStrategy.MEASURE);
    Assert.assertNull(subVar);

    subVar = var.getVariable("OUTPUT_PARAM");
    Assert.assertNotNull(subVar);

    subVar = var.getVariable(DefaultInstrumentTypeToVariableMappingStrategy.INSTRUMENT_RUN);
    Assert.assertNotNull(subVar);
    Assert.assertEquals(6, subVar.getVariables().size());

    Variable subSubVar = subVar.getVariable(DefaultInstrumentTypeToVariableMappingStrategy.CONTRAINDICATION);
    Assert.assertNotNull(subSubVar);
  }

  @Test
  public void testRepeatableVariable() {
    Variable root = createInstrumentTypeVariable(repeatableInstrumentType);
    // log.info(VariableStreamer.toXML(root));

    Assert.assertEquals(1, root.getVariables().size());
    Variable var = root.getVariable(repeatableInstrumentType.getName());
    Assert.assertNotNull(var);
    Assert.assertEquals(6, var.getVariables().size());

    Variable subVar = var.getVariable(DefaultInstrumentTypeToVariableMappingStrategy.MEASURE);
    Assert.assertNotNull(subVar);
    Assert.assertTrue(subVar.isRepeatable());
    Assert.assertEquals(4, subVar.getVariables().size());
    Assert.assertNotNull(subVar.getVariable("OUTPUT_PARAM"));

    subVar = var.getVariable("CALC_OUTPUT_PARAM");
    Assert.assertNotNull(subVar);

    subVar = var.getVariable(DefaultInstrumentTypeToVariableMappingStrategy.INSTRUMENT_RUN);
    Assert.assertNotNull(subVar);
    Assert.assertEquals(5, subVar.getVariables().size());

    Variable subSubVar = subVar.getVariable(DefaultInstrumentTypeToVariableMappingStrategy.CONTRAINDICATION);
    Assert.assertNotNull(subSubVar);
  }

  @Test
  public void testInstrumentInputParameter() {
    Variable root = createInstrumentTypeVariable(instrumentType);
    // log.info(VariableStreamer.toXML(root));

    Variable variable = root.getVariable(instrumentType.getName()).getVariable("INPUT_PARAM");
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    InstrumentRunValue runValue = new InstrumentRunValue();
    runValue.setInstrumentRun(instrumentRun);
    runValue.setInstrumentParameter(inputParam.getCode());
    runValue.setData(DataBuilder.buildText("coucou"));

    expect(instrumentServiceMock.getInstrumentType(instrumentType.getName())).andReturn(instrumentType);
    expect(instrumentRunServiceMock.getInstrumentRunValue(participant, instrumentType.getName(), "INPUT_PARAM", null)).andReturn(runValue);
    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);

    Data data = instrumentTypeToVariableMappingStrategy.getVariableData(participant, variable, variablePathNamingStrategy, new VariableData(variablePathNamingStrategy.getPath(variable))).getDatas().get(0);

    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertNotNull(data);
    Assert.assertEquals(DataType.TEXT, data.getType());
    Assert.assertEquals("coucou", data.getValue());
  }

  @Test
  public void testInstrumentInputParameterCategory() {
    Variable root = createInstrumentTypeVariable(instrumentType);
    // log.info(VariableStreamer.toXML(root));

    Variable variable = root.getVariable(instrumentType.getName()).getVariable("INPUT_PARAM2").getVariable("L");
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    InstrumentRunValue runValue = new InstrumentRunValue();
    runValue.setInstrumentRun(instrumentRun);
    runValue.setInstrumentParameter(inputParam.getCode());
    runValue.setData(DataBuilder.buildText("L"));

    expect(instrumentServiceMock.getInstrumentType(instrumentType.getName())).andReturn(instrumentType);
    expect(instrumentRunServiceMock.getInstrumentRunValue(participant, instrumentType.getName(), "INPUT_PARAM2", null)).andReturn(runValue);
    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);

    Data data = instrumentTypeToVariableMappingStrategy.getVariableData(participant, variable, variablePathNamingStrategy, new VariableData(variablePathNamingStrategy.getPath(variable))).getDatas().get(0);

    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertNotNull(data);
    Assert.assertEquals(DataType.BOOLEAN, data.getType());
    Assert.assertEquals(true, data.getValue());
  }

  @Test
  public void testInstrumentOutputParameter() {
    Variable root = createInstrumentTypeVariable(instrumentType);
    // log.info(VariableStreamer.toXML(root));

    Variable variable = root.getVariable(instrumentType.getName()).getVariable("OUTPUT_PARAM");
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    InstrumentRunValue runValue = new InstrumentRunValue();
    runValue.setInstrumentRun(instrumentRun);
    runValue.setInstrumentParameter(outputParam.getCode());
    runValue.setData(DataBuilder.buildInteger(123l));

    expect(instrumentServiceMock.getInstrumentType(instrumentType.getName())).andReturn(instrumentType);
    expect(instrumentRunServiceMock.getInstrumentRunValue(participant, instrumentType.getName(), "OUTPUT_PARAM", null)).andReturn(runValue);
    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);

    Data data = instrumentTypeToVariableMappingStrategy.getVariableData(participant, variable, variablePathNamingStrategy, new VariableData(variablePathNamingStrategy.getPath(variable))).getDatas().get(0);

    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertNotNull(data);
    Assert.assertEquals(DataType.INTEGER, data.getType());
    Assert.assertEquals(123l, data.getValue());
  }

  @Test
  public void testInstrumentInterpretiveParameter() {
    Variable root = createInstrumentTypeVariable(instrumentType);
    // log.info(VariableStreamer.toXML(root));

    Variable variable = root.getVariable(instrumentType.getName()).getVariable("INTERPRETIVE_PARAM");
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    InstrumentRunValue runValue = new InstrumentRunValue();
    runValue.setInstrumentRun(instrumentRun);
    runValue.setInstrumentParameter(interParam.getCode());
    runValue.setData(DataBuilder.buildText("coucou"));

    expect(instrumentServiceMock.getInstrumentType(instrumentType.getName())).andReturn(instrumentType);
    expect(instrumentRunServiceMock.getInstrumentRunValue(participant, instrumentType.getName(), "INTERPRETIVE_PARAM", null)).andReturn(runValue);
    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);

    Data data = instrumentTypeToVariableMappingStrategy.getVariableData(participant, variable, variablePathNamingStrategy, new VariableData(variablePathNamingStrategy.getPath(variable))).getDatas().get(0);

    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertNotNull(data);
    Assert.assertEquals(DataType.TEXT, data.getType());
    Assert.assertEquals("coucou", data.getValue());
  }

  @Test
  public void testInstrumentInterpretiveParameterCategory() {
    Variable root = createInstrumentTypeVariable(instrumentType);
    // log.info(VariableStreamer.toXML(root));

    Variable variable = root.getVariable(instrumentType.getName()).getVariable("INTERPRETIVE_PARAM").getVariable(InterpretativeParameter.YES);
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    InstrumentRunValue runValue = new InstrumentRunValue();
    runValue.setInstrumentRun(instrumentRun);
    runValue.setInstrumentParameter(interParam.getCode());
    runValue.setData(DataBuilder.buildText(InterpretativeParameter.YES));

    expect(instrumentServiceMock.getInstrumentType(instrumentType.getName())).andReturn(instrumentType);
    expect(instrumentRunServiceMock.getInstrumentRunValue(participant, instrumentType.getName(), "INTERPRETIVE_PARAM", null)).andReturn(runValue);
    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);

    Data data = instrumentTypeToVariableMappingStrategy.getVariableData(participant, variable, variablePathNamingStrategy, new VariableData(variablePathNamingStrategy.getPath(variable))).getDatas().get(0);

    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertNotNull(data);
    Assert.assertEquals(DataType.BOOLEAN, data.getType());
    Assert.assertEquals(true, data.getValue());
  }

  @Test
  public void testInstrumentRun() {
    Variable root = createInstrumentTypeVariable(instrumentType);
    // log.info(VariableStreamer.toXML(root));

    Variable variable = root.getVariable(instrumentType.getName()).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.INSTRUMENT_RUN).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.USER);
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    InstrumentRun run = new InstrumentRun();
    run.setStatus(InstrumentRunStatus.COMPLETED);
    User user = new User();
    user.setLogin("toto");
    run.setUser(user);

    expect(instrumentRunServiceMock.getInstrumentRun(participant, instrumentType.getName())).andReturn(run);
    replay(instrumentRunServiceMock);

    Data data = instrumentTypeToVariableMappingStrategy.getVariableData(participant, variable, variablePathNamingStrategy, new VariableData(variablePathNamingStrategy.getPath(variable))).getDatas().get(0);

    verify(instrumentRunServiceMock);

    Assert.assertNotNull(data);
    Assert.assertEquals(DataType.TEXT, data.getType());
    Assert.assertEquals("toto", data.getValue());
  }

  @Test
  public void testInstrument() {
    Variable root = createInstrumentTypeVariable(instrumentType);
    // log.info(VariableStreamer.toXML(root));

    Variable variable = root.getVariable(instrumentType.getName()).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.INSTRUMENT_RUN).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.BARCODE);
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    InstrumentRun run = new InstrumentRun();
    run.setStatus(InstrumentRunStatus.COMPLETED);
    Instrument instrument = createInstrument(instrumentType);
    run.setInstrument(instrument);

    expect(instrumentRunServiceMock.getInstrumentRun(participant, instrumentType.getName())).andReturn(run);
    replay(instrumentRunServiceMock);

    Data data = instrumentTypeToVariableMappingStrategy.getVariableData(participant, variable, variablePathNamingStrategy, new VariableData(variablePathNamingStrategy.getPath(variable))).getDatas().get(0);

    verify(instrumentRunServiceMock);

    Assert.assertNotNull(data);
    Assert.assertEquals(DataType.TEXT, data.getType());
    Assert.assertEquals("123", data.getValue());
  }

  @Test
  public void testContraindication() {
    Variable root = createInstrumentTypeVariable(instrumentType);
    // log.info(VariableStreamer.toXML(root));

    Variable variable = root.getVariable(instrumentType.getName()).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.INSTRUMENT_RUN).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.CONTRAINDICATION).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.CONTRAINDICATION_CODE);
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    InstrumentRun run = new InstrumentRun();
    run.setStatus(InstrumentRunStatus.COMPLETED);
    run.setInstrumentType(instrumentType.getName());
    run.setContraindication(instrumentType.getContraindications().get(0));

    expect(instrumentRunServiceMock.getInstrumentRun(participant, instrumentType.getName())).andReturn(run);
    replay(instrumentRunServiceMock);

    Data data = instrumentTypeToVariableMappingStrategy.getVariableData(participant, variable, variablePathNamingStrategy, new VariableData(variablePathNamingStrategy.getPath(variable))).getDatas().get(0);

    verify(instrumentRunServiceMock);

    Assert.assertNotNull(data);
    Assert.assertEquals(DataType.TEXT, data.getType());
    Assert.assertEquals("CI", data.getValue());
  }

  @Test
  public void testNoContraindication() {
    Variable root = createInstrumentTypeVariable(instrumentType);
    // log.info(VariableStreamer.toXML(root));

    Variable variable = root.getVariable(instrumentType.getName()).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.INSTRUMENT_RUN).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.CONTRAINDICATION).getVariable(DefaultInstrumentTypeToVariableMappingStrategy.CONTRAINDICATION_CODE);
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    InstrumentRun run = new InstrumentRun();
    run.setStatus(InstrumentRunStatus.COMPLETED);
    run.setInstrumentType(instrumentType.getName());

    expect(instrumentRunServiceMock.getInstrumentRun(participant, instrumentType.getName())).andReturn(run);
    replay(instrumentRunServiceMock);

    Assert.assertEquals(0, instrumentTypeToVariableMappingStrategy.getVariableData(participant, variable, variablePathNamingStrategy, new VariableData(variablePathNamingStrategy.getPath(variable))).getDatas().size());

    verify(instrumentRunServiceMock);

  }

  private Variable createInstrumentTypeVariable(InstrumentType type) {
    Variable variable = new Variable("Root");
    variable.addVariable(instrumentTypeToVariableMappingStrategy.getVariable(type));
    return variable;
  }

  /**
   * @return
   */
  private InstrumentType createInstrumentType() {
    InstrumentType type = new InstrumentType();
    type.setName("InstrumentType");

    type.addContraindication(new Contraindication("CI", Contraindication.Type.ASKED));

    inputParam = new InstrumentInputParameter();
    inputParam.setCode("INPUT_PARAM");
    inputParam.setDataType(DataType.TEXT);
    type.addInstrumentParameter(inputParam);

    InstrumentParameter anotherInputParam = new InstrumentInputParameter();
    anotherInputParam.setCode("INPUT_PARAM2");
    anotherInputParam.setDataType(DataType.TEXT);
    anotherInputParam.addAllowedValues(DataBuilder.buildText("S"), DataBuilder.buildText("M"), DataBuilder.buildText("L"));
    type.addInstrumentParameter(anotherInputParam);

    outputParam = new InstrumentOutputParameter();
    outputParam.setCode("OUTPUT_PARAM");
    outputParam.setCaptureMethod(InstrumentParameterCaptureMethod.AUTOMATIC);
    outputParam.setDataType(DataType.INTEGER);
    type.addInstrumentParameter(outputParam);

    interParam = new InterpretativeParameter();
    interParam.setCode("INTERPRETIVE_PARAM");
    interParam.setDataType(DataType.TEXT);
    type.addInstrumentParameter(interParam);

    return type;
  }

  private InstrumentType createRepeatableInstrumentType() {
    InstrumentType type = createInstrumentType();

    InstrumentOutputParameter outputParam = new InstrumentOutputParameter();
    outputParam.setCode("CALC_OUTPUT_PARAM");
    outputParam.setCaptureMethod(InstrumentParameterCaptureMethod.COMPUTED);
    outputParam.setDataType(DataType.INTEGER);
    type.addInstrumentParameter(outputParam);

    type.setExpectedMeasureCount(new FixedDataSource(DataBuilder.buildInteger(2)));

    return type;
  }

  private Instrument createInstrument(InstrumentType instrumentType) {
    Instrument instrument = new Instrument();
    instrument.addType(instrumentType.getName());
    instrument.setBarcode("123");
    instrument.setName("Instrument");
    instrument.setSerialNumber("321");
    instrument.setStatus(InstrumentStatus.ACTIVE);
    instrument.setVendor("Cag");

    return instrument;
  }
}

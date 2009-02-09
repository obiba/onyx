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

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.core.service.EntityQueryService;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InputDataSourceVisitor;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.SessionScope;

public class InputSourceTest extends BaseDefaultSpringContextTestCase {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(InputSourceTest.class);

  // @Autowired(required = true)
  InputDataSourceVisitor inputDataSourceVisitor;

  @Autowired(required = true)
  EntityQueryService queryService;

  @Test
  public void test() {
  }

  // @Before
  public void setUp() {
    ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext("test-spring-context.xml");
    applicationContext.getBeanFactory().registerScope("session", new SessionScope());

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpSession session = new MockHttpSession();
    request.setSession(session);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    inputDataSourceVisitor = (InputDataSourceVisitor) applicationContext.getBean("inputDataSourceVisitor");

  }

  // @Test
  // @Dataset
  public void testParticipantPropertyRetriever() {
    Participant participant = queryService.get(Participant.class, 1l);

    InstrumentInputParameter param = new InstrumentInputParameter();
    ParticipantPropertySource participantPropertySource = new ParticipantPropertySource();
    participantPropertySource.setProperty("birthDate");
    param.setDataType(DataType.DATE);
    param.setInputSource(participantPropertySource);
    Data resultData = inputDataSourceVisitor.getData(participant, param);
    Assert.assertNotNull("Result Data is null", resultData);
    Assert.assertEquals("1979-09-04", resultData.getValueAsString());
    Assert.assertEquals(DataType.DATE, resultData.getType());

    participantPropertySource.setProperty("lastName");
    param.setDataType(DataType.TEXT);
    resultData = inputDataSourceVisitor.getData(participant, param);
    Assert.assertNotNull("Result Data is null", resultData);
    Assert.assertEquals("Dupont", resultData.getValue());
    Assert.assertEquals(DataType.TEXT, resultData.getType());

    participantPropertySource.setProperty("gender");
    param.setDataType(DataType.TEXT);
    resultData = inputDataSourceVisitor.getData(participant, param);
    Assert.assertNotNull("Result Data is null", resultData);
    Assert.assertEquals("FEMALE", resultData.getValue());
    Assert.assertEquals(DataType.TEXT, resultData.getType());
  }

  // @Test
  // @Dataset
  public void testOutputParameterSource() {
    Participant participant = queryService.get(Participant.class, 1l);
    Assert.assertNotNull(participant);

    InstrumentInputParameter param = new InstrumentInputParameter();
    OutputParameterSource outputParameterSource = new OutputParameterSource();
    outputParameterSource.setParameterName("height");
    outputParameterSource.setInstrumentType("BIM");
    param.setDataType(DataType.INTEGER);
    param.setInputSource(outputParameterSource);

    Data resultData = inputDataSourceVisitor.getData(participant, param);
    Assert.assertNotNull(resultData);
    Assert.assertEquals(new Long(187), resultData.getValue());
    Assert.assertEquals(DataType.INTEGER, resultData.getType());
  }

  // @Test
  // @Dataset
  public void testInstrumentParameterValueConverter() {
    Participant participant = queryService.get(Participant.class, 1l);

    // Testing date data
    InstrumentRunValue sourceInstrumentRunValue = queryService.get(InstrumentRunValue.class, 5l);
    InstrumentParameter targetInstrumentParameter = queryService.get(InstrumentParameter.class, 6l);

    InstrumentRunValue targetInstrumentRunValue = new InstrumentRunValue();
    targetInstrumentRunValue.setInstrumentParameter(targetInstrumentParameter);

    if(sourceInstrumentRunValue.getData().getValue() == null) {
      sourceInstrumentRunValue.setData(new Data(DataType.DATE, participant.getBirthDate()));
    }
    Date date = sourceInstrumentRunValue.getValue();
    log.info("date=" + date);

    DateParameterValueConverter dateConverter = new DateParameterValueConverter();
    dateConverter.convert(targetInstrumentRunValue, sourceInstrumentRunValue);

    InstrumentParameter finalInstrumentParameter = queryService.get(InstrumentParameter.class, 8l);
    InstrumentRunValue finalInstrumentRunValue = new InstrumentRunValue();
    finalInstrumentRunValue.setInstrumentParameter(finalInstrumentParameter);

    UnitParameterValueConverter unitConverter = new UnitParameterValueConverter();
    unitConverter.convert(finalInstrumentRunValue, targetInstrumentRunValue);
    Assert.assertEquals(Long.valueOf("29"), finalInstrumentRunValue.getValue());

    // Testing metric data
    sourceInstrumentRunValue = queryService.get(InstrumentRunValue.class, 1l);
    targetInstrumentParameter = queryService.get(InstrumentParameter.class, 3l);
    targetInstrumentRunValue.setInstrumentParameter(targetInstrumentParameter);

    unitConverter.convert(targetInstrumentRunValue, sourceInstrumentRunValue);
    Assert.assertEquals(Double.valueOf("1.85"), targetInstrumentRunValue.getValue());
  }

}

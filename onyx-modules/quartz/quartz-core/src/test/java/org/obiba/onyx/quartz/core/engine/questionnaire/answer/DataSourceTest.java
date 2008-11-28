/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.answer;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.DataSourceBuilder;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class DataSourceTest {

  static final Logger log = LoggerFactory.getLogger(DataSourceTest.class);

  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationServiceMock;

  private Questionnaire questionnaire;

  @Before
  public void setUp() {
    activeQuestionnaireAdministrationServiceMock = createMock(ActiveQuestionnaireAdministrationService.class);
    questionnaire = createQuestionnaire();
  }

  @Test
  public void testCurrentYearSource() {
    DataSource source = DataSourceBuilder.createCurrentYearSource(questionnaire).getDataSource();
    Data data = source.getData(activeQuestionnaireAdministrationServiceMock);

    Calendar c = Calendar.getInstance();
    c.setTime(new Date());
    Assert.assertEquals(DataType.INTEGER, data.getType());
    Assert.assertEquals(c.get(Calendar.YEAR), data.getValue());

    log.debug("CurrentYearSource.data={}", data);
  }

  @Test
  public void testFixedSource() {
    DataSource source = DataSourceBuilder.createFixedSource(questionnaire, DataBuilder.buildInteger(1)).getDataSource();
    Data data = source.getData(activeQuestionnaireAdministrationServiceMock);

    Assert.assertEquals(DataType.INTEGER, data.getType());
    Assert.assertEquals(1, data.getValue());

    log.debug("FixedSource.data={}", data);
  }

  @Test
  public void testParticipantPropertySource() {
    Participant participant = new Participant();
    Calendar c = Calendar.getInstance();
    c.set(1973, 1, 15);
    participant.setBirthDate(c.getTime());
    QuestionnaireParticipant questionnaireParticipant = new QuestionnaireParticipant();
    questionnaireParticipant.setParticipant(participant);

    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaireParticipant()).andReturn(questionnaireParticipant).anyTimes();
    replay(activeQuestionnaireAdministrationServiceMock);

    DataSource source = DataSourceBuilder.createParticipantPropertySource(questionnaire, "birthDate").getDataSource();
    Assert.assertEquals("birthDate", ((ParticipantPropertySource) source).getProperty());
    Data data = source.getData(activeQuestionnaireAdministrationServiceMock);

    log.debug("ParticipantPropertySource.data={}", data);

    Assert.assertEquals(DataType.DATE, data.getType());

    Date date = data.getValue();
    c.setTime(date);
    Assert.assertEquals(1973, c.get(Calendar.YEAR));
    Assert.assertEquals(1, c.get(Calendar.MONTH));
    Assert.assertEquals(15, c.get(Calendar.DATE));

    verify(activeQuestionnaireAdministrationServiceMock);
  }

  @Test
  public void testParticipantGenderPropertySource() {
    Participant participant = new Participant();
    participant.setGender(Gender.MALE);
    QuestionnaireParticipant questionnaireParticipant = new QuestionnaireParticipant();
    questionnaireParticipant.setParticipant(participant);

    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaireParticipant()).andReturn(questionnaireParticipant).anyTimes();
    replay(activeQuestionnaireAdministrationServiceMock);

    DataSource source = DataSourceBuilder.createParticipantPropertySource(questionnaire, "gender").getDataSource();
    Data data = source.getData(activeQuestionnaireAdministrationServiceMock);

    log.debug("ParticipantPropertySource.data={}", data);

    Assert.assertEquals(DataType.TEXT, data.getType());

    String gender = data.getValue();
    Assert.assertEquals(Gender.MALE.toString(), gender);

    verify(activeQuestionnaireAdministrationServiceMock);
  }

  @Test
  public void testInvalidParticipantPropertySource() {
    Participant participant = new Participant();
    Calendar c = Calendar.getInstance();
    c.set(1973, 1, 15);
    participant.setBirthDate(c.getTime());
    QuestionnaireParticipant questionnaireParticipant = new QuestionnaireParticipant();
    questionnaireParticipant.setParticipant(participant);

    expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaireParticipant()).andReturn(questionnaireParticipant).anyTimes();
    replay(activeQuestionnaireAdministrationServiceMock);

    DataSource source = DataSourceBuilder.createParticipantPropertySource(questionnaire, "coucou").getDataSource();

    try {
      source.getData(activeQuestionnaireAdministrationServiceMock);
      Assert.fail();
    } catch(IllegalArgumentException e) {

    }

    verify(activeQuestionnaireAdministrationServiceMock);
  }

  @Test
  public void testTimestampSource() {
    DataSource source = DataSourceBuilder.createTimestampSource(questionnaire).getDataSource();
    Data data = source.getData(activeQuestionnaireAdministrationServiceMock);

    Assert.assertEquals(DataType.DATE, data.getType());

    Calendar c1 = Calendar.getInstance();
    c1.setTime(new Date());

    Date date = data.getValue();
    Calendar c2 = Calendar.getInstance();
    c2.setTime(date);
    Assert.assertEquals(c1.get(Calendar.YEAR), c2.get(Calendar.YEAR));
    Assert.assertEquals(c1.get(Calendar.MONTH), c2.get(Calendar.MONTH));
    Assert.assertEquals(c1.get(Calendar.DATE), c2.get(Calendar.DATE));
    Assert.assertEquals(c1.get(Calendar.HOUR), c2.get(Calendar.HOUR));

    log.debug("TimestampSource.data={}", data);
  }

  @Test
  public void testOpenAnswerSource() {

    OpenAnswer openAnswer = new OpenAnswer();
    CategoryAnswer categoryAnswer = new CategoryAnswer();
    categoryAnswer.setActive(true);
    openAnswer.setCategoryAnswer(categoryAnswer);
    openAnswer.setDataType(DataType.TEXT);
    openAnswer.setData(DataBuilder.buildText("coucou"));

    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer((Question) EasyMock.anyObject(), (Category) EasyMock.anyObject(), (OpenAnswerDefinition) EasyMock.anyObject())).andReturn(openAnswer).anyTimes();
    replay(activeQuestionnaireAdministrationServiceMock);

    DataSource source = DataSourceBuilder.createOpenAnswerSource(questionnaire, "Q2", "2", "OPEN_TEXT").getDataSource();
    Data data = source.getData(activeQuestionnaireAdministrationServiceMock);

    log.debug("OpenAnswerSource.data={}", data);

    Assert.assertEquals(DataType.TEXT, data.getType());
    Assert.assertEquals("coucou", data.getValue());

    verify(activeQuestionnaireAdministrationServiceMock);
  }

  @Test
  public void testNullOpenAnswerSource() {

    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer((Question) EasyMock.anyObject(), (Category) EasyMock.anyObject(), (OpenAnswerDefinition) EasyMock.anyObject())).andReturn(null).anyTimes();
    replay(activeQuestionnaireAdministrationServiceMock);

    DataSource source = DataSourceBuilder.createOpenAnswerSource(questionnaire, "Q2", "2", "OPEN_TEXT").getDataSource();
    Data data = source.getData(activeQuestionnaireAdministrationServiceMock);

    log.debug("OpenAnswerSource.data={}", data);

    Assert.assertNull(data);

    verify(activeQuestionnaireAdministrationServiceMock);
  }

  @Test
  public void testInactiveOpenAnswerSource() {

    OpenAnswer openAnswer = new OpenAnswer();
    CategoryAnswer categoryAnswer = new CategoryAnswer();
    categoryAnswer.setActive(false);
    openAnswer.setCategoryAnswer(categoryAnswer);
    openAnswer.setDataType(DataType.TEXT);
    openAnswer.setData(DataBuilder.buildText("coucou"));

    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer((Question) EasyMock.anyObject(), (Category) EasyMock.anyObject(), (OpenAnswerDefinition) EasyMock.anyObject())).andReturn(openAnswer).anyTimes();
    replay(activeQuestionnaireAdministrationServiceMock);

    DataSource source = DataSourceBuilder.createOpenAnswerSource(questionnaire, "Q2", "2", "OPEN_TEXT").getDataSource();
    Data data = source.getData(activeQuestionnaireAdministrationServiceMock);

    log.debug("OpenAnswerSource.data={}", data);

    Assert.assertNull(data);

    verify(activeQuestionnaireAdministrationServiceMock);
  }

  @Test
  public void testExternalOpenAnswerSource() {

    OpenAnswer openAnswer = new OpenAnswer();
    CategoryAnswer categoryAnswer = new CategoryAnswer();
    categoryAnswer.setActive(true);
    openAnswer.setCategoryAnswer(categoryAnswer);
    openAnswer.setDataType(DataType.TEXT);
    openAnswer.setData(DataBuilder.buildText("coucou"));

    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer("HealthQuestionnaire1", "Q2", "2", "OPEN_TEXT")).andReturn(openAnswer).anyTimes();
    replay(activeQuestionnaireAdministrationServiceMock);

    DataSource source = DataSourceBuilder.createExternalOpenAnswerSource(questionnaire, "HealthQuestionnaire1", "Q2", "2", "OPEN_TEXT").getDataSource();
    Data data = source.getData(activeQuestionnaireAdministrationServiceMock);

    log.debug("ExternalOpenAnswerSource.data={}", data);

    Assert.assertEquals(DataType.TEXT, data.getType());
    Assert.assertEquals("coucou", data.getValue());

    verify(activeQuestionnaireAdministrationServiceMock);
  }

  @Test
  public void testNullExternalOpenAnswerSource() {

    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer("HealthQuestionnaire1", "Q2", "2", "OPEN_TEXT")).andReturn(null).anyTimes();
    replay(activeQuestionnaireAdministrationServiceMock);

    DataSource source = DataSourceBuilder.createExternalOpenAnswerSource(questionnaire, "HealthQuestionnaire1", "Q2", "2", "OPEN_TEXT").getDataSource();
    Data data = source.getData(activeQuestionnaireAdministrationServiceMock);

    log.debug("ExternalOpenAnswerSource.data={}", data);

    Assert.assertNull(data);

    verify(activeQuestionnaireAdministrationServiceMock);
  }

  @Test
  public void testInvalidExternalOpenAnswerSource() {

    OpenAnswer openAnswer = new OpenAnswer();
    CategoryAnswer categoryAnswer = new CategoryAnswer();
    categoryAnswer.setActive(false);
    openAnswer.setCategoryAnswer(categoryAnswer);
    openAnswer.setDataType(DataType.TEXT);
    openAnswer.setData(DataBuilder.buildText("coucou"));

    expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer("HealthQuestionnaire1", "Q2", "2", "OPEN_TEXT")).andReturn(openAnswer).anyTimes();
    replay(activeQuestionnaireAdministrationServiceMock);

    DataSource source = DataSourceBuilder.createExternalOpenAnswerSource(questionnaire, "HealthQuestionnaire1", "Q2", "2", "OPEN_TEXT").getDataSource();
    Data data = source.getData(activeQuestionnaireAdministrationServiceMock);

    log.debug("ExternalOpenAnswerSource.data={}", data);

    Assert.assertNull(data);

    verify(activeQuestionnaireAdministrationServiceMock);
  }

  public Questionnaire createQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    builder.withSection("SB").withSection("GENDER").withPage("P1").withQuestion("Q1").withCategories("1", "2", "3");
    builder.withSection("S1").withPage("P2").withQuestion("Q2").withCategory("1").withOpenAnswerDefinition("OPEN_INT", DataType.INTEGER);
    builder.inQuestion("Q2").withCategory("2").withOpenAnswerDefinition("OPEN_TEXT", DataType.TEXT);
    builder.inQuestion("Q2").withCategory("3").withOpenAnswerDefinition("OPEN_DATE", DataType.DATE);
    builder.inQuestion("Q2").withCategory("4").withOpenAnswerDefinition("OPEN_TEXT_DEFAULT_VALUES", DataType.TEXT).setDefaultData("a", "b", "c");

    Questionnaire q = builder.getQuestionnaire();
    q.addLocale(Locale.FRENCH);
    q.addLocale(Locale.ENGLISH);

    return q;
  }
}

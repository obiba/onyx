/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.data;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Calendar;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class OpenAnswerDataSourceTest {

  private static final String QUESTIONNAIRE = "Quest";

  private static final String QUESTION = "Q_01";

  private static final String CATEGORY = "Cat_01";

  private static final String OPENANSWERDEFINITION = "Oad_01";

  private QuestionnaireParticipantService questionnaireParticipantServiceMock = createMock(QuestionnaireParticipantService.class);

  private QuestionnaireBundleManager questionnaireBundleManagerMock = createMock(QuestionnaireBundleManager.class);

  private QuestionnaireBundle questionnaireBundleMock = createMock(QuestionnaireBundle.class);

  @Test
  public void testOpenAnswerDataSourceNoParticipant() {
    OpenAnswerDataSource openAnswerDataSource = new OpenAnswerDataSource(QUESTIONNAIRE, QUESTION, CATEGORY, OPENANSWERDEFINITION);
    Data data = openAnswerDataSource.getData(null);
    Assert.assertNull(data);
  }

  @Test
  public void testOpenAnswerDataSourceNoOpenAnswer() {
    Participant participant = createParticipant();
    OpenAnswerDataSource openAnswerDataSource = initOpenAnswerDataSource();

    expect(questionnaireParticipantServiceMock.getOpenAnswer(participant, QUESTIONNAIRE, QUESTION, CATEGORY, OPENANSWERDEFINITION)).andReturn(null);
    replay(questionnaireParticipantServiceMock);
    Data data = openAnswerDataSource.getData(participant);
    verify(questionnaireParticipantServiceMock);

    Assert.assertNull(data);
  }

  @Test
  public void testOpenAnswerDataSourceNoData() {
    Participant participant = createParticipant();
    OpenAnswerDataSource openAnswerDataSource = initOpenAnswerDataSource();

    expect(questionnaireParticipantServiceMock.getOpenAnswer(participant, QUESTIONNAIRE, QUESTION, CATEGORY, OPENANSWERDEFINITION)).andReturn(createOpenAnswer(false));
    replay(questionnaireParticipantServiceMock);
    Data data = openAnswerDataSource.getData(participant);
    verify(questionnaireParticipantServiceMock);

    Assert.assertNull(data);
  }

  @Test
  public void testOpenAnswerDataSourceWithDataNoUnit() {
    Participant participant = createParticipant();
    OpenAnswerDataSource openAnswerDataSource = initOpenAnswerDataSource();

    expect(questionnaireParticipantServiceMock.getOpenAnswer(participant, QUESTIONNAIRE, QUESTION, CATEGORY, OPENANSWERDEFINITION)).andReturn(createOpenAnswer(true));
    expect(questionnaireBundleManagerMock.getBundle(QUESTIONNAIRE)).andReturn(questionnaireBundleMock);
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(createQuestionnaire(false));

    replay(questionnaireParticipantServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    Data data = openAnswerDataSource.getData(participant);
    String unit = openAnswerDataSource.getUnit();

    verify(questionnaireParticipantServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

    Assert.assertNotNull(data);
    Assert.assertEquals("56", data.getValueAsString());
    Assert.assertNull(unit);
  }

  @Test
  public void testOpenAnswerDataSourceWithDataWithUnit() {
    Participant participant = createParticipant();
    OpenAnswerDataSource openAnswerDataSource = initOpenAnswerDataSource();

    expect(questionnaireParticipantServiceMock.getOpenAnswer(participant, QUESTIONNAIRE, QUESTION, CATEGORY, OPENANSWERDEFINITION)).andReturn(createOpenAnswer(true));
    expect(questionnaireBundleManagerMock.getBundle(QUESTIONNAIRE)).andReturn(questionnaireBundleMock);
    expect(questionnaireBundleMock.getQuestionnaire()).andReturn(createQuestionnaire(true));

    replay(questionnaireParticipantServiceMock);
    replay(questionnaireBundleManagerMock);
    replay(questionnaireBundleMock);

    Data data = openAnswerDataSource.getData(participant);
    String unit = openAnswerDataSource.getUnit();

    verify(questionnaireParticipantServiceMock);
    verify(questionnaireBundleManagerMock);
    verify(questionnaireBundleMock);

    Assert.assertNotNull(data);
    Assert.assertEquals("56", data.getValueAsString());
    Assert.assertEquals("years", unit);
  }

  private Questionnaire createQuestionnaire(boolean withUnit) {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire(QUESTIONNAIRE, "1.0");

    builder.withSection("SectionS").withPage("P1").withQuestion(QUESTION).withCategory(QUESTION).withOpenAnswerDefinition(OPENANSWERDEFINITION, DataType.INTEGER);
    if(withUnit == true) builder.inOpenAnswerDefinition(OPENANSWERDEFINITION).setUnit("years");

    Questionnaire q = builder.getQuestionnaire();
    q.addLocale(Locale.FRENCH);
    q.addLocale(Locale.ENGLISH);

    return q;
  }

  private OpenAnswer createOpenAnswer(boolean withData) {
    CategoryAnswer categoryAnswer = new CategoryAnswer();
    categoryAnswer.setCategoryName(CATEGORY);
    categoryAnswer.setActive(true);

    OpenAnswer openAnswer = new OpenAnswer();
    openAnswer.setOpenAnswerDefinitionName(OPENANSWERDEFINITION);
    openAnswer.setCategoryAnswer(categoryAnswer);
    if(withData == true) {
      openAnswer.setIntegerValue(56l);
      openAnswer.setDataType(DataType.INTEGER);
    }
    return openAnswer;
  }

  private OpenAnswerDataSource initOpenAnswerDataSource() {
    OpenAnswerDataSource openAnswerDataSource = new OpenAnswerDataSource(QUESTIONNAIRE, QUESTION, CATEGORY, OPENANSWERDEFINITION);
    openAnswerDataSource.setQuestionnaireParticipantService(questionnaireParticipantServiceMock);
    openAnswerDataSource.setQuestionnaireBundleManager(questionnaireBundleManagerMock);
    return openAnswerDataSource;
  }

  private Participant createParticipant() {
    Participant p = new Participant();
    p.setBarcode("1187432");
    p.setLastName("Tremblay");
    p.setFirstName("Patricia");
    p.setGender(Gender.FEMALE);
    Calendar c = Calendar.getInstance();
    c.set(1973, 1, 15);
    p.setBirthDate(c.getTime());
    return p;
  }
}

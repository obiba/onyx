/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.engine.variable.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import org.apache.wicket.spring.test.ApplicationContextMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.impl.DefaultVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.util.VariableStreamer;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;
import org.obiba.onyx.quartz.engine.variable.IQuestionToVariableMappingStrategy;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class DefaultQuestionToVariableMappingStrategyTest {

  private static final Logger log = LoggerFactory.getLogger(DefaultQuestionToVariableMappingStrategyTest.class);

  private QuestionnaireParticipantService questionnaireParticipantServiceMock;

  private IVariablePathNamingStrategy variablePathNamingStrategy;

  private IQuestionToVariableMappingStrategy questionToVariableMappingStrategy;

  private Questionnaire questionnaire;

  @Before
  public void setUp() {
    ApplicationContextMock mockCtx = new ApplicationContextMock();
    questionnaireParticipantServiceMock = createMock(QuestionnaireParticipantService.class);
    mockCtx.putBean("questionnaireParticipantService", questionnaireParticipantServiceMock);

    variablePathNamingStrategy = new DefaultVariablePathNamingStrategy();

    questionToVariableMappingStrategy = new DefaultQuestionToVariableMappingStrategy();

    questionnaire = createQuestionnaire();
  }

  @Test
  public void testVariable() {
    Variable questionnaireVariable = createQuestionnaireVariable();
    log.info("\n" + VariableStreamer.toXML(questionnaireVariable));

    Assert.assertEquals(questionnaire.getName(), questionnaireVariable.getName());
    Assert.assertEquals(6, questionnaireVariable.getVariables().size());
    Assert.assertEquals(0, questionnaireVariable.getCategories().size());

    Variable questionVariable = questionnaireVariable.getVariable("Q1");
    Assert.assertEquals("Q1", questionVariable.getName());
    Assert.assertTrue(questionVariable.isMultiple());
    Assert.assertEquals(3, questionVariable.getCategories().size());
    Assert.assertEquals("1", questionVariable.getCategories().get(0).getName());
    Assert.assertEquals("2", questionVariable.getCategories().get(1).getName());
    Assert.assertEquals("3", questionVariable.getCategories().get(2).getName());
    Assert.assertEquals(5, questionVariable.getVariables().size());
    Assert.assertEquals(DefaultQuestionToVariableMappingStrategy.QUESTION_COMMENT, questionVariable.getVariables().get(0).getName());
    Assert.assertEquals(DefaultQuestionToVariableMappingStrategy.QUESTION_ACTIVE, questionVariable.getVariables().get(1).getName());

    questionVariable = questionnaireVariable.getVariable("Q2");
    Assert.assertEquals("Q2", questionVariable.getName());
    Assert.assertFalse(questionVariable.isMultiple());
    Assert.assertEquals(4, questionVariable.getCategories().size());
    Assert.assertEquals(6, questionVariable.getVariables().size());

    Variable categoryVariable = questionVariable.getVariable("1");
    Assert.assertNotNull(categoryVariable);
    Assert.assertEquals(1, categoryVariable.getVariables().size());
    Assert.assertNotNull(categoryVariable.getVariable("OPEN_INT"));

    categoryVariable = questionVariable.getVariable("3");
    Assert.assertNotNull(categoryVariable);
    Assert.assertEquals(1, categoryVariable.getVariables().size());
    Assert.assertNotNull(categoryVariable.getVariable("OPEN_YEAR"));

    categoryVariable = questionVariable.getVariable("4");
    Assert.assertNotNull(categoryVariable);
    Assert.assertEquals(1, categoryVariable.getVariables().size());
    Assert.assertNotNull(categoryVariable.getVariable("OPEN_TEXT_DEFAULT_VALUES"));

    Variable openVariable = categoryVariable.getVariable("OPEN_TEXT_DEFAULT_VALUES");
    Assert.assertEquals(3, openVariable.getVariables().size());
    Assert.assertNotNull(openVariable.getVariable("a"));
    Assert.assertNotNull(openVariable.getVariable("b"));
    Assert.assertNotNull(openVariable.getVariable("c"));

    questionVariable = questionnaireVariable.getVariable("Q3");
    Assert.assertEquals("Q3", questionVariable.getName());
    Assert.assertEquals(0, questionVariable.getCategories().size());
    Assert.assertEquals(4, questionVariable.getVariables().size());

    Variable subQuestionVariable = questionVariable.getVariables().get(2);
    Assert.assertEquals("Q3_1", subQuestionVariable.getName());
    Assert.assertEquals(2, subQuestionVariable.getCategories().size());
    Assert.assertEquals("Y", subQuestionVariable.getCategories().get(0).getName());
    Assert.assertEquals("N", subQuestionVariable.getCategories().get(1).getName());
    Assert.assertEquals(3, subQuestionVariable.getVariables().size());

    subQuestionVariable = questionVariable.getVariables().get(3);
    Assert.assertEquals("Q3_2", subQuestionVariable.getName());
    Assert.assertEquals(2, subQuestionVariable.getCategories().size());
    Assert.assertEquals("Y", subQuestionVariable.getCategories().get(0).getName());
    Assert.assertEquals("N", subQuestionVariable.getCategories().get(1).getName());
    Assert.assertEquals(3, subQuestionVariable.getVariables().size());

    questionVariable = questionnaireVariable.getVariable("Q4");
    Assert.assertEquals("Q4", questionVariable.getName());
    Assert.assertEquals(0, questionVariable.getCategories().size());
    Assert.assertEquals(4, questionVariable.getVariables().size());

    subQuestionVariable = questionVariable.getVariables().get(2);
    Assert.assertEquals("Q4_1", subQuestionVariable.getName());
    Assert.assertEquals(2, subQuestionVariable.getCategories().size());
    Assert.assertEquals("1", subQuestionVariable.getCategories().get(0).getName());
    Assert.assertEquals("2", subQuestionVariable.getCategories().get(1).getName());
    Assert.assertEquals(4, subQuestionVariable.getVariables().size());

    subQuestionVariable = questionVariable.getVariables().get(3);
    Assert.assertEquals("Q4_2", subQuestionVariable.getName());
    Assert.assertEquals(2, subQuestionVariable.getCategories().size());
    Assert.assertEquals("Y", subQuestionVariable.getCategories().get(0).getName());
    Assert.assertEquals("N", subQuestionVariable.getCategories().get(1).getName());
    Assert.assertEquals(4, subQuestionVariable.getVariables().size());

  }

  @Test
  public void testVariableDataQuestionnaire() {
    Variable questionnaireVariable = createQuestionnaireVariable();

    Variable studyVariable = new Variable("ROOT");
    studyVariable.addVariable(questionnaireVariable);

    Participant participant = new Participant();
    QuestionnaireParticipant questionnaireParticipant = new QuestionnaireParticipant();
    questionnaireParticipant.setLocale(Locale.CANADA_FRENCH);
    questionnaireParticipant.setQuestionnaireVersion("1.0");

    expect(questionnaireParticipantServiceMock.getQuestionnaireParticipant(participant, questionnaire.getName())).andReturn(questionnaireParticipant).atLeastOnce();
    replay(questionnaireParticipantServiceMock);

    Variable variable = questionnaireVariable.getVariables().get(0).getVariables().get(0);
    VariableData data = questionToVariableMappingStrategy.getVariableData(questionnaireParticipantServiceMock, participant, variable, new VariableData(variablePathNamingStrategy.getPath(variable)), questionnaire);
    log.info("\n" + VariableStreamer.toXML(data));
    Assert.assertEquals(1, data.getDatas().size());
    Assert.assertEquals("1.0", data.getDatas().get(0).getValue());

    variable = questionnaireVariable.getVariables().get(0).getVariables().get(1);
    data = questionToVariableMappingStrategy.getVariableData(questionnaireParticipantServiceMock, participant, variable, new VariableData(variablePathNamingStrategy.getPath(variable)), questionnaire);
    log.info("\n" + VariableStreamer.toXML(data));

    Assert.assertEquals(1, data.getDatas().size());
    Assert.assertEquals("fr_CA", data.getDatas().get(0).getValue());

    verify(questionnaireParticipantServiceMock);

  }

  @Test
  public void testVariableDataQuestionComment() {
    Variable questionnaireVariable = createQuestionnaireVariable();

    Variable studyVariable = new Variable("ROOT");
    studyVariable.addVariable(questionnaireVariable);
    // log.info("\n"+VariableStreamer.toXML(studyVariable));

    Variable variable = questionnaireVariable.getVariable("Q1").getVariables().get(0);
    Assert.assertEquals(DefaultQuestionToVariableMappingStrategy.QUESTION_COMMENT, variable.getName());

    Variable testVariable = questionToVariableMappingStrategy.getQuestionnaireVariable(variable);
    Assert.assertEquals(questionnaire.getName(), testVariable.getName());

    Participant participant = new Participant();

    expect(questionnaireParticipantServiceMock.getQuestionComment(participant, questionnaire.getName(), variable.getParent().getName())).andReturn("This is a comment.").atLeastOnce();
    replay(questionnaireParticipantServiceMock);

    VariableData data = questionToVariableMappingStrategy.getVariableData(questionnaireParticipantServiceMock, participant, variable, new VariableData(variablePathNamingStrategy.getPath(variable)), questionnaire);
    log.info("\n" + VariableStreamer.toXML(data));

    verify(questionnaireParticipantServiceMock);

    Assert.assertEquals(1, data.getDatas().size());
    Assert.assertEquals("This is a comment.", data.getDatas().get(0).getValue());
  }

  @Test
  public void testVariableDataQuestionActive() {
    Variable questionnaireVariable = createQuestionnaireVariable();

    Variable studyVariable = new Variable("ROOT");
    studyVariable.addVariable(questionnaireVariable);
    // log.info("\n"+VariableStreamer.toXML(studyVariable));

    Variable variable = questionnaireVariable.getVariable("BOILER_PLATE").getVariables().get(0);
    Assert.assertEquals("BOILER_PLATE", variable.getParent().getName());
    Assert.assertEquals(DefaultQuestionToVariableMappingStrategy.QUESTION_ACTIVE, variable.getName());

    Variable testVariable = questionToVariableMappingStrategy.getQuestionnaireVariable(variable);
    Assert.assertEquals(questionnaire.getName(), testVariable.getName());

    Participant participant = new Participant();

    expect(questionnaireParticipantServiceMock.isQuestionActive(participant, questionnaire.getName(), variable.getParent().getName())).andReturn(true).atLeastOnce();
    replay(questionnaireParticipantServiceMock);

    VariableData data = questionToVariableMappingStrategy.getVariableData(questionnaireParticipantServiceMock, participant, variable, new VariableData(variablePathNamingStrategy.getPath(variable)), questionnaire);
    log.info("\n" + VariableStreamer.toXML(data));

    verify(questionnaireParticipantServiceMock);

    Assert.assertEquals(1, data.getDatas().size());
    Assert.assertEquals(Boolean.TRUE, data.getDatas().get(0).getValue());
  }

  @Test
  public void testVariableDataQuestionInactive() {
    Variable questionnaireVariable = createQuestionnaireVariable();

    Variable studyVariable = new Variable("ROOT");
    studyVariable.addVariable(questionnaireVariable);
    // log.info("\n"+VariableStreamer.toXML(studyVariable));

    Variable variable = questionnaireVariable.getVariable("BOILER_PLATE").getVariables().get(0);
    Assert.assertEquals("BOILER_PLATE", variable.getParent().getName());
    Assert.assertEquals(DefaultQuestionToVariableMappingStrategy.QUESTION_ACTIVE, variable.getName());

    Variable testVariable = questionToVariableMappingStrategy.getQuestionnaireVariable(variable);
    Assert.assertEquals(questionnaire.getName(), testVariable.getName());

    Participant participant = new Participant();

    expect(questionnaireParticipantServiceMock.isQuestionActive(participant, questionnaire.getName(), variable.getParent().getName())).andReturn(false).atLeastOnce();
    replay(questionnaireParticipantServiceMock);

    VariableData data = questionToVariableMappingStrategy.getVariableData(questionnaireParticipantServiceMock, participant, variable, new VariableData(variablePathNamingStrategy.getPath(variable)), questionnaire);
    log.info("\n" + VariableStreamer.toXML(data));

    verify(questionnaireParticipantServiceMock);

    Assert.assertEquals(1, data.getDatas().size());
    Assert.assertEquals(Boolean.FALSE, data.getDatas().get(0).getValue());
  }

  @Test
  public void testVariableDataQuestionActivityUnknown() {
    Variable questionnaireVariable = createQuestionnaireVariable();

    Variable studyVariable = new Variable("ROOT");
    studyVariable.addVariable(questionnaireVariable);
    // log.info("\n"+VariableStreamer.toXML(studyVariable));

    Variable variable = questionnaireVariable.getVariable("BOILER_PLATE").getVariables().get(0);
    Assert.assertEquals("BOILER_PLATE", variable.getParent().getName());
    Assert.assertEquals(DefaultQuestionToVariableMappingStrategy.QUESTION_ACTIVE, variable.getName());

    Variable testVariable = questionToVariableMappingStrategy.getQuestionnaireVariable(variable);
    Assert.assertEquals(questionnaire.getName(), testVariable.getName());

    Participant participant = new Participant();

    expect(questionnaireParticipantServiceMock.isQuestionActive(participant, questionnaire.getName(), variable.getParent().getName())).andReturn(null).atLeastOnce();
    replay(questionnaireParticipantServiceMock);

    VariableData data = questionToVariableMappingStrategy.getVariableData(questionnaireParticipantServiceMock, participant, variable, new VariableData(variablePathNamingStrategy.getPath(variable)), questionnaire);
    log.info("\n" + VariableStreamer.toXML(data));

    verify(questionnaireParticipantServiceMock);

    Assert.assertEquals(0, data.getDatas().size());
  }

  @Test
  public void testVariableDataCategoryActive() {
    Variable questionnaireVariable = createQuestionnaireVariable();

    Variable studyVariable = new Variable("ROOT");
    studyVariable.addVariable(questionnaireVariable);

    Variable variable = questionnaireVariable.getVariable("Q1").getCategory("1");
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    CategoryAnswer answer = new CategoryAnswer();
    answer.setCategoryName("1");

    expect(questionnaireParticipantServiceMock.getCategoryAnswer(participant, questionnaire.getName(), variable.getParent().getName(), variable.getName())).andReturn(answer).atLeastOnce();
    replay(questionnaireParticipantServiceMock);

    VariableData data = questionToVariableMappingStrategy.getVariableData(questionnaireParticipantServiceMock, participant, variable, new VariableData(variablePathNamingStrategy.getPath(variable)), questionnaire);
    log.info("\n" + VariableStreamer.toXML(data));

    verify(questionnaireParticipantServiceMock);

    Assert.assertEquals(1, data.getDatas().size());
    Assert.assertEquals(Boolean.TRUE, data.getDatas().get(0).getValue());
  }

  @Test
  public void testVariableDataCategoryInactive() {
    Variable questionnaireVariable = createQuestionnaireVariable();

    Variable studyVariable = new Variable("ROOT");
    studyVariable.addVariable(questionnaireVariable);

    Variable variable = questionnaireVariable.getVariable("Q1").getCategory("1");
    Assert.assertNotNull(variable);

    Participant participant = new Participant();

    expect(questionnaireParticipantServiceMock.getCategoryAnswer(participant, questionnaire.getName(), variable.getParent().getName(), variable.getName())).andReturn(null).atLeastOnce();
    replay(questionnaireParticipantServiceMock);

    VariableData data = questionToVariableMappingStrategy.getVariableData(questionnaireParticipantServiceMock, participant, variable, new VariableData(variablePathNamingStrategy.getPath(variable)), questionnaire);
    log.info("\n" + VariableStreamer.toXML(data));

    verify(questionnaireParticipantServiceMock);

    Assert.assertEquals(0, data.getDatas().size());
  }

  @Test
  public void testVariableDataCategory() {
    Variable questionnaireVariable = createQuestionnaireVariable();

    Variable studyVariable = new Variable("ROOT");
    studyVariable.addVariable(questionnaireVariable);
    // log.info("\n"+VariableStreamer.toXML(studyVariable));

    Variable variable = questionnaireVariable.getVariable("Q1");

    Variable testVariable = questionToVariableMappingStrategy.getQuestionnaireVariable(variable);
    Assert.assertEquals(questionnaire.getName(), testVariable.getName());

    Participant participant = new Participant();
    List<CategoryAnswer> answers = new ArrayList<CategoryAnswer>();
    CategoryAnswer answer = new CategoryAnswer();
    answer.setCategoryName("1");
    answers.add(answer);
    answer = new CategoryAnswer();
    answer.setCategoryName("3");
    answers.add(answer);

    expect(questionnaireParticipantServiceMock.getCategoryAnswers(participant, questionnaire.getName(), variable.getName())).andReturn(answers).atLeastOnce();
    replay(questionnaireParticipantServiceMock);

    VariableData data = questionToVariableMappingStrategy.getVariableData(questionnaireParticipantServiceMock, participant, variable, new VariableData(variablePathNamingStrategy.getPath(variable)), questionnaire);
    log.info("\n" + VariableStreamer.toXML(data));

    verify(questionnaireParticipantServiceMock);

    Assert.assertEquals(2, data.getDatas().size());
    Assert.assertEquals("3", data.getDatas().get(1).getValue());
  }

  @Test
  public void testVariableDataOpen() {
    Variable questionnaireVariable = createQuestionnaireVariable();

    Variable studyVariable = new Variable("ROOT");
    studyVariable.addVariable(questionnaireVariable);
    // log.info("\n"+VariableStreamer.toXML(studyVariable));

    // open_int variable
    Variable variable = questionnaireVariable.getVariable("Q2").getVariable("1").getVariable("OPEN_INT");

    Variable testVariable = questionToVariableMappingStrategy.getQuestionnaireVariable(variable);
    Assert.assertEquals(questionnaire.getName(), testVariable.getName());

    Participant participant = new Participant();
    List<CategoryAnswer> answers = new ArrayList<CategoryAnswer>();
    CategoryAnswer answer = new CategoryAnswer();
    answer.setCategoryName("1");
    OpenAnswer open = new OpenAnswer();
    open.setDataType(DataType.INTEGER);
    open.setData(DataBuilder.buildInteger(123l));
    answer.addOpenAnswer(open);
    answers.add(answer);

    answer = new CategoryAnswer();
    answer.setCategoryName("3");
    open = new OpenAnswer();
    open.setDataType(DataType.DATE);
    open.setData(DataBuilder.buildDate(new Date()));
    answer.addOpenAnswer(open);
    answers.add(answer);

    expect(questionnaireParticipantServiceMock.getOpenAnswer(participant, questionnaire.getName(), variable.getParent().getParent().getName(), variable.getParent().getName(), variable.getName())).andReturn(answers.get(0).getOpenAnswers().get(0)).atLeastOnce();
    replay(questionnaireParticipantServiceMock);

    VariableData data = questionToVariableMappingStrategy.getVariableData(questionnaireParticipantServiceMock, participant, variable, new VariableData(variablePathNamingStrategy.getPath(variable)), questionnaire);
    log.info("\n" + VariableStreamer.toXML(data));

    verify(questionnaireParticipantServiceMock);

    Assert.assertEquals(1, data.getDatas().size());
    Assert.assertEquals(DataType.INTEGER, data.getDatas().get(0).getType());
    Assert.assertEquals("123", data.getDatas().get(0).getValueAsString());
  }

  @Test
  public void testVariableDataOpenCategorical() {
    Variable questionnaireVariable = createQuestionnaireVariable();

    Variable studyVariable = new Variable("ROOT");
    studyVariable.addVariable(questionnaireVariable);
    // log.info("\n"+VariableStreamer.toXML(studyVariable));

    // open_int variable
    Variable variable = questionnaireVariable.getVariable("Q2").getVariable("4").getVariable("OPEN_TEXT_DEFAULT_VALUES");

    Variable testVariable = questionToVariableMappingStrategy.getQuestionnaireVariable(variable);
    Assert.assertEquals(questionnaire.getName(), testVariable.getName());

    Participant participant = new Participant();
    List<CategoryAnswer> answers = new ArrayList<CategoryAnswer>();
    CategoryAnswer answer = new CategoryAnswer();
    answer.setCategoryName("4");
    OpenAnswer open = new OpenAnswer();
    open.setDataType(DataType.TEXT);
    open.setData(DataBuilder.buildText("b"));
    answer.addOpenAnswer(open);
    answers.add(answer);

    expect(questionnaireParticipantServiceMock.getOpenAnswer(participant, questionnaire.getName(), variable.getParent().getParent().getName(), variable.getParent().getName(), variable.getName())).andReturn(answers.get(0).getOpenAnswers().get(0)).atLeastOnce();
    replay(questionnaireParticipantServiceMock);

    VariableData data = questionToVariableMappingStrategy.getVariableData(questionnaireParticipantServiceMock, participant, variable, new VariableData(variablePathNamingStrategy.getPath(variable)), questionnaire);
    log.info("\n" + VariableStreamer.toXML(data));

    verify(questionnaireParticipantServiceMock);

    Assert.assertEquals(1, data.getDatas().size());
    Assert.assertEquals(DataType.TEXT, data.getDatas().get(0).getType());
    Assert.assertEquals("b", data.getDatas().get(0).getValueAsString());
  }

  @Test
  public void testVariableDataOpenCategory() {
    Variable questionnaireVariable = createQuestionnaireVariable();

    Variable studyVariable = new Variable("ROOT");
    studyVariable.addVariable(questionnaireVariable);
    // log.info("\n"+VariableStreamer.toXML(studyVariable));

    // open_int variable
    Variable variable = questionnaireVariable.getVariable("Q2").getVariable("4").getVariable("OPEN_TEXT_DEFAULT_VALUES").getCategory("b");

    Variable testVariable = questionToVariableMappingStrategy.getQuestionnaireVariable(variable);
    Assert.assertEquals(questionnaire.getName(), testVariable.getName());

    Participant participant = new Participant();
    List<CategoryAnswer> answers = new ArrayList<CategoryAnswer>();
    CategoryAnswer answer = new CategoryAnswer();
    answer.setCategoryName("4");
    OpenAnswer open = new OpenAnswer();
    open.setDataType(DataType.TEXT);
    open.setData(DataBuilder.buildText("b"));
    answer.addOpenAnswer(open);
    answers.add(answer);

    expect(questionnaireParticipantServiceMock.getOpenAnswer(participant, questionnaire.getName(), variable.getParent().getParent().getParent().getName(), variable.getParent().getParent().getName(), variable.getParent().getName())).andReturn(answers.get(0).getOpenAnswers().get(0)).atLeastOnce();
    replay(questionnaireParticipantServiceMock);

    VariableData data = questionToVariableMappingStrategy.getVariableData(questionnaireParticipantServiceMock, participant, variable, new VariableData(variablePathNamingStrategy.getPath(variable)), questionnaire);
    log.info("\n" + VariableStreamer.toXML(data));

    verify(questionnaireParticipantServiceMock);

    Assert.assertEquals(1, data.getDatas().size());
    Assert.assertEquals(DataType.BOOLEAN, data.getDatas().get(0).getType());
    Assert.assertEquals("true", data.getDatas().get(0).getValueAsString());
  }

  @Test
  public void testVariableDataSharedCategory() {
    Variable questionnaireVariable = createQuestionnaireVariable();

    Variable studyVariable = new Variable("ROOT");
    studyVariable.addVariable(questionnaireVariable);
    // log.info("\n"+VariableStreamer.toXML(studyVariable));

    Variable variable = questionnaireVariable.getVariable("Q3").getVariables().get(2);

    Variable testVariable = questionToVariableMappingStrategy.getQuestionnaireVariable(variable);
    Assert.assertEquals(questionnaire.getName(), testVariable.getName());

    Participant participant = new Participant();
    List<CategoryAnswer> answers = new ArrayList<CategoryAnswer>();
    CategoryAnswer answer = new CategoryAnswer();
    answer.setCategoryName("Y");
    answers.add(answer);

    expect(questionnaireParticipantServiceMock.getCategoryAnswers(participant, questionnaire.getName(), variable.getName())).andReturn(answers).atLeastOnce();
    replay(questionnaireParticipantServiceMock);

    VariableData data = questionToVariableMappingStrategy.getVariableData(questionnaireParticipantServiceMock, participant, variable, new VariableData(variablePathNamingStrategy.getPath(variable)), questionnaire);
    log.info("\n" + VariableStreamer.toXML(data));

    verify(questionnaireParticipantServiceMock);

    Assert.assertEquals(1, data.getDatas().size());
    Assert.assertEquals("Y", data.getDatas().get(0).getValue());
  }

  @Test
  public void testVariableDataSubQuestionCategory() {
    Variable questionnaireVariable = createQuestionnaireVariable();

    Variable studyVariable = new Variable("ROOT");
    studyVariable.addVariable(questionnaireVariable);
    // log.info("\n"+VariableStreamer.toXML(studyVariable));

    Variable variable = questionnaireVariable.getVariable("Q4").getVariables().get(2);

    Variable testVariable = questionToVariableMappingStrategy.getQuestionnaireVariable(variable);
    Assert.assertEquals(questionnaire.getName(), testVariable.getName());

    Participant participant = new Participant();
    List<CategoryAnswer> answers = new ArrayList<CategoryAnswer>();
    CategoryAnswer answer = new CategoryAnswer();
    answer.setCategoryName("Y");
    answers.add(answer);

    expect(questionnaireParticipantServiceMock.getCategoryAnswers(participant, questionnaire.getName(), variable.getName())).andReturn(answers).atLeastOnce();
    replay(questionnaireParticipantServiceMock);

    VariableData data = questionToVariableMappingStrategy.getVariableData(questionnaireParticipantServiceMock, participant, variable, new VariableData(variablePathNamingStrategy.getPath(variable)), questionnaire);
    log.info("\n" + VariableStreamer.toXML(data));

    verify(questionnaireParticipantServiceMock);

    Assert.assertEquals(1, data.getDatas().size());
    Assert.assertEquals("Y", data.getDatas().get(0).getValue());
  }

  public Variable createQuestionnaireVariable() {

    Variable questionnaireVariable = questionToVariableMappingStrategy.getVariable(questionnaire);
    for(Page page : questionnaire.getPages()) {
      for(Question question : page.getQuestions()) {
        questionnaireVariable.addVariable(questionToVariableMappingStrategy.getVariable(question));
      }
    }

    return questionnaireVariable;
  }

  public Questionnaire createQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    builder.withSection("SB").withSection("GENDER").withPage("P1").withQuestion("Q1", true).withCategories("1", "2", "3");
    builder.withSection("S1").withPage("P2").withQuestion("Q2").withCategory("1").withOpenAnswerDefinition("OPEN_INT", DataType.INTEGER);
    builder.inQuestion("Q2").withCategory("2").withOpenAnswerDefinition("OPEN_TEXT", DataType.TEXT);
    builder.inQuestion("Q2").withCategory("3").withOpenAnswerDefinition("OPEN_DATE", DataType.DATE).withOpenAnswerDefinition("OPEN_YEAR", DataType.INTEGER);
    builder.inQuestion("Q2").withCategory("4").withOpenAnswerDefinition("OPEN_TEXT_DEFAULT_VALUES", DataType.TEXT).setDefaultData("a", "b", "c");
    builder.inPage("P2").withQuestion("Q3").withCategories("Y", "N");
    builder.inQuestion("Q3").withQuestion("Q3_1");
    builder.inQuestion("Q3").withQuestion("Q3_2");
    builder.inPage("P2").withQuestion("Q4");
    builder.inQuestion("Q4").withQuestion("Q4_1").withCategories("1", "2");
    builder.inQuestion("Q4").withQuestion("Q4_2").withCategories("Y", "N");
    builder.inPage("P2").withQuestion("BOILER_PLATE");

    Questionnaire q = builder.getQuestionnaire();
    q.addLocale(Locale.FRENCH);
    q.addLocale(Locale.ENGLISH);

    // log.info(QuestionnaireStreamer.toXML(q));

    return q;
  }

}

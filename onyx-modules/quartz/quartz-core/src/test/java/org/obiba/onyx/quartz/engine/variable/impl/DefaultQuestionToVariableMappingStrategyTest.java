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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;
import org.obiba.onyx.quartz.engine.variable.IQuestionToVariableMappingStrategy;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class DefaultQuestionToVariableMappingStrategyTest {

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
    System.out.println(VariableStreamer.toXML(questionnaireVariable));

    Assert.assertEquals(questionnaire.getName(), questionnaireVariable.getName());
    Assert.assertEquals(4, questionnaireVariable.getVariables().size());
    Assert.assertEquals(0, questionnaireVariable.getCategories().size());

    Variable questionVariable = questionnaireVariable.getVariables().get(2);
    Assert.assertEquals("Q1", questionVariable.getName());
    Assert.assertEquals(3, questionVariable.getCategories().size());
    Assert.assertEquals("1", questionVariable.getCategories().get(0));
    Assert.assertEquals("2", questionVariable.getCategories().get(1));
    Assert.assertEquals("3", questionVariable.getCategories().get(2));
    Assert.assertEquals(0, questionVariable.getVariables().size());

    questionVariable = questionnaireVariable.getVariables().get(3);
    Assert.assertEquals("Q2", questionVariable.getName());
    Assert.assertEquals(4, questionVariable.getCategories().size());
    Assert.assertEquals(4, questionVariable.getVariables().size());
    Assert.assertEquals("1", questionVariable.getVariables().get(0).getName());
    Assert.assertEquals(1, questionVariable.getVariables().get(0).getVariables().size());
    Assert.assertEquals("OPEN_INT", questionVariable.getVariables().get(0).getVariables().get(0).getName());
    Assert.assertEquals("3", questionVariable.getVariables().get(2).getName());
    Assert.assertEquals(2, questionVariable.getVariables().get(2).getVariables().size());
    Assert.assertEquals("OPEN_DATE", questionVariable.getVariables().get(2).getVariables().get(0).getName());
    Assert.assertEquals("OPEN_YEAR", questionVariable.getVariables().get(2).getVariables().get(1).getName());

  }

  @Test
  public void testVariableDataQuestion() {
    Variable questionnaireVariable = createQuestionnaireVariable();

    Variable studyVariable = new Variable("ROOT");
    studyVariable.addVariable(questionnaireVariable);
    // System.out.println(VariableStreamer.toXML(studyVariable));

    Variable variable = questionnaireVariable.getVariables().get(2);

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
    System.out.println(VariableStreamer.toXML(data));

    verify(questionnaireParticipantServiceMock);

    Assert.assertEquals(2, data.getDatas().size());
    Assert.assertEquals("3", data.getDatas().get(1).getValue());
  }

  @Test
  public void testVariableDataOpen() {
    Variable questionnaireVariable = createQuestionnaireVariable();

    Variable studyVariable = new Variable("ROOT");
    studyVariable.addVariable(questionnaireVariable);
    // System.out.println(VariableStreamer.toXML(studyVariable));

    // open_int variable
    Variable variable = questionnaireVariable.getVariables().get(3).getVariables().get(0).getVariables().get(0);

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
    System.out.println(VariableStreamer.toXML(data));

    verify(questionnaireParticipantServiceMock);

    Assert.assertEquals(1, data.getDatas().size());
    Assert.assertEquals(DataType.INTEGER, data.getDatas().get(0).getType());
    Assert.assertEquals("123", data.getDatas().get(0).getValueAsString());
  }

  public Variable createQuestionnaireVariable() {

    Variable questionnaireVariable = questionToVariableMappingStrategy.getVariable(questionnaire);
    for(Page page : questionnaire.getPages()) {
      for(Question question : page.getQuestions()) {
        if(!question.isBoilerPlate()) {
          questionnaireVariable.addVariable(questionToVariableMappingStrategy.getVariable(question));
        }
      }
    }

    return questionnaireVariable;
  }

  public Questionnaire createQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    builder.withSection("SB").withSection("GENDER").withPage("P1").withQuestion("Q1").withCategories("1", "2", "3");
    builder.withSection("S1").withPage("P2").withQuestion("Q2").withCategory("1").withOpenAnswerDefinition("OPEN_INT", DataType.INTEGER);
    builder.inQuestion("Q2").withCategory("2").withOpenAnswerDefinition("OPEN_TEXT", DataType.TEXT);
    builder.inQuestion("Q2").withCategory("3").withOpenAnswerDefinition("OPEN_DATE", DataType.DATE).withOpenAnswerDefinition("OPEN_YEAR", DataType.INTEGER);
    builder.inQuestion("Q2").withCategory("4").withOpenAnswerDefinition("OPEN_TEXT_DEFAULT_VALUES", DataType.TEXT).setDefaultData("a", "b", "c");

    Questionnaire q = builder.getQuestionnaire();
    q.addLocale(Locale.FRENCH);
    q.addLocale(Locale.ENGLISH);

    return q;
  }

}

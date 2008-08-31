package org.obiba.onyx.quartz.core.domain.question;

import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

public class QuestionnaireSerializationTest {

	private XStream xstream;
	
	@Before
	public void initializeXStream() {
		xstream = new XStream();
		xstream.setMode(XStream.ID_REFERENCES);
		xstream.alias("questionnaire", Questionnaire.class);
		xstream.alias("questionnaireVersion", QuestionnaireVersion.class);
		xstream.alias("section", QuestionnaireSection.class);
		xstream.alias("page", QuestionnairePage.class);
		xstream.alias("question", ChoiceQuestion.class);
		xstream.alias("array", ArrayQuestion.class);
		xstream.alias("iterative", IterativeQuestion.class);
		xstream.alias("codeAnswer", CodeAnswer.class);
		xstream.alias("questionCodeAnswer", QuestionCodeAnswer.class);
	}
	
	@Test
	public void testQuestionnaire() {
		Questionnaire questionnaire = new Questionnaire("Health Questionnaire");
		
		QuestionnaireVersion questionnaireVersion = new QuestionnaireVersion();
		questionnaireVersion.setVersion("1.0");
		questionnaire.addQuestionnaireVersion(questionnaireVersion);
		
		questionnaireVersion = new QuestionnaireVersion();
		questionnaireVersion.setVersion("2.0");
		questionnaire.addQuestionnaireVersion(questionnaireVersion);
		
		questionnaire.setCurrentQuestionnaireVersion(questionnaireVersion);
		
		QuestionnaireSection firstSection = new QuestionnaireSection();
		firstSection.setDisplayInParentOrder(1);
		firstSection.setLabel("First Section");
		questionnaireVersion.addQuestionnaireSection(firstSection);
		QuestionnaireSection section = new QuestionnaireSection();
		section.setDisplayInParentOrder(1);
		section.setLabel("First Sub-Section");
		firstSection.addQuestionnaireSection(section);
		
		QuestionnairePage page = new QuestionnairePage();
		page.setDisplayInParentOrder(1);
		page.setDisplayOrder(1);
		page.setLabel("Make sure you understand.");
		section.addQuestionnairePage(page);
		
		CodeAnswer c1 = new CodeAnswer();
		c1.setCode("01");
		c1.setLabel("Yes");
		
		CodeAnswer c2 = new CodeAnswer();
		c2.setCode("02");
		c2.setLabel("No");
		
		CodeAnswer c3 = new CodeAnswer();
		c3.setCode("03");
		c3.setLabel("Don't Know");
		
		ChoiceQuestion question = new ChoiceQuestion();
		question.setLabel("Are your sure ?");
		question.setInstructions("Don't be rude.");
		question.setCaption("Make the right choice.");
		question.setDisplayInParentOrder(1);
		question.setCodeAnswerLayout(CodeAnswerLayout.LIST);
		question.setMandatory(false);
		question.setMultiple(false);
		page.addQuestion(question);
		
		QuestionCodeAnswer code = new QuestionCodeAnswer();
		code.setCodeAnswer(c1);
		code.setDisplayInQuestionOrder(1);
		code.setSelected(true);
		question.addQuestionCodeAnswer(code);
		
		code = new QuestionCodeAnswer();
		code.setCodeAnswer(c2);
		code.setDisplayInQuestionOrder(2);
		code.setSelected(false);
		question.addQuestionCodeAnswer(code);
		
		code = new QuestionCodeAnswer();
		code.setCodeAnswer(c3);
		code.setDisplayInQuestionOrder(3);
		code.setSelected(false);
		question.addQuestionCodeAnswer(code);
		
		question = new ChoiceQuestion();
		question.setLabel("Are you really sure ?");
		question.setInstructions("Don't be so rude.");
		question.setCaption("Make the damn right choice.");
		question.setDisplayInParentOrder(2);
		question.setCodeAnswerLayout(CodeAnswerLayout.DROPDOWN);
		question.setMandatory(true);
		question.setMultiple(false);
		page.addQuestion(question);
		
		code = new QuestionCodeAnswer();
		code.setCodeAnswer(c1);
		code.setDisplayInQuestionOrder(1);
		code.setSelected(true);
		question.addQuestionCodeAnswer(code);
		
		code = new QuestionCodeAnswer();
		code.setCodeAnswer(c2);
		code.setDisplayInQuestionOrder(2);
		code.setSelected(false);
		question.addQuestionCodeAnswer(code);
		
		code = new QuestionCodeAnswer();
		code.setCodeAnswer(c3);
		code.setDisplayInQuestionOrder(3);
		code.setSelected(false);
		question.addQuestionCodeAnswer(code);
		
		System.out.println(xstream.toXML(questionnaire));
	}
	
}

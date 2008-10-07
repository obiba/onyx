package org.obiba.onyx.quartz.core.domain.question;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

import com.thoughtworks.xstream.XStream;

public class QuestionnaireSerializationTest {

  private XStream xstream;

  @Before
  public void initializeXStream() {
    xstream = new XStream();
    xstream.setMode(XStream.ID_REFERENCES);
    xstream.alias("questionnaire", Questionnaire.class);
    xstream.alias("section", Section.class);
    xstream.alias("page", Page.class);
    xstream.alias("question", Question.class);
    xstream.alias("category", Category.class);
    xstream.alias("questionCategory", QuestionCategory.class);
  }

  @Test
  public void testQuestionnaire() {
    Questionnaire questionnaire = new Questionnaire("Health Questionnaire", "1.0");

    Section firstSection = new Section();
    firstSection.setName("S1");
    questionnaire.addSection(firstSection);
    Section section = new Section();
    section.setName("S1.1");
    firstSection.addSection(section);

    Page page = new Page();
    questionnaire.addPage(page);
    page.setName("P1");
    section.addPage(page);

    Category c1 = new Category();
    c1.setName("YES");

    Category c2 = new Category();
    c2.setName("NO");

    Category c3 = new Category();
    c3.setName("DONT_KNOW");

    Question question = new Question();
    question.setName("Q1");
    question.setMandatory(false);
    question.setMultiple(false);
    page.addQuestion(question);

    QuestionCategory code = new QuestionCategory();
    code.setCodeAnswer(c1);
    code.setSelected(true);
    question.addQuestionCategories(code);

    code = new QuestionCategory();
    code.setCodeAnswer(c2);
    code.setSelected(false);
    question.addQuestionCategories(code);

    code = new QuestionCategory();
    code.setCodeAnswer(c3);
    code.setSelected(false);
    question.addQuestionCategories(code);

    question = new Question();
    question.setName("Q2");
    question.setMandatory(true);
    question.setMultiple(false);
    page.addQuestion(question);

    code = new QuestionCategory();
    code.setCodeAnswer(c1);
    code.setSelected(true);
    question.addQuestionCategories(code);

    code = new QuestionCategory();
    code.setCodeAnswer(c2);
    code.setSelected(false);
    question.addQuestionCategories(code);

    code = new QuestionCategory();
    code.setCodeAnswer(c3);
    code.setSelected(false);
    question.addQuestionCategories(code);

    System.out.println(xstream.toXML(questionnaire));
  }

}

package org.obiba.onyx.quartz.core.wicket.layout.impl.array;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;

public class ArrayExampleTest {

  private WicketTester tester;

  @Before
  public void setUp() {
    tester = new WicketTester();
  }

  @Test
  public void testDataGridView() {
    Questionnaire questionnaire = createQuestionnaire();
    final Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("SHARED_CATEGORIES_ARRAY_QUESTION");

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      @SuppressWarnings("serial")
      public Panel getTestPanel(String panelId) {

        return new ArrayExample(panelId);
      }
    });

    dumpPage();
  }

  private void dumpPage() {
    tester.dumpPage();
    File dump = new File("target/" + getClass().getSimpleName() + ".html");
    try {
      if(!dump.exists()) dump.createNewFile();
      OutputStream out = new FileOutputStream(dump);
      out.write(tester.getServletResponse().getDocument().getBytes());
      out.flush();
      out.close();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public Questionnaire createQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    builder.withSection("S1").withPage("P1").withQuestion("SHARED_CATEGORIES_ARRAY_QUESTION").withCategories("1", "2", "3");
    builder.inQuestion("SHARED_CATEGORIES_ARRAY_QUESTION").withQuestion("Q1");
    builder.inQuestion("SHARED_CATEGORIES_ARRAY_QUESTION").withQuestion("Q2");
    builder.inQuestion("SHARED_CATEGORIES_ARRAY_QUESTION").withQuestion("Q3");

    Questionnaire q = builder.getQuestionnaire();
    q.addLocale(Locale.ENGLISH);

    return q;
  }

}

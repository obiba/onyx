package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

import com.thoughtworks.xstream.XStream;

public class QuestionnaireStreamer {

  private static final String QUESTIONNAIRE_BASE_NAME = "questionnaire";

  private XStream xstream;

  private QuestionnaireStreamer() {
    initializeXStream();
  }

  public void initializeXStream() {
    xstream = new XStream();
    xstream.setMode(XStream.ID_REFERENCES);
    xstream.alias("questionnaire", Questionnaire.class);
    xstream.useAttributeFor(Questionnaire.class, "name");
    xstream.useAttributeFor(Questionnaire.class, "version");
    xstream.alias("section", Section.class);
    xstream.useAttributeFor(Section.class, "name");
    xstream.alias("page", Page.class);
    xstream.useAttributeFor(Page.class, "name");
    xstream.alias("question", Question.class);
    xstream.useAttributeFor(Question.class, "name");
    xstream.useAttributeFor(Question.class, "number");
    xstream.useAttributeFor(Question.class, "required");
    xstream.useAttributeFor(Question.class, "multiple");
    xstream.useAttributeFor(Question.class, "minCount");
    xstream.useAttributeFor(Question.class, "maxCount");
    xstream.alias("category", Category.class);
    xstream.useAttributeFor(Category.class, "name");
    xstream.alias("questionCategory", QuestionCategory.class);
    xstream.useAttributeFor(QuestionCategory.class, "repeatable");
    xstream.useAttributeFor(QuestionCategory.class, "selected");
    xstream.useAttributeFor(QuestionCategory.class, "exportName");
  }

  public static String toXML(Questionnaire questionnaire) {
    QuestionnaireStreamer streamer = new QuestionnaireStreamer();
    return streamer.xstream.toXML(questionnaire);
  }

  public static void toXML(Questionnaire questionnaire, OutputStream outputStream) {
    QuestionnaireStreamer streamer = new QuestionnaireStreamer();
    streamer.xstream.toXML(questionnaire, outputStream);
  }

  public static void makeBundle(Questionnaire questionnaire, File bundleDirectory) throws IOException {
    if(bundleDirectory == null) throw new IllegalArgumentException("Questionnaire bundle directory cannot be null.");

    if(!bundleDirectory.exists()) {
      bundleDirectory.mkdir();
    } else {
      if(!bundleDirectory.isDirectory()) {
        throw new IllegalArgumentException("Questionnaire bundle directory must be a directory.");
      }
    }

    QuestionnaireStreamer streamer = new QuestionnaireStreamer();

    File questionnaireFile = new File(bundleDirectory, QUESTIONNAIRE_BASE_NAME + ".xml");
    if(!questionnaireFile.exists()) {
      questionnaireFile.createNewFile();
    }

    streamer.xstream.toXML(questionnaire, new FileOutputStream(questionnaireFile));

  }

}

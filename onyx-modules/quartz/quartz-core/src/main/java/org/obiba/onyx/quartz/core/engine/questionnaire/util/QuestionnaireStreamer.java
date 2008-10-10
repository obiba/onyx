package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Properties;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.IQuestionnairePropertiesWriter;
import org.obiba.onyx.util.data.Data;

import com.thoughtworks.xstream.XStream;

public class QuestionnaireStreamer {

  /**
   * The de-serializer.
   */
  private XStream xstream;

  private QuestionnaireStreamer() {
    initializeXStream();
  }

  private void initializeXStream() {
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
    xstream.alias("open", OpenAnswerDefinition.class);
    xstream.useAttributeFor(OpenAnswerDefinition.class, "name");
    xstream.useAttributeFor(OpenAnswerDefinition.class, "dataType");
    xstream.useAttributeFor(OpenAnswerDefinition.class, "unit");
    xstream.useAttributeFor(OpenAnswerDefinition.class, "format");
    xstream.alias("data", Data.class);
    xstream.useAttributeFor(Data.class, "type");
  }

  /**
   * Load a {@link Questionnaire} from its bundle directory.
   * @param inputStream questionnaire input stream
   * @return questionnaire
   */
  public static Questionnaire fromBundle(InputStream inputStream) {
    QuestionnaireStreamer streamer = new QuestionnaireStreamer();
    return (Questionnaire) streamer.xstream.fromXML(inputStream);
  }

  /**
   * Dump a {@link Questionnaire} to a string.
   * @param questionnaire
   * @return
   */
  public static String toXML(Questionnaire questionnaire) {
    QuestionnaireStreamer streamer = new QuestionnaireStreamer();
    return streamer.xstream.toXML(questionnaire);
  }

  /**
   * Dump a {@link Questionnaire} to a stream.
   * @param questionnaire
   * @param outputStream
   */
  public static void toXML(Questionnaire questionnaire, OutputStream outputStream) {
    QuestionnaireStreamer streamer = new QuestionnaireStreamer();
    streamer.xstream.toXML(questionnaire, outputStream);
  }

  /**
   * Stores the a language for a questionnaire, for the given locale, to the specified <code>IQuestionnairePropertiesWriter</code>.
   * 
   * @param questionnaire
   * @param locale
   * @param language
   * @param writer
   */
  public static void storeLanguage(Questionnaire questionnaire, Locale locale, Properties language, IQuestionnairePropertiesWriter writer) {
    QuestionnaireBuilder.getInstance(questionnaire).writeProperties(writer);
  }
  
  /**
   * Stores the a language for a questionnaire, for the given locale, to the specified <code>OutputStream</code>.
   * 
   * @param questionnaire questionnaire
   * @param locale locale
   * @param language language
   * @param outputStream output stream
   */
  public static void storeLanguage(Questionnaire questionnaire, Locale locale, final Properties language, final OutputStream outputStream) {

    storeLanguage(questionnaire, locale, language, new IQuestionnairePropertiesWriter() {

      PrintWriter printWriter = new PrintWriter(outputStream);

      public void endBloc() {
        printWriter.println();
      }

      public void write(String key, String value) {
        printWriter.println(key + "=" + value);
      }

      public void end() {
        printWriter.flush();
        printWriter.close();
      }

      public Properties getReference() {
        return language;
      }

    });
  }

}

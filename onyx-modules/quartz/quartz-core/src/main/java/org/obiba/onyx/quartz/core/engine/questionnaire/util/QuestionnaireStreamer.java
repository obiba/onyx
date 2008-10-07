package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

import com.thoughtworks.xstream.XStream;

public class QuestionnaireStreamer {

  public static final String QUESTIONNAIRE_BASE_NAME = "questionnaire";

  /**
   * The de-serializer.
   */
  private XStream xstream;

  /**
   * A list to store property keys, to make sure there are not repeated multiple times.
   */
  private List<String> propertyKeys;

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
  }

  /**
   * Load a {@link Questionnaire} from its bundle directory.
   * @param bundleDirectory
   * @return
   * @throws FileNotFoundException
   */
  public static Questionnaire fromBundle(File bundleDirectory) throws FileNotFoundException {
    File questionnaireFile = new File(bundleDirectory, QUESTIONNAIRE_BASE_NAME + ".xml");

    QuestionnaireStreamer streamer = new QuestionnaireStreamer();
    return (Questionnaire) streamer.xstream.fromXML(new FileInputStream(questionnaireFile));
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
   * Make the {@link Questionnaire} bundle:
   * <ul>
   * <li>questionnaire description file</li>
   * <li>questionnaire properties file for each language to localize</li>
   * </ul>
   * @param questionnaire
   * @param bundleRootDirectory
   * @param locales
   * @return
   * @throws IOException
   */
  public static File makeBundle(Questionnaire questionnaire, File bundleRootDirectory, Locale... locales) throws IOException {
    if(bundleRootDirectory == null) throw new IllegalArgumentException("Questionnaire bundle directory cannot be null.");

    if(!bundleRootDirectory.exists()) {
      bundleRootDirectory.mkdir();
    } else {
      if(!bundleRootDirectory.isDirectory()) {
        throw new IllegalArgumentException("Questionnaire bundle root directory must be a directory.");
      }
    }

    File bundleDirectory = new File(new File(bundleRootDirectory, questionnaire.getName()), questionnaire.getVersion());
    bundleDirectory.mkdirs();

    QuestionnaireStreamer streamer = new QuestionnaireStreamer();

    File questionnaireFile = new File(bundleDirectory, QUESTIONNAIRE_BASE_NAME + ".xml");
    if(!questionnaireFile.exists()) {
      questionnaireFile.createNewFile();
    }

    streamer.xstream.toXML(questionnaire, new FileOutputStream(questionnaireFile));

    for(Locale locale : locales) {
      File localizedProperties = new File(bundleDirectory, QUESTIONNAIRE_BASE_NAME + "_" + locale + ".properties");
      if(!localizedProperties.exists()) {
        localizedProperties.createNewFile();
      }

      // create an empty property file.
      streamer.propertyKeys = new ArrayList<String>();
      PrintWriter writer = new PrintWriter(localizedProperties);
      streamer.writeLocalizableProperties(questionnaire, writer);
      for(Section section : questionnaire.getSections()) {
        streamer.writeSectionProperties(section, writer);
      }
      writer.flush();
      writer.close();
    }

    return bundleDirectory;
  }

  /**
   * Write localizable properties recursively of {@link Section} content.
   * @param section
   * @param writer
   */
  private void writeSectionProperties(Section section, PrintWriter writer) {
    writeLocalizableProperties(section, writer);
    for(Page page : section.getPages()) {
      writeLocalizableProperties(page, writer);
      for(Question question : page.getQuestions()) {
        writeLocalizableProperties(question, writer);
        for(QuestionCategory questionCategory : question.getQuestionCategories()) {
          writeLocalizableProperties(questionCategory.getCategory(), writer);
          writeLocalizableProperties(questionCategory, questionCategory.getCategory(), writer);
          if(questionCategory.getCategory().getOpenAnswerDefinition() != null) {
            writeLocalizableProperties(questionCategory.getCategory().getOpenAnswerDefinition(), writer);
          }
        }
      }
    }
    for(Section s : section.getSections()) {
      writeSectionProperties(s, writer);
    }
  }

  /**
   * Write localizable properties.
   * @param localizable
   * @param writer
   */
  private void writeLocalizableProperties(ILocalizable localizable, PrintWriter writer) {
    writeLocalizableProperties(localizable, null, writer);
  }

  /**
   * Write localizable properties. If interpolation localizable is provided, set the value to the key reference
   * of interpolation localizable same property key, such as <code>${interpolation.propertyKey}</code>.
   * @param localizable
   * @param interpolationLocalizable
   * @param writer
   */
  private void writeLocalizableProperties(ILocalizable localizable, ILocalizable interpolationLocalizable, PrintWriter writer) {
    boolean written = false;
    for(String property : localizable.getProperties()) {
      String key = localizable.getPropertyKey(property);
      if(!propertyKeys.contains(key)) {
        propertyKeys.add(key);
        if(interpolationLocalizable == null) {
          writer.println(key + "=");
        } else {
          writer.println(key + "=${" + interpolationLocalizable.getPropertyKey(property) + "}");
        }
        written = true;
      }
    }
    if(written) writer.println();
  }
  
}

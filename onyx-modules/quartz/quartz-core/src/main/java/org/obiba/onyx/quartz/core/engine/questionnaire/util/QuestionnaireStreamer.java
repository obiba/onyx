/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Properties;

import org.obiba.onyx.quartz.core.engine.questionnaire.answer.FixedSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.OpenAnswerSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.ParticipantPropertySource;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.TimestampSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.AnswerCondition;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.Condition;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.DataComparator;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.MultipleCondition;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.NoAnswerCondition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.DataValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.IPropertyKeyWriter;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.impl.OutputStreamPropertyKeyWriterImpl;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;
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
    xstream.useAttributeFor(Page.class, "uIFactoryName");
    xstream.alias("question", Question.class);
    xstream.useAttributeFor(Question.class, "name");
    xstream.useAttributeFor(Question.class, "number");
    xstream.useAttributeFor(Question.class, "required");
    xstream.useAttributeFor(Question.class, "multiple");
    xstream.useAttributeFor(Question.class, "minCount");
    xstream.useAttributeFor(Question.class, "maxCount");
    xstream.useAttributeFor(Question.class, "uIFactoryName");
    xstream.alias("category", Category.class);
    xstream.useAttributeFor(Category.class, "name");
    xstream.alias("questionCategory", QuestionCategory.class);
    xstream.useAttributeFor(QuestionCategory.class, "reselectable");
    xstream.useAttributeFor(QuestionCategory.class, "selected");
    xstream.useAttributeFor(QuestionCategory.class, "exportName");
    xstream.alias("open", OpenAnswerDefinition.class);
    xstream.useAttributeFor(OpenAnswerDefinition.class, "name");
    xstream.useAttributeFor(OpenAnswerDefinition.class, "dataType");
    xstream.useAttributeFor(OpenAnswerDefinition.class, "unit");
    xstream.alias("data", Data.class);
    xstream.useAttributeFor(Data.class, "type");
    xstream.alias("dataValidator", DataValidator.class);
    xstream.useAttributeFor(DataValidator.class, "dataType");
    xstream.useAttributeFor(Condition.class, "name");
    xstream.alias("answerCondition", AnswerCondition.class);
    xstream.alias("noAnswerCondition", NoAnswerCondition.class);
    xstream.alias("multipleCondition", MultipleCondition.class);
    xstream.useAttributeFor(MultipleCondition.class, "conditionOperator");
    xstream.alias("dataComparator", DataComparator.class);
    xstream.useAttributeFor(DataComparator.class, "comparisionOperator");
    xstream.alias("fixedSource", FixedSource.class);
    xstream.alias("openAnswerSource", OpenAnswerSource.class);
    xstream.alias("timestampSource", TimestampSource.class);
    xstream.alias("participantPropertySource", ParticipantPropertySource.class);
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
   * Stores the a language for a questionnaire, for the given locale, to the specified
   * <code>IQuestionnairePropertiesWriter</code>.
   * 
   * @param questionnaire
   * @param locale
   * @param language
   * @param writer
   */
  public static void storeLanguage(Questionnaire questionnaire, Locale locale, Properties language, IPropertyKeyProvider propertyKeyProvider, IPropertyKeyWriter writer) {
    QuestionnaireBuilder.getInstance(questionnaire).writeProperties(propertyKeyProvider, writer);
  }

  /**
   * Stores the a language for a questionnaire, for the given locale, to the specified <code>OutputStream</code>.
   * 
   * @param questionnaire questionnaire
   * @param locale locale
   * @param language language
   * @param outputStream output stream
   */
  public static void storeLanguage(Questionnaire questionnaire, Locale locale, Properties language, IPropertyKeyProvider propertyKeyProvider, OutputStream outputStream) {
    storeLanguage(questionnaire, locale, language, propertyKeyProvider, new OutputStreamPropertyKeyWriterImpl(language, outputStream));
  }

}

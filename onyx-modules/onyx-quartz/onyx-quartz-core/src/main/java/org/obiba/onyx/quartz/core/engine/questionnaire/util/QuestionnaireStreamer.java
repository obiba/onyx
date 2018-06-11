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

import org.apache.wicket.util.value.ValueMap;
import org.obiba.core.spring.xstream.InjectingReflectionProviderWrapper;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Variable;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.onyx.core.data.AbstractBeanPropertyDataSource;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.core.data.CurrentDateSource;
import org.obiba.onyx.core.data.DateModifier;
import org.obiba.onyx.core.data.FixedDataSource;
import org.obiba.onyx.core.data.JavascriptDataSource;
import org.obiba.onyx.core.data.ParticipantPropertyDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.data.QuestionnaireDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.IPropertyKeyWriter;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.impl.OutputStreamPropertyKeyWriterImpl;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.data.DataValidator;
import org.obiba.onyx.wicket.data.validation.converter.DataValidatorConverter;
import org.springframework.context.ApplicationContext;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;

public class QuestionnaireStreamer {

  /**
   * The de-serializer.
   */
  private XStream xstream;

  private QuestionnaireStreamer() {
    initializeXStream(null);
  }

  private QuestionnaireStreamer(ApplicationContext applicationContext) {
    initializeXStream(applicationContext);
  }

  private void initializeXStream(ApplicationContext applicationContext) {
    ReflectionProvider reflectionProvider = null;
    if(applicationContext != null) {
      reflectionProvider = new InjectingReflectionProviderWrapper((new XStream()).getReflectionProvider(), applicationContext);
    }
    xstream = MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream(reflectionProvider);
    xstream.setMode(XStream.ID_REFERENCES);

    xstream.alias("questionnaire", Questionnaire.class);
    xstream.useAttributeFor(Questionnaire.class, "name");
    xstream.useAttributeFor(Questionnaire.class, "version");
    xstream.useAttributeFor(Questionnaire.class, "uiType");
    xstream.useAttributeFor(Questionnaire.class, "convertedToMagmaVariables");
    xstream.useAttributeFor(Questionnaire.class, "commentable");

    xstream.alias("section", Section.class);
    xstream.useAttributeFor(Section.class, "name");

    xstream.alias("page", Page.class);
    xstream.useAttributeFor(Page.class, "name");
    xstream.useAttributeFor(Page.class, "uIFactoryName");

    xstream.alias("question", Question.class);
    xstream.useAttributeFor(Question.class, "name");
    xstream.useAttributeFor(Question.class, "number");
    xstream.useAttributeFor(Question.class, "multiple");
    xstream.useAttributeFor(Question.class, "minCount");
    xstream.useAttributeFor(Question.class, "maxCount");
    xstream.useAttributeFor(Question.class, "uIFactoryName");

    xstream.alias("category", Category.class);
    xstream.useAttributeFor(Category.class, "name");
    xstream.useAttributeFor(Category.class, "escape");

    xstream.alias("questionCategory", QuestionCategory.class);
    xstream.useAttributeFor(QuestionCategory.class, "exportName");

    xstream.alias("open", OpenAnswerDefinition.class);
    xstream.useAttributeFor(OpenAnswerDefinition.class, "name");
    xstream.useAttributeFor(OpenAnswerDefinition.class, "dataType");
    xstream.useAttributeFor(OpenAnswerDefinition.class, "unit");
    xstream.useAttributeFor(OpenAnswerDefinition.class, "required");

    xstream.alias("data", Data.class);
    xstream.useAttributeFor(Data.class, "type");

    xstream.alias("dataValidator", DataValidator.class);
    xstream.useAttributeFor(DataValidator.class, "dataType");
    xstream.registerConverter(new DataValidatorConverter().createAliases(xstream));

    xstream.alias("comparingDataSource", ComparingDataSource.class);
    xstream.useAttributeFor(ComparingDataSource.class, "comparisonOperator");

    xstream.alias("fixedDataSource", FixedDataSource.class);

    xstream.alias("questionnaireDataSource", QuestionnaireDataSource.class);
    xstream.useAttributeFor(QuestionnaireDataSource.class, "questionnaire");
    xstream.useAttributeFor(QuestionnaireDataSource.class, "question");
    xstream.useAttributeFor(QuestionnaireDataSource.class, "category");
    xstream.useAttributeFor(QuestionnaireDataSource.class, "openAnswerDefinition");

    xstream.alias("currentDateSource", CurrentDateSource.class);

    xstream.alias("participantPropertyDataSource", ParticipantPropertyDataSource.class);
    xstream.useAttributeFor(AbstractBeanPropertyDataSource.class, "property");
    xstream.useAttributeFor(AbstractBeanPropertyDataSource.class, "unit");

    xstream.alias("dateModifier", DateModifier.class);
    xstream.useAttributeFor(DateModifier.class, "field");
    xstream.useAttributeFor(DateModifier.class, "amount");

    xstream.alias("computingDataSource", ComputingDataSource.class);
    xstream.useAttributeFor(ComputingDataSource.class, "dataType");
    xstream.useAttributeFor(ComputingDataSource.class, "unit");
    xstream.useAttributeFor(ComputingDataSource.class, "expression");

    xstream.alias("scriptDataSource", JavascriptDataSource.class);
    xstream.useAttributeFor(JavascriptDataSource.class, "valueType");
    xstream.useAttributeFor(JavascriptDataSource.class, "valueTable");
    xstream.useAttributeFor(JavascriptDataSource.class, "unit");
    xstream.useAttributeFor(JavascriptDataSource.class, "sequence");

    xstream.alias("variableDataSource", VariableDataSource.class);
    xstream.useAttributeFor(VariableDataSource.class, "path");

    xstream.alias("valueMap", ValueMap.class);
    xstream.omitField(Questionnaire.class, "pages");
  }

  /**
   * Load a {@link Questionnaire} from its bundle directory.
   * @param inputStream questionnaire input stream
   * @return questionnaire
   */
  public static Questionnaire fromBundle(InputStream inputStream, ApplicationContext applicationContext) {
    QuestionnaireStreamer streamer = new QuestionnaireStreamer(applicationContext);
    Questionnaire questionnaire = (Questionnaire) streamer.xstream.fromXML(inputStream);
    questionnaire.getPages().clear();
    QuestionnaireWalker questionnaireWalker = new QuestionnaireWalker(new PageWalkerVisitor(questionnaire));
    questionnaireWalker.walk(questionnaire, true);

    return questionnaire;
  }

  public static class PageWalkerVisitor implements IWalkerVisitor {

    private Questionnaire questionnaire;

    public PageWalkerVisitor(Questionnaire questionnaire) {
      this.questionnaire = questionnaire;
    }

    @Override
    public void visit(Variable variable) {
    }

    @Override
    public void visit(OpenAnswerDefinition openAnswerDefinition) {
    }

    @Override
    public void visit(Category category) {
    }

    @Override
    public void visit(QuestionCategory questionCategory) {
    }

    @Override
    public void visit(Question question) {
    }

    @Override
    public void visit(Page page) {
      questionnaire.addPage(page);
    }

    @Override
    public void visit(Section section) {
    }

    @Override
    public void visit(Questionnaire currentQuestionnaire) {
    }

    @Override
    public boolean visiteMore() {
      return true;
    }
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
  public static void storeLanguage(Questionnaire questionnaire, Locale locale, Properties language, IPropertyKeyWriter writer) {
    QuestionnaireBuilder.getInstance(questionnaire).writeProperties(questionnaire.getPropertyKeyProvider(), writer);
  }

  /**
   * Stores the a language for a questionnaire, for the given locale, to the specified <code>OutputStream</code>.
   * 
   * @param questionnaire questionnaire
   * @param locale locale
   * @param language language
   * @param outputStream output stream
   */
  public static void storeLanguage(Questionnaire questionnaire, Locale locale, Properties language, OutputStream outputStream) {
    storeLanguage(questionnaire, locale, language, new OutputStreamPropertyKeyWriterImpl(language, outputStream));
  }

}

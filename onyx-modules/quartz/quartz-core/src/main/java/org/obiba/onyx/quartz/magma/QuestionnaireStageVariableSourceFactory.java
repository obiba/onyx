/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.magma;

import java.util.Locale;
import java.util.Set;

import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.Variable.Builder;
import org.obiba.magma.beans.BeanPropertyVariableValueSource;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.beans.NoSuchBeanException;
import org.obiba.magma.js.JavascriptVariableBuilder;
import org.obiba.magma.js.JavascriptVariableValueSource;
import org.obiba.magma.support.VariableUnitBuilderVisitor;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.magma.DataTypes;
import org.obiba.onyx.magma.OnyxVariableHelper;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.IWalkerVisitor;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireWalker;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.SimplifiedUIPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.obiba.onyx.util.data.Data;
import org.springframework.context.NoSuchMessageException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Builds the {@code VariableValueSource} instances for a specific {@code Questionnaire}
 */
public class QuestionnaireStageVariableSourceFactory implements VariableValueSourceFactory {

  private QuestionnaireBundle bundle;

  private Stage stage;

  private Questionnaire questionnaire;

  private QuestionnaireBeanResolver beanResolver;

  private IPropertyKeyProvider propertyKeyProvider = new SimplifiedUIPropertyKeyProviderImpl();

  private ImmutableSet.Builder<VariableValueSource> builder;

  public QuestionnaireStageVariableSourceFactory(Stage stage, QuestionnaireBundle bundle, QuestionnaireBeanResolver beanResolver) {
    this.bundle = bundle;
    this.stage = stage;
    this.questionnaire = bundle.getQuestionnaire();
    this.beanResolver = beanResolver;
  }

  public Set<VariableValueSource> createSources(String collection) {
    if(builder == null) {
      builder = new ImmutableSet.Builder<VariableValueSource>();
      buildQuestionnaireRun(collection);
      buildQuestionnaireMetric(collection);
      buildQuestionnaireVariables(collection);
    }
    return builder.build();
  }

  protected void buildQuestionnaireVariables(final String collection) {

    QuestionnaireWalker walker = new QuestionnaireWalker(new IWalkerVisitor() {

      public void visit(OpenAnswerDefinition openAnswerDefinition) {
      }

      public void visit(Category category) {

      }

      public void visit(QuestionCategory questionCategory) {
        buildCategoryVariable(collection, questionCategory);
      }

      public void visit(Question question) {
        // Don't produce variables for boilerplates
        if(question.isBoilerPlate() == false) {
          buildQuestionVariable(collection, question);
        }
      }

      public void visit(Page page) {
      }

      public void visit(Section section) {
      }

      public void visit(Questionnaire questionnaire) {
      }

      public boolean visiteMore() {
        return true;
      }
    });
    walker.walk(questionnaire);

  }

  protected void buildQuestionnaireRun(String collection) {
    BeanVariableValueSourceFactory<QuestionnaireParticipant> factory = new BeanVariableValueSourceFactory<QuestionnaireParticipant>("Participant", QuestionnaireParticipant.class);
    factory.setPrefix(questionnaire.getName() + ".QuestionnaireRun");
    factory.setProperties(ImmutableSet.of("questionnaireVersion", "locale", "timeStart", "timeEnd", "user.login"));
    factory.setPropertyNameToVariableName(ImmutableMap.of("questionnaireVersion", "version", "user.login", "user"));
    factory.setVariableBuilderVisitors(ImmutableSet.of(new BaseQuartzBuilderVisitor()));
    builder.addAll(factory.createSources(collection, beanResolver));
  }

  protected void buildQuestionnaireMetric(String collection) {
    BeanVariableValueSourceFactory<QuestionnairePageMetricAlgorithm> factory = new BeanVariableValueSourceFactory<QuestionnairePageMetricAlgorithm>("Participant", QuestionnairePageMetricAlgorithm.class);
    factory.setPrefix(questionnaire.getName() + ".QuestionnaireMetric");
    factory.setProperties(ImmutableSet.of("section", "page", "duration", "questionCount", "missingCount"));
    factory.setVariableBuilderVisitors(ImmutableSet.of(new BaseQuartzBuilderVisitor(), new VariableUnitBuilderVisitor(ImmutableMap.of("duration", "s"))));
    builder.addAll(factory.createSources(collection, beanResolver));
  }

  /**
   * Builds the variable for a single-choice (exclusive choice) question
   * 
   * @param collection
   * @param question
   */
  protected void buildQuestionVariable(String collection, final Question question) {

    // Build the question.active and question.comment variables
    BeanVariableValueSourceFactory<QuestionAnswer> factory = new BeanVariableValueSourceFactory<QuestionAnswer>("Participant", QuestionAnswer.class);
    factory.setProperties(ImmutableSet.of("active", "comment"));
    factory.setPrefix(variableName(question));
    factory.setVariableBuilderVisitors(ImmutableSet.of(new QuestionElementBuilderVisitor(question)));
    builder.addAll(factory.createSources(collection, beanResolver));

    Variable.Builder questionVariable = Variable.Builder.newVariable(collection, variableName(question), TextType.get(), "Participant");
    if(question.isMultiple()) {
      // Build a repeatable variable for the list of CategoryAnswers
      questionVariable.repeatable();
    }
    questionVariable.accept(new QuestionElementBuilderVisitor(question)).accept(new QuestionnaireElementBuilderVisitor(question));

    // The resolver is expected to return a single CategoryAnswer when the variable is not repeatable and a
    // List<CategoryAnswer> when the variable is repeatable.
    builder.add(new BeanPropertyVariableValueSource(questionVariable.build(), CategoryAnswer.class, beanResolver, "categoryName"));
  }

  protected void buildCategoryVariable(String collection, final QuestionCategory questionCategory) {
    if(questionCategory.hasOpenAnswerDefinition() == false) {
      // Build a derived variable from the Question variable using javascript
      // The script test whether the Question variable has this category amongst its answers
      Variable.Builder categoryVariable = Variable.Builder.newVariable(collection, variableName(questionCategory), BooleanType.get(), "Participant").extend(JavascriptVariableBuilder.class).setScript("$('" + variableName(questionCategory.getQuestion()) + "').any('" + questionCategory.getCategory().getName() + "')");
      categoryVariable.accept(new QuestionElementBuilderVisitor(questionCategory.getQuestion()));
      builder.add(new JavascriptVariableValueSource(categoryVariable.build()));
    } else {
      // Build variable(s) from the open answer(s) of this category
      if(questionCategory.hasOpenAnswerDefinition()) {
        OpenAnswerDefinition parent = questionCategory.getOpenAnswerDefinition();
        // Make an iterable on its children. If no child present, make an with a single value.
        Iterable<OpenAnswerDefinition> oads = parent.hasChildOpenAnswerDefinitions() ? parent.getOpenAnswerDefinitions() : ImmutableSet.of(parent);
        for(OpenAnswerDefinition oad : oads) {
          buildOpenAnswerVariable(collection, questionCategory, oad);
        }
      }
    }
  }

  protected void buildOpenAnswerVariable(String collection, final QuestionCategory questionCategory, final OpenAnswerDefinition oad) {
    Variable.Builder openAnswerVariable = Variable.Builder.newVariable(collection, variableName(questionCategory, oad), DataTypes.valueTypeFor(oad.getDataType()), "Participant");
    openAnswerVariable.accept(new QuestionElementBuilderVisitor(questionCategory.getQuestion())).accept(new OpenAnswerVisitor(oad));

    BeanPropertyVariableValueSource valueSource = new BeanPropertyVariableValueSource(openAnswerVariable.build(), OpenAnswer.class, new AbstractQuartzBeanResolver() {

      public boolean resolves(Class<?> type) {
        return OpenAnswer.class.equals(type);
      }

      public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) throws NoSuchBeanException {
        QuestionAnswer qa = lookupQuestionAnswer(valueSet, variable);
        if(qa != null) {
          return findOpenAnswer(findCategoryAnswer(qa, questionCategory.getCategory().getName()), oad.getName());
        }
        return null;
      }
    }, "data.value");
    builder.add(valueSource);
  }

  /**
   * Returns the name of a Question variable: QuestionnaireName.QuestionName
   * @param question
   * @return
   */
  protected String variableName(Question question) {
    return questionnaire.getName() + '.' + question.getName();
  }

  /**
   * Returns the name of QuestionCategory varaible: ${questionVarName}.CategoryName
   * @param questionCategory
   * @return
   */
  protected String variableName(QuestionCategory questionCategory) {
    return variableName(questionCategory.getQuestion()) + '.' + questionCategory.getCategory().getName();
  }

  /**
   * Returns the name of an OpenAnswerDefinition variable: ${categoryVarName}.OpenAnswerDefinitionName
   * @param questionCategory
   * @param oad
   * @return
   */
  protected String variableName(QuestionCategory questionCategory, OpenAnswerDefinition oad) {
    return variableName(questionCategory) + '.' + oad.getName();
  }

  private class BaseQuartzBuilderVisitor implements Variable.BuilderVisitor {
    public void visit(Builder builder) {
      // Questionnaire name
      builder.addAttribute("questionnaire", questionnaire.getName())
      // Stage name
      .addAttribute("stage", stage.getName());
    }
  }

  private class QuestionnaireElementBuilderVisitor implements Variable.BuilderVisitor {

    IQuestionnaireElement element;

    QuestionnaireElementBuilderVisitor(IQuestionnaireElement element) {
      this.element = element;
    }

    public void visit(Builder builder) {
      boolean open = element instanceof OpenAnswerDefinition;
      for(Locale locale : bundle.getAvailableLanguages()) {
        for(String property : propertyKeyProvider.getProperties(element)) {
          if(open) {
            // the property may be the default value, not to be added to open answer definition annotations
            OpenAnswerDefinition openAnswerDefinition = (OpenAnswerDefinition) element;
            boolean defaultValueProperty = false;
            for(Data defaultValue : openAnswerDefinition.getDefaultValues()) {
              if(defaultValue.getValueAsString().equals(property)) {
                defaultValueProperty = true;
                break;
              }
            }
            if(defaultValueProperty) {
              continue;
            }
          }
          try {
            String stringResource = QuestionnaireStringResourceModelHelper.getMessage(bundle, element, property, null, locale);
            if(stringResource.trim().length() > 0) {
              String noHTMLString = stringResource.replaceAll("\\<.*?\\>", "");
              OnyxVariableHelper.addAttribute(builder, locale, property, noHTMLString);
            }
          } catch(NoSuchMessageException ex) {
            // ignored
          }
        }
      }
    }
  }

  private class QuestionElementBuilderVisitor extends BaseQuartzBuilderVisitor {

    private Question question;

    public QuestionElementBuilderVisitor(Question question) {
      this.question = question;
    }

    public void visit(Builder builder) {
      super.visit(builder);
      // Question number
      builder.addAttribute("questionNumber", question.getNumber())
      // Page name
      .addAttribute("page", question.getPage().getName())
      // TODO bubble-up section hierarchy
      .addAttribute("section", question.getPage().getSection().getName());

      if(question.hasCategories()) {
        for(QuestionCategory c : question.getQuestionCategories()) {
          builder.addCategory(c.getCategory().getName(), c.getExportName(), c.isEscape());
        }
      }
    }

  }

  private class OpenAnswerVisitor extends QuestionnaireElementBuilderVisitor {

    private OpenAnswerDefinition oad;

    public OpenAnswerVisitor(OpenAnswerDefinition oad) {
      super(oad);
      this.oad = oad;
    }

    public void visit(Builder builder) {
      super.visit(builder);
      if(oad.getDefaultValues() != null) {
        int order = 0;
        for(Data defaultValue : oad.getDefaultValues()) {
          builder.addCategory(defaultValue.getValueAsString(), Integer.toString(order++));
          // TODO addLocalizedAttributes(categoryVariable, openAnswerDefinition, defaultValue.getValueAsString());
        }
      }
      builder.unit(oad.getUnit());
    }

  }
}

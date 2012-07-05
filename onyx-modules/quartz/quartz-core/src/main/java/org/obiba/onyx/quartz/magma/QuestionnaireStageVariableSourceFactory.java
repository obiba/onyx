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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.apache.wicket.extensions.validation.validator.RfcCompliantEmailAddressValidator;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.MaximumValidator;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAwareBuilder;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.Variable.Builder;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.beans.BeanPropertyVariableValueSource;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.js.JavascriptVariableBuilder;
import org.obiba.magma.js.JavascriptVariableValueSource;
import org.obiba.magma.support.VariableUnitBuilderVisitor;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.magma.DataTypes;
import org.obiba.onyx.magma.MagmaInstanceProvider;
import org.obiba.onyx.magma.OnyxAttributeHelper;
import org.obiba.onyx.magma.StageAttributeVisitor;
import org.obiba.onyx.quartz.core.data.QuestionnaireDataSource;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.QuestionnaireVariableNameResolver;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinitionAudio;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinitionSuggestion;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.IWalkerVisitor;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireUniqueVariableNameResolver;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireWalker;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.SimplifiedUIPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.data.DataValidator;
import org.obiba.onyx.wicket.data.IDataValidator;
import org.springframework.context.NoSuchMessageException;

/**
 * Builds the {@code VariableValueSource} instances for a specific {@code Questionnaire}
 */
public class QuestionnaireStageVariableSourceFactory implements VariableValueSourceFactory {

  private static final String PARTICIPANT = MagmaInstanceProvider.PARTICIPANT_ENTITY_TYPE;

  private QuestionnaireBundle bundle;

  private Stage stage;

  private Questionnaire questionnaire;

  private IPropertyKeyProvider propertyKeyProvider = new SimplifiedUIPropertyKeyProviderImpl();

  private ImmutableSet.Builder<VariableValueSource> vvsSetBuilder;

  private QuestionnaireVariableNameResolver variableNameResolver;

  public QuestionnaireStageVariableSourceFactory(Stage stage, QuestionnaireBundle bundle) {
    this.bundle = bundle;
    this.stage = stage;
    this.questionnaire = bundle.getQuestionnaire();
  }

  @Override
  public Set<VariableValueSource> createSources() {
    vvsSetBuilder = new ImmutableSet.Builder<VariableValueSource>();
    variableNameResolver = new QuestionnaireUniqueVariableNameResolver();
    buildQuestionnaireRun();
    buildQuestionnaireMetric();
    buildQuestionnaireComment();
    buildQuestionnaireVariables();
    variableNameResolver = null;
    return vvsSetBuilder.build();
  }

  protected void buildQuestionnaireVariables() {

    QuestionnaireWalker walker = new QuestionnaireWalker(new IWalkerVisitor() {
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
        if(question.getParentQuestion() != null) {
          // We've already visited this question since we handle child questions when visiting the parent question
          return;
        }
        if(question.isArrayOfSharedCategories()) {
          new QuestionVariableBuilder(question).build();
          for(Question subQuestion : question.getQuestions()) {
            // Build a variable for each child question, but without comment and using their parent's categories.
            new QuestionVariableBuilder(subQuestion).withParentCategories().build();
          }
        } else if(question.isArrayOfJoinedCategories()) {
          throw new UnsupportedOperationException("Variables for joined categories is not supported.");
        } else if(question.hasSubQuestions()) {
          new QuestionVariableBuilder(question).build();
          for(Question subQuestion : question.getQuestions()) {
            new QuestionVariableBuilder(subQuestion).withCategories().build();
          }
        } else {
          QuestionVariableBuilder questionVariableBuilder = new QuestionVariableBuilder(question).withCategories();
          questionVariableBuilder.build();
        }
      }

      @Override
      public void visit(Page page) {
      }

      @Override
      public void visit(Section section) {
      }

      @Override
      public void visit(Questionnaire questionnaire1) {
      }

      @Override
      public boolean visiteMore() {
        return true;
      }

      @Override
      public void visit(Variable variable) {
        new CustomVariableBuilder(variable).build();
      }
    });
    walker.walk(questionnaire);

  }

  protected void buildQuestionnaireRun() {
    BeanVariableValueSourceFactory<QuestionnaireParticipant> factory = new BeanVariableValueSourceFactory<QuestionnaireParticipant>(
        PARTICIPANT, QuestionnaireParticipant.class);
    factory.setPrefix("QuestionnaireRun");
    factory.setProperties(ImmutableSet.of("questionnaireVersion", "locale", "timeStart", "timeEnd", "user.login"));
    factory.setPropertyNameToVariableName(ImmutableMap.of("questionnaireVersion", "version", "user.login", "user"));
    factory.setVariableBuilderVisitors(
        ImmutableSet.of(new BaseQuartzBuilderVisitor(), new LocalizableBuilderVisitor(questionnaire)));
    vvsSetBuilder.addAll(factory.createSources());
  }

  protected void buildQuestionnaireMetric() {
    BeanVariableValueSourceFactory<QuestionnairePageMetricAlgorithm> factory = new BeanVariableValueSourceFactory<QuestionnairePageMetricAlgorithm>(
        PARTICIPANT, QuestionnairePageMetricAlgorithm.class);
    factory.setPrefix("QuestionnaireMetric");
    factory.setProperties(ImmutableSet.of("section", "page", "duration", "questionCount", "missingCount"));
    factory.setVariableBuilderVisitors(ImmutableSet
        .of(new BaseQuartzBuilderVisitor(), new VariableUnitBuilderVisitor(ImmutableMap.of("duration", "s"))));
    factory.setOccurrenceGroup("QuestionnaireMetric");
    vvsSetBuilder.addAll(factory.createSources());
  }

  protected void buildQuestionnaireComment() {
    if(questionnaire.isCommentable()) {
      BeanVariableValueSourceFactory<QuestionnaireComment> factory = new BeanVariableValueSourceFactory<QuestionnaireComment>(
          PARTICIPANT, QuestionnaireComment.class);
      factory.setPrefix("QuestionnaireComment");
      factory.setProperties(ImmutableSet.of("variable", "comment"));
      factory.setPropertyNameToVariableName(ImmutableMap.of("variable", "question"));
      factory.setVariableBuilderVisitors(ImmutableSet.of(new BaseQuartzBuilderVisitor()));
      factory.setOccurrenceGroup("QuestionnaireComment");
      vvsSetBuilder.addAll(factory.createSources());
    }
  }

  /**
   * Returns the name of a Question variable: QuestionnaireName.QuestionName
   *
   * @param question
   * @return
   */
  protected String variableName(Question question) {
    return variableNameResolver.variableName(question);
  }

  /**
   * Returns the name of QuestionCategory varaible: ${questionVarName}.CategoryName
   *
   * @param questionCategory
   * @return
   */
  protected String variableName(Question question, QuestionCategory questionCategory) {
    return variableNameResolver.variableName(question, questionCategory);
  }

  /**
   * Returns the name of an OpenAnswerDefinition variable: ${categoryVarName}.OpenAnswerDefinitionName
   *
   * @param questionCategory
   * @param oad
   * @return
   */
  protected String variableName(Question question, QuestionCategory questionCategory, OpenAnswerDefinition oad) {
    return variableNameResolver.variableName(question, questionCategory, oad);
  }

  private class CustomVariableBuilder extends BaseQuartzBuilderVisitor {
    private Variable variable;

    public CustomVariableBuilder(Variable variable) {
      super();
      this.variable = variable;
    }

    public void build() {
      Variable.Builder varBuilder = sameAs(variable);
      varBuilder.accept(this);
      vvsSetBuilder.add(new JavascriptVariableValueSource(varBuilder.build()));
    }

    /**
     * Copy the variable explicitly to make sure the value type is not corrupted.
     *
     * @param variable
     * @return
     */
    public Variable.Builder sameAs(Variable variable) {
      Variable.Builder b = Variable.Builder
          .newVariable(variable.getName(), ValueType.Factory.forName(variable.getValueType().getName()),
              variable.getEntityType()).unit(variable.getUnit()).mimeType(variable.getMimeType())
          .referencedEntityType(variable.getReferencedEntityType());
      if(variable.isRepeatable()) {
        b.repeatable().occurrenceGroup(variable.getOccurrenceGroup());
      }
      for(Attribute a : variable.getAttributes()) {
        b.addAttribute(a);
      }
      for(org.obiba.magma.Category c : variable.getCategories()) {
        b.addCategory(c);
      }
      return b;
    }
  }

  private class QuestionVariableBuilder {

    private Question question;

    private List<QuestionCategory> categories;

    public QuestionVariableBuilder(Question question) {
      this.question = question;
    }

    public QuestionVariableBuilder withParentCategories() {
      categories = question.getParentQuestion().getQuestionCategories();
      return this;
    }

    public QuestionVariableBuilder withCategories() {
      categories = question.getQuestionCategories();
      return this;
    }

    public void build() {
      if(categories != null) {
        buildCategoricalVariable();
      } else {
        buildParentPlaceholderVariable();
      }
    }

    private void buildParentPlaceholderVariable() {
      Variable.Builder questionVariable = Variable.Builder
          .newVariable(variableName(question), BooleanType.get(), PARTICIPANT);
      questionVariable.accept(new QuestionAttributesBuilderVisitor(question, true, false))
          .accept(new QuestionBuilderVisitor(question));
      vvsSetBuilder.add(new BeanPropertyVariableValueSource(questionVariable.build(), QuestionAnswer.class, "active"));
    }

    private void buildCategoricalVariable() {
      Variable.Builder questionVariable = Variable.Builder
          .newVariable(variableName(question), TextType.get(), PARTICIPANT);
      if(question.isMultiple()) {
        // Build a repeatable variable for the list of CategoryAnswers
        questionVariable.repeatable();
      }

      questionVariable.accept(new QuestionAttributesBuilderVisitor(question, false, question.isBoilerPlate()))
          .accept(new QuestionBuilderVisitor(question));

      for(QuestionCategory c : categories) {
        org.obiba.magma.Category.Builder cb = org.obiba.magma.Category.Builder.newCategory(c.getCategory().getName());
        cb.accept(new LocalizableBuilderVisitor(c)).withCode(c.getExportName()).missing(c.isEscape());
        addAttributesHierarchy(cb, c);
        questionVariable.addCategory(cb.build());
      }

      // The resolver is expected to return a single CategoryAnswer when the variable is not repeatable and a
      // List<CategoryAnswer> when the variable is repeatable.
      if(question.isBoilerPlate() == false) {
        vvsSetBuilder
            .add(new BeanPropertyVariableValueSource(questionVariable.build(), CategoryAnswer.class, "categoryName"));
      } else {
        vvsSetBuilder
            .add(new BeanPropertyVariableValueSource(questionVariable.build(), QuestionAnswer.class, "active"));
      }

      for(QuestionCategory questionCategory : categories) {
        buildCategoryVariable(questionCategory);
      }
    }

    private void buildCategoryVariable(final QuestionCategory questionCategory) {
      // Build a derived variable from the Question variable using javascript
      // The script test whether the Question variable has this category amongst its answers
      Variable.Builder categoryVariable = Variable.Builder
          .newVariable(variableName(question, questionCategory), BooleanType.get(), PARTICIPANT)
          .extend(JavascriptVariableBuilder.class).setScript(String
              .format("$('%s').isNull().value() ? null : $('%s').any('%s')", variableName(question),
                  variableName(question), questionCategory.getCategory().getName()));
      categoryVariable.accept(new QuestionAttributesBuilderVisitor(question))
          .accept(new QuestionCategoryBuilderVisitor(questionCategory));
      vvsSetBuilder.add(new JavascriptVariableValueSource(categoryVariable.build()));

      // Build variable(s) from the open answer(s) of this category
      if(questionCategory.hasOpenAnswerDefinition()) {
        OpenAnswerDefinition parent = questionCategory.getOpenAnswerDefinition();
        // Make an iterable on its children. If no child present, make an iterable with a single value.
        Iterable<OpenAnswerDefinition> oads = parent.hasChildOpenAnswerDefinitions() ? parent
            .getOpenAnswerDefinitions() : ImmutableSet.of(parent);
        for(OpenAnswerDefinition oad : oads) {
          buildOpenAnswerVariable(questionCategory, oad);
        }
      }
    }

    protected void buildOpenAnswerVariable(final QuestionCategory questionCategory, final OpenAnswerDefinition oad) {
      // Special case for OpenAnswers capturing DataType.DATE:
      // Onyx will only capture the date part (no time). Thus, the valid ValueType is DateType instead of DateTimeType.
      ValueType type = oad.getDataType() == DataType.DATE ? DateType.get() : DataTypes.valueTypeFor(oad.getDataType());

      Variable.Builder openAnswerVariable = Variable.Builder
          .newVariable(variableName(question, questionCategory, oad), type, PARTICIPANT);
      openAnswerVariable.accept(new QuestionAttributesBuilderVisitor(question))
          .accept(new OpenAnswerVisitor(questionCategory, oad));
      BeanPropertyVariableValueSource valueSource = new BeanPropertyVariableValueSource(openAnswerVariable.build(),
          OpenAnswer.class, "data.value");
      vvsSetBuilder.add(valueSource);
    }
  }

  private class BaseQuartzBuilderVisitor extends StageAttributeVisitor {

    BaseQuartzBuilderVisitor() {
      super(stage.getName());
    }

    public void visit(Builder builder1) {
      super.visit(builder1);
      // Questionnaire name
      builder1.addAttribute("questionnaire", questionnaire.getName());
    }
  }

  /**
   * Visitor for setting the question attributes
   */
  private class QuestionAttributesBuilderVisitor extends BaseQuartzBuilderVisitor {

    private Question question;

    private boolean parentPlaceholder;

    private boolean boilerplate;

    public QuestionAttributesBuilderVisitor(Question question, boolean parentPlaceholder, boolean boilerplate) {
      this.question = question;
      this.parentPlaceholder = parentPlaceholder;
      this.boilerplate = boilerplate;
    }

    public QuestionAttributesBuilderVisitor(Question question) {
      this(question, false, false);
    }

    protected Question getQuestion() {
      return question;
    }

    @Override
    public void visit(Builder builder1) {
      super.visit(builder1);

      // Sections : AncestorSection/ParentSection/QuestionSection
      StringBuilder sectionAttribute = new StringBuilder();
      Section s = question.getPage().getSection();
      while(s != null) {
        sectionAttribute.insert(0, s.getName());
        s = s.getParentSection();
        if(s != null) {
          sectionAttribute.insert(0, '/');
        }
      }
      builder1.addAttribute("section", sectionAttribute.toString());

      // Page name
      builder1.addAttribute("page", question.getPage().getName());

      // Question name
      builder1.addAttribute("questionName", question.getName());

      if(question.getNumber() != null) {
        // Question number
        builder1.addAttribute("questionNumber", question.getNumber());
      }

      if(parentPlaceholder) {
        Attribute parentQuestionAttribute = Attribute.Builder.newAttribute("parentQuestion")
            .withValue(BooleanType.get().trueValue()).build();
        builder1.addAttribute(parentQuestionAttribute);
      }

      if(boilerplate) {
        Attribute boilerplateAttribute = Attribute.Builder.newAttribute("boilerplate")
            .withValue(BooleanType.get().trueValue()).build();
        builder1.addAttribute(boilerplateAttribute);
      }
    }

  }

  private class LocalizableBuilderVisitor implements Variable.BuilderVisitor, org.obiba.magma.Category.BuilderVisitor {

    IQuestionnaireElement element;

    LocalizableBuilderVisitor(IQuestionnaireElement element) {
      this.element = element;
    }

    public void visit(Builder builder1) {
      visitAttributes(builder1);
    }

    public void visit(org.obiba.magma.Category.Builder builder1) {
      visitAttributes(builder1);
    }

    protected Iterable<String> getProperties() {
      return propertyKeyProvider.getProperties(element);
    }

    protected void visitAttributes(AttributeAwareBuilder<?> builder1) {
      for(Locale locale : bundle.getAvailableLanguages()) {
        for(String property : getProperties()) {
          try {
            String stringResource = QuestionnaireStringResourceModelHelper
                .getMessage(bundle, element, property, null, locale);
            if(stringResource.trim().length() > 0) {
              String noHTMLString = stringResource.replaceAll("\\<.*?\\>", "");
              builder1.addAttribute(property, noHTMLString, locale);
            }
          } catch(NoSuchMessageException ex) {
            // ignored
          }
        }
      }
    }
  }

  private class QuestionBuilderVisitor extends LocalizableBuilderVisitor {
    Question question;

    public QuestionBuilderVisitor(Question question) {
      super(question);
      this.question = question;
    }

    @Override
    protected void visitAttributes(AttributeAwareBuilder<?> builder1) {
      super.visitAttributes(builder1);

      OnyxAttributeHelper.addConditionAttribute(builder1, question.getCondition());

      // get the min/max settings : for questions having a parent question, if no settings is found parent settings is
      // used.
      Integer minCount = question.getMinCount();
      if(minCount == null && question.getParentQuestion() != null) {
        minCount = question.getParentQuestion().getMinCount();
      }
      Integer maxCount = question.getMaxCount();
      if(maxCount == null && question.getParentQuestion() != null) {
        maxCount = question.getParentQuestion().getMaxCount();
      }

      OnyxAttributeHelper.addRequiredAttribute(builder1, question.isRequired());

      if(minCount != null && minCount > 1) {
        OnyxAttributeHelper.addMinCountAttribute(builder1, minCount);
      }

      if(maxCount != null) {
        OnyxAttributeHelper.addMaxCountAttribute(builder1, maxCount);
      }

      if(question.hasParentQuestion() && question.getParentQuestion().hasAttributes()) {
        builder1.addAttributes(question.getParentQuestion().getAttributes());
      }
      if(question.hasAttributes()) {
        builder1.addAttributes(question.getAttributes());
      }
    }
  }

  private class QuestionCategoryBuilderVisitor extends LocalizableBuilderVisitor {
    QuestionCategory questionCategory;

    public QuestionCategoryBuilderVisitor(QuestionCategory questionCategory) {
      super(questionCategory);
      this.questionCategory = questionCategory;
    }

    protected void visitAttributes(AttributeAwareBuilder<?> builder1) {
      super.visitAttributes(builder1);

      // Provide the category name to the resolver
      builder1.addAttribute("categoryName", questionCategory.getCategory().getName());
      if(this.questionCategory.getQuestion().isMultiple() == false) {
        // Add a marker indicating that this is single-choice category variable
        builder1.addAttribute(
            Attribute.Builder.newAttribute("exclusiveChoiceCategoryVariable").withValue(BooleanType.get().valueOf(true))
                .build());
      }

      // a category variable is implicitly always conditioned by it's parent question
      OnyxAttributeHelper.addConditionAttribute(builder1,
          new QuestionnaireDataSource(bundle.getQuestionnaire().getName(), questionCategory.getQuestion().getName()));

      addAttributesHierarchy(builder1, questionCategory);
    }
  }

  private void addAttributesHierarchy(AttributeAwareBuilder<?> builder1, QuestionCategory questionCategory) {
    Category category = questionCategory.getCategory();
    Question question = questionCategory.getQuestion();
    if(category.hasAttributes()) {
      builder1.addAttributes(category.getAttributes());
    }
    if(question.hasAttributes()) {
      builder1.addAttributes(question.getAttributes());
    }
    if(question.hasParentQuestion() && question.getParentQuestion().hasAttributes()) {
      builder1.addAttributes(question.getParentQuestion().getAttributes());
    }
  }

  private class OpenAnswerVisitor extends LocalizableBuilderVisitor {

    private OpenAnswerDefinition oad;

    private QuestionCategory questionCategory;

    public OpenAnswerVisitor(QuestionCategory questionCategory, OpenAnswerDefinition oad) {
      super(oad);
      this.oad = oad;
      this.questionCategory = questionCategory;
    }

    @Override
    protected Iterable<String> getProperties() {
      // The default behaviour is to include default values as properties so that the labels of default values can also
      // be localised. We want to remove these so that they are not applied as attributes of the Variable.
      return Iterables.filter(super.getProperties(), new Predicate<String>() {
        public boolean apply(String input) {
          for(Data defaultValue : oad.getDefaultValues()) {
            if(input.equals(defaultValue.getValueAsString())) {
              return false;
            }
          }
          return true;
        }
      });
    }

    public void visit(Builder builder1) {
      super.visit(builder1);

      if(oad.getDefaultValues() != null) {
        int order = 0;
        // Make categories out of default values
        for(Data defaultValue : oad.getDefaultValues()) {
          String categoryName = defaultValue.getValueAsString();
          // Make categories out of default values
          org.obiba.magma.Category.Builder cb = org.obiba.magma.Category.Builder.newCategory(categoryName);
          cb.accept(new OadDefaultValueBuilderVisitor(oad, categoryName)).withCode(Integer.toString(order++));
          builder1.addCategory(cb.build());
        }
      }
      builder1.unit(oad.getUnit());

      // Provide the category name to the resolver
      builder1.addAttribute("categoryName", questionCategory.getCategory().getName())
          // Provide the openAnswer name to the resolver
          .addAttribute("openAnswerName", oad.getName());

      if(oad.getDataSource() != null) {
        OnyxAttributeHelper.addSourceAttribute(builder1, oad.getDataSource());
      }

      List<String> validations = new ArrayList<String>();
      if(oad.getDataValidators().size() > 0) {

        for(IDataValidator<?> v : oad.getDataValidators()) {
          if(v instanceof DataValidator) {
            String validator = validatorToString(((DataValidator) v).getValidator());
            if(validator != null) {
              validations.add(validator);
            }
          }
        }
      }

      if(oad.getValidationDataSources().size() > 0) {
        validations.add(oad.getValidationDataSources().toString());
      }
      if(validations.size() > 0) {
        OnyxAttributeHelper.addValidationAttribute(builder1, validations.toString());
      }

      if(oad.isAudioAnswer()) {
        visitAudioOpenAnswer(new OpenAnswerDefinitionAudio(oad), builder1);
      }
      if(oad.isSuggestionAnswer()) {
        visitSuggestedOpenAnswer(new OpenAnswerDefinitionSuggestion(oad), builder1);
      }

      // an open answer is implicitly always conditioned by the selection of its parent category.
      OnyxAttributeHelper.addConditionAttribute(builder1,
          new QuestionnaireDataSource(bundle.getQuestionnaire().getName(), questionCategory.getQuestion().getName(),
              questionCategory.getCategory().getName()).toString());

      if(oad.hasAttributes()) {
        builder1.addAttributes(oad.getAttributes());
      }
      addAttributesHierarchy(builder1, questionCategory);
    }

    private void visitAudioOpenAnswer(OpenAnswerDefinitionAudio audio, Builder builder) {
      builder.mimeType("audio/x-wav");
    }

    private void visitSuggestedOpenAnswer(OpenAnswerDefinitionSuggestion suggestion, Builder builder) {
      if(suggestion.hasVariable()) {
        builder.referencedEntityType(suggestion.getEntityType());
      } else if(suggestion.hasSuggestionItems()) {
        for(String item : suggestion.getSuggestionItems()) {
          String categoryName = item;
          // Make categories out of default values
          org.obiba.magma.Category.Builder cb = org.obiba.magma.Category.Builder.newCategory(categoryName);
          cb.accept(new OadDefaultValueBuilderVisitor(oad, categoryName));
          builder.addCategory(cb.build());
        }
      }
    }
  }

  private class OadDefaultValueBuilderVisitor implements org.obiba.magma.Category.BuilderVisitor {
    OpenAnswerDefinition oad;

    String defaultValue;

    public OadDefaultValueBuilderVisitor(OpenAnswerDefinition oad, String defaultValue) {
      this.oad = oad;
      this.defaultValue = defaultValue;
    }

    public void visit(org.obiba.magma.Category.Builder builder1) {
      for(Locale locale : bundle.getAvailableLanguages()) {
        try {
          String stringResource = QuestionnaireStringResourceModelHelper
              .getMessage(bundle, oad, defaultValue, null, locale);
          if(Strings.isEmpty(stringResource) == false) {
            String noHTMLString = stringResource.replaceAll("\\<.*?\\>", "");
            builder1.addAttribute(OnyxAttributeHelper.LABEL, noHTMLString, locale);
          }
        } catch(NoSuchMessageException ex) {
          // ignored
        }
      }
    }
  }

  /**
   * Turns a IValidator into a String.
   *
   * @param validator
   * @return null if validator not identified.
   */
  private String validatorToString(IValidator<?> validator) {
    if(validator == null) {
      return null;
    }

    if(validator instanceof MaximumValidator) {
      return "Number.Maximum[" + ((MaximumValidator<?>) validator).getMaximum() + "]";
    } else if(validator instanceof MinimumValidator) {
      return "Number.Minimum[" + ((MinimumValidator<?>) validator).getMinimum() + "]";
    } else if(validator instanceof RangeValidator) {
      return "Number.Range[" + ((RangeValidator<?>) validator).getMinimum() + ", " + ((RangeValidator<?>) validator)
          .getMaximum() + "]";
    } else if(validator instanceof StringValidator) {
      if(validator instanceof StringValidator.ExactLengthValidator) {
        return "String.ExactLength[" + ((StringValidator.ExactLengthValidator) validator).getLength() + "]";
      } else if(validator instanceof StringValidator.LengthBetweenValidator) {
        return "String.LengthBetween[" + ((StringValidator.LengthBetweenValidator) validator)
            .getMinimum() + ", " + ((StringValidator.LengthBetweenValidator) validator).getMaximum() + "]";
      } else if(validator instanceof StringValidator.MaximumLengthValidator) {
        return "String.MaximumLength[" + ((StringValidator.MaximumLengthValidator) validator).getMaximum() + "]";
      } else if(validator instanceof StringValidator.MinimumLengthValidator) {
        return "String.MinimumLength[" + ((StringValidator.MinimumLengthValidator) validator).getMinimum() + "]";
      } else if(validator instanceof PatternValidator) {
        if(validator instanceof EmailAddressValidator) {
          return "String.EmailAddress";
        } else if(validator instanceof RfcCompliantEmailAddressValidator) {
          return "String.RfcCompliantEmailAddress";
        } else {
          return "String.Pattern[" + ((PatternValidator) validator).getPattern() + "]";
        }
      }
    }

    return validator.getClass().getSimpleName().replaceAll("Validator", "");
  }

}

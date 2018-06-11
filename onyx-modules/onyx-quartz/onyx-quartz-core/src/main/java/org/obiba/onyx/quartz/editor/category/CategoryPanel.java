package org.obiba.onyx.quartz.editor.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.QuartzEditorPanel;
import org.obiba.onyx.quartz.editor.behavior.VariableNameBehavior;
import org.obiba.onyx.quartz.editor.behavior.tooltip.HelpTooltipPanel;
import org.obiba.onyx.quartz.editor.locale.LabelsPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.utils.MapModel;
import org.obiba.onyx.quartz.editor.utils.SaveablePanel;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

public class CategoryPanel extends Panel implements SaveablePanel {

  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  private final VariableNameBehavior variableNameBehavior;

  private final TextField<String> variable;

  private final List<String> otherCategoryNames = new ArrayList<String>();

  public CategoryPanel(String id,
      IModel<QuestionCategory> model,
      IModel<Questionnaire> questionnaireModel,
      IModel<LocaleProperties> localePropertiesModel, FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow) {

    super(id);

    QuestionCategory questionCategory = model.getObject();
    final Question question = questionCategory.getQuestion();
    final Category category = questionCategory.getCategory();

    String initialName = questionCategory.getCategory().getName();
    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(model, "category.name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new PatternValidator(QuartzEditorPanel.ELEMENT_NAME_PATTERN));

    for(Category otherCategory : question.getCategories()) {
      if(!otherCategory.getName().equals(initialName)) {
        otherCategoryNames.add(otherCategory.getName());
      }
    }

    name.add(new AbstractValidator<String>() {

      @Override
      protected void onValidate(IValidatable<String> validatable) {

        // test if category doesn't already exist when renaming
        String value = validatable.getValue();
        for(String categoryName : otherCategoryNames) {
          if(StringUtils.equalsIgnoreCase(value, categoryName)) {
            error(validatable, "CategoryAlreadyExistsForThisQuestion");
          }
        }
      }
    });
    add(name).add(new SimpleFormComponentLabel("nameLabel", name));
    add(new HelpTooltipPanel("nameHelp", new ResourceModel("Name.Tooltip")));

    variable = new TextField<String>("variable",
        new MapModel<String>(new PropertyModel<Map<String, String>>(model, "category.variableNames"),
            question.getName()));
    variable.setLabel(new ResourceModel("Variable"));
    add(variable).add(new SimpleFormComponentLabel("variableLabel", variable));
    add(new HelpTooltipPanel("variableHelp", new ResourceModel("Variable.Tooltip")));

    add(variableNameBehavior = new VariableNameBehavior(name, variable, question.getParentQuestion(), question,
        null));

    CheckBox escapeCheckBox = new CheckBox("escape", new PropertyModel<Boolean>(model, "category.escape"));
    escapeCheckBox.setLabel(new ResourceModel("EscapeOrMissing"));
    add(escapeCheckBox).add(new SimpleFormComponentLabel("escapeLabel", escapeCheckBox));
    add(new HelpTooltipPanel("escapeHelp", new ResourceModel("EscapeOrMissing.Tooltip")));

    localePropertiesUtils.load(localePropertiesModel.getObject(), questionnaireModel.getObject(), model.getObject());
    add(new LabelsPanel("labels", localePropertiesModel, model, feedbackPanel, feedbackWindow));
  }

  @Override
  public void onSave(AjaxRequestTarget target) {
    if(!variableNameBehavior.isVariableNameDefined()) {
      variable.setModelObject(null);
    }
  }
}

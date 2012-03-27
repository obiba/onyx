/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.openAnswer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.SupportedMedia;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition.OpenAnswerType;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.QuartzEditorPanel;
import org.obiba.onyx.quartz.editor.behavior.VariableNameBehavior;
import org.obiba.onyx.quartz.editor.behavior.tooltip.HelpTooltipPanel;
import org.obiba.onyx.quartz.editor.utils.MapModel;
import org.obiba.onyx.quartz.editor.utils.SaveablePanel;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.wicket.nanogong.NanoGongApplet.Rate;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 *
 */
@SuppressWarnings("serial")
public class AudioOpenAnswerPanel extends Panel implements SaveablePanel {

//  private final transient Logger logger = LoggerFactory.getLogger(getClass());

  private static final NumberFormat MEGABYTE_FORMATTER = new DecimalFormat("#0.00");

  private String initialName;

  private TextField<String> variable;

  private final VariableNameBehavior variableNameBehavior;

  private DropDownChoice<Rate> samplingRateDropDown;

  private TextField<Integer> maxDurationField;

  private Label resultingSizeLabel;

  private WebMarkupContainer resultingSizeContainer;

  public AudioOpenAnswerPanel(String id, final IModel<OpenAnswerDefinition> model, final IModel<Category> categoryModel,
      final IModel<Question> questionModel, final IModel<Questionnaire> questionnaireModel) {
    super(id, model);

    final Question question = questionModel.getObject();
    final Category category = categoryModel.getObject();
    final OpenAnswerDefinition openAnswer = model.getObject();

    initialName = model.getObject().getName();
    final TextField<String> name = new TextField<String>("name", new PropertyModel<String>(model, "name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new PatternValidator(QuartzEditorPanel.ELEMENT_NAME_PATTERN));
    name.add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(IValidatable<String> validatable) {
        if(!StringUtils.equals(initialName, validatable.getValue())) {
          boolean alreadyContains = false;
          if(category != null) {
            Map<String, OpenAnswerDefinition> openAnswerDefinitionsByName = category.getOpenAnswerDefinitionsByName();
            alreadyContains = (openAnswerDefinitionsByName.containsKey(
                validatable.getValue()) && openAnswerDefinitionsByName.get(validatable.getValue()) != openAnswer);
          }
          QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaireModel.getObject());
          questionnaireModel.getObject().setQuestionnaireCache(null);
          OpenAnswerDefinition findOpenAnswerDefinition = questionnaireFinder
              .findOpenAnswerDefinition(validatable.getValue());
          if(alreadyContains || findOpenAnswerDefinition != null && findOpenAnswerDefinition != openAnswer) {
            error(validatable, "OpenAnswerAlreadyExists");
          }
        }
      }
    });
    add(name).add(new SimpleFormComponentLabel("nameLabel", name));
    add(new HelpTooltipPanel("nameHelp", new ResourceModel("Name.Tooltip")));

    variable = new TextField<String>("variable",
        new MapModel<String>(new PropertyModel<Map<String, String>>(model, "variableNames"), question.getName()));
    variable.setLabel(new ResourceModel("Variable"));
    add(variable).add(new SimpleFormComponentLabel("variableLabel", variable));
    add(new HelpTooltipPanel("variableHelp", new ResourceModel("Variable.Tooltip")));

    if(category == null) {
      variableNameBehavior = new VariableNameBehavior(name, variable, question.getParentQuestion(), question, null) {
        @Override
        @SuppressWarnings("hiding")
        protected String
        generateVariableName(Question parentQuestion, Question question, Category category, String name) {
          if(StringUtils.isBlank(name)) return "";
          if(category != null) {
            return super.generateVariableName(parentQuestion, question, category, name);
          }
          String variableName = (parentQuestion == null ? "" : parentQuestion.getName() + ".");
          if(question != null) {
            variableName += question.getName() + "." + question.getName() + ".";
          }
          return variableName + StringUtils.trimToEmpty(name);
        }
      };
    } else {
      variableNameBehavior = new VariableNameBehavior(name, variable, question.getParentQuestion(), question, category);
    }

    add(variableNameBehavior);

    ValueMap uiArgumentsValueMap = openAnswer.getUIArgumentsValueMap();

    Rate samplingRateValue = null;
    if(openAnswer.isAudioAnswer() && uiArgumentsValueMap
        .containsKey(OpenAnswerDefinition.AUDIO_RECORDING_SAMPLING_RATE_KEY)) {
      samplingRateValue = Rate
          .parse(uiArgumentsValueMap.get(OpenAnswerDefinition.AUDIO_RECORDING_SAMPLING_RATE_KEY).toString());
    }
    if(samplingRateValue == null) samplingRateValue = Rate._11025;

    samplingRateDropDown = new DropDownChoice<Rate>("samplingRate", new Model<Rate>(samplingRateValue),
        Arrays.asList(Rate.values()), new IChoiceRenderer<Rate>() {
      @Override
      public Object getDisplayValue(Rate rate) {
        return new StringResourceModel("SamplingRate." + rate.toString(), AudioOpenAnswerPanel.this, null).getString();
      }

      @Override
      public String getIdValue(Rate rate, int index) {
        return rate.toString();
      }
    });

    samplingRateDropDown.setLabel(new ResourceModel("SamplingRate"));
    samplingRateDropDown.add(new RequiredFormFieldBehavior());
    samplingRateDropDown.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        calculateResultingSize();
        target.addComponent(resultingSizeContainer);
      }
    });
    samplingRateDropDown.setNullValid(false);

    add(samplingRateDropDown).add(new SimpleFormComponentLabel("samplingRateLabel", samplingRateDropDown));
    add(new HelpTooltipPanel("samplingRateHelp", new ResourceModel("SamplingRate.Tooltip")));

    Integer maxDurationValue = 1200;
    if(openAnswer.isAudioAnswer() && uiArgumentsValueMap
        .containsKey(OpenAnswerDefinition.AUDIO_RECORDING_MAX_DURATION_KEY)) {
      maxDurationValue = Integer
          .valueOf(uiArgumentsValueMap.get(OpenAnswerDefinition.AUDIO_RECORDING_MAX_DURATION_KEY).toString());
    }
    maxDurationField = new TextField<Integer>("maxDuration", new Model<Integer>(maxDurationValue));
    maxDurationField.setLabel(new ResourceModel("MaxDuration"));
    maxDurationField.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        calculateResultingSize();
        target.addComponent(resultingSizeContainer);
      }
    });
    add(maxDurationField).add(new SimpleFormComponentLabel("maxDurationLabel", maxDurationField));
    add(new HelpTooltipPanel("maxDurationHelp", new ResourceModel("MaxDuration.Tooltip")));

    resultingSizeContainer = new WebMarkupContainer("resultingSizeContainer");
    resultingSizeContainer.setOutputMarkupId(true);
    add(resultingSizeContainer);

    resultingSizeLabel = new Label("resultingSize", new Model<String>(""));
    resultingSizeContainer.add(resultingSizeLabel);
    calculateResultingSize();

    add(new HelpTooltipPanel("resultingSizeHelp", new ResourceModel("ResultingSize.Tooltip")));

  }

  private void calculateResultingSize() {
    String sr = samplingRateDropDown.getValue();
    String md = maxDurationField.getValue();
    if(isNotBlank(sr) && isNotBlank(md)) {
      double megabytes = Bytes.bytes(Double.valueOf(sr) * 2d * Double.valueOf(md)).megabytes();
      resultingSizeLabel.setDefaultModel(new StringResourceModel("ResultingSize", AudioOpenAnswerPanel.this, null,
          new Object[] {MEGABYTE_FORMATTER.format(megabytes)}));
    }
  }

  @Override
  public void onSave(AjaxRequestTarget target) {
    if(!variableNameBehavior.isVariableNameDefined()) {
      variable.setModelObject(null);
    }

    OpenAnswerDefinition opa = (OpenAnswerDefinition) getDefaultModelObject();
    opa.setDataType(DataType.DATA);
    opa.setUnit(SupportedMedia.AUDIO_WAVE.getMimeType());

    // TODO use a specific model instead of use onSave Method
    opa.clearUIArgument();
    opa.addUIArgument(OpenAnswerType.UI_ARGUMENT_KEY, OpenAnswerType.AUDIO_RECORDING.getUiArgument());
    if(StringUtils.isNotBlank(samplingRateDropDown.getValue())) {
      opa.addUIArgument(OpenAnswerDefinition.AUDIO_RECORDING_SAMPLING_RATE_KEY, samplingRateDropDown.getValue());
    }
    if(StringUtils.isNotBlank(maxDurationField.getValue())) {
      opa.addUIArgument(OpenAnswerDefinition.AUDIO_RECORDING_MAX_DURATION_KEY, maxDurationField.getValue());
    }
  }

}

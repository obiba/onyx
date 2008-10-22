/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.data.DataField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefaultOpenAnswerDefinitionPanel extends Panel {

  private static final long serialVersionUID = 8950481253772691811L;

  private static final Logger log = LoggerFactory.getLogger(DefaultOpenAnswerDefinitionPanel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private DataField openField;

  private Data data;

  /**
   * Constructor given the question category (needed for persistency).
   * @param id
   * @param questionCategoryModel
   */
  @SuppressWarnings("serial")
  public DefaultOpenAnswerDefinitionPanel(String id, IModel questionCategoryModel) {
    super(id, questionCategoryModel);

    setOutputMarkupId(true);

    final QuestionCategory questionCategory = (QuestionCategory) questionCategoryModel.getObject();
    final OpenAnswerDefinition openAnswerDefinition = questionCategory.getCategory().getOpenAnswerDefinition();

    CategoryAnswer previousAnswer = activeQuestionnaireAdministrationService.findAnswer(questionCategory);
    if(previousAnswer != null) {
      data = previousAnswer.getData();
    }

    QuestionnaireStringResourceModel openLabel = new QuestionnaireStringResourceModel(openAnswerDefinition, "label");
    QuestionnaireStringResourceModel unitLabel = new QuestionnaireStringResourceModel(openAnswerDefinition, "unitLabel");

    add(new Label("label", openLabel));

    if(openAnswerDefinition.getDefaultValues().size() > 1) {
      openField = new DataField("open", new PropertyModel(this, "data"), openAnswerDefinition.getDataType(), openAnswerDefinition.getDefaultValues(), new IChoiceRenderer() {

        public Object getDisplayValue(Object object) {
          Data data = (Data) object;
          return (String) new QuestionnaireStringResourceModel(openAnswerDefinition, data.getValueAsString()).getObject();
        }

        public String getIdValue(Object object, int index) {
          Data data = (Data) object;
          return data.getValueAsString();
        }

      }, unitLabel.getString());
    } else if(openAnswerDefinition.getDefaultValues().size() > 0) {
      data = openAnswerDefinition.getDefaultValues().get(0);
      openField = new DataField("open", new PropertyModel(this, "data"), openAnswerDefinition.getDataType(), unitLabel.getString());
    } else {
      openField = new DataField("open", new PropertyModel(this, "data"), openAnswerDefinition.getDataType(), unitLabel.getString());
    }
    // TODO check if open answer is always required when defined ?
    openField.setRequired(true);
    add(openField);

    openField.add(new AjaxFormComponentUpdatingBehavior("onblur") {

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        log.info("openField.onUpdate.data={}", data);
        activeQuestionnaireAdministrationService.answer(questionCategory, data);
      }

    });

    openField.add(new AjaxEventBehavior("onclick") {

      @Override
      protected void onEvent(AjaxRequestTarget target) {
        log.info("openField.onClick");
        DefaultOpenAnswerDefinitionPanel.this.onClick(target);
      }

    });

    // set the label of the field
    if(!isEmptyString(openLabel.getString())) {
      openField.setLabel(openLabel);
    } else {
      QuestionnaireStringResourceModel questionCategoryLabel = new QuestionnaireStringResourceModel(questionCategory, "label");
      if(!isEmptyString(questionCategoryLabel.getString())) {
        openField.setLabel(questionCategoryLabel);
      } else {
        if(!isEmptyString(unitLabel.getString())) {
          openField.setLabel(unitLabel);
        } else {
          // last chance : the question label !
          openField.setLabel(new QuestionnaireStringResourceModel(questionCategory.getQuestion(), "label"));
        }
      }
    }
  }

  private boolean isEmptyString(String str) {
    return str == null || str.trim().length() == 0;
  }

  public void setRequired(boolean required) {
    openField.setRequired(required);
  }

  public abstract void onClick(AjaxRequestTarget target);

  public Data getData() {
    return data;
  }

  public void setData(Data data) {
    this.data = data;
  }

  public void setFieldEnabled(boolean enabled) {
    openField.setFieldEnabled(enabled);
  }

  public boolean isFieldEnabled() {
    return openField.isFieldEnabled();
  }

}

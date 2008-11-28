/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidator;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultOpenAnswerDefinitionPanel extends AbstractOpenAnswerDefinitionPanel {

  private static final long serialVersionUID = 8950481253772691811L;

  private static final Logger log = LoggerFactory.getLogger(DefaultOpenAnswerDefinitionPanel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private DataField openField;

  /**
   * Constructor given the question category (needed for persistency).
   * @param id
   * @param questionCategoryModel
   * @param openAnswerDefinitionModel
   */
  public DefaultOpenAnswerDefinitionPanel(String id, IModel questionCategoryModel) {
    super(id, questionCategoryModel);
    initialize();
  }

  /**
   * Constructor.
   * 
   * @param id
   * @param questionModel
   * @param questionCategoryModel
   */
  public DefaultOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel) {
    super(id, questionModel, questionCategoryModel);
    initialize();
  }

  public DefaultOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel, IModel openAnswerDefinitionModel) {
    super(id, questionModel, questionCategoryModel, openAnswerDefinitionModel);
    initialize();
  }

  @SuppressWarnings("serial")
  private void initialize() {
    setOutputMarkupId(true);

    OpenAnswer previousAnswer = activeQuestionnaireAdministrationService.findOpenAnswer(getQuestion(), getQuestionCategory().getCategory(), getOpenAnswerDefinition());
    if(previousAnswer != null) {
      setData(previousAnswer.getData());
    }

    QuestionnaireStringResourceModel openLabel = new QuestionnaireStringResourceModel(getOpenAnswerDefinitionModel(), "label");
    QuestionnaireStringResourceModel unitLabel = new QuestionnaireStringResourceModel(getOpenAnswerDefinitionModel(), "unitLabel");

    add(new Label("label", openLabel));

    if(getOpenAnswerDefinition().getDefaultValues().size() > 1) {
      openField = new DataField("open", new PropertyModel(this, "data"), getOpenAnswerDefinition().getDataType(), getOpenAnswerDefinition().getDefaultValues(), new IChoiceRenderer() {

        public Object getDisplayValue(Object object) {
          Data data = (Data) object;
          return (String) new QuestionnaireStringResourceModel(new PropertyModel(DefaultOpenAnswerDefinitionPanel.this.getModel(), "category.openAnswerDefinition"), data.getValueAsString()).getObject();
        }

        public String getIdValue(Object object, int index) {
          Data data = (Data) object;
          return data.getValueAsString();
        }

      }, unitLabel.getString());
    } else if(getOpenAnswerDefinition().getDefaultValues().size() > 0) {
      setData(getOpenAnswerDefinition().getDefaultValues().get(0));
      openField = new DataField("open", new PropertyModel(this, "data"), getOpenAnswerDefinition().getDataType(), unitLabel.getString());
    } else {
      openField = new DataField("open", new PropertyModel(this, "data"), getOpenAnswerDefinition().getDataType(), unitLabel.getString());
    }

    if(getOpenAnswerDefinition().getValidators() != null) {
      for(IValidator validator : getOpenAnswerDefinition().getValidators()) {
        openField.add(validator);
      }
    }
    add(openField);

    openField.add(new AjaxFormComponentUpdatingBehavior("onblur") {

      @Override
      protected void onUpdate(final AjaxRequestTarget target) {
        log.info("openField.onUpdate.{}.data={}", getQuestion() + ":" + getQuestionCategory() + ":" + getOpenAnswerDefinition().getName(), getData());
        // persist data
        activeQuestionnaireAdministrationService.answer(getQuestion(), getQuestionCategory(), getOpenAnswerDefinition(), getData());

        // refresh feeback to clean a previous error message
        visitParents(WizardForm.class, new Component.IVisitor() {

          public Object component(Component component) {
            log.info("found a wizard form");
            WizardForm form = (WizardForm) component;
            if(form.getFeedbackPanel() != null) {
              target.addComponent(form.getFeedbackPanel());
            }
            return component;
          }

        });
        DefaultOpenAnswerDefinitionPanel.this.onSubmit(target, getQuestionModel(), DefaultOpenAnswerDefinitionPanel.this.getModel());
      }

      @Override
      protected void onError(final AjaxRequestTarget target, RuntimeException e) {
        log.info("openField.onError.{}.data={}", getQuestion() + ":" + getQuestionCategory() + ":" + getOpenAnswerDefinition().getName(), getData());
        log.info("openField.onError={}", Session.get().getFeedbackMessages().iterator().next());
        DefaultOpenAnswerDefinitionPanel.this.onError(target, getQuestionModel(), DefaultOpenAnswerDefinitionPanel.this.getModel());

        // refesh feedback panel to display error messages
        visitParents(WizardForm.class, new Component.IVisitor() {

          public Object component(Component component) {
            log.info("found a wizard form");
            WizardForm form = (WizardForm) component;
            if(form.getFeedbackPanel() != null) {
              target.addComponent(form.getFeedbackPanel());
            }
            return component;
          }

        });
        super.onError(target, e);
      }

    });

    openField.add(new AjaxEventBehavior("onclick") {

      @Override
      protected void onEvent(AjaxRequestTarget target) {
        log.info("openField.onClick");
        DefaultOpenAnswerDefinitionPanel.this.onSelect(target, getQuestionModel(), getQuestionCategoryModel(), getOpenAnswerDefinitionModel());
        openField.focusField(target);
      }

    });

    // set the label of the field
    openField.setLabel(QuestionnaireStringResourceModelHelper.getStringResourceModel(getQuestion(), getQuestionCategory(), getOpenAnswerDefinition()));
  }

  public void setFieldEnabled(boolean enabled) {
    openField.setFieldEnabled(enabled);
  }

  public boolean isFieldEnabled() {
    return openField.isFieldEnabled();
  }

  @Override
  public void setFieldModelObject(Data data) {
    openField.setFieldModelObject(data);
  }

}

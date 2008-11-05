package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RadioQuestionCategoryPanel extends Panel {

  private static final Logger log = LoggerFactory.getLogger(RadioQuestionCategoryPanel.class);

  private DefaultOpenAnswerDefinitionPanel openField;

  public RadioQuestionCategoryPanel(String id, IModel questionCategoryModel) {
    this(id, questionCategoryModel, true);
  }

  @SuppressWarnings("serial")
  public RadioQuestionCategoryPanel(String id, IModel questionCategoryModel, boolean radioLabelVisible) {
    super(id, questionCategoryModel);

    Radio radio = new Radio("radio", questionCategoryModel);
    radio.setLabel(new QuestionnaireStringResourceModel(questionCategoryModel, "label"));

    FormComponentLabel radioLabel = new FormComponentLabel("categoryLabel", radio);
    add(radioLabel);
    radioLabel.add(radio);
    radioLabel.add(new Label("label", radio.getLabel()).setRenderBodyOnly(true).setVisible(radioLabelVisible));

    // previous answer or default selection
    final QuestionCategory questionCategory = (QuestionCategory) questionCategoryModel.getObject();

    if(questionCategory.getCategory().getOpenAnswerDefinition() != null) {
      // there is an open field
      // hide the associated radio and fake selection on click event of open field
      openField = new DefaultOpenAnswerDefinitionPanel("open", new QuestionnaireModel(questionCategory)) {

        @Override
        public void onSelect(AjaxRequestTarget target) {
          log.info("open.onclick.{}", questionCategory.getName());
          onOpenFieldSelection(target);
        }

      };
      add(openField);
      radio.setVisible(false);

    } else {
      // no open answer
      add(new EmptyPanel("open").setVisible(false));
      // persist selection on change event
      // and make sure there is no active open field previously selected
      radio.add(new AjaxEventBehavior("onchange") {

        @Override
        protected void onEvent(AjaxRequestTarget target) {
          log.info("radio.onchange.{}", questionCategory.getName());
          onRadioSelection(target);
        }

      });
    }
  }

  public abstract void onOpenFieldSelection(AjaxRequestTarget target);

  public abstract void onRadioSelection(AjaxRequestTarget target);

  public DefaultOpenAnswerDefinitionPanel getOpenField() {
    return openField;
  }

}

/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.question.condition.ConditionPanel;
import org.obiba.onyx.quartz.editor.utils.SaveCancelPanel;
import org.obiba.onyx.quartz.editor.utils.tab.AjaxSubmitTabbedPanel;
import org.obiba.onyx.quartz.editor.widget.attributes.AttributesPanel;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class QuestionWindow extends Panel {

  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD",
      justification = "Need to be be re-initialized upon deserialization")
  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private Form<EditedQuestion> form;

  private SavableHidableTab conditionTab;

  private SavableHidableTab questionTab;

  public QuestionWindow(String id, final IModel<EditedQuestion> model, final IModel<Questionnaire> questionnaireModel,
      final IModel<LocaleProperties> localePropertiesModel, final ModalWindow modalWindow) {
    super(id, model);

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    questionTab = new SavableHidableTab(new ResourceModel("Question")) {
      private QuestionPanel questionPanel;

      @Override
      public Panel getPanel(String panelId) {
        if(questionPanel == null) {
          questionPanel = new QuestionPanel(panelId, model, questionnaireModel, localePropertiesModel, feedbackPanel,
              feedbackWindow, false) {
            @Override
            public void onQuestionTypeChange(AjaxRequestTarget target, QuestionType questionType) {
            }
          };
        }
        return questionPanel;
      }

      @Override
      public void save(AjaxRequestTarget target) {
        questionPanel.onSave(target);
      }

      @Override
      public boolean isVisible() {
        return true;
      }
    };

    conditionTab = new SavableHidableTab(new ResourceModel("Conditions")) {
      private ConditionPanel panel;

      @Override
      public Panel getPanel(String panelId) {
        if(panel == null) {
          panel = new ConditionPanel(panelId, new Model<Question>(model.getObject().getElement()), questionnaireModel);
        }
        return panel;
      }

      @Override
      public void save(AjaxRequestTarget target) {
        if(panel != null) panel.onSave(target);
      }

      @Override
      public boolean isVisible() {
        return true;
      }
    };

    AbstractTab attributeTab = new AbstractTab(new ResourceModel("Attributes")) {
      @Override
      public Panel getPanel(String panelId) {
        return new AttributesPanel(panelId, new Model<Question>(model.getObject().getElement()),
            questionnaireModel.getObject().getLocales(),
            feedbackPanel, feedbackWindow);
      }
    };

    form = new Form<EditedQuestion>("form", model);
    form.setMultiPart(false);
    localePropertiesUtils
        .load(localePropertiesModel.getObject(), questionnaireModel.getObject(), model.getObject().getElement());

    List<ITab> tabs = new ArrayList<ITab>();
    tabs.add(questionTab);
    tabs.add(attributeTab);
    tabs.add(conditionTab);

    AjaxSubmitTabbedPanel ajaxSubmitTabbedPanel = new AjaxSubmitTabbedPanel("tabs", feedbackPanel, feedbackWindow,
        tabs);
    add(ajaxSubmitTabbedPanel);
    form.add(ajaxSubmitTabbedPanel);
    add(form);

    form.add(new SaveCancelPanel("saveCancel", form) {
      @Override
      protected void onSave(AjaxRequestTarget target, Form<?> form1) {
        questionTab.save(target);
        conditionTab.save(target);
        QuestionWindow.this.onSave(target, form.getModelObject());
        modalWindow.close(target);
      }

      @Override
      protected void onCancel(AjaxRequestTarget target, Form<?> form1) {
        QuestionWindow.this.onCancel(target, form.getModelObject());
        modalWindow.close(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });

  }

  protected abstract void onSave(AjaxRequestTarget target, final EditedQuestion editedQuestion);

  protected abstract void onCancel(AjaxRequestTarget target, final EditedQuestion editedQuestion);

}

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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.PanelCachingTab;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.openAnswer.autocomplete.AutoCompleteOpenAnswerPanel;
import org.obiba.onyx.quartz.editor.utils.SaveCancelPanel;
import org.obiba.onyx.quartz.editor.utils.SaveablePanel;
import org.obiba.onyx.quartz.editor.utils.tab.AjaxSubmitTabbedPanel;
import org.obiba.onyx.quartz.editor.widget.attributes.AttributesPanel;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class OpenAnswerWindow extends Panel {

  // private final transient Logger log = LoggerFactory.getLogger(getClass());

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<OpenAnswerDefinition> form;

  private Panel openAnswerPanel;

  public OpenAnswerWindow(String id, final IModel<OpenAnswerDefinition> model, final IModel<Category> categoryModel,
      final IModel<Question> questionModel, final IModel<Questionnaire> questionnaireModel,
      final IModel<LocaleProperties> localePropertiesModel, final ModalWindow modalWindow) {
    super(id, model);

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    add(form = new Form<OpenAnswerDefinition>("form", model));
    form.setMultiPart(false);

    List<ITab> tabs = new ArrayList<ITab>();
    AjaxSubmitTabbedPanel ajaxSubmitTabbedPanel = new AjaxSubmitTabbedPanel("openAnswerTabs", feedbackPanel,
        feedbackWindow, tabs);
    tabs.add(new PanelCachingTab(new AbstractTab(new ResourceModel("OpenAnswerDefinition")) {
      @Override
      public Panel getPanel(String panelId) {
        switch(model.getObject().getOpenAnswerType()) {
          case AUDIO_RECORDING:
            openAnswerPanel = new AudioOpenAnswerPanel(panelId, model, categoryModel, questionModel,
                questionnaireModel);
            break;
          case AUTO_COMPLETE:
            openAnswerPanel = new AutoCompleteOpenAnswerPanel(panelId, model, categoryModel, questionModel,
                questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow);
            break;
          default:
            openAnswerPanel = new OpenAnswerPanel(panelId, model, categoryModel, questionModel,
                questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow);
        }
        return openAnswerPanel;
      }
    }));

    tabs.add(new PanelCachingTab(new AbstractTab(new ResourceModel("Attributes")) {
      @Override
      public Panel getPanel(String panelId) {
        return new AttributesPanel(panelId, new Model<OpenAnswerDefinition>(model.getObject()),
            questionnaireModel.getObject().getLocales(), feedbackPanel, feedbackWindow);
      }
    }));

    form.add(ajaxSubmitTabbedPanel);

    form.add(new SaveCancelPanel("saveCancel", form) {
      @Override
      protected void onSave(AjaxRequestTarget target, Form<?> form1) {
        ((SaveablePanel) openAnswerPanel).onSave(target);
        if(form.hasError()) return;
        OpenAnswerWindow.this.onSave(target, form.getModelObject());
        modalWindow.close(target);
      }

      @Override
      protected void onCancel(AjaxRequestTarget target, Form<?> form1) {
        OpenAnswerWindow.this.onCancel(target, form.getModelObject());
        modalWindow.close(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, @SuppressWarnings("hiding")
      Form<?> form) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });

  }

  protected abstract void onSave(AjaxRequestTarget target, OpenAnswerDefinition openAnswer);

  protected abstract void onCancel(AjaxRequestTarget target, OpenAnswerDefinition openAnswer);

}

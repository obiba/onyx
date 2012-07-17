/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.category;

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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.openAnswer.OpenAnswerListPanel;
import org.obiba.onyx.quartz.editor.utils.SaveCancelPanel;
import org.obiba.onyx.quartz.editor.utils.tab.AjaxSubmitTabbedPanel;
import org.obiba.onyx.quartz.editor.widget.attributes.AttributesPanel;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

@SuppressWarnings("serial")
public abstract class CategoryWindow extends Panel {

  // private final transient Logger log = LoggerFactory.getLogger(getClass());

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<QuestionCategory> form;

  private CategoryPanel categoryPanel;

  public CategoryWindow(String id, final IModel<QuestionCategory> model, final IModel<Questionnaire> questionnaireModel,
      final IModel<LocaleProperties> localePropertiesModel, final ModalWindow modalWindow) {
    super(id, model);

    QuestionCategory questionCategory = model.getObject();

    final Category category = questionCategory.getCategory();

    add(form = new Form<QuestionCategory>("form", model));
    form.setMultiPart(false);

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    List<ITab> tabs = new ArrayList<ITab>();
    AjaxSubmitTabbedPanel ajaxSubmitTabbedPanel = new AjaxSubmitTabbedPanel("categoryTabs", feedbackPanel, feedbackWindow,
        tabs);

    tabs.add(new PanelCachingTab(new AbstractTab(new ResourceModel("Category")) {
      @Override
      public Panel getPanel(String panelId) {
        categoryPanel = new CategoryPanel(panelId, model, questionnaireModel, localePropertiesModel,
            feedbackPanel,
            feedbackWindow) {
        };
        return categoryPanel;
      }
    }));

    tabs.add(new PanelCachingTab(new AbstractTab(new ResourceModel("OpenAnswerDefinitions")) {
      @Override
      public Panel getPanel(String panelId) {
        return new OpenAnswerListPanel(panelId, model, questionnaireModel, localePropertiesModel, feedbackPanel,
            feedbackWindow);
      }
    }));

    tabs.add(new PanelCachingTab(new AbstractTab(new ResourceModel("Attributes")) {
      @Override
      public Panel getPanel(String panelId) {
        return new AttributesPanel(panelId, new Model<Category>(category),
            questionnaireModel.getObject().getLocales(),
            feedbackPanel, feedbackWindow);
      }
    }));

    form.add(ajaxSubmitTabbedPanel);

    form.add(new SaveCancelPanel("saveCancel", form) {
      @Override
      protected void onSave(AjaxRequestTarget target, Form<?> form1) {
        categoryPanel.onSave(target);
        CategoryWindow.this.onSave(target, form.getModelObject());
        modalWindow.close(target);
      }

      @Override
      protected void onCancel(AjaxRequestTarget target, Form<?> form1) {
        CategoryWindow.this.onCancel(target, form.getModelObject());
        modalWindow.close(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });

  }

  public abstract void onSave(AjaxRequestTarget target, QuestionCategory questionCategory);

  public abstract void onCancel(AjaxRequestTarget target, QuestionCategory questionCategory);

}

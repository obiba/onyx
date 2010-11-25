/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.page;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.wicket.layout.impl.simplified.SimplifiedPageLayoutFactory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultPageLayoutFactory;
import org.obiba.onyx.quartz.editor.QuartzEditorPanel;
import org.obiba.onyx.quartz.editor.behavior.tooltip.HelpTooltipPanel;
import org.obiba.onyx.quartz.editor.locale.LabelsPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.questionnaire.utils.QuestionnairePersistenceUtils;
import org.obiba.onyx.quartz.editor.utils.SaveCancelPanel;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

@SuppressWarnings("serial")
public abstract class PagePanel extends Panel {

  // private final transient Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<Page> form;

  private final IModel<Questionnaire> questionnaireModel;

  private final IModel<LocaleProperties> localePropertiesModel;

  public PagePanel(String id, final IModel<Page> model, final IModel<Questionnaire> questionnaireModel) {
    super(id, model);
    this.questionnaireModel = questionnaireModel;
    final Questionnaire questionnaire = questionnaireModel.getObject();

    final Page page = model.getObject();
    if(StringUtils.isBlank(page.getUIFactoryName())) {
      if(Questionnaire.SIMPLIFIED_UI.equals(questionnaire.getUiType())) {
        page.setUIFactoryName(new SimplifiedPageLayoutFactory().getBeanName());
      } else {
        page.setUIFactoryName(new DefaultPageLayoutFactory().getBeanName());
      }
    }

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    add(form = new Form<Page>("form", model));
    form.setMultiPart(false);

    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "name"), String.class);
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new PatternValidator(QuartzEditorPanel.ELEMENT_NAME_PATTERN));
    name.add(new AbstractValidator<String>() {

      @Override
      protected void onValidate(IValidatable<String> validatable) {
        if(!StringUtils.equals(page.getName(), validatable.getValue())) {
          if(QuestionnaireFinder.getInstance(questionnaire).findPage(validatable.getValue()) != null) {
            error(validatable, "PageAlreadyExists");
          }
        }
      }
    });
    form.add(name);
    form.add(new SimpleFormComponentLabel("nameLabel", name)).add(new HelpTooltipPanel("nameHelp", new ResourceModel("Name.Tooltip")));

    localePropertiesModel = new Model<LocaleProperties>(localePropertiesUtils.load(questionnaire, page));
    form.add(new LabelsPanel("labels", localePropertiesModel, model, feedbackPanel, feedbackWindow));

    form.add(new SaveCancelPanel("saveCancel", form) {
      @Override
      protected void onSave(AjaxRequestTarget target, Form<?> form1) {
        PagePanel.this.onSave(target, form.getModelObject());
      }

      @Override
      protected void onCancel(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
        PagePanel.this.onCancel(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });
  }

  protected abstract void onSave(AjaxRequestTarget target, Page page);

  protected abstract void onCancel(AjaxRequestTarget target);

  protected void persist(AjaxRequestTarget target) throws Exception {
    try {
      questionnairePersistenceUtils.persist(questionnaireModel.getObject(), localePropertiesModel.getObject());
    } catch(Exception e) {
      error(e.getMessage());
      feedbackWindow.setContent(feedbackPanel);
      feedbackWindow.show(target);
      throw e;
    }
  }

}
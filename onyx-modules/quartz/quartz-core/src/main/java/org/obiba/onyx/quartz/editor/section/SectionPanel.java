/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.section;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.behavior.tooltip.TooltipBehavior;
import org.obiba.onyx.quartz.editor.locale.LabelsPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.questionnaire.utils.QuestionnairePersistenceUtils;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public abstract class SectionPanel extends Panel {

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<Section> form;

  private final IModel<Questionnaire> questionnaireModel;

  private final IModel<LocaleProperties> localePropertiesModel;

  public SectionPanel(String id, final IModel<Section> model, final IModel<Questionnaire> questionnaireModel) {
    super(id, model);
    this.questionnaireModel = questionnaireModel;

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    add(form = new Form<Section>("form", model));

    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "name"), String.class);
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior()).add(new TooltipBehavior(new ResourceModel("Name.Tooltip")));
    name.add(new AbstractValidator<String>() {

      @Override
      protected void onValidate(IValidatable<String> validatable) {
        if(!StringUtils.equals(model.getObject().getName(), validatable.getValue())) {
          if(QuestionnaireFinder.getInstance(questionnaireModel.getObject()).findSection(validatable.getValue()) != null) {
            error(validatable, "SectionAlreadyExists");
          }
        }
      }
    });
    form.add(name).add(new SimpleFormComponentLabel("nameLabel", name));

    localePropertiesModel = new Model<LocaleProperties>(localePropertiesUtils.load(questionnaireModel.getObject(), model.getObject()));
    form.add(new LabelsPanel("labels", localePropertiesModel, model, feedbackPanel, feedbackWindow));

    form.add(new AjaxButton("save", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        onSave(target, form.getModelObject());
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form<?> form2) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });

    form.add(new AjaxButton("cancel", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        onCancel(target);
      }
    }.setDefaultFormProcessing(false));
  }

  /**
   * 
   * @param target
   * @param section
   */
  public abstract void onSave(AjaxRequestTarget target, Section section);

  public abstract void onCancel(AjaxRequestTarget target);

  public void persist(AjaxRequestTarget target) {
    try {
      questionnairePersistenceUtils.persist(questionnaireModel.getObject(), localePropertiesModel.getObject());
    } catch(Exception e) {
      log.error("Cannot persist questionnaire", e);
      error(e.getMessage());
      feedbackWindow.setContent(feedbackPanel);
      feedbackWindow.show(target);
    }
  }

}

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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.IHasSection;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.ui.LocalesPropertiesAjaxTabbedPanel;
import org.obiba.onyx.quartz.editor.questionnaire.EditedQuestionnaire;
import org.obiba.onyx.quartz.editor.questionnaire.QuestionnairePersistenceUtils;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public abstract class SectionPropertiesPanel extends Panel {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  private QuestionnaireBundleManager questionnaireBundleManager;

  @SpringBean
  private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<EditedSection> form;

  private final IModel<EditedQuestionnaire> questionnaireModel;

  private ListModel<LocaleProperties> localePropertiesModel;

  @SuppressWarnings("unchecked")
  public SectionPropertiesPanel(String id, IModel<Section> model, final IModel<IHasSection> parentModel, IModel<EditedQuestionnaire> questionnaireModel, final ModalWindow modalWindow) {
    super(id, new Model<EditedSection>(new EditedSection(model.getObject())));
    this.questionnaireModel = questionnaireModel;

    List<LocaleProperties> listLocaleProperties = new ArrayList<LocaleProperties>();
    Questionnaire questionnaire = questionnaireModel.getObject().getElement();
    for(Locale locale : questionnaire.getLocales()) {
      LocaleProperties localeProperties = new LocaleProperties(locale, model);
      List<String> values = new ArrayList<String>();
      for(String property : localeProperties.getKeys()) {
        if(StringUtils.isNotBlank(model.getObject().getName())) {
          QuestionnaireBundle bundle = questionnaireBundleManager.getClearedMessageSourceCacheBundle(questionnaire.getName());
          if(bundle != null) {
            values.add(QuestionnaireStringResourceModelHelper.getNonRecursiveResolutionMessage(bundle, model.getObject(), property, new Object[0], locale));
          }
        }
      }
      localeProperties.setValues(values.toArray(new String[localeProperties.getKeys().length]));
      listLocaleProperties.add(localeProperties);
    }
    localePropertiesModel = new ListModel<LocaleProperties>(listLocaleProperties);

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    add(form = new Form<EditedSection>("form", (IModel<EditedSection>) getDefaultModel()));

    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "element.name"), String.class);
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new AbstractValidator<String>() {

      @Override
      protected void onValidate(IValidatable<String> validatable) {
        for(Section section : parentModel.getObject().getSections()) {
          if(section != form.getModelObject().getElement() && section.getName().equalsIgnoreCase(validatable.getValue())) {
            error(validatable, "SectionAlreadyExists");
            return;
          }
        }
      }
    });
    form.add(name);
    form.add(new SimpleFormComponentLabel("nameLabel", name));

    form.add(new LocalesPropertiesAjaxTabbedPanel("localesPropertiesTabs", new PropertyModel<Section>(form.getModel(), "element"), localePropertiesModel));

    form.add(new AjaxButton("save", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        onSave(target, form.getModelObject());
        modalWindow.close(target);
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
        modalWindow.close(target);
      }
    }.setDefaultFormProcessing(false));
  }

  /**
   * 
   * @param target
   * @param editedSection
   */
  public void onSave(AjaxRequestTarget target, EditedSection editedSection) {
    editedSection.setLocalePropertiesWithNamingStrategy(localePropertiesModel.getObject());
  }

  public void persist(AjaxRequestTarget target) {
    try {
      QuestionnaireBuilder builder = questionnairePersistenceUtils.createBuilder(questionnaireModel.getObject());
      questionnairePersistenceUtils.persist(form.getModelObject(), builder);
    } catch(Exception e) {
      log.error("Cannot persist questionnaire", e);
      error(e.getMessage());
      feedbackWindow.setContent(feedbackPanel);
      feedbackWindow.show(target);
    }
  }

}

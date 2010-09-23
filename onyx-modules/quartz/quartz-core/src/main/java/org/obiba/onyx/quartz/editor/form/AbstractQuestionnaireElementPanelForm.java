/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

@SuppressWarnings("serial")
public abstract class AbstractQuestionnaireElementPanelForm<T extends IQuestionnaireElement> extends Panel {

  @SpringBean
  protected QuestionnaireBundleManager questionnaireBundleManager;

  protected FeedbackPanel feedbackPanel;

  protected FeedbackWindow feedbackWindow;

  protected final ModalWindow modalWindow;

  protected Form<T> form;

  protected ListModel<LocaleProperties> localePropertiesModel;

  protected Questionnaire questionnaireParent;

  public AbstractQuestionnaireElementPanelForm(String id, IModel<T> model, Questionnaire questionnaireParent, ModalWindow modalWindow) {
    super(id, model);
    List<LocaleProperties> listLocaleProperties = new ArrayList<LocaleProperties>();
    if(questionnaireParent != null) {
      for(Locale locale : questionnaireParent.getLocales()) {
        LocaleProperties localeProperties = new LocaleProperties(locale, model.getObject());
        List<String> values = new ArrayList<String>();
        for(String property : localeProperties.getKeys()) {
          if(StringUtils.isNotBlank(model.getObject().getName())) {
            QuestionnaireBundle bundle = questionnaireBundleManager.getClearedMessageSourceCacheBundle(questionnaireParent.getName());
            if(bundle != null) {
              String message = QuestionnaireStringResourceModelHelper.getMessage(bundle, model.getObject(), property, new Object[0], locale);
              values.add(message);
            }
          }
        }
        localeProperties.setValues(values.toArray(new String[localeProperties.getKeys().length]));
        listLocaleProperties.add(localeProperties);
      }
    }
    localePropertiesModel = new ListModel<LocaleProperties>(listLocaleProperties);
    this.questionnaireParent = questionnaireParent;
    this.modalWindow = modalWindow;

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);
    add(form = new Form<T>("form", model));

    form.add(new AjaxButton("save", form) {

      @SuppressWarnings("unchecked")
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        onSave(target, (T) form2.getModelObject());
        AbstractQuestionnaireElementPanelForm.this.modalWindow.close(target);
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
        AbstractQuestionnaireElementPanelForm.this.modalWindow.close(target);
      }
    }.setDefaultFormProcessing(false));
  }

  /**
   * 
   * @param target
   * @param t
   */
  public void onSave(AjaxRequestTarget target, T t) {

  }

  @SuppressWarnings("unchecked")
  protected Map<Locale, Properties> getLocalePropertiesToMap() {
    DefaultPropertyKeyProviderImpl defaultPropertyKeyProviderImpl = new DefaultPropertyKeyProviderImpl();
    Map<Locale, Properties> mapLocaleProperties = new HashMap<Locale, Properties>();
    for(LocaleProperties localeProperties : localePropertiesModel.getObject()) {
      Properties properties = new Properties();
      for(int i = 0; i < localeProperties.getKeys().length; i++) {
        String key = localeProperties.getKeys()[i];
        String value = localeProperties.getValues()[i];
        String keyWithNamingStrategy = defaultPropertyKeyProviderImpl.getPropertyKey((T) getDefaultModelObject(), key);
        properties.setProperty(keyWithNamingStrategy, value != null ? value : "");
      }
      mapLocaleProperties.put(localeProperties.getLocale(), properties);
    }
    return mapLocaleProperties;
  }

  public Form<T> getForm() {
    return form;
  }

  public Questionnaire getQuestionnaireParent() {
    return questionnaireParent;
  }

}

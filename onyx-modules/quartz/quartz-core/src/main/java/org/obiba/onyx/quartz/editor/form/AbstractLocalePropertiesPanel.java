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
public abstract class AbstractLocalePropertiesPanel<T extends IQuestionnaireElement> extends Panel {

  @SpringBean
  protected QuestionnaireBundleManager questionnaireBundleManager;

  protected FeedbackPanel feedbackPanel;

  protected FeedbackWindow feedbackWindow;

  protected ListModel<LocaleProperties> localePropertiesModel;

  protected Form<T> form;

  protected IModel<Questionnaire> questionnaireParentModel;

  /**
   * @param id
   * @param model
   */
  public AbstractLocalePropertiesPanel(String id, IModel<T> model, IModel<Questionnaire> questionnaireParentModel) {
    super(id, model);
    this.questionnaireParentModel = questionnaireParentModel;
    List<LocaleProperties> listLocaleProperties = new ArrayList<LocaleProperties>();
    if(questionnaireParentModel != null && questionnaireParentModel.getObject() != null) {
      for(Locale locale : questionnaireParentModel.getObject().getLocales()) {
        LocaleProperties localeProperties = new LocaleProperties(locale, model);
        List<String> values = new ArrayList<String>();
        for(String property : localeProperties.getKeys()) {
          if(StringUtils.isNotBlank(model.getObject().getName())) {
            QuestionnaireBundle bundle = questionnaireBundleManager.getClearedMessageSourceCacheBundle(questionnaireParentModel.getObject().getName());
            if(bundle != null) {
              String message = QuestionnaireStringResourceModelHelper.getNonRecursiveMessage(bundle, model.getObject(), property, new Object[0], locale);
              values.add(message);
            }
          }
        }
        localeProperties.setValues(values.toArray(new String[localeProperties.getKeys().length]));
        listLocaleProperties.add(localeProperties);
      }
    }
    localePropertiesModel = new ListModel<LocaleProperties>(listLocaleProperties);

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    add(form = new Form<T>("form", model));
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

  public IModel<T> getModel() {
    return form.getModel();
  }

  public IModel<Questionnaire> getQuestionnaireParentModel() {
    return questionnaireParentModel;
  }

  /**
   * 
   * @param target
   * @param t
   */
  public abstract void onSave(AjaxRequestTarget target, T t);

}

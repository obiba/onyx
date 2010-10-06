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

import java.io.File;
import java.io.IOException;
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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireCreator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
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

  protected IModel<Questionnaire> questionnaireModel;

  /**
   * @param id
   * @param model
   */
  public AbstractLocalePropertiesPanel(String id, IModel<T> model, IModel<Questionnaire> questionnaireModel) {
    super(id, model);
    this.questionnaireModel = questionnaireModel;
    List<LocaleProperties> listLocaleProperties = new ArrayList<LocaleProperties>();
    if(questionnaireModel != null && questionnaireModel.getObject() != null) {
      for(Locale locale : questionnaireModel.getObject().getLocales()) {
        LocaleProperties localeProperties = new LocaleProperties(locale, model);
        List<String> values = new ArrayList<String>();
        for(String property : localeProperties.getKeys()) {
          if(StringUtils.isNotBlank(model.getObject().getName())) {
            QuestionnaireBundle bundle = questionnaireBundleManager.getClearedMessageSourceCacheBundle(questionnaireModel.getObject().getName());
            if(bundle != null) {
              // special case when Category (bad to do this in superclass)
              if(model.getObject() instanceof Category) {
                Map<Category, List<Question>> map = QuestionnaireFinder.getInstance(bundle.getQuestionnaire()).findCategories(model.getObject().getName());
                // no message for category if it is not shared
                if(map.get(model.getObject()).size() < 2) {
                  values.add("");
                } else {
                  String message = QuestionnaireStringResourceModelHelper.getNonRecursiveResolutionMessage(bundle, model.getObject(), property, new Object[0], locale);
                  values.add(message);
                }
              } else {
                String message = QuestionnaireStringResourceModelHelper.getNonRecursiveResolutionMessage(bundle, model.getObject(), property, new Object[0], locale);
                values.add(message);
              }
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

  public void saveToFiles() {
    try {
      // TODO change this hardcoded stuff
      File bundleRootDirectory = new File("target\\work\\webapp\\WEB-INF\\config\\quartz\\resources", "questionnaires");
      File bundleSourceDirectory = new File("src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "WEB-INF" + File.separatorChar + "config" + File.separatorChar + "quartz" + File.separatorChar + "resources", "questionnaires");
      new QuestionnaireCreator(bundleRootDirectory, bundleSourceDirectory).createQuestionnaire(QuestionnaireBuilder.getInstance((questionnaireModel != null ? questionnaireModel.getObject() : (Questionnaire) getDefaultModelObject())), getLocalePropertiesToMap());
    } catch(IOException e) {
      throw new RuntimeException("Cannot save questionnaire", e);
    }
  }

  public Form<T> getForm() {
    return form;
  }

  public IModel<T> getModel() {
    return form.getModel();
  }

  public IModel<Questionnaire> getQuestionnaireModel() {
    return questionnaireModel;
  }

  /**
   * 
   * @param target
   * @param t
   */
  public abstract void onSave(AjaxRequestTarget target, T t);

}

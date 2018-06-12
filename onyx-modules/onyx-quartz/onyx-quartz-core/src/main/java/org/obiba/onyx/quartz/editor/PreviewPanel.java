/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor;

import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;

public abstract class PreviewPanel<T extends IQuestionnaireElement> extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  protected ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @SpringBean
  protected QuestionnaireBundleManager bundleManager;

  protected Panel previewLayout;

  private WebMarkupContainer previewLanguageContainer;

  public PreviewPanel(String id, final IModel<T> model, IModel<Questionnaire> questionnaireModel) {
    super(id, model);
    Questionnaire questionnaire = questionnaireModel.getObject();

    QuestionnaireBundle bundle = bundleManager.getBundle(questionnaire.getName());
    bundle.clearMessageSourceCache();
    questionnaire.setQuestionnaireCache(null);

    previewLanguageContainer = new WebMarkupContainer("previewLanguageContainer");
    previewLanguageContainer.setVisible(questionnaire.getLocales().size() > 1);

    final Locale userLocale = Session.get().getLocale();
    IChoiceRenderer<Locale> renderer = new IChoiceRenderer<Locale>() {

      private static final long serialVersionUID = 1L;

      @Override
      public String getIdValue(Locale locale, int index) {
        return locale.toString();
      }

      @Override
      public Object getDisplayValue(Locale locale) {
        return locale.getDisplayLanguage(userLocale);
      }
    };

    final DropDownChoice<Locale> languageChoice = new DropDownChoice<Locale>("languageChoice", new Model<Locale>(userLocale), questionnaire.getLocales(), renderer);
    languageChoice.setNullValid(false);
    languageChoice.add(new OnChangeAjaxBehavior() {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        activeQuestionnaireAdministrationService.setDefaultLanguage(languageChoice.getModelObject());
        previewLayout = createPreviewLayout(model);
        PreviewPanel.this.addOrReplace(previewLayout);
        target.addComponent(previewLayout);
      }
    });
    previewLanguageContainer.add(languageChoice);
    add(previewLanguageContainer);

    activeQuestionnaireAdministrationService.setQuestionnaire(questionnaire);
    activeQuestionnaireAdministrationService.setDefaultLanguage(languageChoice.getModelObject());
    activeQuestionnaireAdministrationService.setQuestionnaireDevelopmentMode(true);
  }

  public abstract Panel createPreviewLayout(IModel<?> model);
}

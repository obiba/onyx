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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultPageLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class PagePreviewPanel extends Panel {

  private transient final Logger logger = LoggerFactory.getLogger(getClass());

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @SpringBean
  private QuestionnaireBundleManager bundleManager;

  public PagePreviewPanel(String id, IModel<Page> model, IModel<Questionnaire> questionnaireModel) {
    super(id, model);
    Questionnaire questionnaire = questionnaireModel.getObject();
    activeQuestionnaireAdministrationService.setQuestionnaire(questionnaire);
    activeQuestionnaireAdministrationService.setDefaultLanguage(questionnaire.getLocales().get(0));
    activeQuestionnaireAdministrationService.setQuestionnaireDevelopmentMode(true);

    QuestionnaireBundle bundle = bundleManager.getBundle(questionnaire.getName());
    bundle.clearMessageSourceCache();
    questionnaire.setQuestionnaireCache(null);
    try {
      add(new DefaultPageLayout("preview", model));
    } catch(Exception e) {
      logger.error(e.getMessage(), e);
      // TODO: localize error message
      add(new Label("preview", "Error while generating the Page preview: " + e.getMessage()));
    }
  }
}

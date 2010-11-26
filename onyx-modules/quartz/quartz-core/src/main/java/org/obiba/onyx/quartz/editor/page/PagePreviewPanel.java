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

import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
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

  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD",
      justification = "Need to be be re-initialized upon deserialization")
  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD",
      justification = "Need to be be re-initialized upon deserialization")
  @SpringBean
  private QuestionnaireBundleManager bundleManager;

  public PagePreviewPanel(String id, final IModel<Page> model, IModel<Questionnaire> questionnaireModel) {
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
      add(new MultiLineLabel("preview", new StringResourceModel("Error", this, null, new Object[] { e.getMessage() })));
    }
  }

}

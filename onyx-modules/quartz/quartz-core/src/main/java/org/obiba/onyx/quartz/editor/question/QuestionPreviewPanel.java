/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultQuestionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DropDownQuestionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DropDownQuestionPanelFactory;
import org.obiba.onyx.wicket.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public abstract class QuestionPreviewPanel extends Panel {

  private transient final Logger logger = LoggerFactory.getLogger(getClass());

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @SpringBean
  private QuestionnaireBundleManager bundleManager;

  public QuestionPreviewPanel(String id, final IModel<Question> model, IModel<Questionnaire> questionnaireModel) {
    super(id, model);
    Questionnaire questionnaire = questionnaireModel.getObject();
    activeQuestionnaireAdministrationService.setQuestionnaire(questionnaire);
    activeQuestionnaireAdministrationService.setDefaultLanguage(questionnaire.getLocales().get(0));
    activeQuestionnaireAdministrationService.setQuestionnaireDevelopmentMode(true);
    QuestionnaireBundle bundle = bundleManager.getBundle(questionnaire.getName());
    bundle.clearMessageSourceCache();
    questionnaire.setQuestionnaireCache(null);
    try {
      if(model.getObject().getUIFactoryName().contains(DropDownQuestionPanelFactory.class.getSimpleName())) {
        add(new DropDownQuestionPanel("preview", model));
      } else {
        add(new DefaultQuestionPanel("preview", model));
      }
    } catch(Exception e) {
      logger.error(e.getMessage(), e);
      add(new MultiLineLabel("preview", new StringResourceModel("Error", this, null, new Object[] { e.getMessage() })));
    }

    add(new AjaxLink<Void>("edit") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        onEdit(target, model);
      }
    }.add(new Image("img", Images.EDIT)));

  }

  protected abstract void onEdit(AjaxRequestTarget target, IModel<Question> model);
}

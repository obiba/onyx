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

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultPageLayout;

@SuppressWarnings("serial")
public class PagePreviewPanel extends Panel {

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  public PagePreviewPanel(String id, IModel<Page> model, IModel<Questionnaire> questionnaireModel) {
    super(id, model);
    activeQuestionnaireAdministrationService.setQuestionnaire(questionnaireModel.getObject());
    activeQuestionnaireAdministrationService.setDefaultLanguage(questionnaireModel.getObject().getLocales().get(0));
    activeQuestionnaireAdministrationService.setQuestionnaireDevelopmentMode(true);
    add(new DefaultPageLayout("preview", model));
  }
}

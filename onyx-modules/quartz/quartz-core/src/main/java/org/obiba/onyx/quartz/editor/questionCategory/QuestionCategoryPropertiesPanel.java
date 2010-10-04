/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionCategory;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.form.AbstractQuestionnaireElementPanelForm;
import org.obiba.onyx.quartz.editor.locale.ui.LocalesPropertiesAjaxTabbedPanel;

@SuppressWarnings("serial")
public class QuestionCategoryPropertiesPanel extends AbstractQuestionnaireElementPanelForm<QuestionCategory> {

  public QuestionCategoryPropertiesPanel(String id, IModel<QuestionCategory> model, Questionnaire questionnaireParent, ModalWindow modalWindow) {
    super(id, model, questionnaireParent, modalWindow);
    createComponent();
  }

  private void createComponent() {
    form.add(new LocalesPropertiesAjaxTabbedPanel("localesPropertiesTabs", form.getModelObject(), localePropertiesModel));
  }

  @Override
  public void onSave(AjaxRequestTarget target, QuestionCategory t) {
    // TODO Auto-generated method stub

  }

}

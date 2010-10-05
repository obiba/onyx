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
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.form.AbstractLocalePropertiesPanel;
import org.obiba.onyx.quartz.editor.locale.ui.LocalesPropertiesAjaxTabbedPanel;

@SuppressWarnings("serial")
public class QuestionCategoryPropertiesPanel extends AbstractLocalePropertiesPanel<QuestionCategory> {

  public QuestionCategoryPropertiesPanel(String id, IModel<QuestionCategory> model, IModel<Questionnaire> questionnaireParent) {
    super(id, model, questionnaireParent);
    createComponent();
  }

  private void createComponent() {
    form.add(new TextField<String>("exportName", new PropertyModel<String>(form.getModel(), "exportName")));
    form.add(new LocalesPropertiesAjaxTabbedPanel("localesPropertiesTabs", form.getModel(), localePropertiesModel));
  }

  @Override
  public void onSave(AjaxRequestTarget target, QuestionCategory t) {

  }

}

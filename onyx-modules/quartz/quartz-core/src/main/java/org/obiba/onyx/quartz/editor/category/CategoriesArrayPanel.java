/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.category;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.question.EditedQuestion;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

@SuppressWarnings("serial")
public class CategoriesArrayPanel extends Panel {

  public CategoriesArrayPanel(String id, IModel<EditedQuestion> model, IModel<Questionnaire> questionnaireModel, IModel<LocaleProperties> localePropertiesModel, FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow) {
    super(id, model);

    MultipleChoiceCategoryHeaderPanel multipleChoiceCategoryHeaderPanel = new MultipleChoiceCategoryHeaderPanel("headerMultipleChoice", questionnaireModel, model);
    add(multipleChoiceCategoryHeaderPanel);

    add(new CategoryListPanel("categories", model, questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow, multipleChoiceCategoryHeaderPanel));
  }

}

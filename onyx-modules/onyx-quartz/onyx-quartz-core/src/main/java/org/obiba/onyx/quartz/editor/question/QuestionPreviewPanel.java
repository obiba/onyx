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

import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultQuestionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DropDownQuestionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DropDownQuestionPanelFactory;
import org.obiba.onyx.quartz.editor.PreviewPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class QuestionPreviewPanel extends PreviewPanel<Question> {

  private static final Logger logger = LoggerFactory.getLogger(QuestionPreviewPanel.class);

  public QuestionPreviewPanel(String id, final IModel<Question> model, IModel<Questionnaire> questionnaireModel) {
    super(id, model, questionnaireModel);

    try {
      add(previewLayout = createPreviewLayout(model));
    } catch(Exception e) {
      logger.error(e.getMessage(), e);
      add(new MultiLineLabel("preview", new StringResourceModel("Error", this, null, new Object[] { e.getMessage() })));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Panel createPreviewLayout(IModel<?> model) {
    IModel<Question> modelTyped = (IModel<Question>) model;
    // if(Questionnaire.SIMPLIFIED_UI.equals(questionnaireModel.getObject().getUiType())) {
    // return new SimplifiedQuestionPanel("preview", modelTyped);
    // } else {
    if(modelTyped.getObject().getUIFactoryName().contains(DropDownQuestionPanelFactory.class.getSimpleName())) {
      return new DropDownQuestionPanel("preview", modelTyped);
    }
    return new DefaultQuestionPanel("preview", modelTyped);
    // }
  }
}

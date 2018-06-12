/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.singledocument;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.impl.BaseQuestionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultQuestionCategoriesPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultQuestionSharedCategoriesPanel;
import org.obiba.onyx.quartz.core.wicket.provider.AllChildQuestionsProvider;

/**
 * Support for question multiple or not, with(out) child questions, with shared categories, but not yet with joined
 * categories.
 */
public class SingleDocumentQuestionPanel extends BaseQuestionPanel {

  private static final long serialVersionUID = 2951128797454847260L;

  public SingleDocumentQuestionPanel(String id, IModel<Question> questionModel) {
    super(id, questionModel);
    setOutputMarkupId(false);
    setCommentVisible(false);
    // TODO: this should probably be done by setCommentVisible, but it breaks some unit tests of the Simplified laytout.
    // Need to check if the component really is used in the simplified layout. Otherwise, hide it.
    get("comment-action").setVisible(false);
  }

  protected Panel createCategoriesPanel(String id, IModel<Question> questionModel) {
    return new DefaultQuestionCategoriesPanel(id, questionModel);
  }

  protected Panel createQuetionListPanel(String id, IModel<Question> questionModel) {
    return new SingleDocumentQuestionListPanel(id, new AllChildQuestionsProvider(questionModel));
  }

  protected Panel createSharedCategoriesPanel(String id, IModel<Question> questionModel) {
    return new DefaultQuestionSharedCategoriesPanel(id, questionModel, new AllChildQuestionsProvider(questionModel));
  }

  protected Panel createJoinedCategoriesPanel(String id, IModel<Question> questionModel) {
    throw new UnsupportedOperationException("Joined categories array questions not supported yet");
  }
}

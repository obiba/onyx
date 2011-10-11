/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.standard;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.impl.BaseQuestionPanel;
import org.obiba.onyx.quartz.core.wicket.provider.AnswerableChildQuesionProvider;

/**
 * Support for question multiple or not, with(out) child questions, with shared categories, but not yet with joined
 * categories.
 */
public class DefaultQuestionPanel extends BaseQuestionPanel {

  private static final long serialVersionUID = 2951128797454847260L;

  public DefaultQuestionPanel(String id, IModel<Question> questionModel) {
    super(id, questionModel);
    setOutputMarkupId(true);
  }

  @Override
  protected Panel createCategoriesPanel(String id, IModel<Question> questionModel) {
    return new DefaultQuestionCategoriesPanel(id, questionModel);
  }

  @Override
  protected Panel createQuetionListPanel(String id, IModel<Question> questionModel) {
    return new DefaultQuestionListPanel(id, new AnswerableChildQuesionProvider(questionModel));
  }

  @Override
  protected Panel createSharedCategoriesPanel(String id, IModel<Question> questionModel) {
    return new DefaultQuestionSharedCategoriesPanel(id, questionModel, new AnswerableChildQuesionProvider(questionModel));
  }

  @Override
  protected Panel createJoinedCategoriesPanel(String id, IModel<Question> questionModel) {
    throw new UnsupportedOperationException("Joined categories array questions not supported yet");
  }
}

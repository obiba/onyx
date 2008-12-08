/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionListProvider;

/**
 * Support for question multiple or not, but without child questions.
 */
public class DefaultQuestionPanel extends BaseQuestionPanel {

  private static final long serialVersionUID = 2951128797454847260L;

  public DefaultQuestionPanel(String id, IModel questionModel) {
    super(id, questionModel);
    setOutputMarkupId(true);
  }

  @Override
  protected void setContent(String id) {
    Question question = (Question) getModelObject();

    if(question.getQuestions().size() == 0) {
      add(new DefaultQuestionCategoriesPanel(id, getModel()));
    } else if(question.getQuestionCategories().size() == 0) {
      add(new DefaultQuestionListPanel(id, new QuestionListProvider(question)));
    } else {
      boolean shared = true;
      for(Question child : question.getQuestions()) {
        if(child.getCategories().size() > 0) {
          shared = false;
          break;
        }
      }
      if(shared) {
        add(new DefaultQuestionSharedCategoriesPanel(id, getModel()));
      } else {
        add(new DefaultQuestionJoinedCategoriesPanel(id, getModel()));
      }
    }
  }
}

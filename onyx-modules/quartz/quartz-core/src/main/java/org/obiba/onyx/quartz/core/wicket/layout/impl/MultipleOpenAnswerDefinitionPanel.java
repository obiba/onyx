/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;

/**
 * UI for OpenAnswer having other open answer children.
 */
public class MultipleOpenAnswerDefinitionPanel extends AbstractOpenAnswerDefinitionPanel {

  private static final long serialVersionUID = 1L;

  public MultipleOpenAnswerDefinitionPanel(String id, IModel questionCategoryModel, IModel openAnswerDefinitionModel) {
    this(id, new QuestionnaireModel(((QuestionCategory) questionCategoryModel.getObject()).getQuestion()), questionCategoryModel, openAnswerDefinitionModel);
  }

  public MultipleOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel, IModel openAnswerDefinitionModel) {
    super(id, questionModel, questionCategoryModel, openAnswerDefinitionModel);

  }

  @Override
  public void setRequired(boolean required) {
    // TODO Auto-generated method stub

  }
}

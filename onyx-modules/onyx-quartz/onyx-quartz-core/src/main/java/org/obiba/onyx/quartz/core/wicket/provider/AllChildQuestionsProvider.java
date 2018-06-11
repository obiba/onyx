/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.provider;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;

/**
 * A {@code IDataProvider<Question>} that provides all child questions of a question.
 */
public class AllChildQuestionsProvider extends AbstractChildQuestionProvider {

  private static final long serialVersionUID = -2203581882836613910L;

  /**
   * 
   */
  public AllChildQuestionsProvider(IModel<Question> model) {
    super(model);
  }

  @Override
  protected boolean acceptChild(Question question) {
    return true;
  }
}

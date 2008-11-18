/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.condition;

import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;

public class NotCondition extends Condition {

  /**
   * @param name
   */
  public NotCondition(String name) {
    super(name);
  }

  private static final long serialVersionUID = -7934445960755750180L;

  private Condition condition;

  public Condition getCondition() {
    return condition;
  }

  public void setCondition(Condition condition) {
    this.condition = condition;
  }

  public boolean isToBeAnswered(ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService) {
    return !condition.isToBeAnswered(activeQuestionnaireAdministrationService);
  }

}

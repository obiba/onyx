/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.answer;

import java.io.Serializable;

import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.data.Data;

public abstract class AnswerSource implements Serializable {

  /**
   * Get the data for answer provisionning.
   * @return
   */
  public abstract Data getData(ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService);

}

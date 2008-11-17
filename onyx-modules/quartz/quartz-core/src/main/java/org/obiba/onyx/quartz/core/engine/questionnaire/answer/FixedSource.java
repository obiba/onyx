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

import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.data.Data;

public class FixedSource extends AnswerSource {

  private static final long serialVersionUID = 1L;

  private Data data;

  public FixedSource() {
  }

  public FixedSource(Data data) {
    this.data = data;
  }

  public void setData(Data data) {
    this.data = data;
  }

  public Data getData(ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService) {
    return data;
  }

}

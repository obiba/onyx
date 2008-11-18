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

import java.util.Date;

import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

/**
 * Get the current {@link Date}.
 */
public class TimestampSource extends DataSource {

  private static final long serialVersionUID = 5049448952613044101L;

  public TimestampSource() {
  }

  public Data getData(ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService) {
    return new Data(DataType.DATE, new Date());
  }

}

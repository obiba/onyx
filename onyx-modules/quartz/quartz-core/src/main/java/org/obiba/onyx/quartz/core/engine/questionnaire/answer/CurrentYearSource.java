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

import java.util.Calendar;
import java.util.Date;

import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;

/**
 * Get the current year integer value.
 */
public class CurrentYearSource extends DataSource {

  private static final long serialVersionUID = 5049448952613044101L;

  public CurrentYearSource() {
  }

  public Data getData(ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService) {
    Calendar c = Calendar.getInstance();
    c.setTime(new Date());
    return DataBuilder.buildInteger(c.get(Calendar.YEAR));
  }

  public String getUnit() {
    // TODO Auto-generated method stub
    return null;
  }
}

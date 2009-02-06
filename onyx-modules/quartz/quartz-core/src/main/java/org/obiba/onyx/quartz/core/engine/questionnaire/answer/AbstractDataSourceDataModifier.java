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

/**
 * abstract class used to modify the data (type and/or value)
 */
public abstract class AbstractDataSourceDataModifier extends AbstractDataSourceWrapper {

  protected DataSource innerSource;

  @Override
  public Data getData(ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService) {
    Data data = innerSource.getData(activeQuestionnaireAdministrationService);
    return modify(data);
  }

  public DataSource getInnerSource() {
    return innerSource;
  }

  public void setInnerSource(DataSource innerSource) {
    this.innerSource = innerSource;
  }

  protected abstract Data modify(Data data);

}

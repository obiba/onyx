/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.workstation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

public class ExperimentalConditionBuilder {

  private String name;

  private Date time;

  private User user;

  private String workstation;

  private Boolean exported = false;

  private List<ExperimentalConditionValue> experimentalConditionValues = new ArrayList<ExperimentalConditionValue>();

  public static ExperimentalConditionBuilder anExperimentalCondition() {
    return new ExperimentalConditionBuilder();
  }

  public ExperimentalConditionBuilder name(String name) {
    this.name = name;
    return this;
  }

  public ExperimentalConditionBuilder time(Date time) {
    this.time = time;
    return this;
  }

  public ExperimentalConditionBuilder user(String firstName, String lastName) {
    User user = new User();
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setLogin(firstName);
    this.user = user;
    return this;
  }

  public ExperimentalConditionBuilder workstation(String workstation) {
    this.workstation = workstation;
    return this;
  }

  public ExperimentalConditionBuilder exported() {
    this.exported = true;
    return this;
  }

  public ExperimentalConditionBuilder value(String attributeName, Serializable value, DataType dataType) {
    ExperimentalConditionValue ecv = new ExperimentalConditionValue();
    ecv.setAttributeName(attributeName);
    ecv.setAttributeType(dataType);
    ecv.setData(new Data(dataType, value));
    experimentalConditionValues.add(ecv);
    return this;
  }

  public ExperimentalCondition build() {
    ExperimentalCondition ec = new ExperimentalCondition();
    ec.setName(name);
    ec.setTime(time);
    ec.setUserName(user.getLogin());
    ec.setWorkstation(workstation);
    ec.setExported(exported);
    ec.setExperimentalConditionValues(experimentalConditionValues);
    return ec;
  }

}

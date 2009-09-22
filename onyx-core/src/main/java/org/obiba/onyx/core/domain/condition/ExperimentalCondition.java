/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.condition;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.user.User;

@Entity
public class ExperimentalCondition extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @Column(length = 2000)
  private String name;

  @Temporal(TemporalType.TIMESTAMP)
  private Date time;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(length = 2000)
  private String workstation;

  private Boolean exported;

  @OneToMany(mappedBy = "experimentalCondition", cascade = CascadeType.ALL)
  private List<ExperimentalConditionValue> experimentalConditionValues;

  public List<ExperimentalConditionValue> getExperimentalConditionValues() {
    return experimentalConditionValues != null ? experimentalConditionValues : new ArrayList<ExperimentalConditionValue>();
  }

  public void setExperimentalConditionValues(List<ExperimentalConditionValue> experimentalConditionValues) {
    this.experimentalConditionValues = experimentalConditionValues;
  }

  public void addExperimentalConditionValue(ExperimentalConditionValue experimentalConditionValue) {
    if(experimentalConditionValues == null) experimentalConditionValues = new ArrayList<ExperimentalConditionValue>();
    experimentalConditionValues.add(experimentalConditionValue);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getWorkstation() {
    return workstation;
  }

  public void setWorkstation(String workstation) {
    this.workstation = workstation;
  }

  public Boolean getExported() {
    return exported;
  }

  public void setExported(Boolean exported) {
    this.exported = exported;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[ExperimentalCondition ").append("[name=").append(getName()).append("] ");
    sb.append("[workstation= ").append(getWorkstation()).append("] [ExperimentalConditionValues=\n");
    for(ExperimentalConditionValue ecv : getExperimentalConditionValues()) {
      sb.append("\t").append(ecv);
    }
    sb.append("\n]");
    return sb.toString();
  }

}

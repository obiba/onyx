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

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.obiba.onyx.core.domain.AttributeValue;

/**
 * Experimental Condition attribute value represents individual values for experimental condition logs (such as
 * temperature) as will as Instrument Calibration logs.
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "experimental_condition_id", "attributeName" }) })
public class ExperimentalConditionValue extends AttributeValue {

  private static final long serialVersionUID = 1L;

  @ManyToOne(optional = false)
  @JoinColumn(name = "experimental_condition_id")
  private ExperimentalCondition experimentalCondition;

  public ExperimentalCondition getExperimentalCondition() {
    return experimentalCondition;
  }

  public void setExperimentalCondition(ExperimentalCondition experimentalCondition) {
    this.experimentalCondition = experimentalCondition;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[ExperimentalConditionValue ").append("[name=").append(getAttributeName()).append("] ");
    sb.append("[value=").append(getData()).append("]]");
    return sb.toString();
  }
}

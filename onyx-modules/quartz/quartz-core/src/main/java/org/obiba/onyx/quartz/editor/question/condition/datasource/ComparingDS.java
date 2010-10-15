/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question.condition.datasource;

import java.io.Serializable;

import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.util.data.ComparisonOperator;

/**
 *
 */
public class ComparingDS implements Serializable, DS {

  private static final long serialVersionUID = 1L;

  public static final String GENDER_TYPE = "gender";

  private int variable;

  private ComparisonOperator operator;

  private String type;

  private String value;

  private Gender gender;

  public ComparisonOperator getOperator() {
    return operator;
  }

  public void setOperator(ComparisonOperator operator) {
    this.operator = operator;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  @Override
  public int getVariable() {
    return variable;
  }

  public void setVariable(int index) {
    this.variable = index;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}

/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question.condition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Conditions implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<ConditionDataSource> dataSources = new ArrayList<ConditionDataSource>();

  private String expression;

  public List<ConditionDataSource> getDataSources() {
    return dataSources;
  }

  public void setDataSources(List<ConditionDataSource> dataSources) {
    this.dataSources = dataSources;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

}

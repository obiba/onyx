/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.export;

import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.filter.CollectionFilterChain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("valueset")
class ValueSetFilter {

  @XStreamAlias("entityType")
  @XStreamAsAttribute
  private final String entityTypeName;

  @XStreamAlias("valueTable")
  @XStreamAsAttribute
  private String valueTableName;

  @XStreamAlias("entities")
  private CollectionFilterChain<ValueSet> entityFilterChain;

  @XStreamAlias("variables")
  private CollectionFilterChain<Variable> variableFilterChain;

  ValueSetFilter(String entityTypeName) {
    this.entityTypeName = entityTypeName;
  }

  ValueSetFilter(String entityTypeName, String valueTableName) {
    this.entityTypeName = entityTypeName;
    this.valueTableName = valueTableName;
  }

  CollectionFilterChain<ValueSet> getEntityFilterChain() {
    if(entityFilterChain == null) entityFilterChain = new CollectionFilterChain<ValueSet>(entityTypeName);
    if(entityFilterChain.getEntityType() == null) entityFilterChain.setEntityType(entityTypeName);
    return entityFilterChain;
  }

  void setEntityFilterChain(CollectionFilterChain<ValueSet> entityFilterChain) {
    this.entityFilterChain = entityFilterChain;
  }

  CollectionFilterChain<Variable> getVariableFilterChain() {
    if(variableFilterChain == null) variableFilterChain = new CollectionFilterChain<Variable>(entityTypeName);
    if(variableFilterChain.getEntityType() == null) variableFilterChain.setEntityType(entityTypeName);
    return variableFilterChain;
  }

  void setVariableFilterChain(CollectionFilterChain<Variable> variableFilterChain) {
    this.variableFilterChain = variableFilterChain;
  }

  public String getEntityTypeName() {
    return entityTypeName;
  }

  public String getValueTableName() {
    return valueTableName;
  }

}

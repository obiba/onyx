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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.engine.output.Strategies;
import org.obiba.magma.filter.CollectionFilterChain;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class OnyxDataExportDestination {

  private String name;

  private Strategies strategies;

  @XStreamImplicit
  private List<ValueSetFilter> valueSetFilters;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Strategies getStrategies() {
    return strategies;
  }

  void setStrategies(Strategies strategies) {
    this.strategies = strategies;
  }

  public List<ValueSetFilter> getValueSetFilters() {
    return valueSetFilters;
  }

  public void setValueSetFilters(List<ValueSetFilter> valueSetFilters) {
    this.valueSetFilters = valueSetFilters;
  }

  /**
   * Returns a map of entity type names to Variable FilterChains for this destination. Each FilterChain contains a
   * number filters associated with one particular entity type (e.g. Participant).
   */
  Map<String, CollectionFilterChain<VariableValueSource>> getVariableFilterChainMap() {
    Map<String, CollectionFilterChain<VariableValueSource>> result = new HashMap<String, CollectionFilterChain<VariableValueSource>>();
    for(ValueSetFilter valueSetFilter : valueSetFilters) {
      result.put(valueSetFilter.getEntityTypeName(), valueSetFilter.getVariableFilterChain());
    }
    return result;
  }

  /**
   * Returns a map of entity type names to Entity FilterChains for this destination. Each FilterChain contains a number
   * filters associated with one particular entity type (e.g. Participant).
   */
  Map<String, CollectionFilterChain<ValueSet>> getEntityFilterChainMap() {
    Map<String, CollectionFilterChain<ValueSet>> result = new HashMap<String, CollectionFilterChain<ValueSet>>();
    for(ValueSetFilter valueSetFilter : valueSetFilters) {
      result.put(valueSetFilter.getEntityTypeName(), valueSetFilter.getEntityFilterChain());
    }
    return result;
  }

}

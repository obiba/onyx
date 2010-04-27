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

import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.crypt.KeyProvider;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.crypt.GeneratedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.magma.filter.CompositeFilterChain;
import org.obiba.magma.filter.FilterChain;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class OnyxDataExportDestination {

  private String name;

  private EncryptionOptions encrypt;

  @XStreamImplicit
  private List<ValueSetFilter> valueSetFilters;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DatasourceEncryptionStrategy getEncryptionStrategy(KeyProvider provider) {
    if(encrypt != null) {
      GeneratedSecretKeyDatasourceEncryptionStrategy strategy = new GeneratedSecretKeyDatasourceEncryptionStrategy();
      strategy.setKeyProvider(provider);
      encrypt.configureStrategy(strategy);
      return strategy;
    }
    return null;
  }

  public boolean isEncryptionRequested() {
    return encrypt != null;
  }

  public List<ValueSetFilter> getValueSetFilters() {
    return valueSetFilters;
  }

  public List<ValueSetFilter> getValueSetFilters(final ValueTable valueTable) {
    List<ValueSetFilter> valueSetFiltersForTable = new ArrayList<ValueSetFilter>();
    for(ValueSetFilter filter : getValueSetFilters()) {
      if(filter.getEntityTypeName().equals(valueTable.getEntityType()) && (filter.getValueTableName() == null || filter.getValueTableName().equals(valueTable.getName()))) {
        valueSetFiltersForTable.add(filter);
      }
    }
    return valueSetFiltersForTable;
  }

  public void setValueSetFilters(List<ValueSetFilter> valueSetFilters) {
    this.valueSetFilters = valueSetFilters;
  }

  public boolean wantsEntityType(String entityType) {
    for(ValueSetFilter valueSetFilter : getValueSetFilters()) {
      if(valueSetFilter.getEntityTypeName().equalsIgnoreCase(entityType)) {
        return true;
      }
    }
    return false;
  }

  public boolean wantsTable(ValueTable valueTable) {
    for(ValueSetFilter valueSetFilter : getValueSetFilters()) {
      if(valueSetFilter.getValueTableName() != null && valueSetFilter.getValueTableName().equalsIgnoreCase(valueTable.getName())) {
        return true;
      }
    }
    return false;
  }

  FilterChain<Variable> getVariableFilterChainForTable(ValueTable valueTable) {
    CompositeFilterChain<Variable> compositeFilterChain = new CompositeFilterChain<Variable>(valueTable.getEntityType());
    for(ValueSetFilter valueSetFilter : getValueSetFilters(valueTable)) {
      compositeFilterChain.addFilterChain(valueSetFilter.getVariableFilterChain());
    }
    return compositeFilterChain;
  }

  FilterChain<ValueSet> getEntityFilterChainForTable(ValueTable valueTable) {
    CompositeFilterChain<ValueSet> compositeFilterChain = new CompositeFilterChain<ValueSet>(valueTable.getEntityType());
    for(ValueSetFilter valueSetFilter : getValueSetFilters(valueTable)) {
      compositeFilterChain.addFilterChain(valueSetFilter.getEntityFilterChain());
    }
    return compositeFilterChain;
  }

  public static class EncryptionOptions {
    private String algorithm;

    private String mode;

    private String padding;

    private Integer keySize;

    private void configureStrategy(GeneratedSecretKeyDatasourceEncryptionStrategy strategy) {
      if(algorithm != null) {
        strategy.setAlgorithm(algorithm);
      }
      if(mode != null) {
        strategy.setMode(mode);
      }
      if(padding != null) {
        strategy.setPadding(padding);
      }
      if(keySize != null) {
        strategy.setKeySize(keySize);
      }
    }
  }

}

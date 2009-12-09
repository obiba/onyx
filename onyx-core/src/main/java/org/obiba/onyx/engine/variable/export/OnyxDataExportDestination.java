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

import java.util.List;

import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.crypt.PublicKeyProvider;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.crypt.GeneratedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.magma.filter.CollectionFilterChain;

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

  public DatasourceEncryptionStrategy getEncryptionStrategy(PublicKeyProvider provider) {
    if(encrypt != null) {
      GeneratedSecretKeyDatasourceEncryptionStrategy strategy = new GeneratedSecretKeyDatasourceEncryptionStrategy(provider);
      encrypt.configureStrategy(strategy);
      return strategy;
    }
    return null;
  }

  public List<ValueSetFilter> getValueSetFilters() {
    return valueSetFilters;
  }

  public void setValueSetFilters(List<ValueSetFilter> valueSetFilters) {
    this.valueSetFilters = valueSetFilters;
  }

  CollectionFilterChain<Variable> getVariableFilterChainForEntityName(String entityName) {
    for(ValueSetFilter valueSetFilter : getValueSetFilters()) {
      if(valueSetFilter.getEntityTypeName().equalsIgnoreCase(entityName)) {
        return valueSetFilter.getVariableFilterChain();
      }
    }
    return new CollectionFilterChain<Variable>(entityName);
  }

  CollectionFilterChain<ValueSet> getEntityFilterChainForEntityName(String entityName) {
    for(ValueSetFilter valueSetFilter : getValueSetFilters()) {
      if(valueSetFilter.getEntityTypeName().equalsIgnoreCase(entityName)) {
        return valueSetFilter.getEntityFilterChain();
      }
    }
    return new CollectionFilterChain<ValueSet>(entityName);
  }

  public class EncryptionOptions {
    private String algorithm;

    private String mode;

    private String padding;

    private Integer keySize;

    private void configureStrategy(GeneratedSecretKeyDatasourceEncryptionStrategy strategy) {
      if(algorithm != null) {
        strategy.setAlgorithm(encrypt.algorithm);
      }
      if(mode != null) {
        strategy.setMode(encrypt.mode);
      }
      if(padding != null) {
        strategy.setPadding(encrypt.padding);
      }
      if(keySize != null) {
        strategy.setKeySize(encrypt.keySize);
      }
    }
  }

}

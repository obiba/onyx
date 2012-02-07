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

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.crypt.GeneratedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.magma.filter.CompositeFilterChain;
import org.obiba.magma.filter.FilterChain;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class OnyxDataExportDestination {

  private String name;

  private Options options;

  private EncryptionOptions encrypt;

  @XStreamImplicit
  private List<ValueSetFilter> valueSetFilters;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Options getOptions() {
    return options;
  }

  public EncryptionOptions getEncryptOptions() {
    return this.encrypt != null ? this.encrypt : options != null ? options.getEncrypt() : null;
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

  public File createOutputFile(File outputRootDirectory) {
    String filename = getName() + "-" + getCurrentDateTimeString();
    if(options == null || options.getFormat() == null || options.getFormat() == Format.XML) {
      filename = filename + ".zip";
    }
    return new File(outputRootDirectory, filename);
  }

  private String getCurrentDateTimeString() {
    DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    return df.format(new Date());
  }

  public enum Format {
    CSV, XML, OPAL
  }

  public static class Options {

    private Format format;

    private String opalUri;

    private String opalDatasource;

    private String username;

    private String password;

    private String characterSet;

    private String separator;

    private String quote;

    private EncryptionOptions encrypt;

    public Format getFormat() {
      return format;
    }

    public void setFormat(Format format) {
      this.format = format;
    }

    public String getOpalUri() {
      return opalUri;
    }

    public void setOpalUri(String opalUri) {
      this.opalUri = opalUri;
    }

    public String getOpalDatasource() {
      return opalDatasource;
    }

    public void setOpalDatasource(String opalDatasource) {
      this.opalDatasource = opalDatasource;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getCharacterSet() {
      return characterSet;
    }

    public void setCharacterSet(String characterSet) {
      this.characterSet = characterSet;
    }

    public String getSeparator() {
      return separator;
    }

    public void setSeparator(String separator) {
      this.separator = separator;
    }

    public String getQuote() {
      return quote;
    }

    public void setQuote(String quote) {
      this.quote = quote;
    }

    public EncryptionOptions getEncrypt() {
      return encrypt;
    }

    public void setEncrypt(EncryptionOptions encrypt) {
      this.encrypt = encrypt;
    }

  }

  public static class EncryptionOptions {
    private String algorithm;

    private String mode;

    private String padding;

    private Integer keySize;

    public void configureStrategy(GeneratedSecretKeyDatasourceEncryptionStrategy strategy) {
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

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
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.crypt.KeyProvider;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.crypt.GeneratedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.magma.datasource.csv.CsvDatasource;
import org.obiba.magma.datasource.csv.support.CsvDatasourceFactory;
import org.obiba.magma.datasource.csv.support.CsvUtil;
import org.obiba.magma.datasource.fs.support.FsDatasourceFactory;
import org.obiba.magma.filter.CompositeFilterChain;
import org.obiba.magma.filter.FilterChain;

import au.com.bytecode.opencsv.CSVWriter;

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

  public DatasourceFactory getDatasourceFactory(File outputDir, KeyProvider provider, Iterable<ValueTable> tables) {
    if(options == null || options.getFormat() == null) {
      return xmlDatasourceFactory(outputDir, provider);
    }

    switch(options.getFormat()) {
    case CSV:
      return csvDatasourceFactory(outputDir, tables);
    case XML:
      return xmlDatasourceFactory(outputDir, provider);
    }
    throw new IllegalStateException("Unknown output format " + options.getFormat());
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

  private DatasourceFactory csvDatasourceFactory(File outputFile, Iterable<ValueTable> tables) {
    CsvDatasourceFactory factory = new CsvDatasourceFactory();
    factory.setName(getName());

    outputFile.mkdir();
    factory.setBundle(outputFile);

    if(options.getSeparator() != null) factory.setSeparator(options.getSeparator());
    if(options.getQuote() != null) factory.setQuote(options.getQuote());
    if(options.getCharacterSet() != null) factory.setCharacterSet(options.getCharacterSet());

    // TODO: this is very painful! The CsvDatasource needs serious work.
    // We have to build the datasource structure ourselves, beforehand (CsvDatasource won't do it for us)
    // This is the only combination of disk structure and "addTable" call that I was able to get working.
    for(ValueTable table : tables) {
      File tableDir = new File(outputFile, table.getName());
      tableDir.mkdir();

      File variablesFile = new File(tableDir, CsvDatasource.VARIABLES_FILE);
      File dataFile = new File(tableDir, CsvDatasource.DATA_FILE);

      CSVWriter writer = null;
      try {
        writer = new CSVWriter(new FileWriter(variablesFile));
        writer.writeAll(Collections.singletonList(CsvUtil.getCsvVariableHeader(table)));

        dataFile.createNewFile();
      } catch(IOException e) {
        throw new RuntimeException(e);
      } finally {
        if(writer != null) {
          try {
            writer.close();
          } catch(IOException e) {
            // ignore
          }
        }
      }
      factory.addTable(table.getName(), variablesFile, dataFile);
    }

    return factory;
  }

  private DatasourceFactory xmlDatasourceFactory(File outputFile, KeyProvider provider) {
    FsDatasourceFactory factory = new FsDatasourceFactory();
    factory.setName(getName());
    factory.setFile(outputFile);
    factory.setEncryptionStrategy(getEncryptionStrategy(provider));
    return factory;
  }

  private DatasourceEncryptionStrategy getEncryptionStrategy(KeyProvider provider) {
    EncryptionOptions encrypt = this.encrypt != null ? this.encrypt : options != null ? options.getEncrypt() : null;
    if(encrypt != null) {
      GeneratedSecretKeyDatasourceEncryptionStrategy strategy = new GeneratedSecretKeyDatasourceEncryptionStrategy();
      strategy.setKeyProvider(provider);
      encrypt.configureStrategy(strategy);
      return strategy;
    }
    return null;
  }

  private String getCurrentDateTimeString() {
    DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    return df.format(new Date());
  }

  public enum Format {
    CSV, XML
  }

  public static class Options {

    private Format format;

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

/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.export.format;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.ValueTable;
import org.obiba.magma.crypt.KeyProvider;
import org.obiba.magma.datasource.csv.CsvDatasource;
import org.obiba.magma.datasource.csv.support.CsvDatasourceFactory;
import org.obiba.magma.datasource.csv.support.CsvUtil;
import org.obiba.onyx.engine.variable.export.OnyxDataExportDestination;
import org.obiba.onyx.engine.variable.export.OnyxDataExportDestination.Options;

import au.com.bytecode.opencsv.CSVWriter;

/**
 *
 */
public class CsvDatasourceFactoryProvider implements DatasourceFactoryProvider {

  @Override
  public String getFormat() {
    return "CSV";
  }

  @Override
  public DatasourceFactory getDatasourceFactory(OnyxDataExportDestination destination, File outputDir, KeyProvider provider, Iterable<ValueTable> tables) {
    CsvDatasourceFactory factory = new CsvDatasourceFactory();
    factory.setName(destination.getName());

    outputDir.mkdir();
    factory.setBundle(outputDir);

    Options options = destination.getOptions();

    if(options.getSeparator() != null) factory.setSeparator(options.getSeparator());
    if(options.getQuote() != null) factory.setQuote(options.getQuote());
    if(options.getCharacterSet() != null) factory.setCharacterSet(options.getCharacterSet());

    // TODO: this is very painful! The CsvDatasource needs serious work.
    // We have to build the datasource structure ourselves, beforehand (CsvDatasource won't do it for us)
    // This is the only combination of disk structure and "addTable" call that I was able to get working.
    for(ValueTable table : tables) {
      File tableDir = new File(outputDir, table.getName());
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

}

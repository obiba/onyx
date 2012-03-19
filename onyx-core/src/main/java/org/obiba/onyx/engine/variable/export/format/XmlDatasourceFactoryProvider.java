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

import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.ValueTable;
import org.obiba.magma.crypt.KeyProvider;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.crypt.GeneratedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.magma.datasource.fs.support.FsDatasourceFactory;
import org.obiba.onyx.engine.variable.export.OnyxDataExportDestination;
import org.obiba.onyx.engine.variable.export.OnyxDataExportDestination.EncryptionOptions;

public class XmlDatasourceFactoryProvider implements DatasourceFactoryProvider {

  public static final String FORMAT = "XML";

  @Override
  public String getFormat() {
    return FORMAT;
  }

  @Override
  public DatasourceFactory getDatasourceFactory(OnyxDataExportDestination destination, File outputDir, KeyProvider provider, Iterable<ValueTable> tables) {

    FsDatasourceFactory factory = new FsDatasourceFactory();
    factory.setName(destination.getName());
    factory.setFile(outputDir);
    factory.setEncryptionStrategy(getEncryptionStrategy(destination, provider));
    return factory;
  }

  private DatasourceEncryptionStrategy
      getEncryptionStrategy(OnyxDataExportDestination destination, KeyProvider provider) {
    EncryptionOptions encrypt = destination.getEncryptOptions();
    if(encrypt != null) {
      GeneratedSecretKeyDatasourceEncryptionStrategy strategy = new GeneratedSecretKeyDatasourceEncryptionStrategy();
      strategy.setKeyProvider(provider);
      encrypt.configureStrategy(strategy);
      return strategy;
    }
    return null;
  }

}

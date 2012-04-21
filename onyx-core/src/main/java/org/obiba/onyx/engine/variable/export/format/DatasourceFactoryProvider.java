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
import org.obiba.onyx.engine.variable.export.OnyxDataExportDestination;

public interface DatasourceFactoryProvider {

  public String getFormat();

  public DatasourceFactory getDatasourceFactory(OnyxDataExportDestination destination, File outputDir, KeyProvider provider, Iterable<ValueTable> tables);

}

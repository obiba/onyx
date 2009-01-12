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

import java.io.OutputStream;

/**
 * Definition of the contract to export data out from Onyx.
 */
public interface IOnyxDataExportStrategy {

  public void prepare(OnyxDataExportContext context);

  public OutputStream newEntry(String name);

  public void terminate(OnyxDataExportContext context);

}

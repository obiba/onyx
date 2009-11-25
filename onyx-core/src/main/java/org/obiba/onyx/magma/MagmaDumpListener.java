/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.magma;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.core.util.StreamUtil;
import org.obiba.magma.Collection;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.xstream.Io;
import org.obiba.wicket.application.WebApplicationStartupListener;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Transactional
public class MagmaDumpListener implements WebApplicationStartupListener {

  public void startup(WebApplication webapp) {
    MagmaEngine engine = MagmaEngine.get();
    FileOutputStream os = null;
    Io io = new Io();
    try {
      Collection collection = engine.lookupCollection("onyx-baseline");
      File temp = new File(System.getProperty("java.io.tmpdir"), "magma-variables.xml");
      os = new FileOutputStream(temp);
      io.writeVariables(collection, os);
      os.close();
    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      StreamUtil.silentSafeClose(os);
    }
  }

  public void shutdown(WebApplication webapp) {
    MagmaEngine engine = MagmaEngine.get();
    FileOutputStream os = null;
    Io io = new Io();
    try {
      Collection collection = engine.lookupCollection("onyx-baseline");
      File temp = new File(System.getProperty("java.io.tmpdir"), "magma-valuesets.xml");
      os = new FileOutputStream(temp);
      io.writeEntities(collection, os);

    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      StreamUtil.silentSafeClose(os);
    }
  }
}

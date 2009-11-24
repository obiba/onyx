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
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.xstream.Io;
import org.obiba.wicket.application.WebApplicationStartupListener;

/**
 *
 */
public class MagmaDumpListener implements WebApplicationStartupListener {

  public void startup(WebApplication webapp) {
    MagmaEngine engine = MagmaEngine.get();
    Io io = new Io();
    try {
      File temp = new File(System.getProperty("java.io.tmpdir"), "magma-dump.xml");
      FileOutputStream os = new FileOutputStream(temp);
      io.writeVariables(engine.lookupCollection("onyx-baseline"), os);
      os.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void shutdown(WebApplication webapp) {
  }
}

/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.exception;

import org.mozilla.javascript.EcmaError;
import org.obiba.magma.MagmaRuntimeException;

/**
 *
 */
public class ExceptionUtils {

  public static String getCauseMessage(Exception e) {
    Throwable t = e;
    String message = e.getMessage();

    while(true) {
      if(t instanceof MagmaRuntimeException || t instanceof EcmaError) {
        message = t.getMessage();
        break;
      }

      if(t.getCause() != null) {
        t = t.getCause();
        message = t.getMessage();
      } else {
        break;
      }
    }

    return cleanMessage(message);
  }

  public static String cleanMessage(String message) {
    String msg = message;
    int idx = msg.lastIndexOf("Exception: ");
    if(idx > -1) {
      msg = msg.substring(idx + 11);
    }

    return msg;
  }

}

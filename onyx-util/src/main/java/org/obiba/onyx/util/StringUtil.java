/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.util;

public class StringUtil {

  /**
   * Tokenizes a string using the <code>String<code> class's <code>split</code> method
   * and returns the indicated token.
   * 
   * @param s string
   * @param delimiter delimiter (regex)
   * @param index token index
   * @return token at the specified index (<code>null</code> if no token exists at that index)
   */
  public static String splitAndReturnTokenAt(String s, String delimiter, int index) {
    String[] tokens = s.split(delimiter);
    if(index < tokens.length) {
      return tokens[index];
    }
    return null;
  }
}

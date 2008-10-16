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

import java.text.MessageFormat;
import java.util.Locale;

public class StringReferenceCompatibleMessageFormat extends MessageFormat {

  //
  // Constants
  //
  private static final long serialVersionUID = 1L;

  public static final String STRING_REFERENCE_PREFIX = "${";

  public static final String STRING_REFERENCE_SUFFIX = "}";

  //
  // Constructors
  //

  public StringReferenceCompatibleMessageFormat(String pattern) {
    super(adjustPattern(pattern));
  }

  public StringReferenceCompatibleMessageFormat(String pattern, Locale locale) {
    super(adjustPattern(pattern), locale);
  }

  //
  // Methods
  //

  /**
   * This methods checks whether the specified pattern is a string reference. If so, it adds single quotes around the
   * pattern.
   * 
   * @param pattern
   * @return quoted pattern if it is a string reference, otherwise original pattern
   */
  private static String adjustPattern(String pattern) {
    // Quote patterns that are string references (i.e., beginning with "${" and ending with "}")
    // to prevent the contents from being parsed as a number. Note that, as usual, the quotes will
    // not be included in the formatted string.
    if(pattern != null && pattern.startsWith(STRING_REFERENCE_PREFIX) && pattern.endsWith(STRING_REFERENCE_SUFFIX)) {
      pattern = "'" + pattern + "'";
    }

    return pattern;
  }
}

/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.print;

import java.util.Locale;
import java.util.Set;

import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSourceResolvable;

/**
 * Main interface for Printable Reports (see http://wiki.obiba.org/confluence/display/ONYX/Printable+Reports).
 */
public interface IPrintableReport extends ApplicationContextAware {

  /**
   * Gets the name of the printable report.
   * 
   * @return The report's name.
   */
  public String getName();

  /**
   * Gets the report's localized description displayed on the UI.
   * 
   * @return The report's description.
   */
  public MessageSourceResolvable getLabel();

  /**
   * Determines if the report is ready or not to be printed.
   * 
   * @return True if the report is ready, false otherwise.
   */
  public boolean isReady();

  /**
   * Prints the report for a specific Locale.
   * 
   * @param locale The Locale for which the report will be printed.
   */
  public void print(Locale locale);

  /**
   * Determines if the report is localizable of not.
   * 
   * @return
   */
  public boolean isLocalisable();

  /**
   * Determines the available Locales for this report.
   * 
   * @return A list of Locale.
   */
  public Set<Locale> availableLocales();

}

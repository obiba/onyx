/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.mica.core.service;

import java.io.InputStream;
import java.util.Locale;

public interface ModuleReportContributor {

  /**
   * Returns the module's name that contributes to the conclusion report
   * @return module's name
   */
  public String getName();

  /**
   * Returns true if the given module contributes to the conclusion report
   * @return true or false
   */
  public boolean isExists();

  /**
   * Returns the constructed report as an InputStream
   * @param locale: report's locale
   * @return InputStream
   */
  public InputStream getReport(Locale locale);

}
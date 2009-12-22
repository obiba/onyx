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

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import org.obiba.onyx.core.io.support.LocalizedResourceLoader;
import org.obiba.onyx.core.service.ActiveInterviewService;

public interface PdfTemplateEngine {
  /**
   * Generates a report based on a PDF form template. The values for each form fields is retrieved from the Variable
   * Directory following the field-to-variable mapping.
   * 
   * @param locale The Locale of the template
   * @param fieldToVariableMap The field-to-variable mapping.
   * @param reportTemplateLoader A {@link LocalizedResourceLoader} for the template.
   * @param activeInterviewService The active interview information service.
   * 
   * @return An InputStream on the generated report.
   */
  public InputStream applyTemplate(Locale locale, Map<String, String> fieldToVariableMap, LocalizedResourceLoader reportTemplateLoader, ActiveInterviewService activeInterviewService);

  public void setDateFormat(String dateFormat);

}

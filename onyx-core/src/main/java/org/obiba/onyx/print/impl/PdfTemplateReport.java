/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.print.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.obiba.onyx.core.io.support.LocalizedResourceLoader;
import org.obiba.onyx.print.PdfTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PdfReport implementation for reports that are constructed at the time of printing by filling in the template with
 * data from one or more variables.
 */
public class PdfTemplateReport extends PdfReport {

  private static final Logger log = LoggerFactory.getLogger(PdfTemplateReport.class);

  private LocalizedResourceLoader reportTemplateLoader;

  private PdfTemplateEngine pdfTemplateEngine;

  private Map<String, String> fieldToVariableMap;

  @Override
  protected InputStream getReport(Locale locale) {
    return pdfTemplateEngine.applyTemplate(locale, fieldToVariableMap, reportTemplateLoader, activeInterviewService);
  }

  public void setFieldToVariableMap(String keyValuePairs) {
    fieldToVariableMap = new HashMap<String, String>();
    // Get list of strings separated by the delimiter
    StringTokenizer tokenizer = new StringTokenizer(keyValuePairs, ",");
    while(tokenizer.hasMoreElements()) {
      String token = tokenizer.nextToken();
      String[] entry = token.split("=");
      if(entry.length == 2) {
        fieldToVariableMap.put(entry[0].trim(), entry[1].trim());
      } else {
        log.error("Could not identify PDF field name to variable path mapping: " + token);
      }
    }
  }

  public void setReportTemplateLoader(LocalizedResourceLoader reportTemplateLoader) {
    this.reportTemplateLoader = reportTemplateLoader;
  }

  public Set<Locale> availableLocales() {
    List<Locale> availableLocalesList = reportTemplateLoader.getAvailableLocales();
    Set<Locale> availableLocales = new HashSet<Locale>();
    availableLocales.addAll(availableLocalesList);
    return availableLocales;
  }

  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    pdfTemplateEngine = (PdfTemplateEngine) applicationContext.getBean("pdfTemplateEngine");
  }

}

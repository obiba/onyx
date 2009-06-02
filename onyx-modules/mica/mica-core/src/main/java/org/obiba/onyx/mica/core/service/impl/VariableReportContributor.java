/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.mica.core.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.io.support.LocalizedResourceLoader;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableDirectory;
import org.obiba.onyx.mica.core.service.ModuleReportContributor;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

/**
 * 
 */
public class VariableReportContributor implements ModuleReportContributor {

  private static final Logger log = LoggerFactory.getLogger(VariableReportContributor.class);

  private ActiveInterviewService activeInterviewService;

  private LocalizedResourceLoader reportTemplateLoader;

  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  private VariableDirectory variableDirectory;

  private Map<String, String> fieldToVariableMap;

  public InputStream getReport(Locale locale) {
    // Get report template
    Resource resource = reportTemplateLoader.getLocalizedResource(locale);

    // Read report template
    PdfReader pdfReader;
    try {
      pdfReader = new PdfReader(resource.getInputStream());
    } catch(Exception ex) {
      throw new RuntimeException("Report to participant template cannot be read", ex);
    }

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    PdfStamper stamper = null;

    // Fill report
    try {
      stamper = new PdfStamper(pdfReader, output);
      AcroFields form = stamper.getAcroFields();

      Participant participant = activeInterviewService.getParticipant();

      setVariableDataFields(participant, form);
      setParticipantDataFields(participant, form);

      stamper.setFormFlattening(true);

      stamper.close();
      output.close();
      pdfReader.close();

      return new ByteArrayInputStream(output.toByteArray());

    } catch(Exception ex) {
      throw new RuntimeException("An error occured while preparing the report to participant", ex);
    }
  }

  private void setParticipantDataFields(Participant participant, AcroFields form) {
    // Fill with participant and current information
    try {
      form.setField("Participant\\.fullName", participant.getFullName());
      form.setField("Participant\\.age", participant.getAge().toString());
      form.setField("Participant\\.enrollmentId", participant.getEnrollmentId());
      form.setField("Participant\\.barcode", participant.getBarcode());
      form.setField("DateInterview\\.date", dateFormat.format(new Date()));
    } catch(Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private void setVariableDataFields(Participant participant, AcroFields form) {

    HashMap<String, String> fieldList = form.getFields();

    try {
      // Iterate on each field of pdf template
      for(Entry<String, String> field : fieldList.entrySet()) {
        String[] keys = splitData(field.getKey());

        // TEMPORARY FIX so that "NA" is displayed in a field if the data is not available.
        // This should be replaced by a default value for the field in the PDF template,
        // however this didn't seem to work when I tested it (default values were not getting printed)
        // We need to find a way to fix that (might be a bug in Acrobat forms).
        form.setField(field.getKey(), "N/A");

        // Iterate on each key for one field of pdf template (for example when a variable depends on several
        // instruments)
        for(String variableKey : keys) {
          String variablePath = fieldToVariableMap.get(variableKey);

          if(variablePath != null) {
            Variable variable = variableDirectory.getVariable(variablePath);

            if(variable != null) {
              VariableData variableData = variableDirectory.getVariableData(participant, variablePath);

              if(variableData != null && variableData.getDatas().size() > 0) {
                String fieldValue = null;

                // find data to put in field
                for(Data data : variableData.getDatas()) {
                  if(fieldValue != null) fieldValue += " ";

                  if(data.getType().equals(DataType.DATE)) {
                    fieldValue = dateFormat.format(data.getValue());
                  } else {
                    fieldValue = data.getValueAsString();
                    if(variable.getUnit() != null) fieldValue += " " + variable.getUnit();
                  }

                }
                form.setField(field.getKey(), fieldValue);
                break;
              }
            }
          }
        }
      }
    } catch(Exception ex) {
      throw new RuntimeException(ex);
    }
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

  private String[] splitData(String string) {

    // By default, a field name in a PDF form starts with "form[0].page[0].", concatenated with the custom name that we
    // gave to this field
    String pattern = "([a-zA-Z0-9]+\\[+[0-9]+\\]+\\.){2}";

    // Delete "form[0].page[0]." string to keep only the custom name given to the variable
    String variable = string.replaceFirst(pattern, "");
    variable = variable.replaceAll("\\[[0-9]\\]", "");

    // If multiple instrument types are used for the same field, they are separated by "-"
    String[] list = variable.split("-");
    return list;
  }

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public void setReportTemplateLoader(LocalizedResourceLoader reportTemplateLoader) {
    this.reportTemplateLoader = reportTemplateLoader;
  }

  public void setDateFormat(String format) {
    this.dateFormat = new SimpleDateFormat(format);
  }

  public void setVariableDirectory(VariableDirectory variableDirectory) {
    this.variableDirectory = variableDirectory;
  }
}

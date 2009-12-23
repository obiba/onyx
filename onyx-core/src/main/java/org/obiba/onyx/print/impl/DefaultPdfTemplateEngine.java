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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.DateType;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.io.support.LocalizedResourceLoader;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.print.PdfTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class DefaultPdfTemplateEngine implements PdfTemplateEngine {

  private static final Logger log = LoggerFactory.getLogger(PdfTemplateReport.class);

  private static final String PARTICIPANT_TABLE_NAME = "Participants";

  private static final Pattern onyxPattern = Pattern.compile("^Onyx\\.");

  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  private ActiveInterviewService activeInterviewService;

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public InputStream applyTemplate(Locale locale, Map<String, String> fieldToVariableMap, LocalizedResourceLoader reportTemplateLoader, ActiveInterviewService activeInterviewService) {

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

    // Set the values in the report data fields
    try {
      stamper = new PdfStamper(pdfReader, output);
      stamper.setFormFlattening(true);

      AcroFields form = stamper.getAcroFields();
      Participant participant = activeInterviewService.getParticipant();

      setVariableDataFields(participant, form, fieldToVariableMap);
      setAdditionalDataFields(form);

    } catch(Exception ex) {
      throw new RuntimeException("An error occured while preparing the report to participant", ex);
    } finally {
      try {
        stamper.close();
      } catch(Exception e) {
        log.warn("Could not close PdfStamper", e);
      }
      try {
        output.close();
      } catch(IOException e) {
        log.warn("Could not close OutputStream", e);
      }
      pdfReader.close();
    }

    return new ByteArrayInputStream(output.toByteArray());
  }

  private void setAdditionalDataFields(AcroFields form) {
    try {
      form.setField("DateInterview\\.date", dateFormat.format(new Date()));
    } catch(Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private void setVariableDataFields(Participant participant, AcroFields form, Map<String, String> fieldToVariableMap) {

    HashMap<String, String> fieldList = form.getFields();

    ValueTable participantValueTable = getOnyxParticipantTable();
    ValueSet valueSet = getCurrentParticipantValueSet(participantValueTable);

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
            String valueString = getStringValue(participantValueTable, valueSet, field.getKey(), variablePath);
            form.setField(field.getKey(), valueString);
          }
        }
      }
    } catch(Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private String getStringValue(ValueTable participantValueTable, ValueSet valueSet, String fieldName, String variablePath) {
    try {
      VariableValueSource variableValueSource = participantValueTable.getVariableValueSource(stripOnyxPrefix(variablePath));
      Value value = variableValueSource.getValue(valueSet);
      return getValueAsString(variableValueSource.getVariable(), value);
    } catch(NoSuchVariableException e) {
      log.error("Invalid PDF template definition. Field '{}' is linked to inexistent variable '{}'.", fieldName, variablePath);
      throw e;

    }
  }

  private ValueTable getOnyxParticipantTable() {
    ValueTable onyxParticipantTable = null;
    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      onyxParticipantTable = datasource.getValueTable(PARTICIPANT_TABLE_NAME);
    }
    return onyxParticipantTable;
  }

  private ValueSet getCurrentParticipantValueSet(ValueTable onyxParticipantTable) {
    Assert.notNull(onyxParticipantTable, "onyxParticipantTable must not be null.");
    // Get the currently interviewed participant's ValueSet.
    VariableEntity entity = new VariableEntityBean("Participant", activeInterviewService.getParticipant().getBarcode());
    return onyxParticipantTable.getValueSet(entity);
  }

  private String stripOnyxPrefix(String variablePath) {
    Assert.notNull(variablePath, "variablePath must not be null.");
    Matcher matcher = onyxPattern.matcher(variablePath);
    return matcher.replaceAll("");
  }

  private String getValueAsString(org.obiba.magma.Variable variable, Value value) {
    if(value == null || value.isNull()) return "";

    String valueString = "";
    if(value.isSequence()) {
      ValueSequence valueSequence = value.asSequence();
      for(Value v : valueSequence.getValues()) {
        valueString += " " + getValueAsString(variable, v);
      }
    }
    if(value.getValueType() == DateType.get()) {
      valueString = dateFormat.format(value.getValue());
    } else {
      valueString = value.toString();

      if(variable != null && variable.getUnit() != null) valueString += " " + variable.getUnit();
    }
    return valueString;
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

  public void setDateFormat(String dateFormat) {
    this.dateFormat = new SimpleDateFormat(dateFormat);
  }

}

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
import java.io.InputStream;
import java.util.Locale;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.onyx.magma.DataValueConverter;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PdfReport implementation for reports that are retrieved from a variable to be printed.
 */
public class PdfVariableReport extends PdfReport {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(PdfVariableReport.class);

  private static final String PARTICIPANT_TABLE_NAME = "Participants";

  //
  // Instance Variables
  //

  private String pdfVariablePath;

  //
  // PdfReport Methods
  //

  @Override
  protected InputStream getReport(Locale locale) {
    // Get the Participant ValueTable.
    ValueTable onyxParticipantTable = null;
    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      onyxParticipantTable = datasource.getValueTable(PARTICIPANT_TABLE_NAME);
    }

    if(onyxParticipantTable != null) {
      String magmaVariableName = pdfVariablePath.replaceFirst("Onyx.", "");
      try {
        // Get the currently interviewed participant's ValueSet.
        VariableEntity entity = new VariableEntityBean("Participant", activeInterviewService.getParticipant().getBarcode());
        ValueSet valueSet = onyxParticipantTable.getValueSet(entity);

        // Get the PDF data.
        VariableValueSource variableValueSource = onyxParticipantTable.getVariableValueSource(magmaVariableName);
        Value pdfValue = variableValueSource.getValue(valueSet);
        if(!pdfValue.isNull()) {
          Data pdfData = DataValueConverter.valueToData(pdfValue);
          return new ByteArrayInputStream((byte[]) pdfData.getValue());
        }

      } catch(NoSuchVariableException noSuchVariableEx) {
        log.error("No Magma variable found for the following name: {}", magmaVariableName);
      }
    } else {
      log.error("No Magma ValueTable found for the following name: {}", PARTICIPANT_TABLE_NAME);
    }

    return null;
  }

  public void afterPropertiesSet() {
    super.afterPropertiesSet();
  }

  //
  // Methods
  //

  public void setPdfVariablePath(String pdfVariablePath) {
    this.pdfVariablePath = pdfVariablePath;
  }

}

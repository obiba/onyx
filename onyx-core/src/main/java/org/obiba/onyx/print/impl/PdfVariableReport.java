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
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.onyx.core.data.VariableDataSource;
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

  private static final Logger log = LoggerFactory.getLogger(VariableDataSource.class);

  //
  // Instance Variables
  //

  private String pdfVariablePath;

  //
  // PdfReport Methods
  //

  @Override
  protected InputStream getReport(Locale locale) {
    MagmaEngineReferenceResolver resolver = MagmaEngineVariableResolver.valueOf(pdfVariablePath);
    Datasource datasource = MagmaEngine.get().getDatasources().iterator().next();

    try {
      ValueTable table = datasource.getValueTable(resolver.getTableName());
      VariableValueSource variableValueSource = table.getVariableValueSource(resolver.getVariableName());

      // Get the currently interviewed participant's ValueSet.
      VariableEntity entity = new VariableEntityBean("Participant", activeInterviewService.getParticipant().getBarcode());
      ValueSet valueSet = table.getValueSet(entity);

      // Get the PDF data.
      Value pdfValue = variableValueSource.getValue(valueSet);
      if(!pdfValue.isNull()) {
        Data pdfData = DataValueConverter.valueToData(pdfValue);
        return new ByteArrayInputStream((byte[]) pdfData.getValue());
      }

    } catch(NoSuchVariableException e) {
      log.error("No variable found for the following name: {}", e.getName());
    } catch(NoSuchValueTableException e) {
      log.error("No ValueTable found for the following name: {}", resolver.getTableName());
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

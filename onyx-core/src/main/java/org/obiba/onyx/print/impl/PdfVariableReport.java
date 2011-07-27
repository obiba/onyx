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
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.onyx.magma.MagmaInstanceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PdfReport implementation for reports that are retrieved from a variable to be printed.
 */
public class PdfVariableReport extends PdfReport {

  private static final Logger log = LoggerFactory.getLogger(PdfVariableReport.class);

  private transient MagmaInstanceProvider magmaInstanceProvider;

  private String pdfVariablePath;

  @Override
  protected InputStream getReport(Locale locale) {

    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf(pdfVariablePath);
    Datasource datasource = magmaInstanceProvider.getOnyxDatasource();

    try {
      ValueTable table = datasource.getValueTable(resolver.getTableName());
      VariableValueSource variableValueSource = table.getVariableValueSource(resolver.getVariableName());

      // Get the currently interviewed participant's ValueSet.
      VariableEntity entity = magmaInstanceProvider.newParticipantEntity(activeInterviewService.getParticipant().getBarcode());
      ValueSet valueSet = table.getValueSet(entity);

      // Get the PDF data.
      Value pdfValue = variableValueSource.getValue(valueSet);
      if(!pdfValue.isNull()) {
        return new ByteArrayInputStream((byte[]) pdfValue.getValue());
      }

    } catch(NoSuchVariableException e) {
      log.error("Cannot resolve variable '{}' for PDF report '{}'. No such variable {}.", new String[] { pdfVariablePath, this.getName(), e.getName() });
    } catch(NoSuchValueTableException e) {
      log.error("Cannot resolve variable '{}' for PDF report '{}'. No such Value Table.", pdfVariablePath, this.getName());
    }

    return null;
  }

  public void setPdfVariablePath(String pdfVariablePath) {
    this.pdfVariablePath = pdfVariablePath;
  }

  public void setMagmaInstanceProvider(MagmaInstanceProvider magmaInstanceProvider) {
    this.magmaInstanceProvider = magmaInstanceProvider;
  }

}

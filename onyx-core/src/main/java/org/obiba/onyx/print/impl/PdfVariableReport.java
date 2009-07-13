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
import java.util.List;
import java.util.Locale;

import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableDirectory;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

/**
 * PdfReport implementation for reports that are retrieved from a variable to be printed.
 */
public class PdfVariableReport extends PdfReport {

  private String pdfVariablePath;

  private VariableDirectory variableDirectory;

  @Override
  protected InputStream getReport(Locale locale) {
    VariableData variableData = variableDirectory.getVariableData(activeInterviewService.getParticipant(), pdfVariablePath);
    List<Data> datas = variableData.getDatas();
    if(datas.size() > 1) {
      throw new RuntimeException("The report variable has more than one data object associated to it, please verify the variable path is one of an actual PDF report.");
    }
    for(Data data : datas) {
      if(data.getType() == DataType.DATA) {
        return new ByteArrayInputStream((byte[]) data.getValue());
      } else {
        throw new RuntimeException("The report variable does not contain any data, please verify the variable path is one of an actual PDF report.");
      }
    }
    return null;
  }

  public void afterPropertiesSet() {
    super.afterPropertiesSet();
    variableDirectory = (VariableDirectory) applicationContext.getBean("variableDirectory");
  }

  public void setPdfVariablePath(String pdfVariablePath) {
    this.pdfVariablePath = pdfVariablePath;
  }

}

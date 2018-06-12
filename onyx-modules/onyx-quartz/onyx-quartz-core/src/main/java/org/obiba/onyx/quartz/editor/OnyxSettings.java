/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor;

import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Required;

public class OnyxSettings {

  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  @Required
  public void setDateFormatStr(String dateFormat) {
    this.dateFormat = new SimpleDateFormat(dateFormat);
  }

  public SimpleDateFormat getDateFormat() {
    return dateFormat;
  }

}

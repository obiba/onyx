/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebSession;

public class DateModelUtils {

  public static IModel getShortDateTimeModel(IModel dateModel) {
    return new FormatingDateTimeModel(SimpleDateFormat.SHORT, dateModel);
  }

  public static IModel getDateTimeModel(IModel dateModel) {
    return new FormatingDateTimeModel(SimpleDateFormat.MEDIUM, dateModel);
  }

  public static IModel getShortDateModel(IModel dateModel) {
    return new FormatingDateModel(SimpleDateFormat.SHORT, dateModel);
  }

  public static IModel getDateModel(IModel dateModel) {
    return new FormatingDateModel(SimpleDateFormat.MEDIUM, dateModel);
  }

  private static class FormatingDateTimeModel extends Model {

    private static final long serialVersionUID = 0L;

    int format;

    IModel dateModel;

    FormatingDateTimeModel(int dateFormat, IModel dateModel) {
      this.format = dateFormat;
      this.dateModel = dateModel;
    }

    @Override
    public Object getObject() {
      Date date = (Date) dateModel.getObject();
      if(date != null) {
        return SimpleDateFormat.getDateTimeInstance(format, SimpleDateFormat.SHORT, WebSession.get().getLocale()).format(date);
      }
      return "";
    }
  }

  private static class FormatingDateModel extends Model {

    private static final long serialVersionUID = 0L;

    int format;

    IModel dateModel;

    FormatingDateModel(int dateFormat, IModel dateModel) {
      this.format = dateFormat;
      this.dateModel = dateModel;
    }

    @Override
    public Object getObject() {
      Date date = (Date) dateModel.getObject();
      if(date != null) {
        return SimpleDateFormat.getDateInstance(format, WebSession.get().getLocale()).format(date);
      }
      return "";
    }
  }
}

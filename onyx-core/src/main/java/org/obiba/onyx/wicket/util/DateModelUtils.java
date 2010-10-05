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

import java.text.DateFormat;
import java.util.Date;

import org.apache.wicket.Session;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class DateModelUtils {

  public static IModel<String> getShortDateTimeModel(IModel<Date> dateModel) {
    return new FormatingDateTimeModel(DateFormat.SHORT, dateModel);
  }

  public static IModel<String> getDateTimeModel(IModel<Date> dateModel) {
    return new FormatingDateTimeModel(DateFormat.MEDIUM, dateModel);
  }

  public static IModel<String> getDateTimeModel(IModel<DateFormat> formatModel, IModel<Date> dateModel) {
    return new FormatingDateTimeModel(formatModel, dateModel);
  }

  public static IModel<String> getShortDateModel(IModel<Date> dateModel) {
    return new FormatingDateModel(DateFormat.SHORT, dateModel);
  }

  public static IModel<String> getDateModel(IModel<Date> dateModel) {
    return new FormatingDateModel(DateFormat.MEDIUM, dateModel);
  }

  public static IModel<String> getDateModel(IModel<DateFormat> formatModel, IModel<Date> dateModel) {
    return new FormatingDateModel(formatModel, dateModel);
  }

  private static class FormatingDateTimeModel extends Model<String> {

    private static final long serialVersionUID = 0L;

    IModel<DateFormat> formatModel;

    int format;

    IModel<Date> dateModel;

    FormatingDateTimeModel(int dateFormat, IModel<Date> dateModel) {
      this.format = dateFormat;
      this.dateModel = dateModel;
    }

    FormatingDateTimeModel(IModel<DateFormat> formatModel, IModel<Date> dateModel) {
      this.formatModel = formatModel;
      this.dateModel = dateModel;
    }

    @Override
    public String getObject() {
      Date date = dateModel.getObject();

      if(date != null) {
        if(formatModel != null) {
          DateFormat dateFormat = formatModel.getObject();

          if(dateFormat != null) {
            return dateFormat.format(date);
          }
        }

        return DateFormat.getDateTimeInstance(format, DateFormat.SHORT, Session.get().getLocale()).format(date);
      }
      return "";
    }
  }

  private static class FormatingDateModel extends Model<String> {

    private static final long serialVersionUID = 0L;

    IModel<DateFormat> formatModel;

    int format;

    IModel<Date> dateModel;

    FormatingDateModel(int dateFormat, IModel<Date> dateModel) {
      this.format = dateFormat;
      this.dateModel = dateModel;
    }

    FormatingDateModel(IModel<DateFormat> formatModel, IModel<Date> dateModel) {
      this.formatModel = formatModel;
      this.dateModel = dateModel;
    }

    @Override
    public String getObject() {
      Date date = dateModel.getObject();

      if(date != null) {
        if(formatModel != null) {
          DateFormat dateFormat = formatModel.getObject();

          if(dateFormat != null) {
            return dateFormat.format(date);
          }
        }

        return DateFormat.getDateInstance(format, Session.get().getLocale()).format(date);
      }
      return "";
    }
  }
}

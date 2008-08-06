package org.obiba.onyx.webapp.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebSession;

public class DateUtils {
  
  public static IModel getDateModel(IModel dateModel) {
    Date date = (Date) dateModel.getObject();
    if(date != null) {
      String dateStr = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, WebSession.get().getLocale()).format(date);
      return new Model(dateStr);
    } else
      return new Model("");
  }
  
  public static IModel getFullDateModel(IModel dateModel) {
    Date date = (Date) dateModel.getObject();
    if(date != null) {
      String dateStr = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT, WebSession.get().getLocale()).format(date);
      return new Model(dateStr);
    } else
      return new Model("");
  }
  
}

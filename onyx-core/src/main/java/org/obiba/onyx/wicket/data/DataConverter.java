/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.data;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataConverter implements IConverter {

  private static final long serialVersionUID = 3639500916194340039L;

  private static final Logger log = LoggerFactory.getLogger(DataConverter.class);

  private DataType type;

  private UserSessionService userSessionService;

  public DataConverter(DataType type, UserSessionService userSessionService) {
    if(type == null) throw new IllegalArgumentException("DataType cannot be null.");
    this.type = type;
    this.userSessionService = userSessionService;
  }

  public Object convertToObject(String value, Locale locale) {

    Data data = null;

    if(value == null || value.length() == 0) return null;

    try {
      switch(type) {
      case BOOLEAN:
        data = new Data(type, Boolean.valueOf(value));
        break;

      case DATE:
        data = new Data(type, (Serializable) getDateConverter().convertToObject(value, locale));
        break;

      case DECIMAL:
        data = new Data(type, Double.valueOf(value));
        break;

      case INTEGER:
        data = new Data(type, Long.valueOf(value));
        break;

      case TEXT:
        data = new Data(type, value);
        break;

      case DATA:
        // TODO
        break;

      default:
        break;
      }
    } catch(Exception ex) {
      log.warn("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
      ConversionException cex = new ConversionException(ex.getMessage());
      throw cex;
    }
    return data;
  }

  public String convertToString(Object value, Locale locale) {
    Data data = (Data) value;
    if(data == null || data.getValue() == null) return null;

    if(type.equals(DataType.DATE)) {
      Date date = data.getValue();
      return getDateConverter().convertToString(date, locale);
    }
    return data.getValueAsString();
  }

  public IConverter getDateConverter() {
    return new PatternDateConverter(userSessionService.getDatePattern(), true);
  }

}

/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.data.validation.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.DateValidator;
import org.apache.wicket.validation.validator.MaximumValidator;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.data.DataValidator;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * A {@code Converter} implementation that handles instances of {@code NumberValidator} and {@code DateValidator}.
 * Specifically, this class can unmarshal all flavors of Wicket's classes using a single node structure:
 * 
 * <pre>
 *   &lt;rangeValidator type=&quot;integer&quot;&gt;
 *     &lt;minimum&gt;10&lt;/minimum&gt;
 *   &lt;/rangeValidator&gt;
 *   &lt;rangeValidator type=&quot;decimal&quot;&gt;
 *     &lt;maximum&gt;1.5&lt;/maximum&gt;
 *   &lt;/rangeValidator&gt;
 *   &lt;rangeValidator type=&quot;date&quot;&gt;
 *     &lt;minimum&gt;1099-01-01&lt;/maximum&gt;
 *     &lt;maximum&gt;2099-01-01&lt;/maximum&gt;
 *   &lt;/rangeValidator&gt;
 *   &lt;rangeValidator type=&quot;text&quot;&gt;
 *     &lt;minimum&gt;10&lt;/maximum&gt;
 *     &lt;maximum&gt;250&lt;/maximum&gt;
 *   &lt;/rangeValidator&gt;
 * </pre>
 * 
 * In order, these are mapped to {@code NumberValidator.MinimumValidator}, {@code
 * NumberValidator.DoubleMaximumValidator} and {@code DateValidator.RangeValidator}.
 */
public class RangeValidatorNodeConverter extends AbstractValidatorNodeConverter {

  private SimpleDateFormat[] formats;

  public RangeValidatorNodeConverter() {
    this.formats = new SimpleDateFormat[] { new SimpleDateFormat("yyyy-MM-dd"), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S z") };
  }

  public String getNodeName() {
    return "rangeValidator";
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    // TODO
    throw new UnsupportedOperationException("Method marshal() is not supported.");
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String typeName = reader.getAttribute("type");
    if(typeName == null || typeName.length() == 0) {
      throw new ConversionException("Empty or missing type attribute for rangeValidator node. Make sure you specify a 'type' attribute on your 'rangeValidator' node.");
    }

    DataType type = null;
    try {
      type = DataType.valueOf(typeName.toUpperCase());
    } catch(IllegalArgumentException e) {
      throw new ConversionException("Invalid type attribute for rangeValidator node.", e);
    }

    String minimumStr = null;
    String maximumStr = null;
    while(reader.hasMoreChildren()) {
      reader.moveDown();
      String nodeName = reader.getNodeName();
      if(nodeName.equalsIgnoreCase("minimum")) {
        minimumStr = reader.getValue();
      } else if(nodeName.equalsIgnoreCase("maximum")) {
        maximumStr = reader.getValue();
      }
      reader.moveUp();
    }

    IValidator validator = null;
    switch(type) {
    case INTEGER: {
      Long minimum = minimumStr == null ? null : Long.valueOf(minimumStr);
      Long maximum = maximumStr == null ? null : Long.valueOf(maximumStr);
      if(minimum != null && maximum != null) {
        validator = new RangeValidator(minimum, maximum);
      } else if(minimum != null) {
        validator = new MinimumValidator(minimum);
      } else if(maximum != null) {
        validator = new MaximumValidator(maximum);
      }
      break;
    }
    case DECIMAL: {
      Double minimum = minimumStr == null ? null : Double.valueOf(minimumStr);
      Double maximum = maximumStr == null ? null : Double.valueOf(maximumStr);
      if(minimum != null && maximum != null) {
        validator = new RangeValidator(minimum, maximum);
      } else if(minimum != null) {
        validator = new MinimumValidator(minimum);
      } else if(maximum != null) {
        validator = new MaximumValidator(maximum);
      }
      break;
    }
    case DATE: {
      Date minimum = minimumStr == null ? null : parseDate(minimumStr);
      Date maximum = maximumStr == null ? null : parseDate(maximumStr);
      if(minimum != null && maximum != null) {
        validator = DateValidator.range(minimum, maximum);
      } else if(minimum != null) {
        validator = DateValidator.minimum(minimum);
      } else if(maximum != null) {
        validator = DateValidator.maximum(maximum);
      }
      break;
    }
    case TEXT: {
      Integer minimum = minimumStr == null ? null : Integer.valueOf(minimumStr);
      Integer maximum = maximumStr == null ? null : Integer.valueOf(maximumStr);
      if(minimum != null && maximum != null) {
        validator = StringValidator.lengthBetween(minimum, maximum);
      } else if(minimum != null) {
        validator = StringValidator.minimumLength(minimum);
      } else if(maximum != null) {
        validator = StringValidator.maximumLength(maximum);
      }
      break;
    }
    default:
      throw new ConversionException("Invalid type for rangeValidator node: '" + typeName + "'");
    }
    if(validator == null) {
      throw new ConversionException("Invalid rangeValidator node");
    }
    return new DataValidator(validator, type);
  }

  protected Date parseDate(String dateString) {
    for(SimpleDateFormat sdf : formats) {
      try {
        return sdf.parse(dateString);
      } catch(ParseException e) {
        // Ignore, try the next format if any
      }
    }
    throw new ConversionException("Cannot parse date '" + dateString + "'");
  }
}
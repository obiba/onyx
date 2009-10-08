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

import org.apache.wicket.validation.validator.PatternValidator;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts a Wicket {@code PatternValidator} using a simple node structure.
 * 
 * <pre>
 *   &lt;patternValidator&gt;\d+&lt;/patternValidator&gt;
 * </pre>
 */
public class PatternValidatorNodeConverter extends AbstractValidatorNodeConverter {
  public String getNodeName() {
    return "patternValidator";
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean canConvert(Class type) {
    return type != null && PatternValidator.class.isAssignableFrom(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String pattern = null;
    int flags = 0;
    Boolean reverse = null;

    while(reader.hasMoreChildren()) {
      reader.moveDown();

      if(reader.getNodeName().equals("pattern")) {
        while(reader.hasMoreChildren()) {
          reader.moveDown();

          if(reader.getNodeName().equals("pattern")) {
            pattern = reader.getValue();
          } else if(reader.getNodeName().equals("flags")) {
            flags = Integer.parseInt(reader.getValue());
          }

          reader.moveUp();
        }
      } else if(reader.getNodeName().equals("reverse")) {
        reverse = Boolean.valueOf(reader.getValue());
      }

      reader.moveUp();
    }

    PatternValidator validator = new PatternValidator(pattern, flags);
    if(reverse != null) {
      validator.setReverse(reverse);
    }

    return validator;
    // String pattern = reader.getValue();
    // if(pattern == null || pattern.isEmpty()) {
    // throw new ConversionException("Missing pattern in patternValidator node.");
    // }
    // return new DataValidator(new PatternValidator(pattern), DataType.TEXT);
  }
}
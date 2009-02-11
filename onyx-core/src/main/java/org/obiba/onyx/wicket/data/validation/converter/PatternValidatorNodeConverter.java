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
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.data.DataValidator;

import com.thoughtworks.xstream.converters.ConversionException;
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

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String pattern = reader.getValue();
    if(pattern == null || pattern.isEmpty()) {
      throw new ConversionException("Missing pattern in patternValidator node.");
    }
    return new DataValidator(new PatternValidator(pattern), DataType.TEXT);
  }
}
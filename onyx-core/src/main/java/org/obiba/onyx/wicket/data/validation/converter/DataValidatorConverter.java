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

import java.util.LinkedList;
import java.util.List;

import org.obiba.onyx.wicket.data.IDataValidator;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * An implementation of XStream's {@code Converter} interface that handles {@code DataValidator} instances. This class
 * offloads the work of un/marshaling {@code IValidator} instances to implementations of {@code IValidatorNodeConverter}
 * . These instances are responsible for handling specific {@code IValidator} instances wrapped in a {@code
 * IDataValidator}
 * <p>
 * Using this class requires that some aliases are set on the XStream instance. Specifically, each implementation of
 * {@code IValidatorNodeConverter} handles a specific node name, as such, an alias from this node name to {@code
 * IDataValidator} is required.
 * <p>
 * Consider {@code RangeValidatorNodeConverter} which handles nodes named &lt;rangeValidator&gt;. An alias must be added
 * like so:
 * 
 * <pre>
 * XStream x = new XStream();
 * x.alias(&quot;rangeValidator&quot;, IDataValidator.class);
 * x.registerConverter(new DataValidatorConverter());
 * </pre>
 * 
 * The helper method {@link #createAliases(XStream)} exists that creates all these aliases automatically:
 * 
 * <pre>
 * XStream x = new XStream();
 * x.registerConverter(new DataValidatorConverter().createAliases(x));
 * </pre>
 */
public class DataValidatorConverter implements Converter {

  private List<IValidatorNodeConverter> converters = new LinkedList<IValidatorNodeConverter>();

  /**
   * Builds a {@code DataValidatorConverter} instance with the default {@code ValidatorNodeConverter} instances only.
   */
  public DataValidatorConverter() {
    converters.add(new RangeValidatorNodeConverter());
    converters.add(new PatternValidatorNodeConverter());
  }

  /**
   * Builds a {@code DataValidatorConverter} adding the specified list of {@code ValidatorNodeConverter} instances to
   * the list of default converters. The custom converters are added after the default converters as such, they cannot
   * override the default converters.
   * 
   * @param customConverters a list of custom {@code ValidatorNodeConverter} instances
   */
  public DataValidatorConverter(List<IValidatorNodeConverter> customConverters) {
    this();
    if(customConverters != null && customConverters.size() > 0) {
      this.converters.addAll(converters);
    }
  }

  /**
   * Helper method for creating an alias for each instance of {@code IValidatorNodeConverter} registered in this
   * instance. The method returns {@code this} to allow chaining it within the call to {@code
   * XStream#registerConverter(Converter)}:
   * 
   * <pre>
   * XStream x = new XStream();
   * x.registerConverter(new DataValidatorConverter().createAliases(x));
   * </pre>
   * @param xstream the XStream instance in which to create the aliases
   * @return this for method chaining
   */
  public DataValidatorConverter createAliases(XStream xstream) {
    for(IValidatorNodeConverter converter : converters) {
      xstream.alias(converter.getNodeName(), IDataValidator.class);
    }
    return this;
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    // TODO
    throw new UnsupportedOperationException("Method marshal() is not supported.");
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    // The name of the validator is either directly the node's name or the node's class attribute
    // <rangeValidator>...</rangeValidator> or <someNode class="rangeValidator">...</someNode>

    String validatorName = reader.getNodeName();
    String className = reader.getAttribute("class");
    if(className != null) {
      validatorName = className;
    }
    for(IValidatorNodeConverter converter : converters) {
      if(converter.getNodeName().equals(validatorName)) {
        return converter.unmarshal(reader, context);
      }
    }
    throw new ConversionException("Unknown IValidator node '" + validatorName + "'");
  }

  @SuppressWarnings("unchecked")
  public boolean canConvert(Class type) {
    return type != null && type.equals(IDataValidator.class);
  }

}

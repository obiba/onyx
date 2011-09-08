/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.service.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obiba.core.spring.xstream.InjectingReflectionProviderWrapper;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.core.data.FirstNotNullDataSource;
import org.obiba.onyx.core.data.FixedDataSource;
import org.obiba.onyx.core.data.HashedDataSource;
import org.obiba.onyx.core.data.JavascriptDataSource;
import org.obiba.onyx.core.data.ParticipantPropertyDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.io.support.XStreamDataConverter;
import org.obiba.onyx.jade.core.data.InstrumentParameterDataSource;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.domain.instrument.validation.EqualsParameterCheck;
import org.obiba.onyx.jade.core.domain.instrument.validation.EqualsValueCheck;
import org.obiba.onyx.jade.core.domain.instrument.validation.ParameterSpreadCheck;
import org.obiba.onyx.jade.core.domain.instrument.validation.RangeCheck;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

public class InstrumentTypeFactoryBean implements FactoryBean, ApplicationContextAware, ResourceLoaderAware, InitializingBean {
  //
  // Constants
  //

  private static final String resourceEncoding = "ISO-8859-1";

  private static final Logger log = LoggerFactory.getLogger(InstrumentTypeFactoryBean.class);

  //
  // Instance Variables
  //

  private ApplicationContext applicationContext;

  private String[] xstreamResourcePatterns;

  private ResourcePatternResolver resolver;

  private XStream xstream;

  private Map<String, InstrumentType> instrumentTypes;

  //
  // Constructors
  //

  public InstrumentTypeFactoryBean() {
    initializeXstream();
  }

  //
  // ApplicationContextAware Methods
  //

  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  //
  // ResourceLoaderAware Methods
  //

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resolver = (ResourcePatternResolver) resourceLoader;
  }

  //
  // InitializingBean Methods
  //

  public void afterPropertiesSet() {
    initializeXstream();
  }

  //
  // FactoryBean Methods
  //

  @SuppressWarnings("unchecked")
  public Class getObjectType() {
    return Map.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public Object getObject() throws Exception {
    if(instrumentTypes == null) {
      instrumentTypes = new HashMap<String, InstrumentType>();
      handleResourcePattern();
    }
    return instrumentTypes;
  }

  //
  // Methods
  //

  public void setResourcePatterns(String[] xstreamResourcePatterns) {
    this.xstreamResourcePatterns = xstreamResourcePatterns;
  }

  private void initializeXstream() {
    xstream = new XStream(new InjectingReflectionProviderWrapper((new XStream()).getReflectionProvider(), applicationContext));

    xstream.setMode(XStream.ID_REFERENCES);
    xstream.alias("instrumentType", InstrumentType.class);
    xstream.alias("contraIndication", Contraindication.class);
    xstream.alias("interpretative", InterpretativeParameter.class);
    xstream.alias("input", InstrumentInputParameter.class);
    xstream.alias("output", InstrumentOutputParameter.class);

    xstream.alias("participantPropertyDataSource", ParticipantPropertyDataSource.class);
    xstream.alias("fixedDataSource", FixedDataSource.class);
    xstream.alias("variableDataSource", VariableDataSource.class);
    xstream.alias("instrumentParameterDataSource", InstrumentParameterDataSource.class);
    xstream.alias("firstNotNullDataSource", FirstNotNullDataSource.class);
    xstream.alias("computingDataSource", ComputingDataSource.class);
    xstream.alias("scriptDataSource", JavascriptDataSource.class);
    xstream.useAttributeFor(JavascriptDataSource.class, "valueTable");
    xstream.useAttributeFor(JavascriptDataSource.class, "valueType");
    xstream.useAttributeFor(JavascriptDataSource.class, "unit");
    xstream.useAttributeFor(JavascriptDataSource.class, "sequence");
    xstream.alias("hashedDataSource", HashedDataSource.class);
    xstream.alias("comparingDataSource", ComparingDataSource.class);
    xstream.useAttributeFor(ComparingDataSource.class, "comparisonOperator");

    xstream.alias("equalsValueCheck", EqualsValueCheck.class);
    xstream.alias("equalsParameterCheck", EqualsParameterCheck.class);
    xstream.alias("rangeCheck", RangeCheck.class);
    xstream.alias("parameterSpreadCheck", ParameterSpreadCheck.class);

    xstream.alias("data", Data.class);
    xstream.registerConverter(new XStreamDataConverter());
  }

  private void handleResourcePattern() {
    if(xstreamResourcePatterns != null) {
      for(String locationPattern : xstreamResourcePatterns) {
        try {
          Resource[] resources = resolver.getResources(locationPattern);
          if(resources != null) {
            for(Resource resource : resources) {
              Object result = handleXtreamResource(resource);
              handleXstreamResult(resource, result);
            }
          }
        } catch(IOException e) {
          log.error("Error resolving resource pattern {}: {}", locationPattern, e.getMessage());
          throw new RuntimeException(e);
        }
      }
    }
  }

  protected void handleXstreamResult(Resource resource, Object result) {
    if(result != null && result instanceof InstrumentType) {
      InstrumentType type = (InstrumentType) result;
      List<InstrumentParameter> parameters = type.getInstrumentParameters();
      log.info("Loaded instrument type {} with {} parameters.", type.getName(), (parameters != null ? parameters.size() : -1));
      Set<String> parameterCodes = new HashSet<String>();
      for(InstrumentParameter parameter : parameters) {
        if(parameterCodes.add(parameter.getCode()) == false) {
          log.error("Instrument descriptor for type {} is invalid. Multiple parameters with the same code '{}' are defined. Parameter codes must be unique for an instrument type.", type.getName(), parameter.getCode());
          throw new IllegalStateException("Duplicate parameter code for type '" + type.getName() + "': " + parameter.getCode());
        }
      }

      instrumentTypes.put(type.getName(), type);
    }
  }

  private Object handleXtreamResource(Resource resource) {
    log.info("Loading resource {}.", resource);
    InputStreamReader reader = null;
    try {
      reader = new InputStreamReader(resource.getInputStream(), resourceEncoding);
      return xstream.fromXML(new InputStreamReader(resource.getInputStream(), resourceEncoding));
    } catch(UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      log.error("Error parsing XStream resource {}: {}", resource, e.getMessage());
      throw new RuntimeException(e);
    } catch(XStreamException e) {
      log.error("Invalid XStream resource {}: {}", resource, e.getMessage());
      throw e;
    } catch(RuntimeException e) {
      log.error("Error parsing XStream resource {}: {}", resource, e.getMessage());
      throw e;
    } finally {
      if(reader != null) {
        try {
          reader.close();
        } catch(Exception e) {
          // ignore
        }
      }
    }
  }

}

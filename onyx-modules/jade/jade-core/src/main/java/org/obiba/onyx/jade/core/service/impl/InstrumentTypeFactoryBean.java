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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.jade.core.domain.instrument.FixedSource;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentComputedOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.domain.instrument.MultipleOutputParameterSource;
import org.obiba.onyx.jade.core.domain.instrument.OperatorSource;
import org.obiba.onyx.jade.core.domain.instrument.OutputParameterSource;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantPropertySource;
import org.obiba.onyx.jade.core.domain.instrument.validation.EqualsParameterCheck;
import org.obiba.onyx.jade.core.domain.instrument.validation.EqualsValueCheck;
import org.obiba.onyx.jade.core.domain.instrument.validation.ParameterSpreadCheck;
import org.obiba.onyx.jade.core.domain.instrument.validation.RangeCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

public class InstrumentTypeFactoryBean implements FactoryBean, ResourceLoaderAware {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private String[] xstreamResourcePatterns;

  private ResourcePatternResolver resolver;

  private XStream xstream;

  private List<InstrumentType> instrumentTypes;

  private static final String resourceEncoding = "ISO-8859-1";

  public InstrumentTypeFactoryBean() {
    instrumentTypes = new ArrayList<InstrumentType>();
    xstream = new XStream();
    initializeXstream();
  }

  public void setResourcePatterns(String[] xstreamResourcePatterns) {
    this.xstreamResourcePatterns = xstreamResourcePatterns;
  }

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resolver = (ResourcePatternResolver) resourceLoader;
  }

  protected void initializeXstream() {
    xstream.setMode(XStream.ID_REFERENCES);
    xstream.alias("instrumentType", InstrumentType.class);
    xstream.alias("instrument", Instrument.class);
    xstream.alias("contraIndication", Contraindication.class);
    xstream.alias("contraindication", Contraindication.class);
    xstream.alias("interpretative", InterpretativeParameter.class);
    xstream.alias("input", InstrumentInputParameter.class);
    xstream.alias("output", InstrumentOutputParameter.class);
    xstream.alias("computedOutput", InstrumentComputedOutputParameter.class);
    xstream.alias("fixedSource", FixedSource.class);
    xstream.alias("participantPropertySource", ParticipantPropertySource.class);
    xstream.alias("outputParameterSource", OutputParameterSource.class);
    xstream.alias("multipleOutputParameterSource", MultipleOutputParameterSource.class);
    xstream.alias("operatorSource", OperatorSource.class);
    xstream.alias("equalsValueCheck", EqualsValueCheck.class);
    xstream.alias("equalsParameterCheck", EqualsParameterCheck.class);
    xstream.alias("rangeCheck", RangeCheck.class);
    xstream.alias("parameterSpreadCheck", ParameterSpreadCheck.class);
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

      if(type.getInstruments().size() == 0) {
        log.error("Instruments list for type {} is empty.", type.getName());
        throw new IllegalStateException("Instruments list for type " + type.getName() + " is empty.");
      }

      instrumentTypes.add(type);
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

  public Class getObjectType() {
    return instrumentTypes.getClass();
  }

  public boolean isSingleton() {
    return true;
  }

  public Object getObject() throws Exception {
    handleResourcePattern();
    return instrumentTypes;
  }

}

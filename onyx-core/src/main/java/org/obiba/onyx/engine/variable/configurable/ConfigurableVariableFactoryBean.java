/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.configurable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.obiba.core.spring.xstream.InjectingReflectionProviderWrapper;
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.core.data.FirstNotNullDataSource;
import org.obiba.onyx.core.data.FixedDataSource;
import org.obiba.onyx.core.data.ParticipantPropertyDataSource;
import org.obiba.onyx.core.data.RegexDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.core.io.support.XStreamDataConverter;
import org.obiba.onyx.engine.variable.Variable;
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

/**
 * Initializes the {@link ConfigurableVariableProvider} from an XML file.
 */
public class ConfigurableVariableFactoryBean implements FactoryBean, ApplicationContextAware, ResourceLoaderAware, InitializingBean {

  private static final String resourceEncoding = "ISO-8859-1";

  private static final Logger log = LoggerFactory.getLogger(ConfigurableVariableFactoryBean.class);

  private ApplicationContext applicationContext;

  private String[] xstreamResourcePatterns;

  private ResourcePatternResolver resolver;

  private XStream xstream;

  private List<DataSourceVariable> dataSourceVariables;

  private ConfigurableVariableProvider configurableVariableProvider;

  public ConfigurableVariableFactoryBean() {
    initializeXstream();
  }

  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resolver = (ResourcePatternResolver) resourceLoader;
  }

  public void afterPropertiesSet() {
    initializeXstream();
  }

  @SuppressWarnings("unchecked")
  public Class getObjectType() {
    return ConfigurableVariableProvider.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public Object getObject() throws Exception {
    if(configurableVariableProvider == null) {
      dataSourceVariables = new ArrayList<DataSourceVariable>();
      handleResourcePattern();
      configurableVariableProvider = new ConfigurableVariableProvider(dataSourceVariables);
      dataSourceVariables = null;
    }
    return configurableVariableProvider;
  }

  public void setResourcePatterns(String[] xstreamResourcePatterns) {
    this.xstreamResourcePatterns = xstreamResourcePatterns;
  }

  private void initializeXstream() {
    xstream = new XStream(new InjectingReflectionProviderWrapper((new XStream()).getReflectionProvider(), applicationContext));

    xstream.setMode(XStream.ID_REFERENCES);
    xstream.processAnnotations(Variable.class);
    xstream.processAnnotations(DataSourceVariable.class);
    xstream.alias("regexDataSource", RegexDataSource.class);
    xstream.alias("variableDataSource", VariableDataSource.class);
    xstream.alias("participantPropertyDataSource", ParticipantPropertyDataSource.class);
    xstream.alias("fixedDataSource", FixedDataSource.class);
    xstream.alias("firstNotNullDataSource", FirstNotNullDataSource.class);
    xstream.alias("computingDataSource", ComputingDataSource.class);

    xstream.registerConverter(new XStreamDataConverter());
  }

  private void handleResourcePattern() {
    if(xstreamResourcePatterns != null) {
      for(String locationPattern : xstreamResourcePatterns) {
        try {
          Resource[] resources = resolver.getResources(locationPattern);
          if(resources != null) {
            for(Resource resource : resources) {
              if(resource.exists()) {
                Object result = handleXtreamResource(resource);
                handleXstreamResult(resource, result);
              }
            }
          }
        } catch(IOException e) {
          log.error("Error resolving resource pattern {}: {}", locationPattern, e.getMessage());
          throw new RuntimeException(e);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected void handleXstreamResult(Resource resource, Object result) {
    if(result != null && result instanceof List) {
      for(Object object : (List) result) {
        if(object != null && object instanceof DataSourceVariable) {
          DataSourceVariable dataSourceVariable = (DataSourceVariable) object;
          dataSourceVariables.add(dataSourceVariable);
        }
      }
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
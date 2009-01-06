/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.obiba.onyx.util.data.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.ReflectionProviderWrapper;

/**
 * Loads {@link Stage} instances from an XML XStream file.
 */
public class StageDescriptorFactoryBean implements FactoryBean, ApplicationContextAware {

  protected ApplicationContext applicationContext;

  protected XStream xstream;

  protected Resource stageDescriptor;

  public StageDescriptorFactoryBean() {
    XStream defaultProvider = new XStream();

    xstream = new XStream(new InjectingReflectionProviderWrapper(defaultProvider.getReflectionProvider()));
    xstream.alias("stages", LinkedList.class);
    xstream.alias("stage", Stage.class);
    xstream.alias("stageCondition", PreviousStageDependencyCondition.class);
    xstream.alias("variableCondition", StageVariableStageDependencyCondition.class);
    xstream.alias("stageVariableCondition", VariableStageDependencyCondition.class);
    xstream.alias("multipleCondition", MultipleStageDependencyCondition.class);
    xstream.alias("inverseCondition", InverseStageDependencyCondition.class);
    xstream.alias("moduleCondition", ModuleDependencyCondition.class);
    xstream.alias("data", Data.class);
    xstream.useAttributeFor(Data.class, "type");
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setStageDescriptor(Resource stageDescriptor) {
    this.stageDescriptor = stageDescriptor;
  }

  public Object getObject() throws Exception {
    InputStream is = stageDescriptor.getInputStream();
    try {
      return xstream.fromXML(stageDescriptor.getInputStream());
    } finally {
      if(is != null) {
        try {
          is.close();
        } catch(Exception e) {
        }

      }
    }
  }

  public Class<?> getObjectType() {
    return List.class;
  }

  public boolean isSingleton() {
    return true;
  }

  /**
   * A implementation of {@code ReflectionProvider} that autowires the created objects. This could be moved to a
   * top-level class when it is better tested.
   */
  public class InjectingReflectionProviderWrapper extends ReflectionProviderWrapper {

    public InjectingReflectionProviderWrapper(ReflectionProvider wrapper) {
      super(wrapper);
    }

    @Override
    public Object newInstance(Class type) {
      // Let the wrapped instance create the bean
      Object value = super.newInstance(type);
      if(value != null) {
        // If we can, autowire the instance
        if(applicationContext != null && applicationContext.getAutowireCapableBeanFactory() != null) {
          // Autowire by type
          applicationContext.getAutowireCapableBeanFactory().autowireBeanProperties(value, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
        }
      }
      return value;
    }
  }

}

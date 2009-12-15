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

import org.obiba.core.spring.xstream.InjectingReflectionProviderWrapper;
import org.obiba.onyx.util.data.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;

/**
 * Loads {@link Stage} instances from an XML XStream file.
 */
public class StageDescriptorFactoryBean implements FactoryBean, ApplicationContextAware, InitializingBean {

  protected ApplicationContext applicationContext;

  protected XStream xstream;

  protected Resource stageDescriptor;

  public StageDescriptorFactoryBean() {
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setStageDescriptor(Resource stageDescriptor) {
    this.stageDescriptor = stageDescriptor;
  }

  public void afterPropertiesSet() throws Exception {
    xstream = new XStream(new InjectingReflectionProviderWrapper(new XStream().getReflectionProvider(), applicationContext));
    xstream.alias("stages", LinkedList.class);
    xstream.alias("stage", Stage.class);
    xstream.alias("stageCondition", PreviousStageDependencyCondition.class);
    xstream.alias("variableCondition", VariableStageDependencyCondition.class);
    xstream.alias("multipleCondition", MultipleStageDependencyCondition.class);
    xstream.alias("inverseCondition", InverseStageDependencyCondition.class);
    xstream.alias("moduleCondition", ModuleDependencyCondition.class);
    xstream.alias("data", Data.class);
    xstream.useAttributeFor(Data.class, "type");
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

}

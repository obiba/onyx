/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.quartz.Trigger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Factory bean for creating a list of job triggers (for quartz scheduler).
 */
public class JobTriggerListFactoryBean implements FactoryBean, ApplicationContextAware {
  //
  // Instance Methods
  //

  private List<Trigger> jobTriggers;

  private ApplicationContext applicationContext;

  //
  // FactoryBean Methods
  //

  @SuppressWarnings("unchecked")
  public Object getObject() throws Exception {
    if(jobTriggers == null) {
      jobTriggers = new ArrayList<Trigger>();

      Map<String, Trigger> triggersInContext = applicationContext.getBeansOfType(Trigger.class);
      for(Trigger trigger : triggersInContext.values()) {
        if(!(trigger instanceof NullTrigger)) {
          jobTriggers.add(trigger);
        }
      }
    }
    return jobTriggers;
  }

  @SuppressWarnings("unchecked")
  public Class getObjectType() {
    return List.class;
  }

  public boolean isSingleton() {
    return true;
  }

  //
  // ApplicationContextAware Methods
  //

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

}

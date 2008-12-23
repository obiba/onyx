/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable;

import java.util.Map;

import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.wicket.application.WebApplicationStartupListener;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 */
public class VariableRegistrationListener implements WebApplicationStartupListener, ApplicationContextAware {

  private ApplicationContext applicationContext;

  private VariableDirectory variableDirectory;

  public void shutdown(WebApplication application) {
    // TODO Auto-generated method stub

  }

  @SuppressWarnings("unchecked")
  public void startup(WebApplication application) {
    Map<String, IVariableProvider> providers = applicationContext.getBeansOfType(IVariableProvider.class);
    if(providers != null) {
      for(IVariableProvider provider : providers.values()) {
        variableDirectory.registerVariables(provider);
      }
    }
  }

  public void setVariableDirectory(VariableDirectory variableDirectory) {
    this.variableDirectory = variableDirectory;
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

}

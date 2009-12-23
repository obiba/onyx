/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.workstation;

import java.io.IOException;

import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ExperimentalConditionFactory implements ResourceLoaderAware {

  private static final Logger log = LoggerFactory.getLogger(ExperimentalConditionFactory.class);

  private ExperimentalConditionLogReader experimentalConditionLogReader = new ExperimentalConditionLogReader();

  private ExperimentalConditionService experimentalConditionService;

  private String[] resourcePatterns;

  private ResourcePatternResolver resolver;

  public void setResourcePatterns(String[] resourcePatterns) {
    this.resourcePatterns = resourcePatterns;
  }

  public void registerExperimentalConditions() {
    if(resourcePatterns != null) {
      for(String resourcePattern : resourcePatterns) {
        try {
          experimentalConditionLogReader.setResources(resolver.getResources(resourcePattern));
          for(ExperimentalConditionLog experimentalConditionLog : experimentalConditionLogReader.read()) {
            experimentalConditionService.register(experimentalConditionLog);
          }
        } catch(IOException e) {
          log.warn("No Experimental Log configuration file was found at {}.  This file is optional but required to setup Experimental Condition Logs and/or Instrument Calibration Logs.", resourcePattern);
        }
      }
    }

  }

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resolver = (ResourcePatternResolver) resourceLoader;
  }

  public void setExperimentalConditionService(ExperimentalConditionService experimentalConditionService) {
    this.experimentalConditionService = experimentalConditionService;
  }

}

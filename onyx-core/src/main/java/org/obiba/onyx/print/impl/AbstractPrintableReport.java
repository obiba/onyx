/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.print.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.obiba.onyx.core.data.AbstractBeanPropertyDataSource;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.core.data.FixedDataSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.data.ParticipantPropertyDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.print.IPrintableReport;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.xstream.InjectingReflectionProviderWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.thoughtworks.xstream.XStream;

/**
 * Base implementation of a IPrintableReport.
 */
abstract public class AbstractPrintableReport implements IPrintableReport, ApplicationContextAware, InitializingBean, ResourceLoaderAware {

  private static final Logger log = LoggerFactory.getLogger(AbstractPrintableReport.class);

  private String name;

  private MessageSourceResolvable label;

  private IDataSource readyCondition;

  private String readyConditionConfigPath;

  /** Indicates how data for this report was collected. (e.g. manually or electronically) */
  private IDataSource dataCollectionMode;

  protected ActiveInterviewService activeInterviewService;

  protected ApplicationContext applicationContext;

  private ResourceLoader resourceLoader;

  public MessageSourceResolvable getLabel() {
    return label;
  }

  public void setLabel(MessageSourceResolvable label) {
    this.label = label;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isReady() {
    if(readyCondition != null) {
      Participant participant = activeInterviewService.getParticipant();
      Data readyData = readyCondition.getData(participant);
      if(readyData == null && readyData.getValue() == null) {
        log.info("Cannot get data for readyCondition of report {}, so readyCondition is false");
        return false;
      } else if(readyData.getType() == DataType.BOOLEAN) {
        log.info("Report {} readyCondition is {}", getName(), readyData.getValue());
        return (Boolean) readyData.getValue();
      } else {
        throw new RuntimeException("readyData not a BOOLEAN.  Please review the readyCondition for report : " + getName());
      }
    }

    // If no readyCondition exist than we assume that the report is "always ready".
    log.info("No readyCondition for report {}, so assume that the report is READY", getName());
    return true;
  }

  public boolean isElectronic() {
    if(dataCollectionMode != null) {
      Participant participant = activeInterviewService.getParticipant();
      Data collectionModeData = dataCollectionMode.getData(participant);
      if(collectionModeData == null) {
        log.info("Cannot get data for dataCollectionMode of report {}. Returning false.");
        return false;
      } else if(collectionModeData.getType() == DataType.TEXT) {
        log.info("Report {} dataCollectionMode is {}", getName(), collectionModeData.getValue());
        if(collectionModeData.getValue() != null && collectionModeData.getValue().equals("ELECTRONIC")) {
          return true;
        } else {
          return false;
        }
      } else {
        throw new RuntimeException("dataCollectionMode not of type TEXT. Please review the dataCollectionMode for report : " + getName());
      }
    }

    log.info("No dataCollectionMode exists for report {}, so assume that the report is ELECTRONIC.", getName());
    return true;
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void afterPropertiesSet() {
    activeInterviewService = (ActiveInterviewService) applicationContext.getBean("activeInterviewService");

    XStream xstream = new XStream(new InjectingReflectionProviderWrapper((new XStream()).getReflectionProvider(), applicationContext));
    xstream.alias("variableDataSource", VariableDataSource.class);
    xstream.alias("comparingDataSource", ComparingDataSource.class);
    xstream.useAttributeFor(ComparingDataSource.class, "comparisonOperator");
    xstream.alias("participantPropertyDataSource", ParticipantPropertyDataSource.class);
    xstream.useAttributeFor(AbstractBeanPropertyDataSource.class, "property");
    xstream.alias("fixedDataSource", FixedDataSource.class);
    xstream.useAttributeFor("type", DataType.class);
    xstream.useAttributeFor("dataType", DataType.class);
    xstream.alias("computingDataSource", ComputingDataSource.class);

    if(readyConditionConfigPath != null) {
      Resource readyConditionFile = null;
      InputStream readyConditionStream = null;
      try {
        readyConditionFile = resourceLoader.getResource(readyConditionConfigPath + File.separator + getName() + "-condition.xml");
        readyConditionStream = readyConditionFile.getInputStream();

        try {
          readyCondition = (IDataSource) xstream.fromXML(readyConditionStream);
        } finally {
          try {
            readyConditionStream.close();
          } catch(IOException e) {
          }
        }

        log.info("Loaded the report condition for the following report: {} ({})", getName(), readyConditionFile.getDescription());
      } catch(IOException e) {
        log.warn("Cannot find the report condition file for the following report: {} ({})", getName(), readyConditionFile.getDescription());
      }
    }

  }

  public void setDataCollectionMode(IDataSource dataCollectionMode) {
    this.dataCollectionMode = dataCollectionMode;
  }

  public Set<Locale> availableLocales() {
    return new HashSet<Locale>();
  }

  public boolean isLocalisable() {
    if(availableLocales().isEmpty()) {
      return false;
    }
    return true;
  }

  public void setReadyConditionConfigPath(String readyConditionConfigPath) {
    this.readyConditionConfigPath = readyConditionConfigPath;
  }

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }
}

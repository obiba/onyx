/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.obiba.onyx.core.data.AbstractBeanPropertyDataSource;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.core.data.FixedDataSource;
import org.obiba.onyx.core.data.ParticipantPropertyDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.xstream.InjectingReflectionProviderWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import com.thoughtworks.xstream.XStream;

/**
 * <p>
 * This class is used to configure the tube registration process.
 * </p>
 * 
 * <p>
 * A <code>TubeRegistrationConfiguration</code> contains the following information:
 * <ul>
 * <li>conditions (observed or asked) that "contra-indicate" collection of samples</li>
 * <li>the tube barcode structure</code>
 * <li>the expected number of tubes to collected and registered (per participant)</li>
 * <li>a pre-defined set of tube registration remarks</li>
 * </ul>
 * </p>
 */
public class TubeRegistrationConfiguration implements ApplicationContextAware, ResourceLoaderAware, InitializingBean {
  //
  // Constants
  //

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(TubeRegistrationConfiguration.class);

  //
  // Instance Variables
  //

  private ResourceLoader resourceLoader;

  private File configDir;

  private BarcodeStructure barcodeStructure;

  private int expectedTubeCount;

  private List<Contraindication> observedContraindications;

  private List<Contraindication> askedContraindications;

  private List<Remark> availableRemarks;

  private List<ConditionalMessage> infoMessages;

  private ApplicationContext applicationContext;

  //
  // Constructors
  //

  /**
   * <p>
   * Creates a <code>TubeRegistrationConfiguration</code> with the specified configuration directory.
   * </p>
   * 
   * <p>
   * Any files used for configuration must be present in the configuration directory.
   * </p>
   * 
   * <p>
   * <em> Note: Configuration files are not loaded by the constructor. To (re-)load them, the
   * <code>initConfig()</code> method must be called. </blockquote>
   * </em>
   * </p>
   */
  public TubeRegistrationConfiguration(File configDir) {
    this.configDir = configDir;

    observedContraindications = new ArrayList<Contraindication>();
    askedContraindications = new ArrayList<Contraindication>();
    availableRemarks = new ArrayList<Remark>();
    infoMessages = new ArrayList<ConditionalMessage>();
  }

  public TubeRegistrationConfiguration() {
    this(null);
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
    this.resourceLoader = resourceLoader;
  }

  //
  // InitializingBean Methods
  //

  public void afterPropertiesSet() throws Exception {
    if(configDir != null) {
      String resourcePath = configDir.isAbsolute() ? "file:" + configDir.getPath() : configDir.getPath();

      configDir = resourceLoader.getResource(resourcePath).getFile();

      if(configDir.exists()) {
        initConfig();
      } else {
        throw new IllegalArgumentException("Invalid config directory: " + configDir);
      }
    }
  }

  //
  // Methods
  //

  /**
   * Loads (or re-loads) configuration information stored in the configuration directory.
   * 
   * If a <code>TubeRegistrationConfiguration</code> was created without specifying a configuration directory, calling
   * this method does nothing.
   * 
   * @throws IOException on an I/O error
   */
  public void initConfig() throws IOException {
    if(configDir != null) {
      File contraIndicationsFile = new File(configDir, "contra-indications.xml");
      if(contraIndicationsFile.exists()) {
        initContraindications(contraIndicationsFile);
      }

      File remarksFile = new File(configDir, "remarks.xml");
      if(remarksFile.exists()) {
        initRemarks(remarksFile);
      }

      File infoMessagesFile = new File(configDir, "info-messages.xml");
      if(infoMessagesFile.exists()) {
        initInfoMessages(infoMessagesFile);
      }
    }
  }

  public void setBarcodeStructure(BarcodeStructure barcodeStructure) {
    this.barcodeStructure = barcodeStructure;
  }

  public BarcodeStructure getBarcodeStructure() {
    return barcodeStructure;
  }

  public void setExpectedTubeCount(int expectedTubeCount) {
    this.expectedTubeCount = expectedTubeCount;
  }

  public int getExpectedTubeCount() {
    return expectedTubeCount;
  }

  public void setContraindications(List<Contraindication> contraIndications) {
    observedContraindications.clear();
    askedContraindications.clear();

    if(contraIndications != null) {
      for(Contraindication contraIndication : contraIndications) {
        if(contraIndication.getType().equals(Contraindication.Type.OBSERVED)) {
          observedContraindications.add(contraIndication);
        } else if(contraIndication.getType().equals(Contraindication.Type.ASKED)) {
          askedContraindications.add(contraIndication);
        }
      }
    }
  }

  public List<Contraindication> getObservedContraindications() {
    return Collections.unmodifiableList(observedContraindications);
  }

  public List<Contraindication> getAskedContraindications() {
    return Collections.unmodifiableList(askedContraindications);
  }

  public void setAvailableRemarks(List<Remark> remarks) {
    availableRemarks.clear();

    if(remarks != null) {
      availableRemarks.addAll(remarks);
    }
  }

  public List<Remark> getAvailableRemarks() {
    return Collections.unmodifiableList(availableRemarks);
  }

  public void setInfoMessages(List<ConditionalMessage> infoMessages) {
    this.infoMessages.clear();

    if(infoMessages != null) {
      this.infoMessages.addAll(infoMessages);
    }
  }

  public List<ConditionalMessage> getInfoMessages() {
    return Collections.unmodifiableList(infoMessages);
  }

  /**
   * Sets the list of contra-indications to the contents of the specified file.
   * 
   * @param contraIndicationsFile contra-indications file
   * @throws IOException on any I/O error
   */
  private void initContraindications(File contraIndicationsFile) throws IOException {
    FileInputStream fis = null;

    try {
      fis = new FileInputStream(contraIndicationsFile);

      XStream xstream = new XStream();
      xstream.alias("contraindications", LinkedList.class);
      xstream.alias("contraindication", Contraindication.class);

      List<Contraindication> contraIndications = (List<Contraindication>) xstream.fromXML(fis);
      setContraindications(contraIndications);

      log.info("Configured tube registration contra-indications ({} OBSERVED, {} ASKED)", new Object[] { getObservedContraindications().size(), getAskedContraindications().size() });
    } finally {
      if(fis != null) {
        fis.close();
      }
    }
  }

  /**
   * Sets the list of tube registration remarks to the contents of the specified file.
   * 
   * @param remarksFile remarks file
   * @throws IOException on any I/O error
   */
  private void initRemarks(File remarksFile) throws IOException {
    FileInputStream fis = null;

    try {
      fis = new FileInputStream(remarksFile);

      XStream xstream = new XStream();
      xstream.alias("remarks", LinkedList.class);
      xstream.alias("remark", Remark.class);

      List<Remark> remarks = (List<Remark>) xstream.fromXML(fis);
      setAvailableRemarks(remarks);

      log.info("Configured tube registration remarks ({})", new Object[] { getAvailableRemarks().size() });
    } finally {
      if(fis != null) {
        fis.close();
      }
    }
  }

  private void initInfoMessages(File infoMessagesFile) throws IOException {
    FileInputStream fis = null;

    try {
      fis = new FileInputStream(infoMessagesFile);

      XStream xstream = new XStream(new InjectingReflectionProviderWrapper((new XStream()).getReflectionProvider(), applicationContext));
      xstream.alias("info-messages", LinkedList.class);
      xstream.alias("message", ConditionalMessage.class);
      xstream.alias("arguments", LinkedList.class);
      xstream.alias("variableDataSource", VariableDataSource.class);
      xstream.alias("comparingDataSource", ComparingDataSource.class);
      xstream.useAttributeFor(ComparingDataSource.class, "comparisonOperator");
      xstream.alias("participantPropertyDataSource", ParticipantPropertyDataSource.class);
      xstream.useAttributeFor(AbstractBeanPropertyDataSource.class, "property");
      xstream.alias("fixedDataSource", FixedDataSource.class);
      xstream.useAttributeFor("type", DataType.class);
      xstream.useAttributeFor("dataType", DataType.class);
      xstream.alias("computingDataSource", ComputingDataSource.class);

      List<ConditionalMessage> infoMessages = (List<ConditionalMessage>) xstream.fromXML(fis);
      setInfoMessages(infoMessages);

      log.info("Configured tube registration info messages ({})", new Object[] { getInfoMessages().size() });
    } finally {
      if(fis != null) {
        fis.close();
      }
    }
  }
}

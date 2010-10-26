/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.etl.participant.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.core.io.Resource;

/**
 * 
 */
public abstract class AbstractParticipantReader implements ItemStreamReader<Participant> {

  private static final Logger log = LoggerFactory.getLogger(AbstractParticipantReader.class);

  //
  // Instance variables
  //
  private Resource inputDirectory;

  private ParticipantMetadata participantMetadata;

  private FileInputStream fileInputStream = null;

  // Maps Excel column names to attribute names.
  protected Map<String, String> columnNameToAttributeNameMap;

  // Maps attribute names to column indices.
  protected Map<String, Integer> attributeNameToColumnIndexMap;

  // Interface implemented methods
  public void open(ExecutionContext context) throws ItemStreamException {
    File currentFile = null;

    try {
      if(getInputDirectory() == null || getInputDirectory().getFile() == null) return;

      AppointmentUpdateLog.addErrorLog(context, new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.INFO, "Start updating appointments"));
      File[] appointmentFiles = getInputDirectory().getFile().listFiles(this.getFilter());

      if(appointmentFiles.length > 1) AppointmentUpdateLog.addErrorLog(context, new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.INFO, "Found " + appointmentFiles.length + " appointment lists. Will process the most recent one only and archive the others."));
      sortFilesOnDateAsc(appointmentFiles);

      currentFile = appointmentFiles[appointmentFiles.length - 1];
      AppointmentUpdateLog.addErrorLog(context, new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.INFO, "Processing appointment list file " + currentFile.getName()));

      context.put("fileName", currentFile.getName());
      fileInputStream = new FileInputStream(currentFile);

    } catch(IOException e) {
      String message = "Abort updating appointments: Reading file error: " + ((currentFile == null) ? "unknown file" : currentFile.getName()) + " - " + e.getMessage();
      AppointmentUpdateLog.addErrorLog(context, new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.ERROR, message));

      ValidationRuntimeException vex = new ValidationRuntimeException();
      vex.reject("ParticipantsListFileReadingError", new String[] { e.getMessage() }, "Reading file error: " + e.getMessage());
      throw vex;
    }
  }

  public void update(ExecutionContext context) throws ItemStreamException {
  }

  public void close() throws ItemStreamException {
    if(fileInputStream != null) {
      try {
        fileInputStream.close();
      } catch(IOException e) {
        // Ignored
      }
    }
  }

  //
  // Local methods
  //

  public FilenameFilter getFilter() {
    return (new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return AbstractParticipantReader.this.accept(dir, name);
      }
    });
  }

  public void sortFilesOnDateAsc(File[] appointmentFiles) {
    Arrays.sort(appointmentFiles, new Comparator<File>() {
      public int compare(File f1, File f2) {
        return (Long.valueOf(f1.lastModified()).compareTo(Long.valueOf(f2.lastModified())));
      }
    });
  }

  private void addDefaultColumnNameToAttributeNameMapEntries() {
    if(columnNameToAttributeNameMap == null) {
      columnNameToAttributeNameMap = new HashMap<String, String>();
    }

    // Set default mappings for essential attributes.
    for(ParticipantAttribute essentialAttribute : participantMetadata.getEssentialAttributes()) {
      if(!essentialAttribute.isAssignableAtEnrollment()) {
        continue;
      }

      String essentialAttributeName = essentialAttribute.getName();

      if(!columnNameToAttributeNameMap.containsValue(essentialAttributeName)) {
        columnNameToAttributeNameMap.put(essentialAttributeName.toUpperCase(), essentialAttributeName);
      }
    }

    // Set default mappings for configured attributes.
    for(ParticipantAttribute configuredAttribute : participantMetadata.getConfiguredAttributes()) {
      String configuredAttributeName = configuredAttribute.getName();

      if(!columnNameToAttributeNameMap.containsValue(configuredAttributeName)) {
        columnNameToAttributeNameMap.put(configuredAttributeName.toUpperCase(), configuredAttributeName);
      }
    }
  }

  protected boolean accept(File dir, String name) {
    return (name.toLowerCase().endsWith(getFilePattern()));
  }

  public abstract String getFilePattern();

  //
  // Common methods used in subclasses
  //
  protected void checkColumnsForMandatoryAttributesPresent() {
    List<ParticipantAttribute> allAttributes = new ArrayList<ParticipantAttribute>();
    allAttributes.addAll(participantMetadata.getEssentialAttributes());
    allAttributes.addAll(participantMetadata.getConfiguredAttributes());

    // Check that all attributes mandatory at enrollment are present.
    for(ParticipantAttribute attribute : allAttributes) {
      if(attribute.isMandatoryAtEnrollment()) {
        if(!attributeNameToColumnIndexMap.containsKey(attribute.getName().toUpperCase())) {
          throw new IllegalArgumentException("Invalid worksheet; no column exists for mandatory field '" + attribute.getName() + "'");
        }
      }
    }
  }

  //
  // Getters and setters
  //
  public void setInputDirectory(Resource inputDirectory) {
    this.inputDirectory = inputDirectory;
  }

  public void setParticipantMetadata(ParticipantMetadata participantMetadata) {
    this.participantMetadata = participantMetadata;

    if(participantMetadata != null) {
      addDefaultColumnNameToAttributeNameMapEntries();
    }
  }

  public Resource getInputDirectory() {
    return inputDirectory;
  }

  public FileInputStream getFileInputStream() {
    return fileInputStream;
  }

  public void setFileInputStream(FileInputStream fileInputStream) {
    this.fileInputStream = fileInputStream;
  }

  public void setColumnNameToAttributeNameMap(Map<String, String> columnNameToAttributeNameMap) {
    if(columnNameToAttributeNameMap != null) {
      // Add map entries to columnNameToAttributeNameMap. Convert all keys to UPPERCASE.
      Iterator<Map.Entry<String, String>> mapIter = columnNameToAttributeNameMap.entrySet().iterator();
      while(mapIter.hasNext()) {
        Map.Entry<String, String> mapEntry = mapIter.next();
        this.columnNameToAttributeNameMap.put(mapEntry.getKey().toUpperCase(), mapEntry.getValue());
      }
    }
  }

  /**
   * Set the column name to attribute name map with a configuration string.
   * 
   * @param keyValuePairs list of key/value pairs separated by a comma. For example, "<code>param1=foo,param2=bar</code>
   * ".
   */
  public void setColumnToAttribute(String keyValuePairs) {
    if(columnNameToAttributeNameMap != null) {
      // Get list of strings separated by the delimiter
      StringTokenizer tokenizer = new StringTokenizer(keyValuePairs, ",");
      while(tokenizer.hasMoreElements()) {
        String token = tokenizer.nextToken();
        String[] entry = token.split("=");
        if(entry.length == 2) {
          columnNameToAttributeNameMap.put(entry[0].toUpperCase().trim(), entry[1].trim());
        } else {
          log.error("Could not identify Participant column to attribute mapping: " + token);
        }
      }
    }
  }

  public ParticipantMetadata getParticipantMetadata() {
    return participantMetadata;
  }
}

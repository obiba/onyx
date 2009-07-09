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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.etl.participant.IParticipantReader;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractParticipantReader implements IParticipantReader {

  //
  // Constants
  //
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractParticipantReader.class);

  protected static final String ENROLLMENT_ID_ATTRIBUTE_NAME = "Enrollment ID";

  protected static final String ASSESSMENT_CENTER_ID_ATTRIBUTE_NAME = "Assessment Center ID";

  protected static final String FIRST_NAME_ATTRIBUTE_NAME = "First Name";

  protected static final String LAST_NAME_ATTRIBUTE_NAME = "Last Name";

  protected static final String BIRTH_DATE_ATTRIBUTE_NAME = "Birth Date";

  protected static final String GENDER_ATTRIBUTE_NAME = "Gender";

  protected static final String APPOINTMENT_TIME_ATTRIBUTE_NAME = "Appointment Time";

  //
  // Instance Variables
  //
  /**
   * Participant meta-data.
   */
  protected ParticipantMetadata participantMetadata;

  /**
   * Maps Excel column names to attribute names.
   */
  protected Map<String, String> columnNameToAttributeNameMap;

  /**
   * Maps attribute names to column indices.
   */
  protected Map<String, Integer> attributeNameToColumnIndexMap;

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

  protected void checkMandatoryCondition(ParticipantAttribute attribute, Data attributeValue) {
    if(attribute.isMandatoryAtEnrollment()) {
      if(attributeValue == null || attributeValue.getValue() == null) {
        throw new IllegalArgumentException("No value for mandatory field: " + attribute.getName());
      }
    }
  }

  protected void checkValueAllowed(ParticipantAttribute attribute, Data data) {
    Set<String> allowedValues = attribute.getAllowedValues();

    if(!allowedValues.isEmpty()) {
      String textValue = data.getValue();

      Iterator<String> iter = allowedValues.iterator();
      String matchingValue = null;

      while(iter.hasNext()) {
        String s = iter.next();
        if(s.equalsIgnoreCase(textValue)) {
          matchingValue = s;
          data.setValue(matchingValue);
          break;
        }
      }

      if(matchingValue == null) {
        if(attribute.isMandatoryAtEnrollment()) {
          throw new IllegalArgumentException("Value not allowed for field '" + attribute.getName() + "': " + textValue);
        } else {
          data.setValue("");
        }
      }
    }
  }

  protected void checkUniqueEnrollmentId(Set<String> enrollmentIds, String enrollmentId) {
    if(enrollmentIds.contains(enrollmentId)) {
      throw new IllegalArgumentException("Duplicate " + ENROLLMENT_ID_ATTRIBUTE_NAME);
    }

    enrollmentIds.add(enrollmentId);
  }

  public void setParticipantMetadata(ParticipantMetadata participantMetadata) {
    this.participantMetadata = participantMetadata;

    if(participantMetadata != null) {
      addDefaultColumnNameToAttributeNameMapEntries();
    }
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
   * Set he column name to attribute name map with a configuration string.
   * 
   * @param keyValuePairs list of key/value pairs separated by a comma. For example, "<code>param1=foo,param2=bar</code>".
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

}

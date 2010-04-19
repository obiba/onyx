/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.magma;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.obiba.core.service.EntityQueryService;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.beans.NoSuchBeanException;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.magma.AbstractOnyxBeanResolver;
import org.obiba.onyx.ruby.core.domain.BarcodePart;
import org.obiba.onyx.ruby.core.domain.BarcodeStructure;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.domain.parser.IBarcodePartParser;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ValueSetBeanResolver for Ruby-related beans (ParticipantTubeRegistration, RegisteredParticipantTube).
 */
public class TubeValueSetBeanResolver extends AbstractOnyxBeanResolver {
  //
  // Instance Variables
  //

  private Map<String, TubeRegistrationConfiguration> tubeRegistrationConfigurationMap;

  @Autowired
  private EntityQueryService queryService;

  //
  // AbstractOnyxBeanResolver Methods
  //

  public boolean resolves(Class<?> type) {
    return ParticipantTubeRegistration.class.equals(type) || RegisteredParticipantTube.class.equals(type) || BarcodePart.class.equals(type) || Contraindication.class.equals(type);
  }

  public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) throws NoSuchBeanException {
    Object bean = null;

    if(type.equals(ParticipantTubeRegistration.class)) {
      bean = resolveParticipantTubeRegistration(valueSet, variable);
    } else if(type.equals(RegisteredParticipantTube.class)) {
      bean = resolveRegisteredParticipantTubes(valueSet, variable);
    } else if(type.equals(BarcodePart.class)) {
      bean = resolveBarcodeParts(valueSet, variable);
    } else if(type.equals(Contraindication.class)) {
      bean = resolveContraindication(valueSet, variable);
    }

    return bean;
  }

  //
  // Methods
  //

  public void setTubeRegistrationConfigurationMap(Map<String, TubeRegistrationConfiguration> tubeRegistrationConfigurationMap) {
    this.tubeRegistrationConfigurationMap = tubeRegistrationConfigurationMap;
  }

  public void setQueryService(EntityQueryService queryService) {
    this.queryService = queryService;
  }

  protected ParticipantTubeRegistration resolveParticipantTubeRegistration(ValueSet valueSet, Variable variable) {
    ParticipantTubeRegistration tubeRegistration = null;

    String stageName = valueSet.getValueTable().getName();
    if(stageName != null) {
      Participant participant = getParticipant(valueSet);
      if(participant != null) {
        tubeRegistration = new ParticipantTubeRegistration();
        tubeRegistration.setInterview(participant.getInterview());
        tubeRegistration.setTubeSetName(stageName);
        tubeRegistration = queryService.matchOne(tubeRegistration);
      }
    }

    return tubeRegistration;
  }

  protected List<RegisteredParticipantTube> resolveRegisteredParticipantTubes(ValueSet valueSet, Variable variable) {
    List<RegisteredParticipantTube> registeredParticipantTubes = null;

    ParticipantTubeRegistration tubeRegistration = resolveParticipantTubeRegistration(valueSet, variable);
    if(tubeRegistration != null) {
      registeredParticipantTubes = tubeRegistration.getRegisteredParticipantTubes();

      // Return null if the list of RegisteredParticipantTubes is empty.
      if(registeredParticipantTubes.isEmpty()) {
        registeredParticipantTubes = null;
      }
    }

    return registeredParticipantTubes;
  }

  protected List<BarcodePart> resolveBarcodeParts(ValueSet valueSet, Variable variable) {
    List<BarcodePart> barcodeParts = null;

    String stageName = valueSet.getValueTable().getName();
    if(stageName != null) {
      String barcodePartName = extractBarcodePartName(variable.getName());
      if(barcodePartName != null) {
        List<RegisteredParticipantTube> registeredParticipantTubes = resolveRegisteredParticipantTubes(valueSet, variable);
        if(registeredParticipantTubes != null) {
          TubeRegistrationConfiguration tubeRegistrationConfiguration = tubeRegistrationConfigurationMap.get(stageName);
          BarcodeStructure barcodeStructure = tubeRegistrationConfiguration.getBarcodeStructure();
          List<IBarcodePartParser> partParsers = barcodeStructure.getParsers();

          barcodeParts = new ArrayList<BarcodePart>();

          for(RegisteredParticipantTube registeredParticipantTube : registeredParticipantTubes) {
            List<BarcodePart> currentBarcodeParts = barcodeStructure.parseBarcode(registeredParticipantTube.getBarcode());

            for(int i = 0; i < partParsers.size(); i++) {
              IBarcodePartParser partParser = partParsers.get(i);

              if(partParser.getVariableName() != null && partParser.getVariableName().equals(barcodePartName)) {
                barcodeParts.add(currentBarcodeParts.get(i));
                break;
              }
            }
          }
        }
      }
    }

    return barcodeParts;
  }

  protected Contraindication resolveContraindication(ValueSet valueSet, Variable variable) {
    String stageName = valueSet.getValueTable().getName();
    if(stageName != null) {
      ParticipantTubeRegistration tubeRegistration = resolveParticipantTubeRegistration(valueSet, variable);
      if(tubeRegistration != null) {
        tubeRegistration.setTubeRegistrationConfig(tubeRegistrationConfigurationMap.get(stageName));
        return tubeRegistration.getContraindication();
      }
    }
    return null;
  }

  private String extractBarcodePartName(String variableName) {
    String[] elements = variableName.split("\\.");
    if(elements.length != 0) {
      return elements[elements.length - 1];
    }

    return null;
  }
}

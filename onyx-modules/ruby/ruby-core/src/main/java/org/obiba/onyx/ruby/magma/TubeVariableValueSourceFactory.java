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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.Variable.Builder;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.magma.IdentifierAttributeVisitor;
import org.obiba.onyx.magma.OccurrenceCountAttributeVisitor;
import org.obiba.onyx.magma.OnyxAttributeHelper;
import org.obiba.onyx.magma.StageAttributeVisitor;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.Remark;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Factory for creating VariableValueSources for Ruby variables.
 */
public class TubeVariableValueSourceFactory implements VariableValueSourceFactory {
  //
  // Constants
  //

  public static final String PARTICIPANT_TUBE_REGISTRATION = "ParticipantTubeRegistration";

  public static final String REGISTERED_PARTICIPANT_TUBE = "RegisteredParticipantTube";

  public static final String CONTRAINDICATION = "Contraindication";

  //
  // Instance Variables
  //

  private OnyxAttributeHelper attributeHelper;

  private String stageName;

  private TubeRegistrationConfiguration tubeRegistrationConfiguration;

  private String variableRoot;

  //
  // Constructors
  //

  public TubeVariableValueSourceFactory(String stageName, TubeRegistrationConfiguration tubeRegistrationConfiguration) {
    this.stageName = stageName;
    this.tubeRegistrationConfiguration = tubeRegistrationConfiguration;
  }

  //
  // VariableValueSourceFactory Methods
  //

  public Set<VariableValueSource> createSources() {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    String prefix = (variableRoot != null) ? variableRoot + '.' + stageName : stageName;
    Variable.BuilderVisitor stageAttributeVisitor = new StageAttributeVisitor(stageName);

    sources.addAll(createParticipantTubeRegistrationSources(prefix, stageAttributeVisitor));
    sources.addAll(createRegisteredParticipantTubeSources(prefix, stageName, stageAttributeVisitor));

    return sources;
  }

  //
  // Methods
  //

  public void setAttributeHelper(OnyxAttributeHelper attributeHelper) {
    this.attributeHelper = attributeHelper;
  }

  public void setVariableRoot(String variableRoot) {
    this.variableRoot = variableRoot;
  }

  private Set<VariableValueSource> createParticipantTubeRegistrationSources(String prefix, Variable.BuilderVisitor stageAttributeVisitor) {
    String tubeRegistrationPrefix = prefix + '.' + PARTICIPANT_TUBE_REGISTRATION;

    // Create sources for participant tube registration variables.
    BeanVariableValueSourceFactory<ParticipantTubeRegistration> delegateFactory = new BeanVariableValueSourceFactory<ParticipantTubeRegistration>("Participant", ParticipantTubeRegistration.class);
    delegateFactory.setPrefix(tubeRegistrationPrefix);
    delegateFactory.setProperties(ImmutableSet.of("startTime", "endTime", "otherContraindication"));
    delegateFactory.setVariableBuilderVisitors(ImmutableSet.of(stageAttributeVisitor));
    Set<VariableValueSource> sources = delegateFactory.createSources();

    // Create source for contraindication code variable.
    String ciVariablePrefix = tubeRegistrationPrefix + '.' + CONTRAINDICATION;
    delegateFactory.setPrefix(ciVariablePrefix);
    delegateFactory.setProperties(ImmutableSet.of("contraindicationCode"));
    delegateFactory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("contraindicationCode", "code").build());
    sources.addAll(delegateFactory.createSources());

    // Create source for contraindication type variable.
    sources.add(createContraindicationTypeSource(ciVariablePrefix, stageAttributeVisitor));

    return sources;
  }

  private Set<VariableValueSource> createRegisteredParticipantTubeSources(String prefix, String stageName, Variable.BuilderVisitor stageAttributeVisitor) {
    String tubePrefix = prefix + '.' + REGISTERED_PARTICIPANT_TUBE;

    // Create sources for registered participant tube variables.
    BeanVariableValueSourceFactory<RegisteredParticipantTube> delegateFactory = new BeanVariableValueSourceFactory<RegisteredParticipantTube>("Participant", RegisteredParticipantTube.class);
    delegateFactory.setPrefix(tubePrefix);
    delegateFactory.setOccurrenceGroup(REGISTERED_PARTICIPANT_TUBE);
    delegateFactory.setProperties(ImmutableSet.of("barcode", "registrationTime", "comment"));
    delegateFactory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("type", "actionType").build());
    delegateFactory.setMappedPropertyType(new ImmutableMap.Builder<String, Class<?>>().put("remarkSelections", Boolean.class).build());
    delegateFactory.setVariableBuilderVisitors(ImmutableSet.of(stageAttributeVisitor, new LocalizedAttributeVisitor(), new OccurrenceCountAttributeVisitor(tubeRegistrationConfiguration.getExpectedTubeCount()), new IdentifierAttributeVisitor(tubePrefix + ".barcode")));
    Set<VariableValueSource> sources = delegateFactory.createSources();

    // Create sources for registered participant tube remarks.
    sources.addAll(createRemarkSources(tubePrefix, stageAttributeVisitor));

    // Create sources for barcode part variables.
    TubeBarcodePartVariableValueSourceFactory barcodePartFactory = new TubeBarcodePartVariableValueSourceFactory(tubeRegistrationConfiguration);
    barcodePartFactory.setPrefix(tubePrefix);
    barcodePartFactory.setOccurrenceGroup(REGISTERED_PARTICIPANT_TUBE);
    barcodePartFactory.setVariableBuilderVisitors(ImmutableSet.of(stageAttributeVisitor, new IdentifierAttributeVisitor((String[]) barcodePartFactory.getKeyVariableNames().toArray(new String[0]))));
    sources.addAll(barcodePartFactory.createSources());

    return sources;
  }

  private VariableValueSource createContraindicationTypeSource(String prefix, Variable.BuilderVisitor stageAttributeVisitor) {
    BeanVariableValueSourceFactory<Contraindication> delegateFactory = new BeanVariableValueSourceFactory<Contraindication>("Participant", Contraindication.class);
    delegateFactory.setPrefix(prefix);
    delegateFactory.setProperties(ImmutableSet.of("type"));
    delegateFactory.setVariableBuilderVisitors(ImmutableSet.of(stageAttributeVisitor));

    return delegateFactory.createSources().iterator().next();
  }

  private Set<VariableValueSource> createRemarkSources(String tubePrefix, Variable.BuilderVisitor stageAttributeVisitor) {
    List<String> remarkProperties = Arrays.asList(getRemarkProperties(stageName));

    ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>();
    for(int i = 0; i < remarkProperties.size(); i++) {
      String qualifiedRemarkCode = tubeRegistrationConfiguration.getAvailableRemarks().get(i).getCode();
      int lastSeparatorIndex = qualifiedRemarkCode.lastIndexOf('.');
      String remarkCode = (lastSeparatorIndex != -1) ? qualifiedRemarkCode.substring(lastSeparatorIndex + 1) : qualifiedRemarkCode;

      builder.put(remarkProperties.get(i), remarkCode);
    }
    ImmutableMap<String, String> propertyNameToVariableNameMap = builder.build();

    // Create sources for registered participant tube variables.
    BeanVariableValueSourceFactory<RegisteredParticipantTube> delegateFactory = new BeanVariableValueSourceFactory<RegisteredParticipantTube>("Participant", RegisteredParticipantTube.class);
    delegateFactory.setPrefix(tubePrefix + '.' + "remark");
    delegateFactory.setOccurrenceGroup(REGISTERED_PARTICIPANT_TUBE);
    delegateFactory.setProperties(ImmutableSet.copyOf(remarkProperties.iterator()));
    delegateFactory.setPropertyNameToVariableName(propertyNameToVariableNameMap);
    delegateFactory.setMappedPropertyType(new ImmutableMap.Builder<String, Class<?>>().put("remarkSelections", Boolean.class).build());
    delegateFactory.setVariableBuilderVisitors(ImmutableSet.of(stageAttributeVisitor, new LocalizedAttributeVisitor()));

    return delegateFactory.createSources();
  }

  private String[] getRemarkProperties(String stageName) {
    List<Remark> availableRemarks = tubeRegistrationConfiguration.getAvailableRemarks();

    String[] remarkProperties = new String[availableRemarks.size()];
    for(int i = 0; i < availableRemarks.size(); i++) {
      remarkProperties[i] = "remarkSelections[" + availableRemarks.get(i).getCode() + "]";
    }

    return remarkProperties;
  }

  //
  // Inner Classes
  //

  private class LocalizedAttributeVisitor implements Variable.BuilderVisitor {
    public void visit(Builder builder) {
      if(builder.isName("barcode")) {
        attributeHelper.addLocalizedAttributes(builder, "Ruby.Barcode");
      } else if(builder.isName("comment")) {
        attributeHelper.addLocalizedAttributes(builder, "Ruby.Comment");
      }
    }
  }
}

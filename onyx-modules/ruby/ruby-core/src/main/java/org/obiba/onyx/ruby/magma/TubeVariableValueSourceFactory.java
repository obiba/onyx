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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.Variable.Builder;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.beans.ValueSetBeanResolver;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.magma.IdentifierAttributeVisitor;
import org.obiba.onyx.magma.OccurrenceCountAttributeVisitor;
import org.obiba.onyx.magma.OnyxAttributeHelper;
import org.obiba.onyx.magma.StageAttributeVisitor;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.Remark;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

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

  private TubeValueSetBeanResolver beanResolver;

  private OnyxAttributeHelper attributeHelper;

  @Autowired(required = true)
  private Map<String, TubeRegistrationConfiguration> tubeRegistrationConfigurationMap;

  private String variableRoot;

  private List<Stage> stages;

  //
  // VariableValueSourceFactory Methods
  //

  public Set<VariableValueSource> createSources(String collection) {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    for(Stage stage : stages) {
      String prefix = (variableRoot != null) ? variableRoot + '.' + stage.getName() : stage.getName();
      Variable.BuilderVisitor stageAttributeVisitor = new StageAttributeVisitor(stage.getName());

      sources.addAll(createParticipantTubeRegistrationSources(collection, prefix, stageAttributeVisitor));
      sources.addAll(createRegisteredParticipantTubeSources(collection, prefix, stage.getName(), stageAttributeVisitor));
    }

    return sources;
  }

  //
  // Methods
  //

  public void setBeanResolver(TubeValueSetBeanResolver beanResolver) {
    this.beanResolver = beanResolver;
  }

  public void setAttributeHelper(OnyxAttributeHelper attributeHelper) {
    this.attributeHelper = attributeHelper;
  }

  public void setVariableRoot(String variableRoot) {
    this.variableRoot = variableRoot;
  }

  public String getVariableRoot() {
    return variableRoot;
  }

  public void setStage(List<Stage> stages) {
    this.stages = stages;
  }

  private Set<VariableValueSource> createParticipantTubeRegistrationSources(String collection, String prefix, Variable.BuilderVisitor stageAttributeVisitor) {
    String tubeRegistrationPrefix = prefix + '.' + PARTICIPANT_TUBE_REGISTRATION;

    // Create sources for participant tube registration variables.
    BeanVariableValueSourceFactory<ParticipantTubeRegistration> delegateFactory = new BeanVariableValueSourceFactory<ParticipantTubeRegistration>("Participant", ParticipantTubeRegistration.class);
    delegateFactory.setPrefix(tubeRegistrationPrefix);
    delegateFactory.setProperties(ImmutableSet.of("startTime", "endTime", "otherContraindication"));
    delegateFactory.setVariableBuilderVisitors(ImmutableSet.of(stageAttributeVisitor));
    Set<VariableValueSource> sources = delegateFactory.createSources(collection, beanResolver);

    // Add source for contraindication code variable.
    String ciVariablePrefix = tubeRegistrationPrefix + '.' + CONTRAINDICATION;
    delegateFactory.setPrefix(ciVariablePrefix);
    delegateFactory.setProperties(ImmutableSet.of("contraindicationCode"));
    delegateFactory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("contraindicationCode", "code").build());
    sources.addAll(delegateFactory.createSources(collection, beanResolver));

    // Add source for contraindication type variable.
    sources.add(createContraindicationTypeSource(collection, ciVariablePrefix, stageAttributeVisitor, beanResolver));

    return sources;
  }

  private Set<VariableValueSource> createRegisteredParticipantTubeSources(String collection, String prefix, String stageName, Variable.BuilderVisitor stageAttributeVisitor) {
    String tubePrefix = prefix + '.' + REGISTERED_PARTICIPANT_TUBE;

    List<String> tubeProperties = new ArrayList<String>();
    tubeProperties.add("barcode");
    tubeProperties.add("registrationTime");
    tubeProperties.add("comment");

    String[] remarkProperties = getRemarkProperties(stageName);
    tubeProperties.addAll(Arrays.asList(remarkProperties));

    // Create sources for registered participant tube variables.
    BeanVariableValueSourceFactory<RegisteredParticipantTube> delegateFactory = new BeanVariableValueSourceFactory<RegisteredParticipantTube>("Participant", RegisteredParticipantTube.class);
    delegateFactory.setPrefix(tubePrefix);
    delegateFactory.setOccurrenceGroup(REGISTERED_PARTICIPANT_TUBE);
    delegateFactory.setProperties(ImmutableSet.copyOf(tubeProperties.iterator()));
    delegateFactory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("type", "actionType").build());
    delegateFactory.setMappedPropertyType(getMappedRemarkPropertyType(remarkProperties));
    delegateFactory.setVariableBuilderVisitors(ImmutableSet.of(stageAttributeVisitor, new LocalizedAttributeVisitor(), new OccurrenceCountAttributeVisitor(getExpectedTubeCount(stageName))));
    Set<VariableValueSource> sources = delegateFactory.createSources(collection, beanResolver);

    // Add sources for barcode part variables.
    TubeBarcodePartVariableValueSourceFactory barcodePartFactory = new TubeBarcodePartVariableValueSourceFactory();
    barcodePartFactory.setPrefix(tubePrefix);
    barcodePartFactory.setVariableBuilderVisitors(ImmutableSet.of(stageAttributeVisitor, new IdentifierAttributeVisitor((String[]) barcodePartFactory.getKeyVariableNames().toArray())));
    sources.addAll(barcodePartFactory.createSources(collection, beanResolver));

    return sources;
  }

  private VariableValueSource createContraindicationTypeSource(String collection, String prefix, Variable.BuilderVisitor stageAttributeVisitor, ValueSetBeanResolver resolver) {
    BeanVariableValueSourceFactory<Contraindication> delegateFactory = new BeanVariableValueSourceFactory<Contraindication>("Participant", Contraindication.class);
    delegateFactory.setPrefix(prefix);
    delegateFactory.setProperties(ImmutableSet.of("type"));
    delegateFactory.setVariableBuilderVisitors(ImmutableSet.of(stageAttributeVisitor));

    return delegateFactory.createSources(collection, resolver).iterator().next();
  }

  private String[] getRemarkProperties(String stageName) {
    TubeRegistrationConfiguration tubeRegistrationConfiguration = tubeRegistrationConfigurationMap.get(stageName);
    List<Remark> availableRemarks = tubeRegistrationConfiguration.getAvailableRemarks();

    String[] remarkProperties = new String[availableRemarks.size()];
    for(int i = 0; i < availableRemarks.size(); i++) {
      remarkProperties[i] = "remarks" + '.' + availableRemarks.get(i).getCode();
    }

    return remarkProperties;
  }

  private Map<String, Class<?>> getMappedRemarkPropertyType(String[] remarkProperties) {
    Map<String, Class<?>> mappedPropertyType = new HashMap<String, Class<?>>();
    for(String remarkProperty : remarkProperties) {
      mappedPropertyType.put(remarkProperty, String.class);
    }
    return mappedPropertyType;
  }

  private int getExpectedTubeCount(String stageName) {
    TubeRegistrationConfiguration tubeRegistrationConfiguration = tubeRegistrationConfigurationMap.get(stageName);
    return tubeRegistrationConfiguration.getExpectedTubeCount();
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
      } else if(builder.isName("remarks")) {
        attributeHelper.addLocalizedAttributes(builder, "Ruby.Remark");
      }
    }
  }
}

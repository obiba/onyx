/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.magma;

import java.util.HashSet;
import java.util.Set;

import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.beans.BeanPropertyVariableValueSource;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;

import com.google.common.collect.ImmutableSet;

/**
 * VariableValueSourceFactory for configured participant attributes.
 */
public class ParticipantBeanVariableValueSourceFactory extends BeanVariableValueSourceFactory<Participant> {
  //
  // Instance Variables
  //

  private OnyxAttributeHelper attributeHelper;

  private ParticipantMetadata participantMetadata;

  //
  // Constructors
  //

  public ParticipantBeanVariableValueSourceFactory() {
    super("Participant", Participant.class);
  }

  //
  // BeanVariableValueSourceFactory Methods
  //

  @Override
  public Set<VariableValueSource> createSources(String collection) {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    for(ParticipantAttribute attribute : participantMetadata.getConfiguredAttributes()) {
      String variableName = lookupVariableName(attribute.getName());
      setVariableBuilderVisitors(ImmutableSet.of(new ParticipantVariableBuilderVisitor(attribute)));

      ValueType valueType = ValueType.Factory.forName(attribute.getType().toString().toLowerCase());
      Variable variable = this.doBuildVariable(collection, valueType.getJavaClass(), variableName);

      sources.add(new BeanPropertyVariableValueSource(variable, Participant.class, "attributes[" + attribute.getName() + "]"));
    }

    return sources;
  }

  //
  // Methods
  //

  public void setAttributeHelper(OnyxAttributeHelper attributeHelper) {
    this.attributeHelper = attributeHelper;
  }

  public void setParticipantMetadata(ParticipantMetadata participantMetadata) {
    this.participantMetadata = participantMetadata;
  }

  //
  // Inner Classes
  //

  private class ParticipantVariableBuilderVisitor implements Variable.BuilderVisitor {
    private ParticipantAttribute attribute;

    public ParticipantVariableBuilderVisitor(ParticipantAttribute attribute) {
      this.attribute = attribute;
    }

    public void visit(Variable.Builder builder) {
      // Add localized attributes.
      attributeHelper.addLocalizedAttributes(builder, lookupVariableName(attribute.getName()));

      // If the participant attribute is part of a group, add a group attribute.
      if(attribute.getGroup() != null) {
        OnyxAttributeHelper.addGroupAttribute(builder, attribute.getGroup().getName());
      }

      // Add "pii" attribute (stands for "personally identifiable information").
      OnyxAttributeHelper.addAttribute(builder, "pii", "true");
    }
  }
}

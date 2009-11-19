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
import org.obiba.magma.beans.ValueSetBeanResolver;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * VariableValueSourceFactory for configured participant attributes.
 */
public class ParticipantBeanVariableValueSourceFactory extends BeanVariableValueSourceFactory<Participant> {
  //
  // Instance Variables
  //

  @Autowired(required = true)
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
  public Set<VariableValueSource> createSources(String collection, ValueSetBeanResolver resolver) {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    for(ParticipantAttribute attribute : participantMetadata.getConfiguredAttributes()) {
      ValueType valueType = ValueType.Factory.forName(attribute.getType().toString().toLowerCase());
      Variable variable = this.doBuildVariable(collection, valueType.getJavaClass(), lookupVariableName(attribute.getName()));

      sources.add(new BeanPropertyVariableValueSource(variable, Participant.class, resolver, "attributes[" + attribute.getName() + "]"));

      // TODO: Need to modify VariableHelper (make it Magma-based) and use it here to add localized attributes.
    }

    return sources;
  }

  //
  // Methods
  //

  public void setParticipantMetadata(ParticipantMetadata participantMetadata) {
    this.participantMetadata = participantMetadata;
  }
}

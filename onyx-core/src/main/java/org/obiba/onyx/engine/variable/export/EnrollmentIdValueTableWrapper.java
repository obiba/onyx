/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.export;

import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.transform.BijectiveFunction;
import org.obiba.magma.views.AbstractTransformingValueTableWrapper;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.magma.MagmaInstanceProvider;

/**
 * A {@code ValueTableWrapper} that externally, exposes entities with {@code Participant#getBarcode()} as their
 * identifier and internally uses {@code Participant#getEnrollmentId()}. This can be used to export participants using
 * their {@code enrollmentId} instead of their {@code barcode}.
 */
class EnrollmentIdValueTableWrapper extends AbstractTransformingValueTableWrapper {

  private final ParticipantIdToEnrollmentIdFunction function;

  private final ValueTable wrappedTable;

  EnrollmentIdValueTableWrapper(ParticipantService participantService, ValueTable wrappedTable) {
    if(participantService == null) throw new IllegalArgumentException("participantService cannot be null");
    if(wrappedTable == null) throw new IllegalArgumentException("wrappedTable cannot be null");

    this.function = new ParticipantIdToEnrollmentIdFunction(participantService);
    this.wrappedTable = wrappedTable;
  }

  @Override
  public ValueTable getWrappedValueTable() {
    return wrappedTable;
  }

  @Override
  public BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction() {
    return function;
  }

  static class ParticipantIdToEnrollmentIdFunction implements BijectiveFunction<VariableEntity, VariableEntity> {

    private final ParticipantService participantService;

    ParticipantIdToEnrollmentIdFunction(ParticipantService participantService) {
      this.participantService = participantService;
    }

    @Override
    public VariableEntity apply(VariableEntity input) {
      return new VariableEntityBean(MagmaInstanceProvider.PARTICIPANT_ENTITY_TYPE, fromBarcode(input).getEnrollmentId());
    }

    @Override
    public VariableEntity unapply(VariableEntity from) {
      return new VariableEntityBean(MagmaInstanceProvider.PARTICIPANT_ENTITY_TYPE, fromEnrollmentId(from).getBarcode());
    }

    private Participant fromBarcode(VariableEntity input) {
      Participant p = new Participant();
      p.setBarcode(input.getIdentifier());
      return fromTemplate(p);
    }

    private Participant fromEnrollmentId(VariableEntity input) {
      Participant p = new Participant();
      p.setEnrollmentId(input.getIdentifier());
      return fromTemplate(p);
    }

    private Participant fromTemplate(Participant template) {
      Participant one = participantService.getParticipant(template);
      if(one == null) {
        throw new NoSuchVariableException("");
      }
      return one;
    }

  }

}

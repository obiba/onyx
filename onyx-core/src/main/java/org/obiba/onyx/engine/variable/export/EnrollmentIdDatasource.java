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

import java.io.IOException;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.AbstractDatasourceWrapper;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.variable.export.EnrollmentIdValueTableWrapper.ParticipantIdToEnrollmentIdFunction;
import org.obiba.onyx.magma.MagmaInstanceProvider;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Wraps another {@code Datasource} instance that uses the participant's {@code enrollmentId} instead of the
 * {@code barcode} as the entity identifier. For users of this datasource, the identifier is the {@code barcode} and
 * uses the {@code enrollmentId} internally. This is only the case for entities of type {@code Participant}, other
 * entities are not affected.
 */
public class EnrollmentIdDatasource extends AbstractDatasourceWrapper {

  private final ParticipantService participantService;

  public EnrollmentIdDatasource(ParticipantService participantService, Datasource datasource) {
    super(datasource);
    if(participantService == null) throw new IllegalArgumentException("participantService cannot be null");

    this.participantService = participantService;
  }

  @Override
  public ValueTableWriter createWriter(final String tableName, final String entityType) {
    ValueTableWriter wrappedWriter = getWrappedDatasource().createWriter(tableName, entityType);
    if(isParticipantTable(entityType)) {
      return new EnrollmentIdValueTableWriter(wrappedWriter);
    }
    return wrappedWriter;
  }

  @Override
  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    ValueTable table = getWrappedDatasource().getValueTable(name);
    if(isParticipantTable(table)) {
      return new EnrollmentIdValueTableWrapper(participantService, table);
    }
    return table;
  }

  @Override
  public Set<ValueTable> getValueTables() {
    return ImmutableSet.copyOf(Iterables.transform(getWrappedDatasource().getValueTables(), new Function<ValueTable, ValueTable>() {

      @Override
      public ValueTable apply(ValueTable from) {
        if(isParticipantTable(from)) {
          return new EnrollmentIdValueTableWrapper(participantService, from);
        }
        return from;
      }
    }));
  }

  private boolean isParticipantTable(ValueTable table) {
    return isParticipantTable(table.getEntityType());
  }

  private boolean isParticipantTable(String entityType) {
    return MagmaInstanceProvider.PARTICIPANT_ENTITY_TYPE.equals(entityType);
  }

  private final class EnrollmentIdValueTableWriter implements ValueTableWriter {

    private final ValueTableWriter wrappedWriter;

    private final ParticipantIdToEnrollmentIdFunction function = new ParticipantIdToEnrollmentIdFunction(participantService);

    private EnrollmentIdValueTableWriter(ValueTableWriter wrappedWriter) {
      this.wrappedWriter = wrappedWriter;
    }

    @Override
    public void close() throws IOException {
      wrappedWriter.close();
    }

    @Override
    public VariableWriter writeVariables() {
      return wrappedWriter.writeVariables();
    }

    @Override
    public ValueSetWriter writeValueSet(final VariableEntity entity) {
      return wrappedWriter.writeValueSet(function.unapply(entity));
    }
  }

}

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
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.obiba.magma.Attribute;
import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.VariableEntity;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.variable.export.EnrollmentIdValueTableWrapper.ParticipantIdToEnrollmentIdFunction;
import org.obiba.onyx.magma.MagmaInstanceProvider;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class EnrollmentIdDatasource implements Datasource {

  private final ParticipantService participantService;

  private final Datasource datasource;

  public EnrollmentIdDatasource(ParticipantService participantService, Datasource datasource) {
    if(participantService == null) throw new IllegalArgumentException("participantService cannot be null");
    if(datasource == null) throw new IllegalArgumentException("datasource cannot be null");

    this.participantService = participantService;
    this.datasource = datasource;
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
  public String getName() {
    return getWrappedDatasource().getName();
  }

  @Override
  public String getType() {
    return getWrappedDatasource().getType();
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

  @Override
  public boolean hasValueTable(String name) {
    return getWrappedDatasource().hasValueTable(name);
  }

  @Override
  public void setAttributeValue(String name, Value value) {
    getWrappedDatasource().setAttributeValue(name, value);
  }

  @Override
  public void initialise() {
    getWrappedDatasource().initialise();
  }

  @Override
  public void dispose() {
    getWrappedDatasource().dispose();
  }

  @Override
  public Attribute getAttribute(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttribute(name);
  }

  @Override
  public Attribute getAttribute(String name, Locale locale) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttribute(name, locale);
  }

  @Override
  public String getAttributeStringValue(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributeStringValue(name);
  }

  @Override
  public Value getAttributeValue(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributeValue(name);
  }

  @Override
  public List<Attribute> getAttributes(String name) throws NoSuchAttributeException {
    return getWrappedDatasource().getAttributes(name);
  }

  @Override
  public List<Attribute> getAttributes() {
    return getWrappedDatasource().getAttributes();
  }

  @Override
  public boolean hasAttribute(String name) {
    return getWrappedDatasource().hasAttribute(name);
  }

  @Override
  public boolean hasAttribute(String name, Locale locale) {
    return getWrappedDatasource().hasAttribute(name, locale);
  }

  @Override
  public boolean hasAttributes() {
    return getWrappedDatasource().hasAttributes();
  }

  @Override
  public boolean canDropTable(String name) {
    return getWrappedDatasource().canDropTable(name);
  }

  public void dropTable(String name) {
    getWrappedDatasource().dropTable(name);
  }

  protected Datasource getWrappedDatasource() {
    return this.datasource;
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

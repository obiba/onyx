/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.magma;

import java.util.Map;
import java.util.Set;

import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.onyx.magma.MagmaInstanceProvider;
import org.obiba.onyx.magma.StageAttributeVisitor;
import org.obiba.onyx.marble.domain.consent.Consent;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Factory for creating VariableValueSources for all Consent variables.
 */
public class ConsentVariableValueSourceFactory extends BeanVariableValueSourceFactory<Consent> {

  private String stageName;

  private Map<String, String> variableToFieldMap;

  public ConsentVariableValueSourceFactory(String stageName) {
    super(MagmaInstanceProvider.PARTICIPANT_ENTITY_TYPE, Consent.class);
    this.stageName = stageName;
  }

  //
  // BeanVariableValueSourceFactory Methods
  //

  @Override
  public Set<VariableValueSource> createSources() {
    Set<VariableValueSource> sources = null;

    ImmutableSet.Builder<Variable.BuilderVisitor> visitorSetBuilder = new ImmutableSet.Builder<Variable.BuilderVisitor>();
    visitorSetBuilder.add(new StageAttributeVisitor(stageName), new PdfMimeTypeAttributeVisitor());
    Set<Variable.BuilderVisitor> visitors = visitorSetBuilder.build();

    // Create the non-PDF form sources.
    setProperties(ImmutableSet.of("mode", "locale", "accepted", "pdfForm", "timeStart", "timeEnd"));
    setVariableBuilderVisitors(visitors);
    sources = super.createSources();

    // Create the PDF form sources.
    sources.addAll(createPdfFormFieldSources(visitors));

    return sources;
  }

  //
  // Methods
  //

  public void setVariableToFieldMap(Map<String, String> variableToFieldMap) {
    this.variableToFieldMap = variableToFieldMap;
  }

  private Set<VariableValueSource> createPdfFormFieldSources(Set<Variable.BuilderVisitor> visitors) {
    ImmutableSet.Builder<String> propertySetBuilder = new ImmutableSet.Builder<String>();
    ImmutableMap.Builder<String, String> propertyNameToVariableNameMapBuilder = new ImmutableMap.Builder<String, String>();

    for(Map.Entry<String, String> entry : variableToFieldMap.entrySet()) {
      String propertyName = "pdfFormFields[" + entry.getValue() + "]";
      propertySetBuilder.add(propertyName);
      propertyNameToVariableNameMapBuilder.put(propertyName, entry.getKey());
    }

    BeanVariableValueSourceFactory<Consent> factory = new BeanVariableValueSourceFactory<Consent>(MagmaInstanceProvider.PARTICIPANT_ENTITY_TYPE, Consent.class);
    factory.setProperties(propertySetBuilder.build());
    factory.setPropertyNameToVariableName(propertyNameToVariableNameMapBuilder.build());
    factory.setMappedPropertyType(new ImmutableMap.Builder<String, Class<?>>().put("pdfFormFields", String.class).build());
    factory.setVariableBuilderVisitors(visitors);

    return factory.createSources();
  }

  private static final class PdfMimeTypeAttributeVisitor implements Variable.BuilderVisitor {

    @Override
    public void visit(Variable.Builder builder) {
      if(builder.isName("pdfForm")) builder.mimeType("application/pdf");
    }
  }

}

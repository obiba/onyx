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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.beans.BeanPropertyVariableValueSource;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.beans.ValueSetBeanResolver;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.magma.StageAttributeVisitor;
import org.obiba.onyx.marble.domain.consent.Consent;

import com.google.common.collect.ImmutableSet;

/**
 * Factory for creating VariableValueSources for all Consent variables.
 */
public class ConsentVariableValueSourceFactory extends BeanVariableValueSourceFactory<Consent> {
  //
  // Instance Variables
  //

  private String stageName;

  private Map<String, String> variableToFieldMap;

  //
  // Constructors
  //

  public ConsentVariableValueSourceFactory(String stageName) {
    super("Participant", Consent.class);
    this.stageName = stageName;
  }

  //
  // BeanVariableValueSourceFactory Methods
  //

  @Override
  public Set<VariableValueSource> createSources(String collection, ValueSetBeanResolver resolver) {
    Set<VariableValueSource> sources = null;

    setProperties(ImmutableSet.of("mode", "locale", "accepted", "pdfForm", "timeStart", "timeEnd"));

    setPrefix(stageName);
    setVariableBuilderVisitors(ImmutableSet.of(new StageAttributeVisitor(stageName)));

    // Call superclass method to create the non-PDF form sources.
    sources = super.createSources(collection, resolver);

    // Add PDF form sources (if applicable).
    sources.addAll(createPdfFormFieldSources(collection, resolver));

    return sources;
  }

  //
  // Methods
  //

  public void setVariableToFieldMap(Map<String, String> variableToFieldMap) {
    this.variableToFieldMap = variableToFieldMap;
  }

  private Set<VariableValueSource> createPdfFormFieldSources(String collection, ValueSetBeanResolver resolver) {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    for(Map.Entry<String, String> entry : variableToFieldMap.entrySet()) {
      Variable variable = this.doBuildVariable(collection, TextType.get().getJavaClass(), lookupVariableName(entry.getKey()));
      sources.add(new BeanPropertyVariableValueSource(variable, Consent.class, resolver, "pdfFormFields[" + entry.getValue() + "]"));
    }

    return sources;
  }
}

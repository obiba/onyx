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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.beans.BeanPropertyVariableValueSource;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.beans.ValueSetBeanResolver;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.magma.StageAttributeVisitor;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * Factory for creating VariableValueSources for all Consent variables.
 */
public class ConsentVariableValueSourceFactory extends BeanVariableValueSourceFactory<Consent> {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(ConsentVariableValueSourceFactory.class);

  //
  // Instance Variables
  //

  // TODO: Inject the Consent stages or stage names (actually, there is only one!).
  private List<Stage> stages;

  private Map<String, String> variableToFieldMap;

  //
  // Constructors
  //

  public ConsentVariableValueSourceFactory() {
    super("Participant", Consent.class);
  }

  //
  // BeanVariableValueSourceFactory Methods
  //

  @Override
  public Set<VariableValueSource> createSources(String collection, ValueSetBeanResolver resolver) {
    Set<VariableValueSource> sources = null;

    setProperties(ImmutableSet.of("mode", "locale", "accepted", "pdfForm", "timeStart", "timeEnd"));

    for(Stage stage : stages) {
      setPrefix(stage.getName());
      setVariableBuilderVisitors(ImmutableSet.of(new StageAttributeVisitor(stage.getName())));

      // Call superclass method to create the non-PDF form sources.
      sources = super.createSources(collection, resolver);

      // Add PDF form sources (if applicable).
      sources.addAll(createPdfFormFieldSources(collection, resolver));
    }

    return sources;
  }

  //
  // Methods
  //

  public void setStages(List<Stage> stages) {
    this.stages = stages;
  }

  public void setVariableToFieldMap(String keyValuePairs) {
    variableToFieldMap = new HashMap<String, String>();
    // Get list of strings separated by the delimiter
    StringTokenizer tokenizer = new StringTokenizer(keyValuePairs, ",");
    while(tokenizer.hasMoreElements()) {
      String token = tokenizer.nextToken();
      String[] entry = token.split("=");
      if(entry.length == 2) {
        variableToFieldMap.put(entry[0].trim(), entry[1].trim());
      } else {
        log.error("Could not identify PDF field name to variable path mapping: " + token);
      }
    }
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

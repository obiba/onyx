package org.obiba.onyx.magma;

import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;

public class PrebuiltVariableValueSourceFactory implements VariableValueSourceFactory {
  //
  // Instance Variables
  //

  private Set<VariableValueSource> sources = new LinkedHashSet<VariableValueSource>();

  //
  // VariableValueSourceFactory
  //

  @Override
  public Set<VariableValueSource> createSources() {
    return sources;
  }

  //
  // Methods
  //

  public PrebuiltVariableValueSourceFactory addVariableValueSource(VariableValueSource variableValueSource) {
    sources.add(variableValueSource);
    return this;
  }

  public PrebuiltVariableValueSourceFactory addVariableValueSources(Set<VariableValueSource> variableValueSources) {
    sources.addAll(variableValueSources);
    return this;
  }
}

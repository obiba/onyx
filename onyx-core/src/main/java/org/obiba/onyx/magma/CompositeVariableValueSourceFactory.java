package org.obiba.onyx.magma;

import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;

public class CompositeVariableValueSourceFactory implements VariableValueSourceFactory {
  //
  // Instance Variables
  //
  
  private Set<VariableValueSourceFactory> factories = new LinkedHashSet<VariableValueSourceFactory>();
  
  //
  // VariableValueSourceFactory Methods
  //
  
  @Override
  public Set<VariableValueSource> createSources() {
    Set<VariableValueSource> sources = new LinkedHashSet<VariableValueSource>();
    for (VariableValueSourceFactory factory : factories) {
      sources.addAll(factory.createSources());
    }
    return sources;
  }

  //
  // Methods
  //
  
  public CompositeVariableValueSourceFactory addFactory(VariableValueSourceFactory factory) {
    factories.add(factory);
    return this;
  }
}

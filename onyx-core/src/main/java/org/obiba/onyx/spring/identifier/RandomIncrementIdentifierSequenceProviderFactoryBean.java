/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.spring.identifier;

import org.obiba.onyx.core.identifier.IdentifierSequenceProvider;
import org.obiba.onyx.core.identifier.NullIdentifierSequenceProvider;
import org.obiba.onyx.core.identifier.impl.randomincrement.RandomIncrementIdentifierSequenceProvider;
import org.springframework.beans.factory.FactoryBean;

/**
 * Factory bean for creating a {@link RandomIncrementIdentifierSequenceProvider}.
 * 
 * If <code>useSequence</code> is <code>false</code>, creates a {@link NullIdentifierSequenceProvider} instead.
 */
public class RandomIncrementIdentifierSequenceProviderFactoryBean implements FactoryBean {
  //
  // Instance Variables
  //

  private IdentifierSequenceProvider sequenceProvider;

  private boolean useSequence;

  private int maxIncrement;

  //
  // FactoryBean Methods
  //

  @SuppressWarnings("unchecked")
  public Class getObjectType() {
    return IdentifierSequenceProvider.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public Object getObject() throws Exception {
    if(sequenceProvider == null) {
      sequenceProvider = createSequenceProvider();
    }

    return sequenceProvider;
  }

  //
  // Methods
  //

  public void setUseSequence(boolean useSequence) {
    this.useSequence = useSequence;
  }

  public void setMaxIncrement(int maxIncrement) {
    this.maxIncrement = maxIncrement;
  }

  private IdentifierSequenceProvider createSequenceProvider() {
    return useSequence ? new RandomIncrementIdentifierSequenceProvider(maxIncrement) : new NullIdentifierSequenceProvider();
  }
}

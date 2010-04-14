/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.identifier.impl.randomincrement;

import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.identifier.IdentifierSequenceState;
import org.obiba.onyx.core.identifier.IdentifierSequence;
import org.obiba.onyx.core.identifier.IdentifierSequenceProvider;
import org.obiba.onyx.core.service.ApplicationConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * Implements of {@link IncrementSequenceProvider} that provides a {@link RandomIncrementSequence}.
 */
public class RandomIncrementIdentifierSequenceProvider implements IdentifierSequenceProvider {
  //
  // Instance Variables
  //

  private RandomIncrementIdentifierSequence sequence;

  @Autowired
  private PersistenceManager persistenceManager;

  @Autowired
  private ApplicationConfigurationService applicationConfigurationService;

  private int maxIncrement;

  //
  // Constructors
  //

  public RandomIncrementIdentifierSequenceProvider(int maxIncrement) {
    Assert.isTrue(maxIncrement >= 1, "maxIncrement must be at least 1");
    this.maxIncrement = maxIncrement;
  }

  //
  // IdentifierSequenceProvider Methods
  //

  public IdentifierSequence getSequence() {
    // Lazily instantiate the sequence.
    if(sequence == null) {
      sequence = createSequence();
    }

    return sequence;
  }

  public boolean hasSequence() {
    return true;
  }

  //
  // Methods
  //

  public void setPersistenceManager(PersistenceManager persistenceManager) {
    this.persistenceManager = persistenceManager;
  }

  public void setApplicationConfigurationService(ApplicationConfigurationService applicationConfigurationService) {
    this.applicationConfigurationService = applicationConfigurationService;
  }

  private RandomIncrementIdentifierSequence createSequence() {
    RandomIncrementIdentifierSequence sequence = new RandomIncrementIdentifierSequence();
    sequence.setMaxIncrement(maxIncrement);

    // Initialize the sequence only if a sequence state does not already exist.
    if(!sequenceStateExists()) {
      sequence.startSequence(getIdentifierPrefix(), getFirstIdentifier());
    }

    return sequence;
  }

  private boolean sequenceStateExists() {
    return persistenceManager.count(IdentifierSequenceState.class) != 0;
  }

  private String getIdentifierPrefix() {
    ApplicationConfiguration appConfig = applicationConfigurationService.getApplicationConfiguration();
    return appConfig.getIdentifierPrefix() != null ? appConfig.getIdentifierPrefix() : "TEST";
  }

  private long getFirstIdentifier() {
    ApplicationConfiguration appConfig = applicationConfigurationService.getApplicationConfiguration();
    return appConfig.getFirstIdentifier();
  }
}

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

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.core.domain.identifier.IdentifierSequenceState;
import org.obiba.onyx.core.identifier.IdentifierSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * An implementation of IdentifierSequence that uses a randomly generated value between 1..maxIncrement to increment the
 * state's lastIdentifier value.
 */
public class RandomIncrementIdentifierSequence implements IdentifierSequence {
  //
  // Instance Variables
  //

  @Autowired
  private PersistenceManager persistenceManager;

  private SecureRandom random;

  private int maxIncrement;

  //
  // IdentifierSequence Methods
  //

  public void startSequence(String prefix, long lastIdentifier) {
    Assert.isTrue(!sequenceStateExists(), "called startSequence with existing IdentifierSequenceState");
    createSequenceState(prefix, lastIdentifier);
  }

  public String nextIdentifier() {
    int increment = random.nextInt(maxIncrement) + 1;

    IdentifierSequenceState state = loadSequenceState();
    state.setLastIdentifier(state.getLastIdentifier() + increment);
    persistenceManager.save(state);

    return state.getPrefix() + state.getLastIdentifier();
  }

  //
  // Methods
  //

  public void setPersistenceManager(PersistenceManager persistenceManager) {
    this.persistenceManager = persistenceManager;
  }

  public int getMaxIncrement() {
    return maxIncrement;
  }

  public void setMaxIncrement(int maxIncrement) {
    this.maxIncrement = maxIncrement;
  }

  private boolean sequenceStateExists() {
    return persistenceManager.count(IdentifierSequenceState.class) != 0;
  }

  private void createSequenceState(String prefix, long lastIdentifier) {
    IdentifierSequenceState state = new IdentifierSequenceState();
    state.setPrefix(prefix);
    state.setLastIdentifier(lastIdentifier);
    state.setLastUpdate(new Date());

    persistenceManager.save(state);
  }

  private IdentifierSequenceState loadSequenceState() {
    List<IdentifierSequenceState> result = persistenceManager.list(IdentifierSequenceState.class);
    Assert.notEmpty(result, "IdentifierSequenceState does not exist (was the startSequence method called?)");
    Assert.isTrue(result.size() == 1, "Unexpected number of IdentifierSequenceState entities: " + result.size() + " (expected 1)");

    return result.get(0);
  }
}

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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.identifier.IdentifierSequenceState;
import org.obiba.onyx.core.identifier.IdentifierSequence;
import org.obiba.onyx.core.service.ApplicationConfigurationService;

/**
 * Unit tests for {@link RandomIncrementIdentifierSequenceProvider}.
 */
public class RandomIncrementIdentifierSequenceProviderTest {
  //
  // Constants
  //

  private static final String IDENTIFIER_PREFIX = "PREFIX";

  private static final long FIRST_IDENTIFIER = 1000l;

  //
  // Test Methods
  //

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_ThrowsExceptionIfMaxIncrementArgIsLessThanOne() {
    new RandomIncrementIdentifierSequenceProvider(0);
    new RandomIncrementIdentifierSequenceProvider(-1);
  }

  @Test
  public void testConstructor_InitializesMaxIncrement() {
    int expectedMaxIncrement = 10;
    RandomIncrementIdentifierSequenceProvider sut = new RandomIncrementIdentifierSequenceProvider(expectedMaxIncrement);

    assertEquals(expectedMaxIncrement, sut.getMaxIncrement());
  }

  @Test
  public void testHasSequence() {
    RandomIncrementIdentifierSequenceProvider sut = new RandomIncrementIdentifierSequenceProvider(10);

    assertTrue("hasSequence() should return true", sut.hasSequence());
  }

  @Test
  public void testGetSequence_WhenIdentifierSequenceStateExists() {
    // Setup
    PersistenceManager mockPersistenceManager = createMock(PersistenceManager.class);
    expect(mockPersistenceManager.count(IdentifierSequenceState.class)).andReturn(1).atLeastOnce();

    replay(mockPersistenceManager);

    int expectedMaxIncrement = 10;
    RandomIncrementIdentifierSequenceProvider sut = new RandomIncrementIdentifierSequenceProvider(expectedMaxIncrement);
    sut.setPersistenceManager(mockPersistenceManager);

    // Exercise
    IdentifierSequence sequence = sut.getSequence();

    // Verify
    assertNotNull(sequence);
    assertEquals(RandomIncrementIdentifierSequence.class, sequence.getClass());
    assertEquals(expectedMaxIncrement, ((RandomIncrementIdentifierSequence) sequence).getMaxIncrement());

    verify(mockPersistenceManager);
  }

  @Test
  public void testGetSequence_WhenIdentifierSequenceStateDoesNotExist() {
    // Setup
    PersistenceManager mockPersistenceManager = createMock(PersistenceManager.class);
    expect(mockPersistenceManager.count(IdentifierSequenceState.class)).andReturn(0).atLeastOnce();
    IdentifierSequenceState expectedSequenceState = createIdentifierSequenceState();
    expect(mockPersistenceManager.save(eqIdentifierSequenceState(expectedSequenceState))).andReturn(null).atLeastOnce();

    ApplicationConfigurationService mockAppConfigService = createMock(ApplicationConfigurationService.class);
    ApplicationConfiguration appConfig = createApplicationConfiguration();
    expect(mockAppConfigService.getApplicationConfiguration()).andReturn(appConfig).atLeastOnce();

    replay(mockPersistenceManager, mockAppConfigService);

    int expectedMaxIncrement = 10;
    RandomIncrementIdentifierSequenceProvider sut = new RandomIncrementIdentifierSequenceProvider(expectedMaxIncrement);
    sut.setPersistenceManager(mockPersistenceManager);
    sut.setApplicationConfigurationService(mockAppConfigService);

    // Exercise
    IdentifierSequence sequence = sut.getSequence();

    // Verify
    assertNotNull(sequence);
    assertEquals(RandomIncrementIdentifierSequence.class, sequence.getClass());
    assertEquals(expectedMaxIncrement, ((RandomIncrementIdentifierSequence) sequence).getMaxIncrement());

    verify(mockPersistenceManager, mockAppConfigService);
  }

  //
  // Helper Methods
  //

  private ApplicationConfiguration createApplicationConfiguration() {
    ApplicationConfiguration appConfig = new ApplicationConfiguration();
    appConfig.setIdentifierPrefix(IDENTIFIER_PREFIX);
    appConfig.setFirstIdentifier(FIRST_IDENTIFIER);

    return appConfig;
  }

  private IdentifierSequenceState createIdentifierSequenceState() {
    IdentifierSequenceState identifierSequenceState = new IdentifierSequenceState();
    identifierSequenceState.setPrefix(IDENTIFIER_PREFIX);
    identifierSequenceState.setLastIdentifier(FIRST_IDENTIFIER);

    return identifierSequenceState;
  }

  //
  // Inner Classes
  //

  static class IdentifierSequenceStateMatcher implements IArgumentMatcher {

    private IdentifierSequenceState expected;

    public IdentifierSequenceStateMatcher(IdentifierSequenceState expected) {
      this.expected = expected;
    }

    public boolean matches(Object actual) {
      if(actual instanceof IdentifierSequenceState) {
        return ((IdentifierSequenceState) actual).getPrefix().equals(expected.getPrefix()) && ((IdentifierSequenceState) actual).getLastIdentifier() == (expected.getLastIdentifier());
      } else {
        return false;
      }
    }

    public void appendTo(StringBuffer buffer) {
      buffer.append("eqIdentifierSequenceState(");
      buffer.append(expected.getClass().getName());
      buffer.append(" with prefix \"");
      buffer.append(expected.getPrefix());
      buffer.append("\" and lastIdentifier \"");
      buffer.append(expected.getLastIdentifier());
      buffer.append("\")");
    }
  }

  static IdentifierSequenceState eqIdentifierSequenceState(IdentifierSequenceState in) {
    EasyMock.reportMatcher(new IdentifierSequenceStateMatcher(in));
    return null;
  }
}

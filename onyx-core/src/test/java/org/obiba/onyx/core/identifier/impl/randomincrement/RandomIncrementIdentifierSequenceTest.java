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

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.core.domain.identifier.IdentifierSequenceState;

/**
 * Unit tests for {@link RandomIncrementIdentifierSequence}.
 */
public class RandomIncrementIdentifierSequenceTest {
  //
  // Constants
  //

  private static final String PREFIX = "prefix";

  private static final long LAST_IDENTIFIER = 100;

  //
  // Instance Variables
  //

  private RandomIncrementIdentifierSequence sut;

  private PersistenceManager mockPersistenceManager;

  private List<Object> mocks = new ArrayList<Object>();

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    mockPersistenceManager = createMock(PersistenceManager.class);
    mocks.add(mockPersistenceManager);

    sut = createSequence(1);
    sut.setPersistenceManager(mockPersistenceManager);
  }

  //
  // Test Methods
  //

  @Test(expected = IllegalArgumentException.class)
  public void testStartSequence_ThrowsExceptionIfStateExists() {
    // Test-specific setup.
    expectStateCountQuery(1);
    expectStateSaved(PREFIX, LAST_IDENTIFIER);
    replayMocks();

    // Exercise.
    sut.startSequence(PREFIX, LAST_IDENTIFIER);
  }

  @Test
  public void testStartSequence() {
    // Test-specific setup.
    expectStateCountQuery(0);
    expectStateSaved(PREFIX, LAST_IDENTIFIER);
    replayMocks();

    // Exercise.
    sut.startSequence(PREFIX, LAST_IDENTIFIER);

    // Verify.
    verifyMocks();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNextIdentifier_ThrowsExceptionIfStateDoesNotExist() {
    // Test-specific setup.
    expectStateListQuery(new ArrayList<IdentifierSequenceState>());
    replayMocks();

    // Exercise.
    sut.nextIdentifier();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNextIdentifier_ThrowsExceptionIfMoreThanStateExists() {
    // Test-specific setup.
    List<IdentifierSequenceState> stateList = new ArrayList<IdentifierSequenceState>();
    stateList.add(new IdentifierSequenceState());
    stateList.add(new IdentifierSequenceState());
    expectStateListQuery(stateList);
    replayMocks();

    // Exercise.
    sut.nextIdentifier();
  }

  @Test
  public void testNextIdentifier() {
    // Test-specific setup.
    List<IdentifierSequenceState> stateList = new ArrayList<IdentifierSequenceState>();
    stateList.add(createState(PREFIX, LAST_IDENTIFIER));
    expectStateListQuery(stateList);
    expectStateSaved(PREFIX, LAST_IDENTIFIER + 1); // note: maxIncrement is 1 so id is predictable
    replayMocks();

    // Exercise.
    String id = sut.nextIdentifier();

    // Verify.
    verifyMocks();
    assertEquals(PREFIX + (LAST_IDENTIFIER + 1), id);
  }

  //
  // Methods
  //

  private RandomIncrementIdentifierSequence createSequence(int maxIncrement) {
    RandomIncrementIdentifierSequence sequence = new RandomIncrementIdentifierSequence();
    sequence.setMaxIncrement(maxIncrement);

    return sequence;
  }

  private IdentifierSequenceState createState(String prefix, long lastIdentifier) {
    IdentifierSequenceState state = new IdentifierSequenceState();
    state.setPrefix(prefix);
    state.setLastIdentifier(lastIdentifier);

    return state;
  }

  private void expectStateCountQuery(int queryResult) {
    expect(mockPersistenceManager.count(IdentifierSequenceState.class)).andReturn(queryResult).atLeastOnce();
  }

  private void expectStateSaved(String prefix, long lastIdentifier) {
    IdentifierSequenceState newState = new IdentifierSequenceState();
    newState.setPrefix(prefix);
    newState.setLastIdentifier(lastIdentifier);

    expect(mockPersistenceManager.save(eqIdentifierSequenceState(newState))).andReturn(newState).atLeastOnce();
  }

  private void expectStateListQuery(List<IdentifierSequenceState> queryResult) {
    expect(mockPersistenceManager.list(IdentifierSequenceState.class)).andReturn(queryResult).atLeastOnce();
  }

  private void replayMocks() {
    replay(mocks.toArray());
  }

  private void verifyMocks() {
    verify(mocks.toArray());
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

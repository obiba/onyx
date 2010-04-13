/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.identifier;

/**
 * The interface used to generate participant identifiers and configure the sequence at installation.
 */
public interface IdentifierSequence {

  /**
   * Executes the strategy for generating the 'next' identifier in the sequence.
   * 
   * @return next identifier
   */
  public String nextIdentifier();

  /**
   * Asks the strategy to initialize the sequence with the specified attributes.
   * 
   * Note that the startSequence method should only be called when no existing sequence state exists. That is, this
   * method should only be called once.
   * 
   * @param prefix prefix
   * @param lastIdentifier last identifier
   */
  public void startSequence(String prefix, long lastIdentifier);
}

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
 * An interface that defines the contract for accessing the instance of IdentifierSequence when one such instance is
 * configured in Onyx.
 */
public interface IdentifierSequenceProvider {

  /**
   * Indicates whether a sequence has been configured.
   * 
   * @return <code>true</code> if a sequence has been configured
   */
  public boolean hasSequence();

  /**
   * Returns the configured sequence (<code>null</code> if none)
   * 
   * @return configured sequence
   */
  public IdentifierSequence getSequence();
}

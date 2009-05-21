/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.contraindication;

import java.util.List;

/**
 * Defines the contract by which something can be contraindicated. Implementations of this interface define what is
 * actually being contraindicated. For example, some implementations may contraindicate a whole stage, others may
 * contraindicate a subset of values to be captured.
 */
public interface IContraindicatable {

  /**
   * Returns a {@code List} of possible {@code Contraindication}s for the sepecified {@code Contraindication.Type}.
   * <p>
   * The value passed to the {@link #setContraindication(Contraindication)} method must be an object from this list.
   * 
   * @param type the type of contraindication to lookup
   * @return a List of possible contraindications of the specified type.
   */
  public List<Contraindication> getContraindications(Contraindication.Type type);

  /**
   * Returns true when at least one contraindication exists for the specified type.
   * @param type the type to check
   * @return true when at least one contraindication exists.
   */
  public boolean hasContraindications(Contraindication.Type type);

  /**
   * Returns the current {@code Contraindication} set on this {@code IContraindicatable}.
   * 
   * @return the current {@code Contraindication} or null if none is set.
   */
  Contraindication getContraindication();

  /**
   * Returns true when this {@code IContraindicatable} has a {@code Contraindication} set.
   * @return true when this {@code IContraindicatable} has a {@code Contraindication} set.
   */
  boolean isContraindicated();

  /**
   * Sets the {@code Contraindication}. This method overrides any previous contraindication.
   * 
   * @param contraindication the contraindication to set.
   */
  void setContraindication(Contraindication contraindication);

  /**
   * When none of the pre-defined contraindications returned by
   * {@code #getContraindications(org.obiba.onyx.core.domain.contraindication.Contraindication.Type)} is relevant, this
   * method may be used to set a description of the contraindication reason.
   * @param other the description of the other contraindication.
   */
  void setOtherContraindication(String other);

  /**
   * Returns the description of the other contraindication or null if none is set.
   * @return the description of the other contraindication.
   */
  String getOtherContraindication();
}

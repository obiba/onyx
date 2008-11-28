/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire;


/**
 * A localizable element is visitable for providing the localization key for each of its properties.
 * @author Yannick Marcon
 * 
 */
public interface ILocalizable extends IVisitable {

  /**
   * Unique identifier for the localizable element.
   * @return
   */
  public String getName();
}

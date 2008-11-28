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

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;

/**
 * Visitable {@link Questionnaire} element.
 * @author Yannick Marcon
 *
 */
public interface IVisitable {

  /**
   * Accept the visit.
   * @param visitor
   */
  public void accept(IVisitor visitor);
  
}

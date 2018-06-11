/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;

/**
 * Handle the walk through each questionnaire element.
 * @author Yannick Marcon
 * @see QuestionnaireWalker
 *
 */
public interface IWalkerVisitor extends IVisitor {

  /**
   * Stop walking through the questionnaire hierarchy if false.
   * @return
   */
  public boolean visiteMore();
  
}

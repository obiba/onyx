/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;
import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;

/**
 *
 */
public interface IHasPage extends Serializable, IQuestionnaireElement {

  /**
   * 
   * @return
   */
  List<Page> getPages();

  /**
   * @param page
   */
  void addPage(Page page);

  /**
   * @param page
   * @param index
   */
  void addPage(Page page, int index);

  /**
   * @param page
   */
  void removePage(Page page);

}

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

import java.util.Comparator;

import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;

/**
 *
 */
public class QuestionnaireElementComparator implements Comparator<IQuestionnaireElement> {

  @Override
  public int compare(IQuestionnaireElement arg0, IQuestionnaireElement arg1) {
    return arg0.getName().compareTo(arg1.getName());
  }

}

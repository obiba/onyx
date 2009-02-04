/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionPanelFactory;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;

/**
 * Simplified question panel factory.
 * @see SimplifiedQuestionPanel
 */
public class SimplifiedQuestionPanelFactory implements IQuestionPanelFactory {

  public QuestionPanel createPanel(String id, IModel questionModel) {
    return new SimplifiedQuestionPanel(id, questionModel);
  }

  public String getBeanName() {
    return "quartz." + getClass().getSimpleName();
  }

}

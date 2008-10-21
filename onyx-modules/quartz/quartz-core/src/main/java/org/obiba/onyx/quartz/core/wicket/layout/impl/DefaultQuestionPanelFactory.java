/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionPanelFactory;
import org.obiba.onyx.quartz.core.wicket.layout.AbstractQuestionPanel;

public class DefaultQuestionPanelFactory implements IQuestionPanelFactory {

  public AbstractQuestionPanel createPanel(String id, Question question) {
    return new DefaultQuestionPanel(id, question);
  }

  public String getName() {
    return "quartz." + getClass().getSimpleName();
  }

}

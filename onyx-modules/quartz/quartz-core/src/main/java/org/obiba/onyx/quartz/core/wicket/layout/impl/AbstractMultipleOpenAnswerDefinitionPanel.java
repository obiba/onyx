/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI for OpenAnswer having other open answer children.
 */
public abstract class AbstractMultipleOpenAnswerDefinitionPanel extends AbstractOpenAnswerDefinitionPanel {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractMultipleOpenAnswerDefinitionPanel.class);

  private List<AbstractOpenAnswerDefinitionPanel> abstractOpenAnswerDefinitionPanels = new ArrayList<AbstractOpenAnswerDefinitionPanel>();

  /**
   * Constructor.
   * 
   * @param id
   * @param questionModel
   * @param questionCategoryModel
   */
  public AbstractMultipleOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel) {
    super(id, questionModel, questionCategoryModel);
    setOutputMarkupId(true);

    RepeatingView repeating = new RepeatingView("repeating");
    add(repeating);

    for(OpenAnswerDefinition openAnswerDefinitionChild : getOpenAnswerDefinition().getOpenAnswerDefinitions()) {
      WebMarkupContainer item = new WebMarkupContainer(repeating.newChildId());
      repeating.add(item);

      AbstractOpenAnswerDefinitionPanel open;
      item.add(open = newOpenAnswerDefinitionPanel("open", getQuestionModel(), getQuestionCategoryModel(), new QuestionnaireModel(openAnswerDefinitionChild)));
      abstractOpenAnswerDefinitionPanels.add(open);
    }
  }

  /**
   * @param string
   * @param questionModel
   * @param questionCategoryModel
   * @param questionnaireModel
   * @return
   */
  protected abstract AbstractOpenAnswerDefinitionPanel newOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel, IModel openAnswerDefinitionModel);

  @Override
  public void resetField() {
    for(AbstractOpenAnswerDefinitionPanel panel : abstractOpenAnswerDefinitionPanels) {
      panel.resetField();
    }
  }
}

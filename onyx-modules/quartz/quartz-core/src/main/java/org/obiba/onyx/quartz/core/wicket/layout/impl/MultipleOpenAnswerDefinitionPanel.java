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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI for OpenAnswer having other open answer children.
 */
public class MultipleOpenAnswerDefinitionPanel extends AbstractOpenAnswerDefinitionPanel {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(MultipleOpenAnswerDefinitionPanel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private List<AbstractOpenAnswerDefinitionPanel> abstractOpenAnswerDefinitionPanels = new ArrayList<AbstractOpenAnswerDefinitionPanel>();

  /**
   * Constructor given the question category (needed for persistency).
   * @param id
   * @param questionCategoryModel
   * @param openAnswerDefinitionModel
   */
  public MultipleOpenAnswerDefinitionPanel(String id, IModel questionCategoryModel) {
    super(id, questionCategoryModel);
    initialize();
  }

  /**
   * Constructor.
   * 
   * @param id
   * @param questionModel
   * @param questionCategoryModel
   */
  public MultipleOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel) {
    super(id, questionModel, questionCategoryModel);
    initialize();
  }

  @SuppressWarnings("serial")
  private void initialize() {
    RepeatingView repeating = new RepeatingView("repeating");
    add(repeating);

    for(OpenAnswerDefinition openAnswerDefinitionChild : getOpenAnswerDefinition().getOpenAnswerDefinitions()) {
      WebMarkupContainer item = new WebMarkupContainer(repeating.newChildId());
      repeating.add(item);

      DefaultOpenAnswerDefinitionPanel open;
      item.add(open = new DefaultOpenAnswerDefinitionPanel("open", getQuestionModel(), getQuestionCategoryModel(), new QuestionnaireModel(openAnswerDefinitionChild)) {

        @Override
        public void onSelect(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel, IModel openAnswerDefinitionModel) {
          MultipleOpenAnswerDefinitionPanel.this.onSelect(target, questionModel, questionCategoryModel, openAnswerDefinitionModel);
        }

        @Override
        public void onSubmit(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
          MultipleOpenAnswerDefinitionPanel.this.onSubmit(target, questionModel, questionCategoryModel);
        }

      });
      abstractOpenAnswerDefinitionPanels.add(open);
    }

  }

  @Override
  public void setRequired(boolean required) {
    log.info("required={}", required);
    for(AbstractOpenAnswerDefinitionPanel panel : abstractOpenAnswerDefinitionPanels) {
      panel.setRequired(required);
    }
  }
}

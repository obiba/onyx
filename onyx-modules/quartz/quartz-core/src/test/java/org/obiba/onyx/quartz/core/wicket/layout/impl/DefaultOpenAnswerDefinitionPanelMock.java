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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.util.data.Data;

@SuppressWarnings("serial")
public class DefaultOpenAnswerDefinitionPanelMock extends FormMock {

  private DefaultOpenAnswerDefinitionPanel panel;

  private IModel assertModel;

  public DefaultOpenAnswerDefinitionPanelMock(String id, IModel questionCategoryModel, IModel assertModel) {
    super(id, questionCategoryModel);
    this.assertModel = assertModel;
  }

  @Override
  public Component populateContent(String id, IModel model) {
    return panel = new DefaultOpenAnswerDefinitionPanel(id, model) {

      @Override
      public void onClick(AjaxRequestTarget target) {
        assertModel.setObject(Boolean.TRUE);
        DefaultOpenAnswerDefinitionPanelTest.log.info("onClick.{}", ((QuestionCategory) getModelObject()).getName());
      }

    };
  }

  public Data getData() {
    return panel.getData();
  }

}
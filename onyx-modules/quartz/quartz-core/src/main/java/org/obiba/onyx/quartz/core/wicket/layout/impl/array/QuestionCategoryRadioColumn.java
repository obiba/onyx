/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.array;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.impl.DefaultOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.RadioQuestionCategoryPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class QuestionCategoryRadioColumn extends AbstractQuestionCategoryColumn {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(QuestionCategoryRadioColumn.class);

  private IModel radioGroupsModel;

  private IModel currentOpenFieldsModel;

  /**
   * @param questionCategoryModel
   */
  public QuestionCategoryRadioColumn(IModel questionCategoryModel, IModel radioGroupsModel, IModel currentOpenFieldsModel) {
    super(questionCategoryModel);
    this.radioGroupsModel = radioGroupsModel;
    this.currentOpenFieldsModel = currentOpenFieldsModel;
  }

  @SuppressWarnings("serial")
  @Override
  public void populateItem(Item cellItem, String componentId, IModel rowModel, final int index) {
    RadioGroup radioGroup = ((RadioGroup[]) radioGroupsModel.getObject())[index];
    radioGroup.setRequired(((Question) rowModel.getObject()).isRequired());
    radioGroup.setLabel(new QuestionnaireStringResourceModel(rowModel, "label"));

    cellItem.add(new RadioQuestionCategoryPanel(componentId, rowModel, cellItem.getModel(), radioGroup, false) {

      @Override
      public void onOpenFieldSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
        DefaultOpenAnswerDefinitionPanel currentOpenField = getCurrentOpenField(index);
        log.info("onOpenFieldSelection().currentOpenField={}", currentOpenField != null ? currentOpenField.getModelObject() : null);
        // ignore if multiple click in the same open field
        if(getOpenField().equals(currentOpenField)) return;
        log.info("onOpenFieldSelection().getOpenField={}", getOpenField().getModelObject());

        // make sure a previously selected open field in the same row is not asked for
        if(currentOpenField != null) {
          currentOpenField.setRequired(false);
        }
        // make the open field active
        setCurrentOpenField(index, getOpenField());

        // call for refresh
        onEvent(target);
      }

      @Override
      public void onRadioSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
        // make inactive the previously selected open field
        DefaultOpenAnswerDefinitionPanel currentOpenField = getCurrentOpenField(index);
        if(currentOpenField != null) {
          currentOpenField.setData(null);
          currentOpenField.setRequired(false);
          setCurrentOpenField(index, null);
        }

        // call for refresh
        onEvent(target);
      }

    });
  }

  private DefaultOpenAnswerDefinitionPanel getCurrentOpenField(int index) {
    DefaultOpenAnswerDefinitionPanel[] currentOpenFields = (DefaultOpenAnswerDefinitionPanel[]) currentOpenFieldsModel.getObject();
    return currentOpenFields[index];
  }

  private void setCurrentOpenField(int index, DefaultOpenAnswerDefinitionPanel currentOpenField) {
    DefaultOpenAnswerDefinitionPanel[] currentOpenFields = (DefaultOpenAnswerDefinitionPanel[]) currentOpenFieldsModel.getObject();
    currentOpenFields[index] = currentOpenField;
  }

}

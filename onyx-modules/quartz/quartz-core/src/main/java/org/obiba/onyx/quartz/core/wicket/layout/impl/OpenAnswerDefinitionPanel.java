/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.data.DataField;

public class OpenAnswerDefinitionPanel extends Panel {

  private static final long serialVersionUID = 8950481253772691811L;
  
  private DataField openField;
  
  /**
   * Constructor given the question category (needed for persistency).
   * @param id
   * @param questionCategoryModel
   */
  @SuppressWarnings("serial")
  public OpenAnswerDefinitionPanel(String id, IModel questionCategoryModel) {
    super(id, questionCategoryModel);
    
    QuestionCategory questionCategory = (QuestionCategory)questionCategoryModel.getObject();
    final OpenAnswerDefinition openAnswerDefinition = questionCategory.getCategory().getOpenAnswerDefinition();
    if(openAnswerDefinition.getDefaultValues().size() > 1) {
      openField = new DataField("open", new Model(), openAnswerDefinition.getDataType(), openAnswerDefinition.getDefaultValues(), new IChoiceRenderer() {

        public Object getDisplayValue(Object object) {
          Data data = (Data) object;
          return (String) new QuestionnaireStringResourceModel(openAnswerDefinition, data.getValueAsString(), null).getObject();
        }

        public String getIdValue(Object object, int index) {
          Data data = (Data) object;
          return data.getValueAsString();
        }

      }, (String) new QuestionnaireStringResourceModel(openAnswerDefinition, "unitLabel", null).getObject());
    } else if(openAnswerDefinition.getDefaultValues().size() > 0) {
      openField = new DataField("open", new Model(openAnswerDefinition.getDefaultValues().get(0)), openAnswerDefinition.getDataType(), (String) new QuestionnaireStringResourceModel(openAnswerDefinition, "unitLabel", null).getObject());
    } else {
      openField = new DataField("open", new Model(), openAnswerDefinition.getDataType(), (String) new QuestionnaireStringResourceModel(openAnswerDefinition, "unitLabel", null).getObject());
    }
    
    add(openField);
    
  }
  
  public void setFieldEnabled(boolean enabled) {
    openField.setFieldEnabled(enabled);
  }
  
  public boolean isFieldEnabled() {
    return openField.isFieldEnabled();
  }

  
  
}

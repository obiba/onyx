/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.data.DataField;

public class MultipleChoiceQuestionPanel extends QuestionPanel {

  private static final long serialVersionUID = 2951128797454847260L;

  public MultipleChoiceQuestionPanel(String id, Question question) {
    super(id, question);

    add(new Label("label", new QuestionnaireStringResourceModel(question, "label", null)));
    add(new Label("instructions", new QuestionnaireStringResourceModel(question, "instructions", null)));
    add(new Label("caption", new QuestionnaireStringResourceModel(question, "caption", null)));

    final List<IModel> checkedItems = new ArrayList<IModel>();

    ListView checkList = new ListView("category", question.getQuestionCategories()) {

      private static final long serialVersionUID = 1L;

      @Override
      protected void populateItem(ListItem item) {
        QuestionCategory questionCategory = (QuestionCategory) item.getModelObject();
        CheckBox checkBox = new CheckBox("checkBox", item.getModel());
        checkBox.setLabel(new QuestionnaireStringResourceModel(questionCategory, "label", null));

        FormComponentLabel checkBoxLabel = new FormComponentLabel("categoryLabel", checkBox);
        item.add(checkBoxLabel);
        checkBoxLabel.add(checkBox);
        checkBoxLabel.add(new Label("label", checkBox.getLabel()).setRenderBodyOnly(true));

        if(questionCategory.getCategory().getOpenAnswerDefinition() != null) {
          final OpenAnswerDefinition open = questionCategory.getCategory().getOpenAnswerDefinition();
          if(open.getDefaultValues().size() > 1) {
            checkBoxLabel.add(new DataField("open", new Model(), open.getDataType(), open.getDefaultValues(), new IChoiceRenderer() {

              private static final long serialVersionUID = 1L;

              public Object getDisplayValue(Object object) {
                Data data = (Data) object;
                return (String) new QuestionnaireStringResourceModel(open, data.getValueAsString(), null).getObject();
              }

              public String getIdValue(Object object, int index) {
                Data data = (Data) object;
                return data.getValueAsString();
              }

            }, (String) new QuestionnaireStringResourceModel(open, "unitLabel", null).getObject()));
          } else if(open.getDefaultValues().size() > 0) {
            checkBoxLabel.add(new DataField("open", new Model(open.getDefaultValues().get(0)), open.getDataType(), (String) new QuestionnaireStringResourceModel(open, "unitLabel", null).getObject()));
          } else {
            checkBoxLabel.add(new DataField("open", new Model(), open.getDataType(), (String) new QuestionnaireStringResourceModel(open, "unitLabel", null).getObject()));
          }
        } else {
          checkBoxLabel.add(new EmptyPanel("open"));
        }

        if(questionCategory.isSelected()) {
          checkedItems.add(item.getModel());
        }
      }
    };

    CheckGroup checkGroup = new CheckGroup("categories", checkedItems);
    add(checkGroup);
    checkGroup.add(checkList);

  }

  public void onNext(AjaxRequestTarget target) {
    // TODO Auto-generated method stub

  }

  public void onPrevious(AjaxRequestTarget target) {
    // TODO Auto-generated method stub

  }

}

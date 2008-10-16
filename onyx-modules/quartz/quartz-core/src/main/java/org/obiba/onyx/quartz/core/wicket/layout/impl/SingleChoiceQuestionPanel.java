package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
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

public class SingleChoiceQuestionPanel extends QuestionPanel {

  private static final long serialVersionUID = 2951128797454847260L;

  @SuppressWarnings("serial")
  public SingleChoiceQuestionPanel(String id, IModel model) {
    super(id, model);

    Question question = (Question) model.getObject();
    add(new Label("label", new QuestionnaireStringResourceModel(question, "label", null)));
    add(new Label("instructions", new QuestionnaireStringResourceModel(question, "instructions", null)));
    add(new Label("caption", new QuestionnaireStringResourceModel(question, "caption", null)));

    final RadioGroup radioGroup = new RadioGroup("categories", new Model());
    add(radioGroup);
    ListView radioList = new ListView("category", question.getQuestionCategories()) {

      @Override
      protected void populateItem(ListItem item) {
        QuestionCategory questionCategory = (QuestionCategory) item.getModelObject();
        Radio radio = new Radio("radio", item.getModel());
        radio.setLabel(new QuestionnaireStringResourceModel(questionCategory, "label", null));

        FormComponentLabel radioLabel = new FormComponentLabel("categoryLabel", radio);
        item.add(radioLabel);
        radioLabel.add(radio);
        radioLabel.add(new Label("label", radio.getLabel()).setRenderBodyOnly(true));

        if(questionCategory.getCategory().getOpenAnswerDefinition() != null) {
          final OpenAnswerDefinition open = questionCategory.getCategory().getOpenAnswerDefinition();
          if (open.getDefaultValues().size()>1) {
            radioLabel.add(new DataField("open", new Model(), open.getDataType(), open.getDefaultValues(), new IChoiceRenderer() {

              public Object getDisplayValue(Object object) {
                Data data = (Data)object;
                return (String)new QuestionnaireStringResourceModel(open, data.getValueAsString(), null).getObject();
              }

              public String getIdValue(Object object, int index) {
                Data data = (Data)object;
                return data.getValueAsString();
              }
              
              
            }, (String)new QuestionnaireStringResourceModel(open, "unitLabel", null).getObject()));
          }
          else if (open.getDefaultValues().size()>0) {
            radioLabel.add(new DataField("open", new Model(open.getDefaultValues().get(0)), open.getDataType(), (String)new QuestionnaireStringResourceModel(open, "unitLabel", null).getObject()));
          }
          else {
            radioLabel.add(new DataField("open", new Model(), open.getDataType(), (String)new QuestionnaireStringResourceModel(open, "unitLabel", null).getObject()));
          }
        } else {
          radioLabel.add(new EmptyPanel("open"));
        }

        if(questionCategory.isSelected()) {
          radioGroup.setModel(item.getModel());
        }
      }

    }.setReuseItems(true);
    radioGroup.add(radioList);
    radioGroup.setRequired(question.isRequired());

  }

  public void onNext(AjaxRequestTarget target) {
    // TODO Auto-generated method stub

  }

  public void onPrevious(AjaxRequestTarget target) {
    // TODO Auto-generated method stub

  }

}

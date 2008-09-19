package org.obiba.onyx.jade.core.wicket.contraindication;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

public class ContraIndicationPanel extends Panel {

  private static final long serialVersionUID = 5714683341046355212L;

  private ContraIndicatedModel choiceModel;

  @SuppressWarnings("serial")
  public ContraIndicationPanel(String id, IModel contraIndicationModel) {
    super(id, contraIndicationModel);

    choiceModel = new ContraIndicatedModel();

    add(new Label("label", new PropertyModel(contraIndicationModel, "description")));

    RadioChoice choices = new RadioChoice("choices", new PropertyModel(choiceModel, "choice"), Arrays.asList(new String[] { "Yes", "No" }), new IChoiceRenderer() {

      public Object getDisplayValue(Object object) {
        return ContraIndicationPanel.this.getString(object.toString());
      }

      public String getIdValue(Object object, int index) {
        return object.toString();
      }

    });
    add(choices);
  }

  public boolean isContraIndicated() {
    if(choiceModel.getChoice() == null) return false;
    else if(choiceModel.getChoice().equals("Yes")) return true;
    else
      return false;
  }

  @SuppressWarnings("serial")
  private class ContraIndicatedModel implements Serializable {
    private String choice;

    public String getChoice() {
      return choice;
    }

    public void setChoice(String choice) {
      this.choice = choice;
    }
  }
}

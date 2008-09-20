package org.obiba.onyx.jade.core.wicket.contraindication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.ContraIndication;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.ContraIndicationAnswer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstrumentTypeContraIndicationPanel extends Panel {

  private static final long serialVersionUID = -7360139138848577104L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(InstrumentTypeContraIndicationPanel.class);

  @SpringBean
  private EntityQueryService queryService;

  private List<ContraIndicationAnswer> choices = new ArrayList<ContraIndicationAnswer>();

  public InstrumentTypeContraIndicationPanel(String id, IModel instrumentTypeModel) {
    super(id, instrumentTypeModel);

    RepeatingView repeating = new RepeatingView("repeating");
    add(repeating);
    
    InstrumentType type = (InstrumentType) instrumentTypeModel.getObject();
    for(ContraIndication ci : type.getContraIndications()) {
      WebMarkupContainer item = new WebMarkupContainer(repeating.newChildId());
      repeating.add(item);

      item.add(new Label("label", new PropertyModel(ci, "description")));

      ContraIndicatedModel choiceModel = new ContraIndicatedModel(ci);
      choices.add(choiceModel.getContraIndicationAnswer());

      RadioChoice choices = new RadioChoice("choices", new PropertyModel(choiceModel, "choice"), Arrays.asList(new String[] { "Yes", "No" }), new IChoiceRenderer() {

        public Object getDisplayValue(Object object) {
          return InstrumentTypeContraIndicationPanel.this.getString(object.toString());
        }

        public String getIdValue(Object object, int index) {
          return object.toString();
        }

      });
      item.add(choices);
    }

  }

  public List<ContraIndicationAnswer> getContraIndicationAnswers() {
    return choices;
  }
  
  @SuppressWarnings("serial")
  private class ContraIndicatedModel implements Serializable {

    private String choice;
    
    private ContraIndicationAnswer answer;

    public ContraIndicatedModel(ContraIndication contraIndication) {
      this.answer = new ContraIndicationAnswer(contraIndication);
    }

    public String getChoice() {
      return choice;
    }

    public void setChoice(String choice) {
      this.choice = choice;
      answer.setContraIndicated(isContraIndicated());
    }

    public ContraIndicationAnswer getContraIndicationAnswer() {
      return answer;
    }

    public boolean isContraIndicated() {
      if(choice != null && choice.equals("Yes")) return true;
      else
        return false;
    }
  }

}

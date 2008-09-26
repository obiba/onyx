package org.obiba.onyx.jade.core.wicket.instrument;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.ContraIndication;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantInteractionType;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AskedContraIndicationPanel extends Panel {

  private static final long serialVersionUID = 1839206247478532673L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AskedContraIndicationPanel.class);

  private static final String YES = "Yes";
  private static final String NO = "No";
  private static final String DOESNOT_KNOW = "DoesNotKnow";
  
  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;
  
  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  private List<RadioGroup> radioGroups;

  @SuppressWarnings("serial")
  public AskedContraIndicationPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    radioGroups = new ArrayList<RadioGroup>();

    RepeatingView repeat = new RepeatingView("repeat");
    add(repeat);

    ContraIndication template = new ContraIndication();
    template.setType(ParticipantInteractionType.ASKED);
    template.setInstrument(activeInstrumentRunService.getInstrument());
    for(final ContraIndication ci : queryService.match(template)) {
      WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
      repeat.add(item);
      
      ci.setApplicationContext(((SpringWebApplication) getApplication()).getSpringContextLocator().getSpringContext());
      ci.setUserSessionService(userSessionService);

      item.add(new Label("ciLabel", new PropertyModel(ci, "description")));
      
      // radio group without default selection
      RadioGroup radioGroup = new RadioGroup("radioGroup", new Model());
      radioGroups.add(radioGroup);
      radioGroup.setLabel(new PropertyModel(ci, "description"));
      item.add(radioGroup);
      ListView radioList = new ListView("radioItem", Arrays.asList(new String[] { YES, NO, DOESNOT_KNOW })) {

        @Override
        protected void populateItem(ListItem listItem) {
          final String key = listItem.getModelObjectAsString();
          final ContraIndicationSelection selection = new ContraIndicationSelection();
          selection.setContraIndication(ci);
          selection.setSelectionKey(key);
          
          listItem.add(new Radio("radio", new Model(selection)));
          listItem.add(new Label("label", new StringResourceModel(key, AskedContraIndicationPanel.this, null)));
        }

      }.setReuseItems(true);
      radioGroup.add(radioList);
      radioGroup.setRequired(true);
    }

  }
  
  public void saveContraIndicationSelection() {
    activeInstrumentRunService.setContraIndication(null);
    for (RadioGroup rg : radioGroups) {
      ContraIndicationSelection ciSelection = (ContraIndicationSelection)rg.getModelObject();
      if (ciSelection.isSelected()) {
        activeInstrumentRunService.setContraIndication(ciSelection.getContraIndication());
        // just interested in the first one
        break;
      }
    }
  }

  @SuppressWarnings("serial")
  private class ContraIndicationSelection implements Serializable {

    private String selectionKey;

    private ContraIndication contraIndication;

    public String getSelectionKey() {
      return selectionKey;
    }

    public void setSelectionKey(String selectionKey) {
      this.selectionKey = selectionKey;
    }

    public boolean isSelected() {
      return selectionKey.equals(YES) || selectionKey.equals(DOESNOT_KNOW);
    }

    public ContraIndication getContraIndication() {
      return contraIndication;
    }

    public void setContraIndication(ContraIndication contraIndication) {
      this.contraIndication = contraIndication;
    }

  }

}

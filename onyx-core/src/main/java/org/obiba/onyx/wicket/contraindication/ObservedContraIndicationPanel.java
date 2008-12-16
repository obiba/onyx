/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.contraindication;

import java.util.Arrays;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.contraindication.IContraindicatable;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObservedContraIndicationPanel extends Panel {

  private static final long serialVersionUID = 1839206247478532673L;

  private static final Logger log = LoggerFactory.getLogger(ObservedContraIndicationPanel.class);

  private static final String YES = "Yes";

  private static final String NO = "No";

  private String selectedRadio;

  private DropDownChoice contraIndicationDropDownChoice;

  private TextArea otherContraIndication;

  @SuppressWarnings("serial")
  public ObservedContraIndicationPanel(String id, IModel contraindicatable) {
    super(id, contraindicatable);
    setOutputMarkupId(true);

    final RadioGroup radioGroup = new RadioGroup("radioGroup", new PropertyModel(this, "selectedRadio"));
    radioGroup.setLabel(new StringResourceModel("ContraIndication", this, null));
    radioGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        log.info("radioGroup.onUpdate={}", selectedRadio);
        if(selectedRadio.equals(NO)) {
          log.info("invalidating models");
          contraIndicationDropDownChoice.setModelObject(null);
          otherContraIndication.setModelObject(null);
        }
        target.addComponent(ObservedContraIndicationPanel.this);
      }
    });
    add(radioGroup);
    ListView radioList = new ListView("radioItem", Arrays.asList(new String[] { YES, NO })) {

      @Override
      protected void populateItem(final ListItem item) {
        Radio radio = new Radio("radio", item.getModel());
        radio.setLabel(new StringResourceModel((String) item.getModelObject(), ObservedContraIndicationPanel.this, null));
        item.add(radio);
        item.add(new SimpleFormComponentLabel("radioLabel", radio));
      }

    }.setReuseItems(true);
    radioGroup.add(radioList);
    radioGroup.setRequired(true);

    contraIndicationDropDownChoice = new DropDownChoice("ciChoice", new PropertyModel(getModel(), "contraindication"), getContraindicatable().getContraindications(Contraindication.Type.OBSERVED), new ContraindicationChoiceRenderer()) {

      @Override
      public boolean isEnabled() {
        return selectedRadio == null || selectedRadio.equals(YES);
      }

      @Override
      public boolean isRequired() {
        return isEnabled();
      }
    };
    contraIndicationDropDownChoice.setOutputMarkupId(true);
    contraIndicationDropDownChoice.setLabel(new StringResourceModel("ContraIndicationSelection", this, null));

    // Use an OnChangeAjaxBehavior so that the form submission happens, but only for this component
    // This allows us to have the model set to the proper value
    contraIndicationDropDownChoice.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        log.debug("contraIndicationDropDownChoice.onUpdate() selectedRadio={} selectedCi={}", selectedRadio, getContraindicatable().getContraindication());
        // make sure the right radio is selected
        // use setModelObject in order to let the RadioGroup component be aware of the change.
        radioGroup.setModelObject(getContraindicatable().isContraindicated() ? YES : NO);

        // Invalidate the reason.
        otherContraIndication.setModelObject(null);
        target.addComponent(ObservedContraIndicationPanel.this);
      }

    });
    add(contraIndicationDropDownChoice);

    add(new Label("otherLabel", new StringResourceModel("IfOtherPleaseSpecify", this, null)));

    otherContraIndication = new TextArea("otherCi", new PropertyModel(getModel(), "otherContraindication")) {

      @Override
      public boolean isVisible() {
        // The text area and its associated label should only be displayed when the selected contraindication requires a
        // description. The label's visibility is bound to the text area visibility using a <wicket:enclosure> tag in
        // the markup.
        Contraindication selectedCi = (Contraindication) contraIndicationDropDownChoice.getModelObject();
        return contraIndicationDropDownChoice.isEnabled() && selectedCi != null && selectedCi.getRequiresDescription();
      }

      @Override
      public boolean isRequired() {
        return isVisible();
      }

    };
    otherContraIndication.setOutputMarkupId(true);
    otherContraIndication.setLabel(new StringResourceModel("OtherContraIndication", this, null));
    add(otherContraIndication);

  }

  private IContraindicatable getContraindicatable() {
    return (IContraindicatable) getModelObject();
  }

  private class ContraindicationChoiceRenderer implements IChoiceRenderer {

    private static final long serialVersionUID = 1L;

    public Object getDisplayValue(Object object) {
      Contraindication ci = (Contraindication) object;
      return new MessageSourceResolvableStringModel(ci).getObject();
    }

    public String getIdValue(Object object, int index) {
      Contraindication ci = (Contraindication) object;
      return ci.getCode();
    }

  }

}

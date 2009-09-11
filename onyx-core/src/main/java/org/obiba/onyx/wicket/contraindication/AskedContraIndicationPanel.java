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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.contraindication.IContraindicatable;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AskedContraIndicationPanel extends Panel {

  private static final long serialVersionUID = 1839206247478532673L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AskedContraIndicationPanel.class);

  private static final String YES = "Yes";

  private static final String NO = "No";

  private static final String DOESNOT_KNOW = "DoesNotKnow";

  private List<RadioGroup> radioGroups;

  @SuppressWarnings("serial")
  public AskedContraIndicationPanel(String id, IModel contraindicatable) {
    super(id, contraindicatable);
    setOutputMarkupId(true);

    radioGroups = new ArrayList<RadioGroup>();

    RepeatingView repeat = new RepeatingView("repeat");
    add(repeat);

    for(final Contraindication ci : getContraindicatable().getContraindications(Contraindication.Type.ASKED)) {
      WebMarkupContainer item = new WebMarkupContainer(repeat.newChildId());
      repeat.add(item);

      IModel ciLabelModel = new MessageSourceResolvableStringModel(ci);

      item.add(new Label("ciLabel", ciLabelModel));

      // radio group without default selection
      final RadioGroup radioGroup = new RadioGroup("radioGroup", new Model());
      radioGroups.add(radioGroup);
      radioGroup.setLabel(ciLabelModel);
      item.add(radioGroup);

      ListView radioList = new ListView("radioItem", Arrays.asList(new String[] { YES, NO, DOESNOT_KNOW })) {

        @Override
        protected void populateItem(ListItem listItem) {
          final String key = (String) listItem.getModelObject();
          final ContraIndicationSelection selection = new ContraIndicationSelection();
          selection.setContraIndication(ci);
          selection.setSelectionKey(key);

          Model selectModel = new Model(selection);

          Radio radio = new Radio("radio", selectModel);
          radio.setLabel(new StringResourceModel(key, AskedContraIndicationPanel.this, null));

          // set default selection
          // cannot decide if yes/no/dontknow was selected, so only deal with case the default ci is not null
          // and it was because yes was selected
          if(key == YES && getContraindicatable().isContraindicated() && getContraindicatable().getContraindication().equals(ci)) {
            radioGroup.setModel(selectModel);
          }
          listItem.add(radio);

          FormComponentLabel radioLabel = new SimpleFormComponentLabel("radioLabel", radio);
          listItem.add(radioLabel);
        }

      }.setReuseItems(true);
      radioGroup.add(radioList);
      radioGroup.setRequired(true);
    }

  }

  private IContraindicatable getContraindicatable() {
    return (IContraindicatable) getDefaultModelObject();
  }

  public void saveContraIndicationSelection() {
    getContraindicatable().setContraindication(null);
    for(RadioGroup rg : radioGroups) {
      ContraIndicationSelection ciSelection = (ContraIndicationSelection) rg.getModelObject();
      if(ciSelection.isSelected()) {
        getContraindicatable().setContraindication(ciSelection.getContraIndication());
        // just interested in the first one
        break;
      }
    }
  }

  @SuppressWarnings("serial")
  private class ContraIndicationSelection implements Serializable {

    private String selectionKey;

    private Contraindication contraIndication;

    public String getSelectionKey() {
      return selectionKey;
    }

    public void setSelectionKey(String selectionKey) {
      this.selectionKey = selectionKey;
    }

    public boolean isSelected() {
      return selectionKey.equals(YES) || selectionKey.equals(DOESNOT_KNOW);
    }

    public Contraindication getContraIndication() {
      return contraIndication;
    }

    public void setContraIndication(Contraindication contraIndication) {
      this.contraIndication = contraIndication;
    }

  }

}

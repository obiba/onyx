/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket.wizard;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;

public class TubeBarcodePanel extends Panel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(TubeBarcodePanel.class);

  //
  // Instance Variables
  //

  @SpringBean(name = "activeTubeRegistrationService")
  private ActiveTubeRegistrationService activeTubeRegistrationService;

  private TextField tubeBarcode;

  //
  // Constructors
  //

  public TubeBarcodePanel(String id) {
    super(id);

    add(new TubeBarcodeForm("tubeBarcodeForm"));
    add(new FeedbackPanel("feedback"));
  }

  //
  // Methods
  //

  //
  // Inner Classes
  //

  private class TubeBarcodeForm extends Form {
    //
    // Constants
    //

    private static final long serialVersionUID = 1L;

    //
    // Constructors
    //

    public TubeBarcodeForm(String id) {
      super(id);

      addSubmitLink();
      addTubeBarcodeLabelAndField();
    }

    //
    // Form Methods
    //

    @Override
    protected void onSubmit() {
      log.info("form:TubeBarcodeForm submit (" + tubeBarcode.getModelObjectAsString() + ")");
    }

    //
    // Methods
    //

    private void addSubmitLink() {
      add(new AjaxSubmitLink("submit") {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onSubmit(AjaxRequestTarget target, Form form) {
          String barcode = tubeBarcode.getModelObjectAsString();

          if(barcode.trim().length() != 0) {
            List<MessageSourceResolvable> errors = activeTubeRegistrationService.registerTube(barcode);

            if(!errors.isEmpty()) {
              for(MessageSourceResolvable error : errors) {
                error((new SpringStringResourceModel(error.getCodes()[0], error.getArguments(), error.getCodes()[0]).getString()));
              }
            } else {
              info((new SpringStringResourceModel("Ruby.SuccessfullyRegisteredTube")).getString());
              tubeBarcode.getModel().setObject("");
            }
          }

          target.addComponent(TubeBarcodePanel.this.getParent());
        }
      });
    }

    private void addTubeBarcodeLabelAndField() {
      add(new Label("tubeBarcodeLabel", new SpringStringResourceModel("Ruby.TubeBarcode")));
      tubeBarcode = new TextField("tubeBarcode", new Model(""));

      add(tubeBarcode);
    }
  }
}

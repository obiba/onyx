/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.marble.core.wicket.consent;

import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.marble.core.service.ActiveConsentService;

public class ElectronicConsentPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private ActiveConsentService activeConsentService;

  @SuppressWarnings("serial")
  public ElectronicConsentPanel(String id) {
    super(id);
    setOutputMarkupId(true);
  }

  @Override
  protected void onBeforeRender() {

    if(validate()) {
      addOrReplace(new InlineFrame("pdfSubmitFrame", new ElectronicConsentConfirmedPage()));
    } else {
      addOrReplace(new InlineFrame("pdfSubmitFrame", new ElectronicConsentPage()));
    }

    super.onBeforeRender();
  }

  public boolean validate() {
    return activeConsentService.validateElectronicConsent();
  }

  public boolean isPdfFormSubmited() {
    return activeConsentService.isPdfFormSubmitted();
  }

}

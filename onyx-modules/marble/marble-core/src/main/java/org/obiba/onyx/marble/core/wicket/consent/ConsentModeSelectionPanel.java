/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.marble.core.wicket.consent;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.io.support.LocalizedResourceLoader;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.marble.domain.consent.ConsentMode;
import org.obiba.wicket.markup.html.form.LocaleDropDownChoice;

public class ConsentModeSelectionPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private List<Locale> consentLanguages;

  @SpringBean(name = "consentFormTemplateLoader")
  LocalizedResourceLoader consentFormTemplateLoader;

  @SpringBean
  private ActiveConsentService activeConsentService;

  public ConsentModeSelectionPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    add(createConsentModeRadio());
    add(createConsentLanguageDropDown(consentFormTemplateLoader.getAvailableLocales()));

    // Set default consent mode to electronic.
    activeConsentService.getConsent().setMode(ConsentMode.ELECTRONIC);

  }

  @SuppressWarnings("serial")
  private RadioChoice createConsentModeRadio() {
    RadioChoice consentModeRadio = new RadioChoice("consentMode", new PropertyModel(activeConsentService, "consent.mode"), Arrays.asList(ConsentMode.values()), new ChoiceRenderer()) {

      @Override
      protected boolean localizeDisplayValues() {
        return true;
      }
    };

    consentModeRadio.setRequired(true);

    return consentModeRadio;
  }

  @SuppressWarnings("serial")
  private DropDownChoice createConsentLanguageDropDown(List<Locale> locales) {
    LocaleDropDownChoice consentLanguageDropDown = new LocaleDropDownChoice("consentLanguage", new PropertyModel(activeConsentService, "consent.locale"), locales);
    consentLanguageDropDown.setUseSessionLocale(true);
    consentLanguageDropDown.setRequired(true);
    consentLanguageDropDown.setNullValid(true);
    return consentLanguageDropDown;
  }

}

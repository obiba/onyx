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
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.io.support.LocalizedResourceLoader;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.marble.core.service.ConsentService;
import org.obiba.onyx.marble.domain.consent.ConsentMode;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.wicket.markup.html.form.LocaleDropDownChoice;

public class ConsentModeSelectionPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private List<Locale> consentLanguages;

  @SpringBean(name = "consentFormTemplateLoader")
  LocalizedResourceLoader consentFormTemplateLoader;

  @SpringBean
  private ActiveConsentService activeConsentService;

  @SpringBean
  private ConsentService consentService;

  private RadioChoice<ConsentMode> modeChoice;

  private SeparatorFragment separatorFragment;

  public ConsentModeSelectionPanel(String id) {
    super(id);
    setOutputMarkupId(true);
    List<Locale> locales;

    add(modeChoice = createConsentModeRadio());
    add(separatorFragment = new SeparatorFragment("separator"));

    // Set default consent mode to electronic.
    if(consentService.getSupportedConsentModes().containsAll(EnumSet.allOf(ConsentMode.class))) {
      activeConsentService.getConsent().setMode(ConsentMode.ELECTRONIC);
      locales = consentFormTemplateLoader.getAvailableLocales();
      add(new Label("instruction", new StringResourceModel("ConsentModeSelection", ConsentModeSelectionPanel.this, null)));
    } else {
      activeConsentService.getConsent().setMode(ConsentMode.MANUAL);
      modeChoice.setVisible(false);
      locales = consentService.getSupportedConsentLocales();
      add(new Label("instruction", new StringResourceModel("ConsenLanguageSelection", ConsentModeSelectionPanel.this, null)));
      separatorFragment.setVisible(false);
    }

    add(createConsentLanguageDropDown(locales));
  }

  @SuppressWarnings("serial")
  private RadioChoice<ConsentMode> createConsentModeRadio() {
    RadioChoice<ConsentMode> consentModeRadio = new RadioChoice<ConsentMode>("consentMode", new PropertyModel(activeConsentService, "consent.mode"), Arrays.asList(ConsentMode.values()), new ChoiceRenderer()) {

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

    if(existResourceForLocale(locales)) {
      consentLanguageDropDown.setChoiceRenderer(new ChoiceRenderer() {
        public Object getDisplayValue(Object object) {
          return new SpringStringResourceModel(object.toString()).getString();
        }

        public String getIdValue(Object object, int index) {
          return object.toString();
        }
      });
    } else {
      consentLanguageDropDown.setUseSessionLocale(true);
    }

    consentLanguageDropDown.setRequired(true);
    consentLanguageDropDown.setNullValid(true);
    return consentLanguageDropDown;
  }

  // checks if resource for locales are available in properties files
  private boolean existResourceForLocale(List<Locale> locales) {
    for(Locale locale : locales) {
      if(new SpringStringResourceModel(locale.toString()).getString().equals(locale.toString())) return false;
    }
    return true;
  }

  private class SeparatorFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    public SeparatorFragment(String id) {
      super(id, "separatorFragment", ConsentModeSelectionPanel.this);
    }
  }

}

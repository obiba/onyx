package org.obiba.onyx.marble.core.wicket.consent;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.marble.domain.consent.ConsentMode;

public class ConsentModeSelectionPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private static final List<Locale> consentLanguages = Arrays.asList(new Locale[] { Locale.FRENCH, Locale.ENGLISH });

  @SpringBean
  private ActiveConsentService activeConsentService;

  public ConsentModeSelectionPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    add(createConsentModeRadio());
    add(createConsentLanguageDropDown());

  }

  @SuppressWarnings("serial")
  private RadioChoice createConsentModeRadio() {
    RadioChoice consentModeRadio = new RadioChoice("consentMode", new PropertyModel(activeConsentService.getConsent(), "mode"), Arrays.asList(ConsentMode.values()), new ChoiceRenderer()) {

      @Override
      protected boolean localizeDisplayValues() {
        return true;
      }
    };

    consentModeRadio.setRequired(true);

    return consentModeRadio;
  }

  @SuppressWarnings("serial")
  private DropDownChoice createConsentLanguageDropDown() {

    IChoiceRenderer choiceRenderer = new IChoiceRenderer() {

      public Object getDisplayValue(Object object) {
        Locale lang = (Locale) object;
        return lang.getDisplayLanguage(getLocale());
      }

      public String getIdValue(Object object, int index) {
        return object.toString();
      }

    };

    DropDownChoice consentLanguageDropDown = new DropDownChoice("consentLanguage", new PropertyModel(activeConsentService.getConsent(), "language"), consentLanguages, choiceRenderer) {

      @Override
      protected boolean localizeDisplayValues() {
        return true;
      }

    };

    consentLanguageDropDown.setRequired(true);
    consentLanguageDropDown.setNullValid(true);

    return consentLanguageDropDown;
  }

  public void save() {
    // TODO Auto-generated method stub

  }

}

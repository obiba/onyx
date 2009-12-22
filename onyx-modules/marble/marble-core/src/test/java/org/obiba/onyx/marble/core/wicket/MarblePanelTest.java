/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.core.wicket;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.io.support.LocalizedResourceLoader;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.marble.core.service.ConsentService;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.marble.domain.consent.ConsentMode;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.test.MockSpringApplication;

public class MarblePanelTest {

  private WicketTester tester;

  ActiveConsentService activeConsentService;

  ConsentService consentService;

  EntityQueryService queryService;

  MockSpringApplication application;

  @SuppressWarnings("serial")
  @Before
  public void setup() {
    ExtendedApplicationContextMock mockCtx = new ExtendedApplicationContextMock();

    activeConsentService = createMock(ActiveConsentService.class);
    mockCtx.putBean("activeConsentService", activeConsentService);

    LocalizedResourceLoader consentFormTemplateLoader = new LocalizedResourceLoader() {
      @Override
      public List<Locale> getAvailableLocales() {
        ArrayList<Locale> locales = new ArrayList<Locale>();
        locales.add(Locale.CANADA_FRENCH);
        locales.add(Locale.ENGLISH);
        return locales;
      }
    };
    mockCtx.putBean("consentFormTemplateLoader", consentFormTemplateLoader);
    mockCtx.putBean("moduleRegistry", new ModuleRegistry());
    queryService = createMock(EntityQueryService.class);
    mockCtx.putBean("queryService", queryService);
    consentService = createMock(ConsentService.class);
    mockCtx.putBean("consentService", consentService);
    application = new MockSpringApplication();
    application.setApplicationContext(mockCtx);

    expect(consentService.getSupportedConsentModes()).andReturn(EnumSet.of(ConsentMode.ELECTRONIC, ConsentMode.MANUAL));

    tester = new WicketTester(application);

  }

  private Stage newTestStage() {
    Stage s = new Stage();
    s.setName("Consent");
    return (s);
  }

  @SuppressWarnings("serial")
  @Test
  public void testManualOption() {

    Consent consent;
    expect(activeConsentService.getConsent(true)).andReturn(consent = new Consent());
    expect(activeConsentService.getConsent()).andReturn(consent).anyTimes();
    expect(consentService.getSupportedConsentLocales()).andReturn(Arrays.asList(new Locale[] { Locale.ENGLISH, Locale.FRENCH }));
    expect(consentService.getSupportedConsentModes()).andReturn(EnumSet.of(ConsentMode.ELECTRONIC, ConsentMode.MANUAL));

    EasyMock.replay(consentService);
    EasyMock.replay(activeConsentService);

    tester.startPanel(new TestPanelSource() {
      public Panel getTestPanel(String panelId) {
        return new MarblePanel(panelId, newTestStage());
      }
    });

    FormTester form = tester.newFormTester("panel:content:form");
    form.select("step:panel:consentMode", 1);
    form.select("step:panel:consentLanguage", 1);

    tester.executeAjaxEvent("panel:content:form:nextLink", "onclick");

    tester.assertNoErrorMessage();

    tester.executeAjaxEvent("panel:content:form:adminLink", "onclick");

    form = tester.newFormTester("panel:content:form");
    form.select("step:panel:consentConfirmation", 1);

    tester.executeAjaxEvent("panel:content:form:adminWindow:content:form:customOptionsRight:link:finish", "onclick");

    tester.assertNoErrorMessage();

    EasyMock.verify(consentService);
    EasyMock.verify(activeConsentService);

  }

  @SuppressWarnings("serial")
  @Test
  @Ignore("https://issues.apache.org/jira/browse/WICKET-2616")
  public void testElectronicOption() {

    Consent consent = new Consent() {
      @Override
      public Boolean isAccepted() {
        return true;
      }
    };
    expect(activeConsentService.getConsent(true)).andReturn(consent);
    expect(activeConsentService.getConsent()).andReturn(consent).anyTimes();
    expect(consentService.getSupportedConsentLocales()).andReturn(Arrays.asList(new Locale[] { Locale.ENGLISH, Locale.FRENCH }));
    expect(consentService.getSupportedConsentModes()).andReturn(EnumSet.of(ConsentMode.ELECTRONIC, ConsentMode.MANUAL));

    expect(activeConsentService.validateElectronicConsent()).andReturn(true);
    expect(activeConsentService.isConsentFormSubmitted()).andReturn(true);

    EasyMock.replay(consentService);
    EasyMock.replay(activeConsentService);

    tester.startPanel(new TestPanelSource() {
      public Panel getTestPanel(String panelId) {
        return new MarblePanel(panelId, newTestStage());
      }
    });

    FormTester form = tester.newFormTester("panel:content:form", false);
    form.select("step:panel:consentMode", 0);
    form.select("step:panel:consentLanguage", 1);

    // For some unknown reason, this is broken as of Wicket 1.4.4:
    // https://issues.apache.org/jira/browse/WICKET-2616.
    tester.executeAjaxEvent("panel:content:form:nextLink", "onclick");

    tester.executeAjaxEvent("panel:content:form:adminLink", "onclick");

    tester.executeAjaxEvent("panel:content:form:adminWindow:content:form:customOptionsRight:link:finish", "onclick");

    tester.assertNoErrorMessage();

    EasyMock.verify(consentService);
    EasyMock.verify(activeConsentService);

  }
}

/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.marble.core.wicket.consent;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.IResourceListener;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Resource;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.DynamicWebResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class ElectronicConsentPage extends WebPage implements IResourceListener {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(ElectronicConsentPage.class);

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveConsentService activeConsentService;

  @SpringBean
  private ElectronicConsentTemplateLoader consentTemplateLoader;

  @SuppressWarnings("serial")
  public ElectronicConsentPage() {

    // Add the <embed> tag to html to load the embedded PDF form.
    add(new WebMarkupContainer("embedPdf") {
      @Override
      protected void onComponentTag(ComponentTag tag) {

        // Add link to template in <embed> tag
        CharSequence url = ElectronicConsentPage.this.urlFor(IResourceListener.INTERFACE);
        String srcTagValue = url.toString() + "#toolbar=0&navpanes=0&scrollbar=1";
        tag.put("src", srcTagValue);
        super.onComponentTag(tag);
      }
    });

    // Mount page to specific URL so it can be called from <embed> tag (submit form button).
    try {
      ((WebApplication) getApplication()).mountBookmarkablePage("/uploadConsent", ElectronicConsentUploadPage.class);
    } catch(Exception ex) {
      // Page already mounted.
    }

  }

  @SuppressWarnings("serial")
  public void onResourceRequested() {
    Resource r = createConsentFormResource();
    RequestCycle.get().setRequestTarget(new ResourceStreamRequestTarget(r.getResourceStream()));
  }

  @SuppressWarnings("serial")
  private Resource createConsentFormResource() {

    Resource r = new DynamicWebResource() {
      @Override
      protected ResourceState getResourceState() {

        return new ResourceState() {

          @Override
          public String getContentType() {
            return "application/pdf";
          }

          @Override
          public byte[] getData() {
            ByteArrayOutputStream convertedStream = null;
            if(activeConsentService.isConsentFormSubmitted()) {
              return activeConsentService.getConsent().getPdfForm();
            } else {

              InputStream pdfStream = ElectronicConsentPage.this.getConsentForm();
              convertedStream = new ByteArrayOutputStream();
              byte[] readBuffer = new byte[1024];
              int bytesRead;

              try {
                while((bytesRead = pdfStream.read(readBuffer)) > 0) {
                  convertedStream.write(readBuffer, 0, bytesRead);
                }
              } catch(IOException couldNotReadStream) {
                throw new RuntimeException(couldNotReadStream);
              } finally {
                try {
                  pdfStream.close();
                } catch(IOException e) {
                }
              }
            }

            return convertedStream.toByteArray();
          }

        };
      }

    };

    return r;
  }

  /**
   * Get the consent form with pre-filled information.
   * 
   * @return The initialized consent form.
   */
  protected InputStream getConsentForm() {

    org.springframework.core.io.Resource pdfTemplate = consentTemplateLoader.getConsentTemplate(activeConsentService.getConsent().getLocale());

    PdfReader pdfReader;
    try {
      pdfReader = new PdfReader(pdfTemplate.getInputStream());
    } catch(Exception ex) {
      log.error("Consent Form template cannot be read", ex);
      throw new RuntimeException(ex);
    }

    ByteArrayOutputStream output = setConsentFormFields(pdfReader);
    return new ByteArrayInputStream(output.toByteArray());

  }

  private ByteArrayOutputStream setConsentFormFields(PdfReader pdfReader) {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    PdfStamper stamper = null;

    try {
      stamper = new PdfStamper(pdfReader, output);
      AcroFields form = stamper.getAcroFields();

      setEntityFields(form);
      setSystemFields(form);

      stamper.close();
      output.close();
      pdfReader.close();

      return output;

    } catch(Exception ex) {
      log.error("An error occured while preparing the consent form", ex);
      throw new RuntimeException(ex);
    }
  }

  /**
   * Initialize the consent form fields with data calculated by the application.
   * 
   * @param form The consent form.
   */
  private void setSystemFields(AcroFields form) throws IOException, DocumentException {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    form.setField("System\\.date", formatter.format(new Date()));
  }

  /**
   * Initialize the consent form fields with data coming from data entities.
   * 
   * @param form The consent form.
   */
  private void setEntityFields(AcroFields form) {

    // Set Participant
    Participant participant = activeInterviewService.getParticipant();
    setFields(participant, form);

    // Set User
    User user = activeInterviewService.getInterview().getUser();
    setFields(user, form);

  }

  /**
   * Attempts to match the form fields content with the attribute values of the entity. In the case of a match, the
   * value of the form field is set to the corresponding entity attribute value. The pattern used for form field
   * matching is "EntityName.attributeName", (ex: "Participant.firstName").
   * 
   * @param entity Source entity where the values are coming from.
   * @param form The PDF form where the values will be set.
   */
  private void setFields(Object entity, AcroFields form) {
    Class bean = entity.getClass();

    try {
      for(PropertyDescriptor pd : Introspector.getBeanInfo(bean).getPropertyDescriptors()) {
        Object value = pd.getReadMethod().invoke(entity);
        String fieldName = bean.getSimpleName() + "\\." + pd.getName();
        if(value != null) {
          form.setField(fieldName, value.toString());

          // Set mandatory fields.
          form.setField(fieldName + "\\.mandatoryField", value.toString());
        }
      }
    } catch(Exception ex) {
      log.error("Could not initialize the consent form fields", ex);
      throw new RuntimeException(ex);
    }
  }
}

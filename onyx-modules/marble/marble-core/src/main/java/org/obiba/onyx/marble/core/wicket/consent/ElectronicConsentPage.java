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

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class ElectronicConsentPage extends WebPage implements IResourceListener {

  private static final long serialVersionUID = 1L;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveConsentService activeConsentService;

  @SpringBean
  private ConsentTemplateLoader consentTemplateLoader;

  @SuppressWarnings("serial")
  public ElectronicConsentPage() {

    add(new WebMarkupContainer("embedPdf") {
      @Override
      protected void onComponentTag(ComponentTag tag) {
        CharSequence url = ElectronicConsentPage.this.urlFor(IResourceListener.INTERFACE);
        String srcTagValue = url.toString();
        tag.put("src", srcTagValue);
        super.onComponentTag(tag);
      }
    });

    try {
      ((WebApplication) getApplication()).mountBookmarkablePage("/uploadConsent", ConsentUploadPage.class);
    } catch(Exception ex) {
    }

  }

  @SuppressWarnings("serial")
  public void onResourceRequested() {
    Resource r = createConsentFormResource();
    RequestCycle.get().setRequestTarget(new ResourceStreamRequestTarget(r.getResourceStream()));
  }

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

  protected InputStream getConsentForm() {

    org.springframework.core.io.Resource pdfTemplate = consentTemplateLoader.getConsentTemplate(activeConsentService.getConsent().getLocale());

    PdfReader pdfReader;
    try {
      pdfReader = new PdfReader(pdfTemplate.getInputStream());
    } catch(Exception ex) {
      throw new RuntimeException("Consent Form template cannot be read", ex);
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

      Participant participant = activeInterviewService.getParticipant();
      User user = activeInterviewService.getInterview().getUser();

      setFields(participant, form);
      setFields(user, form);

      stamper.close();
      output.close();
      pdfReader.close();

      return output;

    } catch(Exception ex) {
      throw new RuntimeException("An error occured while preparing the consent form", ex);
    }
  }

  /**
   * Attempts to match the form fields content with the attribute values of the entity. In the case of a match, the
   * value of the form field is set to the corresponding entity attribute value. The pattern used for form field
   * matching is "EntityName_attributeName", (ex: "Participant_firstName").
   * 
   * @param object
   * @param form
   */
  private void setFields(Object entity, AcroFields form) {
    Class bean = entity.getClass();

    try {
      for(PropertyDescriptor pd : Introspector.getBeanInfo(bean).getPropertyDescriptors()) {
        Object value = pd.getReadMethod().invoke(entity);
        String fieldName = bean.getSimpleName() + "_" + pd.getName();
        if(value != null) {
          form.setField(fieldName, value.toString());
        }
      }
    } catch(Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}

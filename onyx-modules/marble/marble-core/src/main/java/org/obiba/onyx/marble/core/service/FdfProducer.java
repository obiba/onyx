/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.core.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.io.support.LocalizedResourceLoader;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.io.Resource;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.CustomFdfWriter;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.AcroFields.Item;

/**
 * Builds a FDF stream used to fill-in a PDF form. Allows filling in data from the Participant, the administrating User
 * and some system fields. The available system fields are "Date", "AcceptURL" and "RefuseURL". Any field named "Date"
 * will be replaced by the current system Date and formatted using the specified date format
 * {@link #setDateFormat(String)}. The URL system fields allow specifying the URLs to submit the PDF to when the
 * consent is accepted or refused.
 */
public class FdfProducer {

  private static final Logger log = LoggerFactory.getLogger(FdfProducer.class);

  private ActiveConsentService activeConsentService;

  private ActiveInterviewService activeInterviewService;

  private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

  private Map<String, Object> replaceContext;

  LocalizedResourceLoader consentFormTemplateLoader;

  public void setActiveConsentService(ActiveConsentService consentService) {
    this.activeConsentService = consentService;
  }

  public void setActiveInterviewService(ActiveInterviewService interviewService) {
    this.activeInterviewService = interviewService;
  }

  public void setDateFormat(String format) {
    this.dateFormatter = new SimpleDateFormat(format);
  }

  public void setConsentFormTemplateLoader(LocalizedResourceLoader consentFormTemplateLoader) {
    this.consentFormTemplateLoader = consentFormTemplateLoader;
  }

  /**
   * Returns an InputStream to the PDF template
   * @return an InputStream to the PDF template.
   * @throws IOException
   */
  public InputStream getPdfTemplate() throws IOException {
    Locale pdfLocale = activeConsentService.getConsent().getLocale();
    Resource pdfTemplate = consentFormTemplateLoader.getLocalizedResource(pdfLocale);
    if(pdfTemplate == null) {
      log.error("Expected non-null PDF template for locale {}. Make sure the ElectronicConsentTemplateLoader instance is configured properly.", pdfLocale);
      throw new IllegalStateException("No PDF template for locale " + pdfLocale);
    }
    return pdfTemplate.getInputStream();
  }

  /**
   * Builds a FDF file that can be used to merge with the a consent PDF template.
   * 
   * @param pdfUrl the URL of the PDF file
   * @param acceptUrl the URL to submit the PDF when the consent is accepted
   * @param refuseUrl the URL to submit the PDF when the consent is refused
   * @return a FDF formated byte array.
   * @throws IOException when an unexpected error occurs
   */
  public byte[] buildFdf(String pdfUrl, String acceptUrl, String refuseUrl) throws IOException {

    buildReplaceContext(acceptUrl, refuseUrl);

    PdfReader pdfReader = new PdfReader(getPdfTemplate());
    log.debug("PDF template has {} fields.", pdfReader.getAcroFields().getFields().size());

    CustomFdfWriter fdf = new CustomFdfWriter();
    // Initialize the FDF with fields that already have a value.
    fdf.setFields(pdfReader);
    fdf.setFile(pdfUrl);

    try {
      setFields(pdfReader.getAcroFields(), fdf);
    } catch(DocumentException e) {
      log.error("An error occurred during FDF file generation.", e);
      throw new RuntimeException(e);
    }

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    fdf.writeTo(output);
    output.close();
    pdfReader.close();

    return output.toByteArray();
  }

  /**
   * Builds a map of key values that is used to fill-in the FDF form. This map could eventually be provided by an
   * external class which would allow a much more flexible/extensible replacement model.
   * 
   * @param acceptUrl the URL to submit the PDF when the consent is accepted
   * @param refuseUrl the URL to submit the PDF when the consent is refused
   */
  protected void buildReplaceContext(String acceptUrl, String refuseUrl) {
    replaceContext = new HashMap<String, Object>();
    replaceContext.put("Participant", activeInterviewService.getParticipant());
    replaceContext.put("User", activeInterviewService.getInterview().getUser());
    replaceContext.put("Date", dateFormatter.format(new Date()));
    replaceContext.put("AcceptURL", acceptUrl);
    replaceContext.put("RefuseURL", refuseUrl);
  }

  private boolean isFieldPushButton(int type) {
    return type == AcroFields.FIELD_TYPE_PUSHBUTTON;
  }

  @SuppressWarnings("unchecked")
  protected void setFields(AcroFields acroFields, CustomFdfWriter fdf) throws IOException, DocumentException {
    Map<String, Item> fields = acroFields.getFields();
    log.debug("PDF template has {} fields", fields.size());
    for(String fieldName : fields.keySet()) {
      log.debug("Processing PDF template field {}", fieldName);
      String shortFieldName = getFieldShortName(fieldName);

      log.debug("Field name converted to {}", shortFieldName);

      Object fieldValue = findValue(shortFieldName);
      if(fieldValue != null) {
        if(isFieldPushButton(acroFields.getFieldType(fieldName))) {
          log.info("Setting submit button {} with URL {}", fieldName, fieldValue);
          fdf.setFieldAsAction(fieldName, PdfAction.createSubmitForm(fieldValue.toString(), null, PdfAction.SUBMIT_PDF));
        } else {
          log.info("Setting field {} with value {} which is of type {}", new Object[] { fieldName, fieldValue, fieldValue.getClass().getName() });
          fdf.setFieldAsString(fieldName, fieldValue.toString());
        }
      } else {
        log.debug("No value found for field (or value is null). No replacement made.");
      }
    }
    log.debug("PDF processing complete");
  }

  /**
   * Returns the value in the replace context with for the specified key. This method handles keys that are paths to an
   * object's attribute.
   * 
   * @param key the key to lookup
   * @return the value for the specified key or null if non exist
   */
  protected Object findValue(String key) {
    if(key.contains(".")) {
      // Handle field names of type "Participant.firstName"
      String values[] = key.split("\\.", 2);
      if(values != null && values.length == 2) {
        // The entity's name
        String entityName = values[0];
        // The entity attribute's name
        String attributeName = values[1];

        Object entity = replaceContext.get(entityName);
        if(entity != null) {
          log.info("Found object of type {}", entity.getClass().getName());
          BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(entity);
          if(wrapper.isReadableProperty(attributeName)) {
            return wrapper.getPropertyValue(attributeName);
          } else {
            log.info("Class {} does not have a readable property {}. No replacement made.", entity.getClass(), attributeName);
          }
        }
      }
    } else {
      return replaceContext.get(key);
    }
    return null;
  }

  /**
   * Converts a fully qualified form name into its short name.
   * 
   * @param longName the fqn
   * @return its short version
   */
  private String getFieldShortName(String longName) {
    // By default, a field name in a PDF form starts with "form[0].page[0].", concatenated with the custom name that we
    // gave to this field
    String pattern = "([a-zA-Z0-9]+\\[[0-9]+\\]\\.){2}";

    // Delete "form[0].page[0]." string to keep only the custom name given to the field
    // The field name ends with [0] which is removed
    // PDF doesn't allow dots in field names. If you use one, it is substituted by "\.", so we undo this also
    return longName.replaceFirst(pattern, "").replaceFirst("\\[[0-9]+\\]", "").replaceAll("\\\\", "");
  }

}

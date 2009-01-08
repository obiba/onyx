package org.obiba.onyx.mica.core.service.impl;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.io.support.LocalizedResourceLoader;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.mica.core.service.ModuleReportContributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class JadeReportContributor implements ModuleReportContributor {

  private static final Logger log = LoggerFactory.getLogger(JadeReportContributor.class);

  private ActiveInterviewService activeInterviewService;

  private InstrumentRunService instrumentRunService;

  private InstrumentService instrumentService;

  private LocalizedResourceLoader reportTemplateLoader;

  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  public JadeReportContributor() {
    super();
  }

  public String getName() {
    return ("jade");
  }

  public boolean isExists() {
    return true;
  }

  /**
   * Returns the constructed report as an InputStream This method loads the report template and fills it with
   * participant information, physical measurement results and interview date
   * @param locale: report's locale
   * @return InputStream
   */
  public InputStream getReport(Locale locale) {

    // Get report template
    Resource resource = reportTemplateLoader.getLocalizedResource(locale);

    // Read report template
    PdfReader pdfReader;
    try {
      pdfReader = new PdfReader(resource.getInputStream());
    } catch(Exception ex) {
      throw new RuntimeException("Report to participant template cannot be read", ex);
    }

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    PdfStamper stamper = null;

    // Fill report
    try {
      stamper = new PdfStamper(pdfReader, output);
      AcroFields form = stamper.getAcroFields();

      // Fill with participant information
      Participant participant = activeInterviewService.getParticipant();
      setPartcipantFields(participant, form);

      // Fill with Jade contribution
      setMeasurementFields(participant, form);

      // Fill date field
      String formattedDate = dateFormat.format(new Date());
      form.setField("DateInterview\\.date", formattedDate);
      form.setField("Date", formattedDate);
      stamper.setFormFlattening(true);

      stamper.close();
      output.close();
      pdfReader.close();

      return new ByteArrayInputStream(output.toByteArray());

    } catch(Exception ex) {
      throw new RuntimeException("An error occured while preparing the report to participant", ex);
    }
  }

  /**
   * Fills participant report with physical measurement results
   * @param participant : current participant
   * @param form : form extracted form participant report template
   */
  @SuppressWarnings("unchecked")
  private void setMeasurementFields(Participant participant, AcroFields form) {

    log.info("Reading PDF template form");

    // Get PDF template fields list
    HashMap<String, String> fieldsList = form.getFields();

    try {

      // Loop over fields list
      for(Entry<String, String> oneField : fieldsList.entrySet()) {

        // Get field name
        String fieldName = oneField.getKey();

        HashMap<String, String> instrumentList = splitData(fieldName);

        for(Entry<String, String> oneInstrument : instrumentList.entrySet()) {

          if(oneInstrument != null) {

            // Extact instrument type name
            String wInstrumentType = oneInstrument.getKey();

            // Extract parameter name
            String wParameterName = oneInstrument.getValue();

            InstrumentType instrumentType = instrumentService.getInstrumentType(wInstrumentType);

            if(instrumentType != null) {

              log.info("Getting instrument type {}", instrumentType.getName());

              // If instrument type is not null, try to get runValue
              InstrumentRunValue runValue = instrumentRunService.findInstrumentRunValue(participant, instrumentType, wParameterName);

              if(runValue != null) {

                // If runValue is not null, get parameter value and measurement unit
                InstrumentParameter instrumentParameter = runValue.getInstrumentParameter();

                log.info("Getting instrument parameter {}", instrumentParameter.getCode());

                String parameterUnit = instrumentParameter.getMeasurementUnit();

                // Set data in pdf form
                try {
                  if(parameterUnit != null) {
                    form.setField(oneField.getKey(), runValue.getData().getValueAsString().toString() + " " + parameterUnit);
                  } else {
                    form.setField(oneField.getKey(), runValue.getData().getValueAsString().toString());
                  }
                } catch(IOException e) {
                  throw new RuntimeException("Input/Output Exception", e);
                } catch(DocumentException e) {
                  throw new RuntimeException("Document Exception", e);
                }
              }
            } else {
              log.info("The field being processed is not a physical measurement result");
            }
          }
        }
      }
    } catch(Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @SuppressWarnings("unchecked")
  private void setPartcipantFields(Object object, AcroFields form) {
    Class bean = object.getClass();

    try {
      for(PropertyDescriptor pd : Introspector.getBeanInfo(bean).getPropertyDescriptors()) {
        Object value = pd.getReadMethod().invoke(object);
        String fieldName = bean.getSimpleName() + "\\." + pd.getName();

        if(value != null) {
          form.setField(fieldName, value.toString());
        }
      }
    } catch(Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Returns an HashMap where keys are instrument types name and values are parameters name.
   * @param string : field name in the PDF report template<
   * @returns HashMap<String, String>
   */
  private HashMap<String, String> splitData(String string) {

    HashMap<String, String> result = new HashMap<String, String>();
    String type;
    String parameter;

    // By default, a field name in a PDF form starts with "form[0].page[0].", concatenated with the custom name taht we
    // gave to this field
    String pattern = "([a-zA-Z0-9]+\\[+[0-9]+\\]+\\.){2}";

    // Delete "form[0].page[0]." string to keep only the custom name given to the field
    String field = string.replaceFirst(pattern, "");

    // When a dot (".") is used in a field name, it's automatically replaced by "\.". In our case, the dot is used to
    // link an instrument type to its parameter

    int fstIndex = field.indexOf("\\.");

    if(fstIndex != -1) {

      // If multiple instrument types are used for the same field, they are separated by "-"
      String[] list = field.split("-");
      for(int i = 0; i < list.length; i++) {

        // Find the dot index that separates the instrument type and its parameter
        int scdIndex = list[i].indexOf("\\.");

        type = new String();
        // Get instrument type name
        type = list[i].substring(0, scdIndex);

        if(scdIndex != list[i].length()) {

          // By default, a PDF form field ends with "[0]", so we delete it
          int quoteIndex = list[i].indexOf("[");
          parameter = new String();

          if(quoteIndex != -1) {
            // Get instrument parameter
            parameter = list[i].substring(scdIndex + 2, quoteIndex);

          } else {
            // Get instrument parameter
            parameter = list[i].substring(scdIndex + 2);

          }
          if(parameter != null) {
            // Add to hashMap
            result.put(type, parameter);
          }
        }
      }
      return result;
    }
    return null;
  }

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public void setInstrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

  public void setReportTemplateLoader(LocalizedResourceLoader reportTemplateLoader) {
    this.reportTemplateLoader = reportTemplateLoader;
  }

  public void setDateFormat(String format) {
    this.dateFormat = new SimpleDateFormat(format);
  }

}
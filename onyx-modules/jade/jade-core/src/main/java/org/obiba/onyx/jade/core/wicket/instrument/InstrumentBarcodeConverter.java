package org.obiba.onyx.jade.core.wicket.instrument;

import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;

/**
 * Converts the instrument barcode, supposed to be unique, to the corresponding instrument. 
 * @author Yannick Marcon
 *
 */
@SuppressWarnings("serial")
public class InstrumentBarcodeConverter implements IConverter {

  private EntityQueryService queryService;

  private boolean activeOnly;

  public InstrumentBarcodeConverter(EntityQueryService queryService) {
    this(queryService, true);
  }

  public InstrumentBarcodeConverter(EntityQueryService queryService, boolean activeOnly) {
    this.queryService = queryService;
    this.activeOnly = activeOnly;
  }

  public Object convertToObject(String value, Locale locale) {
    if(value == null) return null;
    Instrument template = new Instrument();
    template.setBarcode(value);
    Instrument instrument = queryService.matchOne(template);

    if (instrument == null) {
      ConversionException cex = new ConversionException("No instrument for barcode: '" + value + "'");
      cex.setResourceKey("InstrumentBarcodeConverter.NoInstrumentForBarcode");
      throw cex;
    }
    else if(activeOnly && !instrument.getStatus().equals(InstrumentStatus.ACTIVE)) {
      ConversionException cex = new ConversionException("Not an active instrument: '" + value + "'");
      cex.setResourceKey("InstrumentBarcodeConverter.NotAnActiveInstrument");
      throw cex;
    }

    return instrument;
  }

  public String convertToString(Object value, Locale locale) {
    if(value == null) return null;
    else
      return ((Instrument) value).getBarcode();
  }

}

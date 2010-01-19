/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.instrument;

import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.InstrumentService;

/**
 * Converts the instrument barcode, supposed to be unique, to the corresponding instrument.
 * @author Yannick Marcon
 * 
 */
@SuppressWarnings("serial")
public class InstrumentBarcodeConverter implements IConverter {

  private EntityQueryService queryService;

  private InstrumentType instrumentType;

  InstrumentService instrumentService;

  public InstrumentBarcodeConverter(EntityQueryService queryService, InstrumentService instrumentService, InstrumentType instrumentType) {
    this.instrumentType = instrumentType;
    this.queryService = queryService;
    this.instrumentService = instrumentService;
  }

  public Object convertToObject(String value, Locale locale) {
    if(value == null) return null;
    Instrument template = new Instrument();
    template.setBarcode(value);

    Instrument instrument = queryService.matchOne(template);
    if(instrument == null) {
      displayConversionErrorMsg(value, "InstrumentBarcodeConverter.NoInstrumentForBarcode");
    } else if(!instrument.getTypes().contains(instrumentType.getName())) {
      displayConversionErrorMsg(value, "InstrumentBarcodeConverter.WrongInstrumentType");
    } else if(!instrument.getStatus().equals(InstrumentStatus.ACTIVE)) {
      displayConversionErrorMsg(value, "InstrumentBarcodeConverter.NotAnActiveInstrument");
    } else if(!instrumentService.isActiveInstrumentOfCurrentWorkstation(instrument, instrumentType)) {
      displayConversionErrorMsg(value, "InstrumentBarcodeConverter.NotAnActiveInstrumentForCurrentWorkstation");
    }

    return instrument;
  }

  public String convertToString(Object value, Locale locale) {
    if(value == null) return null;
    else
      return ((Instrument) value).getBarcode();
  }

  private void displayConversionErrorMsg(String value, String msgKey) {
    ConversionException cex = new ConversionException("No instrument for barcode: '" + value + "'");
    cex.setResourceKey(msgKey);
    throw cex;
  }
}

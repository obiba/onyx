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

  private boolean activeOnly;

  private InstrumentType instrumentType;

  InstrumentService instrumentService;

  public InstrumentBarcodeConverter(EntityQueryService queryService, InstrumentService instrumentService, InstrumentType instrumentType) {
    this(queryService, instrumentService, true);
    this.instrumentType = instrumentType;
  }

  public InstrumentBarcodeConverter(EntityQueryService queryService, InstrumentService instrumentService, boolean activeOnly) {
    this.queryService = queryService;
    this.activeOnly = activeOnly;
    this.instrumentService = instrumentService;
  }

  public Object convertToObject(String value, Locale locale) {
    if(value == null) return null;
    Instrument template = new Instrument();
    template.setBarcode(value);
    template.setType(instrumentType.getName());

    Instrument instrument = queryService.matchOne(template);

    if(instrument == null) {
      ConversionException cex = new ConversionException("No instrument for barcode: '" + value + "'");
      cex.setResourceKey("InstrumentBarcodeConverter.NoInstrumentForBarcode");
      throw cex;
    } else if(activeOnly && !instrument.getStatus().equals(InstrumentStatus.ACTIVE)) {
      ConversionException cex = new ConversionException("Not an active instrument: '" + value + "'");
      cex.setResourceKey("InstrumentBarcodeConverter.NotAnActiveInstrument");
      throw cex;
    } else if(!instrumentService.isActiveInstrumentOfCurrentWorkstation(instrument)) {
      ConversionException cex = new ConversionException("Not an active instrument for current workstation: '" + value + "'");
      cex.setResourceKey("InstrumentBarcodeConverter.NotAnActiveInstrumentForCurrentWorkstation");
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

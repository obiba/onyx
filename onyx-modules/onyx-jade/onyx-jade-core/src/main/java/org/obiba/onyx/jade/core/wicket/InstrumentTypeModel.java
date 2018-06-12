/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket;

import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.wicket.model.SpringDetachableModel;

public class InstrumentTypeModel extends SpringDetachableModel<InstrumentType> {
  //
  // Constants
  //
  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean
  private InstrumentService instrumentService;

  private String instrumentTypeName;

  //
  // Constructors
  //

  public InstrumentTypeModel(InstrumentType instrumentType) {
    super(instrumentType);
    this.instrumentTypeName = instrumentType.getName();
  }

  //
  // SpringDetachableModel Methods
  //

  @Override
  protected InstrumentType load() {
    return instrumentService.getInstrumentType(instrumentTypeName);
  }

}

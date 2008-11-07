/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.obiba.onyx.core.domain.contraindication.Contraindication;

/**
 * <p>
 * This class is used to configure the tube registration process.
 * </p>
 * 
 * <p>
 * A <code>TubeRegistrationConfiguration</code> contains the following information:
 * <ul>
 * <li>conditions (observed or asked) that "contra-indicate" collection of samples</li>
 * <li>the tube barcode structure</code>
 * <li>the expected number of tubes to collected and registered (per participant)</li>
 * <li>a pre-defined set of tube registration remarks</li>
 * </ul>
 * </p>
 */
public class TubeRegistrationConfiguration {
  //
  // Instance Variables
  //

  private BarcodeStructure barcodeStructure;

  private int expectedTubeCount;

  private List<Contraindication> observedContraindications;

  private List<Contraindication> askedContraindications;

  private List<Remark> availableRemarks;

  //
  // Constructors
  //

  public TubeRegistrationConfiguration() {
    observedContraindications = new ArrayList<Contraindication>();
    askedContraindications = new ArrayList<Contraindication>();
    availableRemarks = new ArrayList<Remark>();
  }

  //
  // Methods
  //

  public void setBarcodeStructure(BarcodeStructure barcodeStructure) {
    this.barcodeStructure = barcodeStructure;
  }

  public BarcodeStructure getBarcodeStructure() {
    return barcodeStructure;
  }

  public void setExpectedTubeCount(int expectedTubeCount) {
    this.expectedTubeCount = expectedTubeCount;
  }

  public int getExpectedTubeCount() {
    return expectedTubeCount;
  }

  public void setContraindications(List<Contraindication> contraIndications) {
    observedContraindications.clear();
    askedContraindications.clear();

    if(contraIndications != null) {
      for(Contraindication contraIndication : contraIndications) {
        if(contraIndication.getType().equals(Contraindication.Type.OBSERVED)) {
          observedContraindications.add(contraIndication);
        } else if(contraIndication.getType().equals(Contraindication.Type.ASKED)) {
          askedContraindications.add(contraIndication);
        }
      }
    }
  }

  public List<Contraindication> getObservedContraindications() {
    return Collections.unmodifiableList(observedContraindications);
  }

  public List<Contraindication> getAskedContraindications() {
    return Collections.unmodifiableList(askedContraindications);
  }

  public void setAvailableRemarks(List<Remark> remarks) {
    availableRemarks.clear();

    if(remarks != null) {
      availableRemarks.addAll(remarks);
    }
  }

  public List<Remark> getAvailableRemarks() {
    return Collections.unmodifiableList(availableRemarks);
  }
}

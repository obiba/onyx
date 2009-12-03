/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.magma;

import java.util.HashSet;
import java.util.Set;

import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.beans.BeanPropertyVariableValueSource;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.ruby.core.domain.BarcodePart;
import org.obiba.onyx.ruby.core.domain.BarcodeStructure;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.domain.parser.IBarcodePartParser;

/**
 * Factory for creating VariableValueSources for tube barcode part variables.
 */
public class TubeBarcodePartVariableValueSourceFactory extends BeanVariableValueSourceFactory<BarcodePart> {
  //
  // Instance Variables
  //

  private TubeRegistrationConfiguration tubeRegistrationConfiguration;

  //
  // Constructors
  //

  public TubeBarcodePartVariableValueSourceFactory(TubeRegistrationConfiguration tubeRegistrationConfiguration) {
    super("Participant", BarcodePart.class);
    this.tubeRegistrationConfiguration = tubeRegistrationConfiguration;
  }

  //
  // BeanVariableValueSourceFactory Methods
  //

  @Override
  public Set<VariableValueSource> createSources(String collection) {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    BarcodeStructure barcodeStructure = tubeRegistrationConfiguration.getBarcodeStructure();

    for(IBarcodePartParser partParser : barcodeStructure.getParsers()) {
      String barcodePartVariableName = partParser.getVariableName();
      if(barcodePartVariableName != null) {
        String variableName = lookupVariableName(barcodePartVariableName);
        Variable variable = this.doBuildVariable(collection, TextType.get().getJavaClass(), variableName);
        sources.add(new BeanPropertyVariableValueSource(variable, BarcodePart.class, "partValue"));
      }
    }

    return sources;
  }

  //
  // Methods
  //

  /**
   * Returns the names of barcode part variables that are keys/identifiers.
   * 
   * @return names of barcode part variables that are keys/identifiers
   */
  public Set<String> getKeyVariableNames() {
    Set<String> keyVariableNames = new HashSet<String>();

    BarcodeStructure barcodeStructure = tubeRegistrationConfiguration.getBarcodeStructure();

    for(IBarcodePartParser partParser : barcodeStructure.getParsers()) {
      String barcodePartVariableName = partParser.getVariableName();
      if(barcodePartVariableName != null) {
        if(partParser.isKey()) {
          keyVariableNames.add(barcodePartVariableName);
        }
      }
    }

    return keyVariableNames;
  }

}

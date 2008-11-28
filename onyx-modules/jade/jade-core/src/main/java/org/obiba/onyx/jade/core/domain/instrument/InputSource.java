/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.instrument;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.jade.core.service.InputSourceVisitor;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "input_source_type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("InputSource")
public abstract class InputSource extends AbstractEntity {

  private static final long serialVersionUID = -2701979694735615418L;

  @OneToMany(mappedBy = "inputSource")
  private List<InstrumentInputParameter> instrumentInputParameters;
  
  public List<InstrumentInputParameter> getInstrumentInputParameters() {
    return instrumentInputParameters != null ? instrumentInputParameters : (instrumentInputParameters = new ArrayList<InstrumentInputParameter>());
  }

  public void addInstrumentInputParameter(InstrumentInputParameter parameter) {
    if(parameter != null) {
      getInstrumentInputParameters().add(parameter);
      parameter.setInputSource(this);
    }
  }

  public abstract void accept(InputSourceVisitor visitor);
  
  public abstract boolean isReadOnly();
  
}

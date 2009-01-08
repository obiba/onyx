/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.seed;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.jade.core.domain.instrument.FixedSource;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentComputedOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.domain.instrument.MultipleOutputParameterSource;
import org.obiba.onyx.jade.core.domain.instrument.OperatorSource;
import org.obiba.onyx.jade.core.domain.instrument.OutputParameterSource;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantPropertySource;
import org.obiba.onyx.jade.core.domain.instrument.validation.EqualsParameterCheck;
import org.obiba.onyx.jade.core.domain.instrument.validation.EqualsValueCheck;
import org.obiba.onyx.jade.core.domain.instrument.validation.ParameterSpreadCheck;
import org.obiba.onyx.jade.core.domain.instrument.validation.RangeCheck;
import org.obiba.wicket.util.seed.XstreamResourceDatabaseSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;

public class JadeDatabaseSeed extends XstreamResourceDatabaseSeed {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private PersistenceManager persistenceManager;

  private boolean toPersist = false;

  public void setPersistenceManager(PersistenceManager persistenceManager) {
    this.persistenceManager = persistenceManager;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void handleXstreamResult(Resource resource, Object result) {
    if(result != null && result instanceof InstrumentType) {
      InstrumentType type = (InstrumentType) result;
      List<InstrumentParameter> parameters = type.getInstrumentParameters();
      log.info("Loaded instrument type {} with {} parameters.", type.getName(), (parameters != null ? parameters.size() : -1));
      Set<String> parameterCodes = new HashSet<String>();
      for(InstrumentParameter parameter : parameters) {
        if(parameterCodes.add(parameter.getCode()) == false) {
          log.error("Instrument descriptor for type {} is invalid. Multiple parameters with the same code '{}' are defined. Parameter codes must be unique for an instrument type.", type.getName(), parameter.getCode());
          throw new IllegalStateException("Duplicate parameter code for type '" + type.getName() + "': " + parameter.getCode());
        }
      }
      if(toPersist) {
        persistenceManager.save(type);
      }
    }
  }

  @Override
  protected boolean shouldSeed(WebApplication application) {
    toPersist = (persistenceManager.list(InstrumentType.class).size() == 0);
    // read the seeding file but optionnaly persist entity (always need to seed instrument descriptor service)
    return true;
  }

  @Override
  protected void initializeXstream(XStream xstream) {
    super.initializeXstream(xstream);
    xstream.alias("instrumentType", InstrumentType.class);
    xstream.alias("instrument", Instrument.class);
    xstream.alias("contraIndication", Contraindication.class);
    xstream.alias("contraindication", Contraindication.class);
    xstream.alias("interpretative", InterpretativeParameter.class);
    xstream.alias("input", InstrumentInputParameter.class);
    xstream.alias("output", InstrumentOutputParameter.class);
    xstream.alias("computedOutput", InstrumentComputedOutputParameter.class);
    xstream.alias("fixedSource", FixedSource.class);
    xstream.alias("participantPropertySource", ParticipantPropertySource.class);
    xstream.alias("outputParameterSource", OutputParameterSource.class);
    xstream.alias("multipleOutputParameterSource", MultipleOutputParameterSource.class);
    xstream.alias("operatorSource", OperatorSource.class);
    xstream.alias("equalsValueCheck", EqualsValueCheck.class);
    xstream.alias("equalsParameterCheck", EqualsParameterCheck.class);
    xstream.alias("rangeCheck", RangeCheck.class);
    xstream.alias("parameterSpreadCheck", ParameterSpreadCheck.class);
  }
}

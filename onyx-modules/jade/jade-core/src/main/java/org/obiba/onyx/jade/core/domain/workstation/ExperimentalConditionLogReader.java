/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.workstation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;

public class ExperimentalConditionLogReader {
  private XStream xstream = new XStream();

  private Resource[] resources;

  public ExperimentalConditionLogReader() {
    xstream.processAnnotations(ExperimentalConditionLog.class);
    xstream.processAnnotations(InstrumentCalibration.class);
  }

  public void setResources(Resource[] resources) {
    this.resources = resources;
  }

  @SuppressWarnings("unchecked")
  public List<ExperimentalConditionLog> read() throws IOException {
    List<ExperimentalConditionLog> experimentalConditionLogs = new ArrayList<ExperimentalConditionLog>();

    for(int i = 0; i < this.resources.length; i++) {
      Resource resource = this.resources[i];

      if(resource.exists()) {
        experimentalConditionLogs.addAll((List<ExperimentalConditionLog>) xstream.fromXML(resource.getInputStream()));
      }
    }

    return experimentalConditionLogs;
  }
}

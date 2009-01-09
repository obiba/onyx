/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.core.service.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.engine.state.StageExecutionContext;
import org.springframework.beans.factory.FactoryBean;

public class InterviewStageContextsFactoryBean implements FactoryBean {

  private Map<Serializable, Map<Serializable, StageExecutionContext>> interviewStageContextsMap;

  public Class getObjectType() {
    return Map.class;
  }

  public Object getObject() throws Exception {
    if(interviewStageContextsMap == null) {
      interviewStageContextsMap = new HashMap<Serializable, Map<Serializable, StageExecutionContext>>();
    }

    return interviewStageContextsMap;
  }

  public boolean isSingleton() {
    return true;
  }

}

/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.service;

import org.obiba.onyx.jade.core.domain.instrument.FixedSource;
import org.obiba.onyx.jade.core.domain.instrument.MultipleOutputParameterSource;
import org.obiba.onyx.jade.core.domain.instrument.OperatorSource;
import org.obiba.onyx.jade.core.domain.instrument.OutputParameterSource;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantPropertySource;

public interface InputSourceVisitor {
  
  public void visit(FixedSource source);

  public void visit(OperatorSource source);

  public void visit(OutputParameterSource source);
  
  public void visit(MultipleOutputParameterSource source);

  public void visit(ParticipantPropertySource source);
  
}

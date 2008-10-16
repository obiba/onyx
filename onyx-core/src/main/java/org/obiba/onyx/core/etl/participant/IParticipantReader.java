/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.etl.participant;

import java.io.IOException;
import java.io.InputStream;

import org.obiba.core.validation.exception.ValidationRuntimeException;

public interface IParticipantReader {

  public void process(InputStream input) throws IOException, ValidationRuntimeException;
  
  public void addParticipantReadListener(IParticipantReadListener listener);
  
  public void removeParticipantReadListener(IParticipantReadListener listener);
  
}

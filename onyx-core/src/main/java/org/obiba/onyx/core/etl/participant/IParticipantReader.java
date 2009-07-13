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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface IParticipantReader {

  public void process(InputStream input, List<IParticipantReadListener> listeners) throws IOException, IllegalArgumentException;

  public boolean accept(File dir, String name);

}

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

import java.io.Serializable;

import org.obiba.onyx.jade.core.service.InputSourceVisitor;

public abstract class InputSource implements Serializable {

  private static final long serialVersionUID = -2701979694735615418L;

  public abstract void accept(InputSourceVisitor visitor);

  public abstract boolean isReadOnly();

}

/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.model;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Base class for making {@link LoadableDetachableModel} that supports Spring injection using {@link SpringBean}
 * annotation.
 */
public abstract class SpringDetachableModel<T> extends LoadableDetachableModel<T> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public SpringDetachableModel() {
    super();
    InjectorHolder.getInjector().inject(this);
  }

  public SpringDetachableModel(T object) {
    super(object);
    InjectorHolder.getInjector().inject(this);
  }
}

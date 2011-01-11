/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.utils;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.model.IModel;

/**
 *
 */
public class MapModel<T> implements IModel<T> {

  private static final long serialVersionUID = 1L;

  /** Any model object */
  private Map<String, T> map;

  private String expression;

  public MapModel(Map<String, T> map, final String expression) {
    if(map == null) {
      throw new IllegalArgumentException("Parameter map cannot be null");
    }
    if(StringUtils.isBlank(expression)) {
      throw new IllegalArgumentException("Parameter expression cannot be null or empty");
    }
    this.map = map;
    this.expression = expression;
  }

  public MapModel(IModel<Map<String, T>> model, final String expression) {
    if(model == null || model.getObject() == null) {
      throw new IllegalArgumentException("Parameter model and model object  cannot be null");
    }
    if(StringUtils.isBlank(expression)) {
      throw new IllegalArgumentException("Parameter expression cannot be null or empty");
    }
    this.map = model.getObject();
    this.expression = expression;
  }

  @Override
  public T getObject() {
    return map.get(expression);
  }

  @Override
  public void setObject(T object) {
    if(object == null) {
      map.remove(expression);
    } else {
      map.put(expression, object);
    }
  }

  @Override
  public void detach() {
  }
}

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

import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.html.form.Form;

/**
 * 
 */
public abstract class AjaxIndicatingSubmitLink extends AjaxSubmitLink implements IAjaxIndicatorAware {

  private static final long serialVersionUID = 1L;

  private final AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender();

  public AjaxIndicatingSubmitLink(String id, Form<?> form) {
    super(id, form);
    add(indicatorAppender);
  }

  public AjaxIndicatingSubmitLink(String id) {
    super(id);
    add(indicatorAppender);
  }

  public String getAjaxIndicatorMarkupId() {
    return indicatorAppender.getMarkupId();
  }

}

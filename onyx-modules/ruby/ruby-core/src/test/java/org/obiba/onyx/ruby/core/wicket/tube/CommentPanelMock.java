/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket.tube;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

public class CommentPanelMock extends FormMock {

  private static final long serialVersionUID = 1L;

  public CommentPanelMock(String id, IModel model) {
    super(id, model);
  }

  @Override
  public Component populateContent(String id, IModel model) {
    return new CommentPanel(id, model);
  }
}

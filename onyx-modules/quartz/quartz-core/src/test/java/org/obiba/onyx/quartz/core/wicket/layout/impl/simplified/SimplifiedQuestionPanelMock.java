/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.FormMock;

@SuppressWarnings("serial")
public class SimplifiedQuestionPanelMock extends FormMock {

  public SimplifiedQuestionPanelMock(String id, IModel model) {
    super(id, model);
  }

  @Override
  public Component populateContent(String id, IModel model) {
    return new SimplifiedQuestionPanel(id, model);
  }

}
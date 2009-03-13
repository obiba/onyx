/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.standard;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.FormMock;
import org.obiba.onyx.util.data.Data;

@SuppressWarnings("serial")
public class DefaultOpenAnswerDefinitionPanelMock extends FormMock {

  private DefaultOpenAnswerDefinitionPanel panel;

  public DefaultOpenAnswerDefinitionPanelMock(String id, IModel questionCategoryModel) {
    super(id, questionCategoryModel);
  }

  @Override
  public Component populateContent(String id, IModel model) {
    return panel = new DefaultOpenAnswerDefinitionPanel(id, new PropertyModel(model, "question"), model);
  }

  public Data getData() {
    return panel.getData();
  }

}
/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.springframework.beans.factory.NamedBean;

/**
 * Page layout factory.
 * @author Yannick Marcon
 * 
 */
public interface IPageLayoutFactory extends NamedBean {

  public PageLayout createLayout(String id, IModel<Page> pageModel);

}

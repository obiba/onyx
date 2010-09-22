/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.form;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;

@Deprecated
/**
 * This class will be delete. use  setContent() insteadof Page Creator
 */
public class QuestionnaireElementWebPage extends WebPage {
  @Deprecated
  public QuestionnaireElementWebPage(Panel panel) {
    add(panel);
  }
}

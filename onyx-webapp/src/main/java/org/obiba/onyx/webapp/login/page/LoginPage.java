/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.login.page;

import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.model.Model;
import org.obiba.onyx.webapp.base.page.AbstractBasePage;
import org.obiba.onyx.webapp.login.panel.LoginPanel;

public class LoginPage extends AbstractBasePage {

  private static final long serialVersionUID = -3536960410263657341L;

  public LoginPage() {
    ContextImage logoImage = new ContextImage("logo", new Model("images/logo/logo_on_light.png"));
    add(logoImage);

    add(new LoginPanel("loginPanel"));
  }

}

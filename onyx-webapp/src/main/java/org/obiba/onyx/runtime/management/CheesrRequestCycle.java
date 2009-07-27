/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.runtime.management;

import org.apache.wicket.Page;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.obiba.onyx.core.service.impl.NoSuchInterviewException;
import org.obiba.onyx.webapp.home.page.ErrorPage;

/**
 * implements an alternative way of managing the runtimeExceptions
 */
public class CheesrRequestCycle extends WebRequestCycle {

  /**
   * @param application
   * @param request
   * @param response
   */
  public CheesrRequestCycle(WebApplication application, WebRequest request, Response response) {
    super(application, request, response);
  }

  @Override
  public Page onRuntimeException(Page page, RuntimeException e) {
    Throwable cause = e;

    if(cause instanceof NoSuchInterviewException) {
      page = new ErrorPage();
      getSession().get().error("No current interview");
      return page;
    } else {
      return super.onRuntimeException(page, e);
    }

  }
}

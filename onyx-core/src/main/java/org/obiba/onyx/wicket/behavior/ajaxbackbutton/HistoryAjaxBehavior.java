/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.behavior.ajaxbackbutton;

import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public abstract class HistoryAjaxBehavior extends AbstractDefaultAjaxBehavior {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(HistoryAjaxBehavior.class);

  private static final String HISTORY_ITEM_PARAM = "hiId";

  @Override
  public final CharSequence getCallbackUrl(final boolean onlyTargetActivePage) {
    return super.getCallbackUrl(onlyTargetActivePage) + "&" + HISTORY_ITEM_PARAM + "=' + HistoryManager.getHistoryItem()";
  }

  @Override
  public void renderHead(final IHeaderResponse response) {
    response.renderCSSReference(new ResourceReference(HistoryAjaxBehavior.class, "history-manager-iframe.css"));
    // conflicts with the jquery imported by the base page
    // response.renderJavascriptReference(new ResourceReference(HistoryAjaxBehavior.class, "jquery.js"));
    response.renderJavascriptReference(new ResourceReference(HistoryAjaxBehavior.class, "history-manager.js"));

    /*
     * Save the callback URL to this behavior to call it on back/forward button clicks
     */
    response.renderJavascript("var notifyBackButton = function() { wicketAjaxGet('" + getCallbackUrl(false) + ", null, null, function() {return true;}.bind(this)); }", "history-manager-url");
  }

  @Override
  protected void respond(AjaxRequestTarget target) {
    final String componentId = RequestCycle.get().getRequest().getParameter(HistoryAjaxBehavior.HISTORY_ITEM_PARAM);
    onAjaxHistoryEvent(target, componentId);
  }

  /**
   * A callback method which will be invoked when the user presses the back/forward buttons of the browser
   * 
   * @param target the current request target
   * @param componentId the wicket:id of the component which had triggered the previous Ajax history entry
   */
  public abstract void onAjaxHistoryEvent(final AjaxRequestTarget target, final String componentId);

  /**
   * Registers a new entry in the history if this request is not triggered by back/forward buttons
   * 
   * @param target the current request target
   * @param component the component which triggered this Ajax request
   */
  public void registerAjaxEvent(final AjaxRequestTarget target, final Component component) {
    if(RequestCycle.get().getRequest().getParameter(HistoryAjaxBehavior.HISTORY_ITEM_PARAM) == null) {
      target.appendJavascript("HistoryManager.addHistoryEntry('" + component.getId() + "');");
    }
  }
}

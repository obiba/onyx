/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;

/**
 * Adds css 'height' to a tbody element that's a child of the component this behaviour is applied to. The 'height' is
 * added only when the content in the tbody exceeds the 'height' requiring a scroll bar to be displayed. If the content
 * does not exceed the height then the css 'height' is removed. (If 'height' was present the table rows would stretch to
 * fill the 'height'. This would not look correct.) This behaviour is using javascript to make up for a weakness in css.
 * To make a tbody scrollable ensure that the following css is applied: {@code overflow-y: auto; overflow-x: hidden;}
 */
public class ScrollableTableBodyBehaviour extends AbstractBehavior {

  private static final long serialVersionUID = 7492802968802658076L;

  private Component component;

  private int tableBodyHeight;

  public ScrollableTableBodyBehaviour(int tableBodyHeight) {
    this.tableBodyHeight = tableBodyHeight;
  }

  public void bind(Component component) {
    this.component = component;
  }

  public void renderHead(IHeaderResponse response) {
    // Works for FireFox. For a solution including IE support see:
    // http://plugins.jquery.com/files/jquery.scrollabletable.js_2.txt
    String script = "if($('#" + component.getMarkupId() + " tbody').height() > " + tableBodyHeight + " ) { $('#" + component.getMarkupId() + " tbody').css('height', '" + tableBodyHeight + "px' ); } else { $('#" + component.getMarkupId() + " tbody').css('height', '' ); }";
    response.renderOnLoadJavascript(script);
  }
}

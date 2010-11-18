/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.behavior.tooltip;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.wicket.Images;

/**
 *
 */
@SuppressWarnings("serial")
public class HelpTooltipPanel extends Panel {

  public HelpTooltipPanel(String id, IModel<String> model) {
    super(id, model);
    add(CSSPackageResource.getHeaderContribution(HelpTooltipPanel.class, "HelpTooltipPanel.css"));
    add(new Image("img", Images.HELP).add(new TooltipBehavior(model)));
  }

  public HelpTooltipPanel(String id, Map<String, Object> tooltipCfg) {
    super(id);
    add(CSSPackageResource.getHeaderContribution(HelpTooltipPanel.class, "HelpTooltipPanel.css"));
    add(new Image("img", Images.HELP).add(new TooltipBehavior(new Model<String>(""), tooltipCfg)));
  }

  public HelpTooltipPanel(String id, ResourceReference resourceReference) {
    super(id);
    add(CSSPackageResource.getHeaderContribution(HelpTooltipPanel.class, "HelpTooltipPanel.css"));
    Map<String, Object> tooltipCfg = new HashMap<String, Object>();
    tooltipCfg.put("delay", 100);
    tooltipCfg.put("showURL", false);
    tooltipCfg.put("bodyHandler", "function() { return \"<img src='" + RequestCycle.get().urlFor(resourceReference) + "' />\"; }");
    add(new Image("img", Images.HELP).add(new TooltipBehavior(new Model<String>(""), tooltipCfg)));
  }

}

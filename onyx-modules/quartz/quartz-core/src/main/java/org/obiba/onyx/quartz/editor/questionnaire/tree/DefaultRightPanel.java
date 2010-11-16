/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire.tree;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType;
import org.obiba.onyx.quartz.editor.behavior.tooltip.TooltipBehavior;
import org.obiba.onyx.wicket.Images;

/**
 *
 */
@SuppressWarnings("serial")
public class DefaultRightPanel extends Panel {

  public DefaultRightPanel(String id) {
    super(id);

    add(CSSPackageResource.getHeaderContribution(DefaultRightPanel.class, "DefaultRightPanel.css"));

    add(new Label("howToUseTree", new ResourceModel("HowToUseTree.content")).setEscapeModelStrings(false));
    add(new Label("terminology", new ResourceModel("Terminology.content")).setEscapeModelStrings(false));
    add(new Label("questionType", new ResourceModel("QuestionType.content")));

    final Map<String, Object> tooltipCfg = new HashMap<String, Object>();
    tooltipCfg.put("delay", 100);
    tooltipCfg.put("opacity", 100);
    tooltipCfg.put("showURL", false);

    add(new ListView<QuestionType>("questionTypeList", Arrays.asList(QuestionType.values())) {
      @Override
      protected void populateItem(ListItem<QuestionType> item) {
        QuestionType type = item.getModelObject();
        item.add(new Label("name", new ResourceModel("QuestionType." + type)));
        item.add(new Label("desc", new ResourceModel("QuestionType." + type + ".desc")));
        item.add(new Image("img", Images.ZOOM));

        tooltipCfg.put("bodyHandler", "function() { return \"<img src='" + RequestCycle.get().urlFor(new ResourceReference(DefaultRightPanel.class, type + ".png")) + "' />\"; }");
        item.add(new TooltipBehavior(new Model<String>(""), tooltipCfg));
      }
    });
  }

}

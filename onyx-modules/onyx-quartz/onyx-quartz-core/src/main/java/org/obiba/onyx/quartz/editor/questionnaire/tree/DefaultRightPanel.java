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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.collections.MiniMap;
import org.apache.wicket.util.template.TextTemplateHeaderContributor;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType;
import org.obiba.onyx.quartz.editor.QuartzImages;
import org.obiba.onyx.quartz.editor.behavior.tooltip.TooltipBehavior;
import org.obiba.onyx.wicket.Images;

/**
 *
 */
@SuppressWarnings("serial")
public class DefaultRightPanel extends Panel {

  public DefaultRightPanel(String id) {
    super(id);

    IModel<Map<String, Object>> variablesModel = new AbstractReadOnlyModel<Map<String, Object>>() {
      private Map<String, Object> variables;

      @Override
      public Map<String, Object> getObject() {
        if(variables == null) {
          this.variables = new MiniMap<String, Object>(4);
          variables.put("sectionImgUrl", RequestCycle.get().urlFor(QuartzImages.SECTION).toString());
          variables.put("pageImgUrl", RequestCycle.get().urlFor(QuartzImages.PAGE).toString());
          variables.put("questionImgUrl", RequestCycle.get().urlFor(QuartzImages.QUESTION).toString());
          variables.put("variableImgUrl", RequestCycle.get().urlFor(QuartzImages.VARIABLE).toString());
        }
        return variables;
      }
    };
    add(TextTemplateHeaderContributor.forCss(DefaultRightPanel.class, "DefaultRightPanel.css", variablesModel));

    add(new Label("howToUseTree", new ResourceModel("HowToUseTree.content")).setEscapeModelStrings(false));
    add(new Label("terminology", new ResourceModel("Terminology.content")).setEscapeModelStrings(false));
    add(new Label("questionType", new ResourceModel("QuestionType.content")));

    final Map<String, Object> tooltipCfg = new HashMap<String, Object>();
    tooltipCfg.put("delay", 100);
    tooltipCfg.put("showURL", false);
    tooltipCfg.put("positionLeft", true);
    tooltipCfg.put("left", 300);

    add(new ListView<QuestionType>("questionTypeList", Arrays.asList(QuestionType.values())) {
      @Override
      protected void populateItem(ListItem<QuestionType> item) {
        QuestionType type = item.getModelObject();
        tooltipCfg.put("bodyHandler", "function() { return \"<img src='" + RequestCycle.get().urlFor(new ResourceReference(DefaultRightPanel.class, type + ".png")) + "' />\"; }");
        item.add(new Label("name", new ResourceModel("QuestionType." + type)));
        item.add(new Label("desc", new ResourceModel("QuestionType." + type + ".desc")));
        item.add(new Image("img", Images.ZOOM).add(new TooltipBehavior(new Model<String>(""), tooltipCfg)));
      }
    });
  }
}

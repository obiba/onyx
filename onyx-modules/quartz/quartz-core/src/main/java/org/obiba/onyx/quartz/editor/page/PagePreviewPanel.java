/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.page;

import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultPageLayout;
import org.obiba.onyx.quartz.editor.PreviewPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class PagePreviewPanel extends PreviewPanel<Page> {

  private static final Logger logger = LoggerFactory.getLogger(PagePreviewPanel.class);

  public PagePreviewPanel(String id, final IModel<Page> model, IModel<Questionnaire> questionnaireModel) {
    super(id, model, questionnaireModel);

    try {
      // if(Questionnaire.SIMPLIFIED_UI.equals(questionnaireModel.getObject().getUiType())) {
      // add(new SimplifiedPageLayout("preview", model));
      // } else {
      add(previewLayout = createPreviewLayout(model));
      // }
    } catch(Exception e) {
      logger.error(e.getMessage(), e);
      add(new MultiLineLabel("preview", new StringResourceModel("Error", this, null, new Object[] { e.getMessage() })));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Panel createPreviewLayout(IModel<?> model) {
    return new DefaultPageLayout("preview", (IModel<Page>) model);
  }
}

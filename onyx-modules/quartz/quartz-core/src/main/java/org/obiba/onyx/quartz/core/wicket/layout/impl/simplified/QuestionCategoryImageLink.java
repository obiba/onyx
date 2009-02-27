/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified;

import java.io.IOException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractQuestionCategoryLinkSelectionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.QuestionCategorySelectionBehavior;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.wicket.util.FileResource;

/**
 * Component for "regular" question categories displayed as image links.
 */
public class QuestionCategoryImageLink extends AbstractQuestionCategoryLinkSelectionPanel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean
  private QuestionnaireBundleManager bundleManager;

  //
  // Constructors
  //

  public QuestionCategoryImageLink(String id, IModel questionModel, IModel questionCategoryModel, IModel labelModel) {
    super(id, questionModel, questionCategoryModel, labelModel);
  }

  public QuestionCategoryImageLink(String id, IModel questionCategoryModel, IModel labelModel) {
    this(id, new QuestionnaireModel(((QuestionCategory) questionCategoryModel.getObject()).getQuestion()), questionCategoryModel, labelModel);
  }

  //
  // AbstractQuestionCategoryLinkSelectionPanel
  //

  protected void addLinkComponent(IModel labelModel) {
    AjaxLink link = new AjaxLink("link", labelModel) {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        QuestionCategoryImageLink.this.handleSelectionEvent(target);
      }

    };
    link.add(new QuestionCategorySelectionBehavior());

    link.add(getCategoryImage("imageSelected", getCategoryImageId(getQuestionCategory(), true)));
    link.add(getCategoryImage("imageDeselected", getCategoryImageId(getQuestionCategory(), false)));

    add(link);
  }

  //
  // Methods
  //

  /**
   * Returns an <code>Image</code> component based on the specified questionnaire bundle image.
   * 
   * @param wicketId the Wicket component id for the <code>Image</code> created and returned by this method
   * @param imageId the id of the questionnaire bundle image (its meaning depends on the
   * <code>QuestionnaireBundle</code> implementation)
   * @return <code>Image</code> component for the specified questionnaire bundle image (or <code>null</code> if not
   * found)
   */
  private Image getCategoryImage(String componentId, String imageId) {
    Image image = null;

    String bundleName = activeQuestionnaireAdministrationService.getQuestionnaire().getName();
    QuestionnaireBundle bundle = bundleManager.getBundle(bundleName);

    try {
      image = new Image(componentId, new FileResource(bundle.getImageResource(imageId).getFile()));
    } catch(IOException ex) {
      ; // nothing to do -- if image not found, return null
    }

    return image;
  }

  /**
   * Returns the id of the specified category's selected or de-selected image.
   * 
   * @param category the category
   * @param selected indicates whether to return the category's selected or de-selected image
   * @return the id of the specified category's selected or de-selected image
   */
  private String getCategoryImageId(QuestionCategory category, boolean selected) {
    QuestionnaireStringResourceModel model = null;

    model = new QuestionnaireStringResourceModel(category, selected ? "imageSelected" : "imageDeselected");
    return model.getString();
  }
}

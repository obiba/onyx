/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.util;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;

public class QuestionCategoryImageFilter implements IDataListFilter<IModel> {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  private boolean acceptCategoryImage;

  //
  // Constructors
  //

  public QuestionCategoryImageFilter(boolean acceptCategoryImage) {
    this.acceptCategoryImage = acceptCategoryImage;
  }

  //
  // IDataListFilter Methods
  //

  /**
   * Accepts a <code>QuestionCategory</code> (i.e., returns <code>true</code>) if the <code>imageSelected</code>
   * and <code>imageDeselected</code> properties are defined for it.
   */
  public boolean accept(IModel item) {
    QuestionnaireStringResourceModel model = null;

    model = new QuestionnaireStringResourceModel(item, "imageSelected");
    String imageSelected = toNullIfEmpty(model.getString());

    model = new QuestionnaireStringResourceModel(item, "imageDeselected");
    String imageDeselected = toNullIfEmpty(model.getString());

    boolean isCategoryImage = ((imageSelected != null) && (imageDeselected != null));

    return acceptCategoryImage ? isCategoryImage : !isCategoryImage;
  }

  //
  // Methods
  //

  /**
   * Converts a string to <code>null</code> if it is either empty or consists of whitespace only.
   * 
   * @param s the string
   * @return <code>null</code> if the string is empty or consists whitespace only; otherwise the original string
   */
  private String toNullIfEmpty(String s) {
    if(s != null && s.trim().length() == 0) {
      s = null;
    }

    return s;
  }
}

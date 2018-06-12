/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.util.Comparator;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;

import com.google.common.collect.Multimap;

/**
 *
 */
public class CategoryByQuestionsComparator implements Comparator<Category> {

  private Multimap<Category, Question> map;

  public CategoryByQuestionsComparator(Multimap<Category, Question> map) {
    this.map = map;
  }

  @Override
  public int compare(Category o1, Category o2) {
    Integer size1 = map.get(o1).size();
    Integer size2 = map.get(o2).size();
    if(!size1.equals(size2)) return size2.compareTo(size1);
    return o1.getName().compareTo(o2.getName());
  }
}

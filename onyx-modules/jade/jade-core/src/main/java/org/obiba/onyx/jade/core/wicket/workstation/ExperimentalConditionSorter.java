/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.workstation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;

/**
 * Sorts {@link ExperimentalCondition}s based on ExperimentalCondition attributes and {@link ExperimentalConditionValue}
 * s. This sort is done in memory because it is not possible to have Hibernate perform the sort.
 */
public class ExperimentalConditionSorter {

  static final String EXPERIMENTAL_CONDITION_KEY = "EXPERIMENTAL_CONDITION_KEY";

  private List<Map<String, Serializable>> sortableList;

  public ExperimentalConditionSorter(List<ExperimentalCondition> experimentalConditions) {
    sortableList = convertToSortableList(experimentalConditions);
  }

  private List<Map<String, Serializable>> convertToSortableList(List<ExperimentalCondition> experimentalConditions) {
    List<Map<String, Serializable>> result = new ArrayList<Map<String, Serializable>>(experimentalConditions.size());
    for(ExperimentalCondition ec : experimentalConditions) {
      Map<String, Serializable> ecMap = new HashMap<String, Serializable>();
      ecMap.put(EXPERIMENTAL_CONDITION_KEY, ec);
      ecMap.put("name", ec.getName());
      ecMap.put("time", ec.getTime());
      ecMap.put("user", ec.getUser().getFullName());
      ecMap.put("workstation", ec.getWorkstation());
      ecMap.put("exported", ec.getExported());
      for(ExperimentalConditionValue ecv : ec.getExperimentalConditionValues()) {
        ecMap.put(ecv.getAttributeName(), (Serializable) ecv.getValue());
      }
      result.add(ecMap);
    }
    return result;
  }

  List<Map<String, Serializable>> getSortableList() {
    return sortableList;
  }

  public List<ExperimentalCondition> getSortedList(final SortingClause sortingClause) {
    return getSortedList(null, sortingClause);

  }

  public List<ExperimentalCondition> getSortedList(final PagingClause pagingClause, final SortingClause... sortingClause) {
    List<ExperimentalCondition> result = new ArrayList<ExperimentalCondition>();

    if(sortingClause != null && sortingClause[0] != null) {

      Collections.sort(sortableList, new Comparator<Map<String, Serializable>>() {

        public int compare(Map<String, Serializable> arg0, Map<String, Serializable> arg1) {

          Serializable s0 = arg0.get(sortingClause[0].getField());
          Serializable s1 = arg1.get(sortingClause[0].getField());

          if(s0 instanceof String) {
            return ((String) s0).compareTo((String) s1);
          } else if(s0 instanceof Date) {
            return ((Date) s0).compareTo((Date) s1);
          } else if(s0 instanceof Boolean) {
            return ((Boolean) s0).compareTo((Boolean) s1);
          } else if(s0 instanceof Long) {
            return ((Long) s0).compareTo((Long) s1);
          } else if(s0 instanceof Double) {
            return ((Double) s0).compareTo((Double) s1);
          } else {
            return 1;
          }
        }

      });
      if(!sortingClause[0].isAscending()) Collections.reverse(sortableList);
    }
    for(Map<String, Serializable> ecMap : sortableList) {
      result.add((ExperimentalCondition) ecMap.get(EXPERIMENTAL_CONDITION_KEY));
    }
    if(pagingClause != null) {
      result = result.subList(pagingClause.getOffset(), pagingClause.getOffset() + pagingClause.getLimit());
    }
    return result;
  }
}

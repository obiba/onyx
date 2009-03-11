/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.export;

import java.util.Set;

import org.obiba.onyx.engine.variable.IVariableFilter;

/**
 * 
 */
public class OnyxDataExportDestination implements IVariableFilter {

  public String name;

  public boolean includeAll;

  public Set<String> filteredVariables;

  public Set<String> includedVariables;

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setIncludedVariables(Set<String> includedVariables) {
    this.includedVariables = includedVariables;
  }

  public void setFilteredVariables(Set<String> filteredVariables) {
    this.filteredVariables = filteredVariables;
  }

  public void setIncludeAll(boolean includeAll) {
    this.includeAll = includeAll;
  }

  public boolean accept(String path) {
    boolean include;

    if(includeAll) {
      // start by including it
      include = true;

      // eventually filter it
      if(filteredVariables != null) {
        include = !containsPath(filteredVariables, path);
      }

      // eventually re-include it after it was filtered
      if(include == false && includedVariables != null) {
        include = containsPath(includedVariables, path);
      }
    } else {
      // start by not including it
      include = false;

      // eventually include it
      if(includedVariables != null) {
        include = containsPath(includedVariables, path);
      }

      // eventually filter it after it was included
      if(include && filteredVariables != null) {
        include = !containsPath(filteredVariables, path);
      }
    }

    return include;
  }

  protected boolean containsPath(Set<String> set, String path) {
    for(String entry : set) {
      // Is this entry the prefix of the path
      if(path.startsWith(entry)) {
        return true;
      }
    }
    return false;
  }
}

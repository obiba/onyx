package org.obiba.onyx.core.service;

import java.util.List;

import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.user.Role;

public interface RoleService {

  /**
   * Returns the list of roles
   * @param clauses
   * @return
   */
  public List<Role> getRoles(SortingClause... clauses);
  
}

/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.base.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.IClusterable;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.Roles;

public class MenuItem implements IClusterable {
  private static final long serialVersionUID = -306635168756788763L;
  private List<MenuItem> subMenuItems;
	private Class<?> page;
	private String label;
	private Roles roles = null;
  private PageParameters parameters;

	public MenuItem(Class<?> page, String label) {
		this.page = page;
		this.label = label;
		this.subMenuItems = new ArrayList<MenuItem>();
	}
  
  public MenuItem(Class<?> page, PageParameters parameters, String label) {
    this(page, label);
    this.parameters = parameters;
  }

	public MenuItem(Class<?> page, String label, List<MenuItem> subMenuItems) {
		this(page, label);
		this.subMenuItems = subMenuItems;
	}
	
	public MenuItem(Class<?> page, String label, Roles pRoles) {
	  this(page, label);
	  roles = pRoles;
  }
	
	public MenuItem(Class<?> page, String label, List<MenuItem> subMenuItems, Roles pRoles) {
    this(page, label, subMenuItems);
    roles = pRoles;
  }
  
  public void add(MenuItem item) {
    if (subMenuItems == null)
      subMenuItems = new ArrayList<MenuItem>();
    
    subMenuItems.add(item);
  }

	public String getLabel() {
		return label;
	}
	public Class<?> getPage() {
		return page;
	}
	public PageParameters getParameters() {
    return parameters;
  }
  public List<MenuItem> getSubMenuItems() {
		return subMenuItems;
	}
	public void setRoles(Roles pRoles) {
	  roles = pRoles;
	}
	public Roles getRoles() {
	  return roles;
	}
}

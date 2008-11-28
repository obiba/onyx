/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.util.testconfig;

import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.ObjectFactory;


public class CustomScope implements Scope {

  public String getConversationId() {
    return null;
  }

  public Object get(String name, ObjectFactory objectFactory) {
    return objectFactory.getObject();
  }

  public Object remove(String name) {
    return null;
  }

  public void registerDestructionCallback(String name, Runnable callback) {
  }

}

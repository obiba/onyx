/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.util.List;
import java.util.Locale;

import org.obiba.magma.Attribute;

public interface Attributable {

  public boolean hasAttributes();

  public List<Attribute> getAttributes();

  public void addAttribute(String namespace, String name, String value, Locale locale);

  public boolean containsAttribute(Attribute attribute);

  public Attribute getAttribute(String namespace, String name, Locale locale);

  public void updateAttribute(Attribute attribute, String namespace, String name,
      String value, Locale locale);

  public void removeAttribute(Attribute attribute);
}

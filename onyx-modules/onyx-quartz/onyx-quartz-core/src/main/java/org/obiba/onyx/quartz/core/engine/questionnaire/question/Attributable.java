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

  List<Attribute> getAttributes();

  void setAttributes(List<Attribute> attributes);

  void addAttribute(String namespace, String name, String value, Locale locale);

  boolean containsAttribute(String namespace, String name);

  void removeAttributes(String namespace, String name);

}

/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor;

import java.util.regex.Pattern;

import org.apache.wicket.markup.html.panel.Panel;
import org.obiba.onyx.quartz.editor.questionnaire.QuestionnaireListPanel;

@SuppressWarnings("serial")
public class QuartzEditorPanel extends Panel {

  /** allows letter, numbers, dashes and underscores */
  public static final Pattern ELEMENT_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-]*$");

  public QuartzEditorPanel(String id) {
    super(id);
    add(new QuestionnaireListPanel("questionnaires"));
  }
}

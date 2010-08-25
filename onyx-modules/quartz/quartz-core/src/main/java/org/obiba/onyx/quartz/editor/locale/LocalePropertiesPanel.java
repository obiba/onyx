/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.locale;

import java.util.Locale;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalePropertiesPanel extends Panel {

  private static final long serialVersionUID = 1L;

  protected final Logger log = LoggerFactory.getLogger(getClass());

  private LocalePropertiesModel localePropertiesModel = new LocalePropertiesModel();

  private Locale locale;

  public LocalePropertiesPanel(String id, Locale locale) {
    super(id);
    this.locale = locale;
    localePropertiesModel.setLocale(locale);
    createComponent();
  }

  private void createComponent() {
    add(new Label("test", "testtest"));

    // final DefaultPropertyKeyProviderImpl propProvider = new DefaultPropertyKeyProviderImpl();
    // ListView<TextField<String>> labels = new ListView<TextField<String>>("labelsItem") {
    //
    // private static final long serialVersionUID = 1L;
    //
    // @Override
    // protected void populateItem(ListItem<TextField<String>> item) {
    // for(String label : propProvider.getProperties(question)) {
    // String key = propProvider.getPropertyKey(question, label);
    // log.info("Key : " + key);
    //
    // TextField<String> labelInput = new TextField<String>("labelValue", new Model<String>());
    // labelInput.add(new StringValidator.MaximumLengthValidator(20));
    //
    // item.add(labelInput);
    // }
    // }
    // };
    // add(labels);
  }
}

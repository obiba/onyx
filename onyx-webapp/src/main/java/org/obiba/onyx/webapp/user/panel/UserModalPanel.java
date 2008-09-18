package org.obiba.onyx.webapp.user.panel;

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.Panel;

public class UserModalPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public UserModalPanel(String id, Panel contentPanel, final ModalWindow modalWindow) {
    super(id);
    add(contentPanel);
  }

}

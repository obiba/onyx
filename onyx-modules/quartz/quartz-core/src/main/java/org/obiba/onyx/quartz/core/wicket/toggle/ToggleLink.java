package org.obiba.onyx.quartz.core.wicket.toggle;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * A labeled toggle link, for showing or hiding a component.
 */
public class ToggleLink extends Panel {

  private static final long serialVersionUID = 40843245024083743L;

  private Label toggleLabel;

  /**
   * Constructor.
   * @param id
   * @param showModel
   * @param hideModel
   * @param toggle
   */
  @SuppressWarnings("serial")
  public ToggleLink(String id, final IModel showModel, final IModel hideModel, final Component toggle) {
    super(id);
    setOutputMarkupId(true);
    toggle.getParent().setOutputMarkupId(true);

    AjaxLink link = new AjaxLink("link") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        toggle.setVisible(!toggle.isVisible());
        if(toggle.isVisible()) {
          toggleLabel.setModel(hideModel);
        } else {
          toggleLabel.setModel(showModel);
        }
        target.addComponent(toggle.getParent());
      }

    };
    add(link);

    link.add(toggleLabel = new Label("label", showModel));

    toggle.setVisible(false);
  }

}

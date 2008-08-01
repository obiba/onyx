package org.obiba.onyx.webapp.base.panel;

import java.io.Serializable;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.application.AppConfiguration;
import org.obiba.onyx.core.domain.user.User;

public class HeaderPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private EntityQueryService queryService;

  HeaderPanelModel model = new HeaderPanelModel();

  public HeaderPanel(String id) {
    super(id);
    
    add(new Label("studyName", new PropertyModel(model, "config.studyName")));
    add(new Label("siteName", new PropertyModel(model, "config.siteName")));
    add(new Label("userLoggedIn", new PropertyModel(model, "userLoggedIn.name")));    

  }

  private class HeaderPanelModel implements Serializable {

    private static final long serialVersionUID = 1L;

    public User getUserLoggedIn() {
      User user = new User();
      user.setName("Dev User");
      return user;
    }

    public AppConfiguration getConfig() {
      AppConfiguration templateConfig = new AppConfiguration();
      return ( queryService.matchOne( templateConfig ) );
    }

  }

}

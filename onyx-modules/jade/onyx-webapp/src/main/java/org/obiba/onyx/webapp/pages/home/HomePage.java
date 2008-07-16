package org.obiba.onyx.webapp.pages.home;

import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.webapp.pages.bootstrap.TestBootstrapPage;

public class HomePage extends WebPage implements IAjaxIndicatorAware {

  private FeedbackPanel feedbackPanel;

  @SpringBean
  private ModuleRegistry registry;

  public HomePage() {
    super();

    // Create feedback panel and add to page
    feedbackPanel = new FeedbackPanel("feedback");
    feedbackPanel.setOutputMarkupId(true);
    add(feedbackPanel);

    add(new Label("baseAjaxIndicator", "Processing..."));

    // TODO make this part dynamic: the list of stages should probably come from the registered modules...
    Stage stage = new Stage();
    stage.setName("Bioimpedence");
    stage.setModule("jade");
    // end TODO

    Module module = registry.getModule(stage.getModule());
    add(module.startStage(stage).createStageComponent("stage-component"));
    
    add(new PageLink( "testBootstrap", TestBootstrapPage.class ));
  }

  /**
   * @see org.apache.wicket.ajax.IAjaxIndicatorAware#getAjaxIndicatorMarkupId()
   */
  public String getAjaxIndicatorMarkupId() {
    return "base-ajax-indicator";
  }

}

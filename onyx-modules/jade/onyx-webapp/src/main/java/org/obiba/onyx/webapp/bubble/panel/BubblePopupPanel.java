package org.obiba.onyx.webapp.bubble.panel;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;

public class BubblePopupPanel extends Panel implements IHeaderContributor {

  private static final long serialVersionUID = 1L;

  private static final JavascriptResourceReference PROTOTYPE_JS = new JavascriptResourceReference(BubblePopupPanel.class, "prototype-1.6.0.js");

  private Component content;
  
  public BubblePopupPanel(String id) {
    super(id);
    setOutputMarkupId(true);
    content = new EmptyPanel("content");
    add(content);
    content.setOutputMarkupId(true);

    add(new Image("ul"));
    add(new Image("ur"));
    add(new Image("ll"));
    add(new Image("lr"));
    add(new Image("ar"));
    add(new Image("c"));
  }

  @Override
  protected void onComponentTag(ComponentTag tag) {
    super.onComponentTag(tag);
    tag.setName("div");
    tag.put("style", "width:400px;z-index:100;display:none;position:absolute");
    tag.put("class", "bubble");
  }

  public void renderHead(IHeaderResponse response) {
    response.renderJavascriptReference(PROTOTYPE_JS);
  }

  public String getContentId() {
    return "content";
  }
  
  public void setContent(AjaxRequestTarget target, Component newContent) {
    content.replaceWith(newContent);
    content = newContent;
    content.setOutputMarkupId(true);
    target.addComponent(content);
  }

  public void show(AjaxRequestTarget target) {
    target.appendJavascript("$('" + getMarkupId() + "').show()");
    target.addComponent(this);
  }

  public void place(AjaxRequestTarget target, Component c) {
    target.appendJavascript("$('" + getMarkupId() + "').clonePosition('"+c.getMarkupId()+"', {setWidth:false, setHeight:false, offsetLeft:$('" + c.getMarkupId() + "').getWidth()/2-$('" + getMarkupId() + "').getWidth()/2, offsetTop:-$('" + getMarkupId() + "').getHeight()});");
    target.addComponent(this);
  }

}

package org.obiba.onyx.quartz.editor.input;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * @author cedric.thiebault
 */
public abstract class InputPanel<TComponent extends Component> extends Panel {

	private static final long serialVersionUID = 1L;

	public InputPanel(String id, IModel<?> model) {
		super(id, model);
	}

	public InputPanel(String id) {
		super(id);
	}

	public abstract TComponent getField();

}

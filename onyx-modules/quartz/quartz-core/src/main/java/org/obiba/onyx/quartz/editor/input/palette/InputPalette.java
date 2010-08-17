package org.obiba.onyx.quartz.editor.input.palette;

import java.util.List;

import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.obiba.onyx.quartz.editor.input.InputPanel;

/**
 * @author cedric.thiebault
 */
public class InputPalette<T> extends InputPanel<Palette<T>> {

  private static final long serialVersionUID = 1L;

  public static final int DEFAULT_NB_ROWS = 7;

  public static final boolean DEFAULT_ALLOW_ORDER = false;

  private String property;

  private String labelKey;

  private IModel<List<T>> choicesModel;

  private IChoiceRenderer<T> choiceRenderer;

  private int rows;

  private boolean allowOrder;

  private Palette<T> palette;

  public InputPalette(String id, IModel<?> model, String property, String labelKey, IModel<List<T>> choicesModel, IChoiceRenderer<T> choiceRenderer) {
    this(id, model, property, labelKey, choicesModel, choiceRenderer, DEFAULT_NB_ROWS, DEFAULT_ALLOW_ORDER);
  }

  public InputPalette(String id, IModel<?> model, String property, String labelKey, IModel<List<T>> choicesModel, IChoiceRenderer<T> choiceRenderer, int rows, boolean allowOrder) {
    super(id, model);
    this.property = property;
    this.labelKey = labelKey;
    this.choicesModel = choicesModel;
    this.choiceRenderer = choiceRenderer;
    this.rows = rows;
    this.allowOrder = allowOrder;
    createComponent();
  }

  @SuppressWarnings("unchecked")
  private void createComponent() {
    setOutputMarkupId(true);
    // FormUtils.addCss(this);
    add(new Label("label", new ResourceModel(labelKey)));
    palette = new Palette<T>("palette", (IModel<List<T>>) new PropertyModel<T>(getDefaultModel(), property), choicesModel, choiceRenderer, rows, allowOrder) {
      private static final long serialVersionUID = 1L;

      @Override
      protected Recorder<T> newRecorderComponent() {
        Recorder<T> recorder = super.newRecorderComponent();
        IBehavior behavior = createRecorderBehavior(recorder);
        if(behavior != null) recorder.add(behavior);
        return recorder;
      }
    };
    add(palette);
  }

  @Override
  public Palette<T> getField() {
    return palette;
  }

  protected IBehavior createRecorderBehavior(Recorder<T> recorder) {
    return null;
  }

}

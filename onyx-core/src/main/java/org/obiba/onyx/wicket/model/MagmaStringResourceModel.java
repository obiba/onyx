/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.model;

import java.util.Locale;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IChainingModel;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.magma.OnyxAttributeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class MagmaStringResourceModel extends AbstractReadOnlyModel<String> implements IChainingModel<String> {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(MagmaStringResourceModel.class);

  private Object target;

  public MagmaStringResourceModel(Object target) {
    super();
    this.target = target;
  }

  protected abstract Participant getParticipant();

  protected abstract Locale getLocale();

  protected abstract String getTableContext();

  @Override
  public String getObject() {
    String message;
    if(target instanceof IModel<?>) {
      message = (String) ((IModel<?>) target).getObject();
    } else {
      message = (String) target;
    }

    return resolveVariableValuesInMessage(message, getTableContext(), getLocale());
  }

  @Override
  public void detach() {
    // Detach nested object if it's a detachable
    if(target instanceof IDetachable) {
      ((IDetachable) target).detach();
    }
  }

  @Override
  public void setChainedModel(IModel<?> model) {
    target = model;
  }

  @Override
  public IModel<?> getChainedModel() {
    if(target instanceof IModel) {
      return (IModel<?>) target;
    }
    return null;
  }

  private String resolveVariableValuesInMessage(String message, String tableContext, Locale locale) {
    String msg = message;
    // Look for variable references and replace by the value as a string
    try {
      int refIndex = msg.indexOf("$('");
      while(refIndex != -1) {
        int refEndIndex = msg.indexOf("')", refIndex);
        String path = msg.substring(refIndex + 3, refEndIndex);
        if(!path.contains(":")) {
          path = tableContext + ":" + path;
        }
        VariableDataSource varDs = new VariableDataSource(path);
        Value value = varDs.getValue(getParticipant());

        String dataStr = getValueAsString(varDs, value, locale);
        msg = msg.substring(0, refIndex) + dataStr + msg.substring(refEndIndex + 2, msg.length());
        refIndex = msg.indexOf("$('");
      }
    } catch(Exception e) {
      log.error("Error while resolving variable values in: " + message, e);
    }
    return msg;
  }

  //
  // Methods
  //

  private String getValueAsString(VariableDataSource varDs, Value value, Locale locale) {
    if(value == null || value.getValue() == null) return "";

    String dataStr = value.toString();
    if(value.getValueType().equals(TextType.get())) {
      Variable variable = varDs.getVariable();
      if(!value.isSequence()) {
        dataStr = getValueAsLabel(variable, value, locale);
      } else {
        StringBuffer buff = new StringBuffer();
        buff.append("<ul>");
        for(Value val : value.asSequence().getValues()) {
          buff.append("<li>").append(getValueAsLabel(variable, val, locale)).append("</li>");
        }
        buff.append("</ul>");
        dataStr = buff.toString();
      }
    }
    return dataStr;
  }

  private String getValueAsLabel(Variable variable, Value value, Locale locale) {
    if(value == null || value.getValue() == null) return "";

    String valueStr = value.getValue().toString();
    if(variable.hasCategories()) {
      for(Category category : variable.getCategories()) {
        if(category.getName().equals(valueStr)) {
          return getCategoryLabel(category, locale);
        }
      }
    }
    return valueStr;
  }

  private String getCategoryLabel(Category category, Locale locale) {
    if(category.hasAttribute(OnyxAttributeHelper.LABEL, locale)) {
      Attribute attr = category.getAttribute(OnyxAttributeHelper.LABEL, locale);
      if(attr.getValue() != null) {
        return attr.getValue().toString();
      }
    }
    return category.getName();
  }
}

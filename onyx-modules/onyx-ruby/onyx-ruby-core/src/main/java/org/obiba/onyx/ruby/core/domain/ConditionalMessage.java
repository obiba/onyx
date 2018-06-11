/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.domain;

import java.io.Serializable;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

public class ConditionalMessage implements MessageSourceResolvable, Serializable {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(ConditionalMessage.class);

  //
  // Instance Variables
  //

  private IModel applicationContextModel;

  private String code;

  private IDataSource[] arguments;

  private IDataSource condition;

  //
  // Constructors
  //

  public ConditionalMessage() {
    super();
  }

  //
  // MessageSourceResolvable Methods
  //

  public Object[] getArguments() {
    if(arguments != null && arguments.length > 0) {
      MessageSourceResolvable[] resolvableArgs = new MessageSourceResolvable[arguments.length];

      ApplicationContext applicationContext = (ApplicationContext) applicationContextModel.getObject();
      ActiveInterviewService activeInterviewService = (ActiveInterviewService) applicationContext.getBean("activeInterviewService");

      for(int i = 0; i < arguments.length; i++) {
        IDataSource dataSource = arguments[i];

        Data data = dataSource.getData(activeInterviewService.getParticipant());
        String argCode = data.getValueAsString();

        resolvableArgs[i] = new DefaultMessageSourceResolvable(new String[] { argCode }, argCode);
      }

      return resolvableArgs;
    }

    return null;
  }

  public String[] getCodes() {
    return new String[] { code };
  }

  public String getDefaultMessage() {
    return code;
  }

  //
  // Methods
  //

  public void setApplicationContext(IModel applicationContextModel) {
    this.applicationContextModel = applicationContextModel;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setCondition(IDataSource condition) {
    this.condition = condition;
  }

  public IDataSource getCondition() {
    return condition;
  }

  /**
   * Indicates whether the message should be displayed, based on the message's condition.
   * 
   * @param participant the currently interview participant
   * @return <code>true</code> if the message should be displayed (i.e., if either there is no condition or the
   * condition is satisfied)
   */
  public boolean shouldDisplay() {
    if(condition == null) return true;

    ApplicationContext applicationContext = (ApplicationContext) applicationContextModel.getObject();
    ActiveInterviewService activeInterviewService = (ActiveInterviewService) applicationContext.getBean("activeInterviewService");

    Data conditionData = condition.getData(activeInterviewService.getParticipant());

    if(conditionData != null) {
      if(conditionData.getType().equals(DataType.BOOLEAN)) {
        return (Boolean) conditionData.getValue();
      } else {
        log.error("Condition data is of type {}, expected BOOLEAN", conditionData.getType());
      }
    } else {
      log.info("Condition data is null, treating as false");
    }

    return false;
  }
}
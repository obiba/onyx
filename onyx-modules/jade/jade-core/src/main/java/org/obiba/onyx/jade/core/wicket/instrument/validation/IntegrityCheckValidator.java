/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.instrument.validation;

import java.io.Serializable;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.validation.IntegrityCheck;
import org.obiba.onyx.jade.core.domain.instrument.validation.IntegrityCheckType;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.data.DataField;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;

public class IntegrityCheckValidator extends AbstractValidator {

  private static final long serialVersionUID = 1L;

  private IntegrityCheck integrityCheck;

  private String checkedParameterCode;

  @SpringBean
  private InstrumentRunService instrumentRunService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean
  private UserSessionService userSessionService;

  @SpringBean
  private MessageSource messageSource;

  public IntegrityCheckValidator(InstrumentParameter parameter, IntegrityCheck integrityCheck) {
    this.checkedParameterCode = parameter.getCode();
    this.integrityCheck = integrityCheck;

    InjectorHolder.getInjector().inject(this);
  }

  //
  // AbstractValidator Methods
  //

  @Override
  protected void onValidate(IValidatable validatable) {
    boolean isValid = integrityCheck.checkParameterValue(getCheckedParameter(), (Data) (validatable.getValue()), instrumentRunService, activeInstrumentRunService);

    if(!isValid) {
      validatable.error(new IntegrityCheckValidationError(integrityCheck));
    }
  }

  //
  // Methods
  //

  /**
   * Convenience method for configuring <code>DataField</code> with a list of integrity checks.
   * 
   * @param targetField the field
   * @param integrityChecks the checks
   */
  public static void addChecks(InstrumentParameter parameter, DataField targetField) {
    for(IntegrityCheck check : parameter.getIntegrityChecks()) {
      if(check.getType().equals(IntegrityCheckType.ERROR)) {
        targetField.add(new IntegrityCheckValidator(parameter, check));
      }
    }
  }

  protected InstrumentParameter getCheckedParameter() {
    return this.activeInstrumentRunService.getParameterByCode(checkedParameterCode);
  }

  class IntegrityCheckValidationError implements IValidationError, Serializable {
    private static final long serialVersionUID = 1L;

    private IntegrityCheck integrityCheck;

    public IntegrityCheckValidationError(IntegrityCheck integrityCheck) {
      this.integrityCheck = integrityCheck;
    }

    public String getErrorMessage(IErrorMessageSource errorMessageSource) {
      MessageSourceResolvable resolvable = integrityCheck.getDescription(getCheckedParameter(), activeInstrumentRunService);
      return messageSource.getMessage(resolvable, userSessionService.getLocale());
    }
  }
}

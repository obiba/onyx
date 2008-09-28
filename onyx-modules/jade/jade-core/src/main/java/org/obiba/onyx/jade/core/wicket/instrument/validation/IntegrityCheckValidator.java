package org.obiba.onyx.jade.core.wicket.instrument.validation;

import java.util.List;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.obiba.onyx.jade.core.domain.instrument.validation.AbstractIntegrityCheck;
import org.obiba.onyx.jade.core.domain.instrument.validation.IntegrityCheck;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.data.DataField;

public class IntegrityCheckValidator extends AbstractValidator {

  private static final long serialVersionUID = 1L;

  private IntegrityCheck integrityCheck;
   
  @SpringBean
  private transient InstrumentRunService instrumentRunService;
  
  @SpringBean
  private transient ActiveInstrumentRunService activeInstrumentRunService;
  
  public IntegrityCheckValidator(IntegrityCheck integrityCheck) {
    this.integrityCheck = integrityCheck;
    
    InjectorHolder.getInjector().inject(this);
  }
  
  //
  // AbstractValidator Methods
  //
  
  @Override
  protected void onValidate(IValidatable validatable) {    
    boolean isValid = integrityCheck.checkParameterValue((Data)(validatable.getValue()), instrumentRunService, activeInstrumentRunService);

    if (!isValid) {
      error(validatable);
    }
  }
  
  @Override
  protected String resourceKey() {
    return integrityCheck.getClass().getSimpleName();
  }
  
  //
  // Methods
  //
  
  /**
   * Convenience method for configuring <code>DataField</code> with a
   * list of integrity checks.
   * 
   * @param targetField the field
   * @param integrityChecks the checks
   */
  public static void addChecks(DataField targetField, List<AbstractIntegrityCheck> integrityChecks) {
    for (IntegrityCheck check : integrityChecks) {
      targetField.add(new IntegrityCheckValidator(check));
    }
  }
}

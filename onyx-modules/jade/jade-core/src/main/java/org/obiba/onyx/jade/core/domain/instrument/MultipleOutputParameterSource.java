package org.obiba.onyx.jade.core.domain.instrument;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.obiba.onyx.jade.core.service.InputSourceVisitor;

@Entity
@DiscriminatorValue("MultipleOutputParameterSource")
public class MultipleOutputParameterSource extends InputSource {

  private static final long serialVersionUID = 1L;

  @ManyToMany
  @JoinTable(
          name="multiple_output_parameters",
          joinColumns = { @JoinColumn(name="multiple_output_parameter_source_id") },
          inverseJoinColumns = @JoinColumn(name="output_parameter_source_id")
  )
  
  private List<OutputParameterSource> outputParameterSourceList;

  public MultipleOutputParameterSource() {
  }

  @Override
  public void accept(InputSourceVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  public List<OutputParameterSource> getOutputParameterSourceList() {
    return outputParameterSourceList;
  }

  public void setOutputParameterSourceList(List<OutputParameterSource> outputParameterSourceList) {
    this.outputParameterSourceList = outputParameterSourceList;
  }
  
}

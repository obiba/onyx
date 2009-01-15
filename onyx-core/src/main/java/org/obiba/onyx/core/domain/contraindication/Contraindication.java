/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.contraindication;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.obiba.core.domain.AbstractEntity;
import org.springframework.context.MessageSourceResolvable;

/**
 * Represents a contraindication that can be used in an {@link IContraindicatable}. Contraindications have a unique
 * {@code code} for their {@code type}. The type allows classifying contraindications into groups that may be treated
 * differently.
 * <p>
 * The {@code label} attribute returns a localized string that describes this contraindication.
 * <p>
 * NOTE: The class extends AsbtractEntity in order to allow persisting them through JPA. This is required due to the
 * fact that Jade persists its model and Contraindications are part of this model. It is not recommended to actually
 * persist these entities.
 */
@Entity
@org.hibernate.annotations.AccessType("field")
public class Contraindication extends AbstractEntity implements MessageSourceResolvable {

  private static final long serialVersionUID = 1L;

  public enum Type {
    ASKED, OBSERVED;
  }

  private String code;

  private boolean requiresDescription = false;

  @Enumerated(EnumType.STRING)
  private Type type;

  public Contraindication() {
    super();
  }

  public Contraindication(String code, Type type) {
    super();
    this.code = code;
    this.type = type;
  }

  public String getCode() {
    return code;
  }

  public boolean getRequiresDescription() {
    return requiresDescription;
  }

  public Type getType() {
    return type;
  }

  public Object[] getArguments() {
    return null;
  }

  public String[] getCodes() {
    return new String[] { code, "ContraIndication." + code };
  }

  public String getDefaultMessage() {
    return code;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Contraindication) {
      Contraindication rhs = (Contraindication) obj;
      return code.equals(rhs.code) && requiresDescription == rhs.requiresDescription && type == rhs.type;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    int hashCode = 17;
    hashCode = hashCode * 37 + code.hashCode();
    hashCode = hashCode * 37 + (requiresDescription ? 0 : 1);
    return hashCode * 37 + type.hashCode();
  }

  @Override
  public String toString() {
    return "Contraindication [" + code + "] [" + type + "]";
  }
}

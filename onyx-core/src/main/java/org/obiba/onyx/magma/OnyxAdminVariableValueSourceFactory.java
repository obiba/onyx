/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.magma;

import java.util.HashSet;
import java.util.Set;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.Variable.Builder;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.js.JavascriptVariableBuilder;
import org.obiba.magma.js.JavascriptVariableValueSource;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.stage.StageInstance;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.variable.impl.ParticipantCaptureAndExportStrategy;
import org.obiba.runtime.Version;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Factory for creating VariableValueSources for all the Onyx "admin" variables.
 */
public class OnyxAdminVariableValueSourceFactory implements VariableValueSourceFactory {
  //
  // Constants
  //

  public static final String ONYX_ADMIN_PREFIX = "Admin";

  public static final String ONYX_VERSION = "onyxVersion";

  public static final String PARTICIPANT = "Participant";

  public static final String INTERVIEW = "Interview";

  public static final String APPLICATION_CONFIGURATION = "ApplicationConfiguration";

  public static final String ACTION = "Action";

  public static final String STAGE_INSTANCE = "StageInstance";

  //
  // Instance Variables
  //

  @Autowired(required = true)
  private OnyxAttributeHelper attributeHelper;

  @Autowired(required = true)
  private ParticipantMetadata participantMetadata;

  @Autowired(required = true)
  private Version version;

  @Autowired(required = true)
  private ParticipantCaptureAndExportStrategy participantCaptureAndExportStrategy;

  //
  // VariableValueSourceFactory Methods
  //

  public Set<VariableValueSource> createSources() {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    sources.add(createVersionSource());
    sources.add(createParticipantExportedSource());
    sources.addAll(createParticipantCaptureDateSources());
    sources.addAll(createParticipantSources());
    sources.addAll(createInterviewSources());
    sources.addAll(createAppConfigSources());
    sources.addAll(createActionSources());
    sources.addAll(createStageInstanceSources());

    return sources;
  }

  //
  // Methods
  //

  public void setAttributeHelper(OnyxAttributeHelper attributeHelper) {
    this.attributeHelper = attributeHelper;
  }

  public void setParticipantMetadata(ParticipantMetadata participantMetadata) {
    this.participantMetadata = participantMetadata;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  public void setParticipantCaptureAndExportStrategy(ParticipantCaptureAndExportStrategy participantCaptureAndExportStrategy) {
    this.participantCaptureAndExportStrategy = participantCaptureAndExportStrategy;
  }

  private VariableValueSource createVersionSource() {
    return new VariableValueSource() {
      private Variable variable;

      private ValueType type = TextType.get();

      public Variable getVariable() {
        if(variable == null) {
          // Building version variable with entityType "Participant". This is a little odd, since version
          // is in fact not related to *any* entityType.
          Variable.Builder builder = Variable.Builder.newVariable(ONYX_ADMIN_PREFIX + '.' + ONYX_VERSION, getValueType(), PARTICIPANT);
          variable = builder.build();
        }
        return variable;
      }

      public Value getValue(ValueSet valueSet) {
        return getValueType().valueOf(version.toString());
      }

      public ValueType getValueType() {
        return type;
      }
    };
  }

  private VariableValueSource createParticipantExportedSource() {
    return new VariableValueSource() {

      public Variable getVariable() {
        Variable.Builder builder = new Variable.Builder(ONYX_ADMIN_PREFIX + '.' + PARTICIPANT + '.' + "exported", getValueType(), PARTICIPANT);
        return builder.build();
      }

      public Value getValue(ValueSet valueSet) {
        return getValueType().valueOf(participantCaptureAndExportStrategy.isExported(valueSet.getVariableEntity().getIdentifier()));
      }

      public ValueType getValueType() {
        return BooleanType.get();
      }
    };
  }

  private Set<VariableValueSource> createParticipantCaptureDateSources() {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    // Create captureStartDate source.
    sources.add(new VariableValueSource() {
      public Variable getVariable() {
        Variable.Builder builder = Variable.Builder.newVariable(ONYX_ADMIN_PREFIX + '.' + PARTICIPANT + '.' + "captureStartDate", getValueType(), PARTICIPANT);
        return builder.build();
      }

      public Value getValue(ValueSet valueSet) {
        return getValueType().valueOf(participantCaptureAndExportStrategy.getCaptureStartDate(valueSet.getVariableEntity().getIdentifier()));
      }

      public ValueType getValueType() {
        return DateType.get();
      }
    });

    // Create captureEndDate source.
    sources.add(new VariableValueSource() {
      public Variable getVariable() {
        Variable.Builder builder = Variable.Builder.newVariable(ONYX_ADMIN_PREFIX + '.' + PARTICIPANT + '.' + "captureEndDate", getValueType(), PARTICIPANT);
        return builder.build();
      }

      public Value getValue(ValueSet valueSet) {
        return getValueType().valueOf(participantCaptureAndExportStrategy.getCaptureEndDate(valueSet.getVariableEntity().getIdentifier()));
      }

      public ValueType getValueType() {
        return DateType.get();
      }
    });

    return sources;
  }

  private Set<VariableValueSource> createParticipantSources() {
    String participantPrefix = ONYX_ADMIN_PREFIX + '.' + PARTICIPANT;

    BeanVariableValueSourceFactory<Participant> delegateFactory = new BeanVariableValueSourceFactory<Participant>(PARTICIPANT, Participant.class);
    delegateFactory.setPrefix(participantPrefix);
    delegateFactory.setProperties(ImmutableSet.of("barcode", "enrollmentId", "appointment.date", "gender", "firstName", "lastName", "fullName", "birthDate", "siteNo", "recruitmentType"));
    delegateFactory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("appointment.date", "appointmentDate").build());
    delegateFactory.setVariableBuilderVisitors(ImmutableSet.of(new AdminVariableAttributeVisitor()));

    // Get the bean property sources.
    Set<VariableValueSource> sources = delegateFactory.createSources();

    // Add Javascript sources for birthYear and age.
    String birthDateVariableName = participantPrefix + '.' + "birthDate";
    sources.add(new JavascriptVariableValueSource(Variable.Builder.newVariable(participantPrefix + '.' + "birthYear", IntegerType.get(), PARTICIPANT).extend(JavascriptVariableBuilder.class).setScript("$('" + birthDateVariableName + "').year()").build()));
    sources.add(new JavascriptVariableValueSource(Variable.Builder.newVariable(participantPrefix + '.' + "age", IntegerType.get(), PARTICIPANT).extend(JavascriptVariableBuilder.class).setScript("now().year() - $('" + birthDateVariableName + "').year() - (now().dayOfYear() < $('" + birthDateVariableName + "').dayOfYear() ? 1 : 0)").build()));

    // Add sources for any configured attributes.
    ParticipantBeanVariableValueSourceFactory participantAttributeSourceFactory = new ParticipantBeanVariableValueSourceFactory();
    participantAttributeSourceFactory.setAttributeHelper(attributeHelper);
    participantAttributeSourceFactory.setParticipantMetadata(participantMetadata);
    participantAttributeSourceFactory.setPrefix(participantPrefix);
    sources.addAll(participantAttributeSourceFactory.createSources());

    return sources;
  }

  private Set<VariableValueSource> createInterviewSources() {
    BeanVariableValueSourceFactory<Interview> delegateFactory = new BeanVariableValueSourceFactory<Interview>(PARTICIPANT, Interview.class);
    delegateFactory.setPrefix(ONYX_ADMIN_PREFIX + '.' + INTERVIEW);
    delegateFactory.setProperties(ImmutableSet.of("startDate", "endDate", "status", "duration"));

    return delegateFactory.createSources();
  }

  private Set<VariableValueSource> createAppConfigSources() {
    BeanVariableValueSourceFactory<ApplicationConfiguration> delegateFactory = new BeanVariableValueSourceFactory<ApplicationConfiguration>(PARTICIPANT, ApplicationConfiguration.class);
    delegateFactory.setPrefix(ONYX_ADMIN_PREFIX + '.' + APPLICATION_CONFIGURATION);
    delegateFactory.setProperties(ImmutableSet.of("siteNo", "siteName", "studyName"));
    delegateFactory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("siteNo", "siteCode").build());

    return delegateFactory.createSources();
  }

  private Set<VariableValueSource> createActionSources() {
    BeanVariableValueSourceFactory<Action> delegateFactory = new BeanVariableValueSourceFactory<Action>(PARTICIPANT, Action.class);
    delegateFactory.setPrefix(ONYX_ADMIN_PREFIX + '.' + ACTION);
    delegateFactory.setOccurrenceGroup(ACTION);
    delegateFactory.setProperties(ImmutableSet.of("user.login", "stage", "fromState", "toState", "actionType", "dateTime", "comment", "eventReason"));
    delegateFactory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("user.login", "user").build());

    return delegateFactory.createSources();
  }

  private Set<VariableValueSource> createStageInstanceSources() {
    BeanVariableValueSourceFactory<StageInstance> delegateFactory = new BeanVariableValueSourceFactory<StageInstance>(PARTICIPANT, StageInstance.class);
    delegateFactory.setPrefix(ONYX_ADMIN_PREFIX + '.' + STAGE_INSTANCE);
    delegateFactory.setOccurrenceGroup(STAGE_INSTANCE);
    delegateFactory.setProperties(ImmutableSet.of("stage", "startTime", "lastTime", "lastState", "duration", "interruptionCount", "user.login", "last"));
    delegateFactory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("user.login", "user").build());

    return delegateFactory.createSources();
  }

  //
  // Inner Classes
  //

  private static class AdminVariableAttributeVisitor implements Variable.BuilderVisitor {

    public void visit(Builder builder) {
      // For variables containing personally identifiable information, add "pii"
      // attribute with value "true".
      if(builder.isName("barcode", "firstName", "lastName", "fullName", "birthDate")) {
        OnyxAttributeHelper.addPiiAttribute(builder);
      }

      // For variables that are participant identifiers, add "identifier" attribute
      // with value "true".
      if(builder.isName("barcode")) {
        OnyxAttributeHelper.addIdentifierAttribute(builder);
      }
    }

  }
}
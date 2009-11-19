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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.js.JavascriptVariableBuilder;
import org.obiba.magma.js.JavascriptVariableValueSource;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.stage.StageInstance;
import org.obiba.onyx.engine.Action;
import org.obiba.runtime.Version;

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

  private OnyxAdminValueSetBeanResolver beanResolver;

  private Version version;

  //
  // VariableValueSourceFactory Methods
  //

  public Set<VariableValueSource> createSources(String collection) {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    sources.add(createVersionSource(collection));
    sources.addAll(createParticipantSources(collection));
    sources.addAll(createInterviewSources(collection));
    sources.addAll(createAppConfigSources(collection));
    sources.addAll(createActionSources(collection));
    sources.addAll(createStageInstanceSources(collection));

    return sources;
  }

  //
  // Methods
  //

  public void setBeanResolver(OnyxAdminValueSetBeanResolver beanResolver) {
    this.beanResolver = beanResolver;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  private VariableValueSource createVersionSource(final String collection) {
    return new VariableValueSource() {
      private Variable variable;

      private ValueType type = TextType.get();

      public Variable getVariable() {
        if(variable != null) {
          // Building version variable with entityType "Participant". This is a little odd, since version
          // is in fact not related to *any* entityType.
          Variable.Builder builder = Variable.Builder.newVariable(collection, ONYX_ADMIN_PREFIX + '.' + ONYX_VERSION, getValueType(), PARTICIPANT);
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

  private Set<VariableValueSource> createParticipantSources(String collection) {
    BeanVariableValueSourceFactory<Participant> delegateFactory = new BeanVariableValueSourceFactory<Participant>(PARTICIPANT, Participant.class);
    delegateFactory.setPrefix(ONYX_ADMIN_PREFIX + '.' + PARTICIPANT);
    delegateFactory.setProperties(toSet("barcode", "enrollmentId", "appointmentDate", "gender", "firstName", "lastName", "fullName", "birthDate", "siteNo", "recruitmentType"));

    // Get the bean property sources.
    Set<VariableValueSource> sources = delegateFactory.createSources(collection, beanResolver);

    // Add Javascript sources for birthYear and age.
    sources.add(new JavascriptVariableValueSource(Variable.Builder.newVariable(collection, "birthYear", IntegerType.get(), PARTICIPANT).extend(JavascriptVariableBuilder.class).setScript("$('birthDate').year()").build()));
    sources.add(new JavascriptVariableValueSource(Variable.Builder.newVariable(collection, "age", IntegerType.get(), PARTICIPANT).extend(JavascriptVariableBuilder.class).setScript("now().year() - $('birthDate').year() - (now().dayOfYear() < $('birthDate').dayOfYear() ? 1 : 0)").build()));

    // Add sources for any configured attributes.
    ParticipantBeanVariableValueSourceFactory participantAttributeSourceFactory = new ParticipantBeanVariableValueSourceFactory();
    participantAttributeSourceFactory.setPrefix(ONYX_ADMIN_PREFIX + '.' + PARTICIPANT);
    sources.addAll(participantAttributeSourceFactory.createSources(collection, beanResolver));

    return sources;
  }

  private Set<VariableValueSource> createInterviewSources(String collection) {
    BeanVariableValueSourceFactory<Interview> delegateFactory = new BeanVariableValueSourceFactory<Interview>(PARTICIPANT, Interview.class);
    delegateFactory.setPrefix(ONYX_ADMIN_PREFIX + '.' + INTERVIEW);
    delegateFactory.setProperties(toSet("startDate", "endDate", "status", "duration"));

    return delegateFactory.createSources(collection, beanResolver);
  }

  private Set<VariableValueSource> createAppConfigSources(String collection) {
    BeanVariableValueSourceFactory<ApplicationConfiguration> delegateFactory = new BeanVariableValueSourceFactory<ApplicationConfiguration>(PARTICIPANT, ApplicationConfiguration.class);
    delegateFactory.setPrefix(ONYX_ADMIN_PREFIX + '.' + APPLICATION_CONFIGURATION);
    delegateFactory.setProperties(toSet("siteCode", "siteName", "studyPrefix"));

    return delegateFactory.createSources(collection, beanResolver);
  }

  private Set<VariableValueSource> createActionSources(String collection) {
    BeanVariableValueSourceFactory<Action> delegateFactory = new BeanVariableValueSourceFactory<Action>(PARTICIPANT, Action.class);
    delegateFactory.setPrefix(ONYX_ADMIN_PREFIX + '.' + ACTION);
    delegateFactory.setOccurrenceGroup(ACTION);
    delegateFactory.setProperties(toSet("user", "stage", "fromState", "toState", "type", "dateTime", "comment", "eventReason"));
    delegateFactory.setPropertyNameToVariableName(toMap("type", "actionType"));

    return delegateFactory.createSources(collection, beanResolver);
  }

  private Set<VariableValueSource> createStageInstanceSources(String collection) {
    BeanVariableValueSourceFactory<StageInstance> delegateFactory = new BeanVariableValueSourceFactory<StageInstance>(PARTICIPANT, StageInstance.class);
    delegateFactory.setPrefix(ONYX_ADMIN_PREFIX + '.' + STAGE_INSTANCE);
    delegateFactory.setOccurrenceGroup(STAGE_INSTANCE);
    delegateFactory.setProperties(toSet("stage", "startTime", "lastTime", "lastState", "duration", "interruptionCount", "user", "last"));

    return delegateFactory.createSources(collection, beanResolver);
  }

  private Set<String> toSet(String... items) {
    return new HashSet<String>(Arrays.asList(items));
  }

  private Map<String, String> toMap(String... entries) {
    Map<String, String> map = new HashMap<String, String>();

    for(int i = 0; i < entries.length - 1; i++) {
      map.put(entries[i], entries[i + 1]);
    }

    return map;
  }
}
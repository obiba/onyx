/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.runtime.upgrade;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.exception.JDBCException;

import org.obiba.runtime.upgrade.support.LiquiBaseUpgradeStep;

public class Constraints_1_6_0 extends LiquiBaseUpgradeStep {
  //
  // LiquiBaseUpgradeStep Methods
  //

  protected List<Change> getChanges() {
    List<Change> changes = new ArrayList<Change>();

    // Get a snapshot of the current database.
    DatabaseSnapshot databaseSnapshot = null;
    try {
      Database database = getDatabase();
      databaseSnapshot = database.createDatabaseSnapshot(null, null);
    } catch(JDBCException ex) {
      throw new RuntimeException("Could not create a database snapshot", ex);
    }

    // action
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "action", "user_id"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "action", "interview_id"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "action", "date_time"));

    // application_configuration
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "application_configuration", "study_name"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "application_configuration", "site_name"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "application_configuration", "site_no"));

    // appointment
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "appointment", "date"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "appointment", "participant_id"));

    // category_answer
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "category_answer", "active"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "category_answer", "category_name"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "category_answer", "question_answer_id"));

    // consent
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "consent", "interview_id"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "consent", "mode"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "consent", "locale"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "consent", "deleted"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "consent", "time_start"));

    // instrument
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "instrument", "type"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "instrument", "barcode"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "instrument", "status"));

    // instrument_run
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "instrument_run", "time_start"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "instrument_run", "status"));

    // instrument_run_value
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "instrument_run_value", "instrument_run_id"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "instrument_run_value", "instrument_parameter"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "instrument_run_value", "capture_method"));

    // interview
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "interview", "start_date"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "interview", "status"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "interview", "participant_id"));

    // measure
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "measure", "instrument_run_id"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "measure", "user_id"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "measure", "time"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "measure", "instrument_barcode"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "measure", "status"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "measure", "workstation"));

    // open_answer
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "open_answer", "open_answer_definition_name"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "open_answer", "category_answer_id"));

    // participant
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "participant", "site_no"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "participant", "first_name"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "participant", "last_name"));

    // participant_attribute_value
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "participant_attribute_value", "participant_id"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "participant_attribute_value", "attribute_name"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "participant_attribute_value", "attribute_type"));

    // question_answer
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "question_answer", "question_name"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "question_answer", "questionnaire_participant_id"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "question_answer", "active"));

    // questionnaire_participant
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "questionnaire_participant", "participant_id"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "questionnaire_participant", "user_id"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "questionnaire_participant", "questionnaire_name"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "questionnaire_participant", "questionnaire_version"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "questionnaire_participant", "locale"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "questionnaire_participant", "time_start"));

    // registered_participant_tube
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "registered_participant_tube", "barcode"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "registered_participant_tube", "registration_time"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "registered_participant_tube", "participant_tube_registration_id"));

    // role
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "role", "name"));

    // stage_execution_memento
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "stage_execution_memento", "stage"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "stage_execution_memento", "interview_id"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "stage_execution_memento", "state"));

    // user
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "user", "login"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "user", "status"));
    changes.add(createAddNotNullConstraintChange(databaseSnapshot, "user", "deleted"));

    return changes;
  }
}
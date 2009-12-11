/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.batch;

import java.util.Date;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

/**
 * JobParametersIncrementer for parameters of appointment list update job.
 */
public class AppointmentListUpdateJobParameterIncrementer implements JobParametersIncrementer {
  //
  // Constants
  //

  public static final String DATE_PARAMETER_KEY = "date";

  //
  // JobParametersIncrementer Methods
  //

  public JobParameters getNext(JobParameters parameters) {
    return new JobParametersBuilder().addDate(DATE_PARAMETER_KEY, new Date()).toJobParameters();
  }

}

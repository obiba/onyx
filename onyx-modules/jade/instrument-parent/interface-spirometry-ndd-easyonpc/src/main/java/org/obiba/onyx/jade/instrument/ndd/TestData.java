/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.ndd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestData<T extends TrialData> {

  static final Logger log = LoggerFactory.getLogger(TestData.class);

  final SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

  private String type;

  private Date date;

  private List<T> trials = new ArrayList<T>();

  public TestData() {
    super();
  }

  public List<T> getTrials() {
    return trials;
  }

  public String getType() {
    return type;
  }

  public Date getDate() {
    return date;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setDate(String date) {
    try {
      this.date = ISO_8601.parse(date);
    } catch(ParseException e) {
      log.error("Unable to parse trial date: " + date, e);
    }
  }
}
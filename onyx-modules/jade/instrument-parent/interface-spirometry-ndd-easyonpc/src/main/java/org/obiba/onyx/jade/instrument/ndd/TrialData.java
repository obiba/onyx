/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
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
import java.util.Date;
import java.util.Map;

public class TrialData {

  private final SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

  TrialData() {
    super();
  }

  private Map<String, Number> results;

  private Date date;

  private Long rank;

  public void setDate(String date) {
    try {
      this.date = ISO_8601.parse(date);
    } catch(ParseException e) {
      TestData.log.error("Unable to parse trial date: " + date, e);
    }
  }

  public Date getDate() {
    return date;
  }

  public void setRank(Long rank) {
    this.rank = rank;
  }

  public Long getRank() {
    return rank;
  }

  public void setResults(Map<String, Number> results) {
    this.results = results;
  }

  public Map<String, Number> getResults() {
    return results;
  }

  @Override
  public String toString() {
    return "date=" + date + ",results=" + results.toString();
  }
}
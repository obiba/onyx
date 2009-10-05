/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.application;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.obiba.core.domain.AbstractEntity;

@Entity
public class ApplicationConfiguration extends AbstractEntity {

  private static final long serialVersionUID = -943397281652866650L;

  @Column(nullable = false)
  private String studyName;

  @Column(nullable = false)
  private String siteName;

  @Column(nullable = false)
  private String siteNo;

  /**
   * Session timeout (in minutes).
   */
  private Integer sessionTimeout;

  public String getStudyName() {
    return studyName;
  }

  public void setStudyName(String studyName) {
    this.studyName = studyName;
  }

  public String getSiteName() {
    return siteName;
  }

  public void setSiteName(String siteName) {
    this.siteName = siteName;
  }

  public String getSiteNo() {
    return siteNo;
  }

  public void setSiteNo(String siteNo) {
    this.siteNo = siteNo;
  }

  /**
   * Sets the session timeout.
   * 
   * @param sessionTimeout session timeout (in minutes)
   */
  public void setSessionTimeout(Integer sessionTimeout) {
    this.sessionTimeout = sessionTimeout;
  }

  /**
   * Returns the session timeout
   * 
   * @return session timeout (in minutes)
   */
  public Integer getSessionTimeout() {
    return sessionTimeout;
  }
}

package org.obiba.onyx.core.domain.application;

import javax.persistence.Entity;

import org.obiba.core.domain.AbstractEntity;

@Entity
public class ApplicationConfiguration extends AbstractEntity {

  private static final long serialVersionUID = -943397281652866650L;

  private String studyName;

  private String siteName;

  private String siteNo;

  private String participantDirectoryPath;

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

  public String getParticipantDirectoryPath() {
    return participantDirectoryPath;
  }

  public void setParticipantDirectoryPath(String participantDirectoryPath) {
    this.participantDirectoryPath = participantDirectoryPath;
  }

}

/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.statistics;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.obiba.core.domain.AbstractEntity;
import org.springframework.util.Assert;

@Entity
public class ExportLog extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false)
  private String type;

  @Column(nullable = false)
  private String identifier;

  @Column(nullable = false)
  private String destination;

  @Column(nullable = false)
  private Date captureStartDate;

  @Column(nullable = false)
  private Date captureEndDate;

  @Column(nullable = false)
  private Date exportDate;

  /**
   * Login name of user who performed the export.
   */
  @Column(nullable = false)
  private String user;

  ExportLog() {
    super();
  }

  private ExportLog(String type, String identifier, String destination, Date captureStartDate, Date captureEndDate, Date exportDate, String user) {
    super();
    this.type = type;
    this.identifier = identifier;
    this.destination = destination;
    this.captureStartDate = captureStartDate;
    this.captureEndDate = captureEndDate;
    this.exportDate = exportDate;
    this.user = user;
  }

  public String getType() {
    return type;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getDestination() {
    return destination;
  }

  public Date getCaptureStartDate() {
    return captureStartDate;
  }

  public Date getCaptureEndDate() {
    return captureEndDate;
  }

  public Date getExportDate() {
    return exportDate;
  }

  public String getUser() {
    return user;
  }

  public static class Builder {
    private String type;

    private String identifier;

    private String destination;

    private Date captureStartDate;

    private Date captureEndDate;

    private Date exportDate;

    private String user;

    public static Builder newLog() {
      return new Builder();
    }

    public Builder type(String type) {
      this.type = type;
      return this;
    }

    public Builder identifier(String identifier) {
      this.identifier = identifier;
      return this;
    }

    public Builder destination(String destination) {
      this.destination = destination;
      return this;
    }

    public Builder start(Date captureStartDate) {
      this.captureStartDate = captureStartDate;
      return this;
    }

    public Builder end(Date captureEndDate) {
      this.captureEndDate = captureEndDate;
      return this;
    }

    public Builder exportDate(Date exportDate) {
      this.exportDate = exportDate;
      return this;
    }

    public Builder user(String user) {
      this.user = user;
      return this;
    }

    public ExportLog build() {
      Assert.hasText(type, "type must not be null or empty");

      if(identifier != null) {
        Assert.isTrue(identifier.trim().length() != 0, "identifier must not be empty");
      }

      if(captureStartDate != null && captureEndDate != null) {
        Assert.isTrue(!captureStartDate.after(captureEndDate), String.format("captureStartDate (%d) must be equal to or before captureEndDate (%d)", captureStartDate.getTime(), captureEndDate.getTime()));
      }

      if(captureEndDate != null && exportDate != null) {
        Assert.isTrue(!captureEndDate.after(exportDate), String.format("captureEndDate (%d) must be equal to or before exportDate (%d)", captureEndDate.getTime(), exportDate.getTime()));
      }

      return new ExportLog(type, identifier, destination, captureStartDate, captureEndDate, exportDate, user);
    }
  }

}

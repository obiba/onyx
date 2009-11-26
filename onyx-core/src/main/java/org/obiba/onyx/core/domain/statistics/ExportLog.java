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

import javax.persistence.Entity;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.user.User;
import org.springframework.util.Assert;

@Entity
public class ExportLog extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  private final String type;

  private final String identifier;

  private final String destination;

  private final Date captureStartDate;

  private final Date captureEndDate;

  private final Date exportDate;

  private User user;

  private ExportLog(String type, String identifier, String destination, Date captureStartDate, Date captureEndDate, Date exportDate, User user) {
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

  public User getUser() {
    return user;
  }

  public static class Builder {
    private String type;

    private String identifier;

    private String destination;

    private Date captureStartDate;

    private Date captureEndDate;

    private Date exportDate;

    private User user;

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

    public Builder user(User user) {
      this.user = user;
      return this;
    }

    public ExportLog build() {
      Assert.hasText(type, "type must not be null or empty");
      Assert.hasText(identifier, "identifier must not be null or empty");
      Assert.hasText(destination, "destination must not be null or empty");
      Assert.notNull(captureStartDate, "captureStartDate must not be null");
      Assert.notNull(captureEndDate, "captureEndDate must not be null");
      Assert.notNull(exportDate, "exportDate must not be null");
      Assert.notNull(user, "user must not be null");
      Assert.isTrue(captureStartDate.before(captureEndDate), "captureStartDate must be before captureEndDate");
      Assert.isTrue(captureEndDate.before(exportDate), "captureEndDate must be before exportDate");
      return new ExportLog(type, identifier, destination, captureStartDate, captureEndDate, exportDate, user);
    }
  }

}

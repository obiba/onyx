package org.obiba.onyx.core.domain.participant;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.engine.Stage;

@Entity
public class Interview extends AbstractEntity {

  private static final long serialVersionUID = -8786498940712113896L;

  @Temporal(TemporalType.TIMESTAMP)
  private Date startDate;

  @Temporal(TemporalType.TIMESTAMP)
  private Date stopDate;

  @OneToOne
  @JoinColumn(name = "participant_id")
  private Participant participant;

  @ManyToMany
  @JoinTable(name = "stage_interview", joinColumns = @JoinColumn(name = "stage_id"), inverseJoinColumns = @JoinColumn(name = "interview_id"))
  private List<Stage> stages;

  @Enumerated(EnumType.STRING)
  private InterviewStatus status;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;
  
  private Boolean closed;

  public Interview() {
  }

  public Interview(Participant participant) {
    this.participant = participant;
    this.startDate = new Date();
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getStopDate() {
    return stopDate;
  }

  public void setStopDate(Date stopDate) {
    this.stopDate = stopDate;
  }

  public Participant getParticipant() {
    return participant;
  }

  public void setParticipant(Participant participant) {
    this.participant = participant;
  }

  public List<Stage> getStages() {
    return stages != null ? stages : (stages = new ArrayList<Stage>());
  }

  public void addStage(Stage stage) {
    if(stage != null) {
      getStages().add(stage);
      stage.addInterview(this);
    }
  }

  public InterviewStatus getStatus() {
    return status;
  }

  public void setStatus(InterviewStatus status) {
    this.status = status;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public void setClosed(Boolean closed) {
    this.closed = closed;
  }

  public boolean isClosed() {
    return closed != null ? closed : (closed = false);
  }
}

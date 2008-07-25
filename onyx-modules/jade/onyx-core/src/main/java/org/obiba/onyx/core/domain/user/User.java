package org.obiba.onyx.core.domain.user;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Interview;

@Entity
public class User extends AbstractEntity {

  private static final long serialVersionUID = -2200053643926715563L;

  private String name;
  
  private String login;
  
  private String password;
  
  @Enumerated(EnumType.STRING)
  private Role role;
  
  @OneToMany(mappedBy = "user")
  private List<Interview> interviews;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public List<Interview> getInterviews() {
    return interviews != null ? interviews : (interviews = new ArrayList<Interview>());
  }
  
  public void addInterview(Interview interview) {
    if (interview != null) {
      getInterviews().add(interview);
      interview.setUser(this);
    }
  }
  
}

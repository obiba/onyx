package org.obiba.onyx.core.domain.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.user.Role;

@Entity
public class User extends AbstractEntity {

  private static final long serialVersionUID = -2200053643926715563L;

  private String lastName;
  
  private String firstName;

  private String login;

  private String password;

  private String email;
  
  private Locale language;

  private Boolean deleted;

  @Enumerated(EnumType.STRING)
  private Status status;
  
  @ManyToMany
  @JoinTable(
          name="user_roles",
          joinColumns = { @JoinColumn(name="user_id") },
          inverseJoinColumns = @JoinColumn(name="role_id")
  )
  private Set<Role> roles;
  
  @OneToMany(mappedBy = "user")
  private List<Interview> interviews;

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }
  
  public String getFullName() {
    return getFirstName() + " " + getLastName();
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
  
  public Set<Role> getRoles() {
    return (roles != null) ? roles : (roles = new HashSet<Role>());
  }
  
  public void setRoles(Set<Role> roles) { this.roles = roles; }
  
  public void addRole(Role role) {
    getRoles().add(role);
  }
  
  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }
  
  public List<Interview> getInterviews() {
    return interviews != null ? interviews : (interviews = new ArrayList<Interview>());
  }

  public void addInterview(Interview interview) {
    if(interview != null) {
      getInterviews().add(interview);
      interview.setUser(this);
    }
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public boolean isDeleted() {
    if(deleted == null) return false;
    else
      return deleted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }
  
  public Locale getLanguage() {
    return language;
  }

  public void setLanguage(Locale language) {
    this.language = language;
  }
  
  /**
   * Digest the password into a predefined algorithm.
   * @param password
   * @return
   */
  public static String digest(String password) {
    try {
      return convertToHex(MessageDigest.getInstance("SHA").digest(password.getBytes()));
    } catch(NoSuchAlgorithmException e) {
      e.printStackTrace();
      return password;
    }
  }

  private static String convertToHex(byte[] data) {
    StringBuffer buf = new StringBuffer();
    for(int i = 0; i < data.length; i++) {
      int halfbyte = (data[i] >>> 4) & 0x0F;
      int two_halfs = 0;
      do {
        if((0 <= halfbyte) && (halfbyte <= 9)) buf.append((char) ('0' + halfbyte));
        else
          buf.append((char) ('a' + (halfbyte - 10)));
        halfbyte = data[i] & 0x0F;
      } while(two_halfs++ < 1);
    }
    return buf.toString();
  }
}

package org.obiba.onyx.core.domain.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Interview;

@TypeDefs({
  @TypeDef(name="role", typeClass=RoleType.class  
  ) // Register the role type
})
@Entity
public class User extends AbstractEntity {

  private static final long serialVersionUID = -2200053643926715563L;

  private String name;

  private String login;

  private String password;

  private String email;
  
  private Locale language;

  private Boolean deleted;

  @Type(type="role")
  @Column(length=30)
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

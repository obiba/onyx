/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.obiba.core.domain.AbstractEntity;
import org.obiba.core.util.HexUtil;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class User extends AbstractEntity {

  private static final long serialVersionUID = -2200053643926715563L;

  @Column(length = 250)
  private String lastName;

  @Column(length = 250)
  private String firstName;

  @Column(length = 250, nullable = false)
  private String login;

  @Column(length = 250)
  private String password;

  @Column(length = 250)
  private String email;

  private Locale language;

  @Column(nullable = false)
  private Boolean deleted;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Status status;

  @ManyToMany
  @JoinTable(name = "user_roles", joinColumns = { @JoinColumn(name = "user_id") }, inverseJoinColumns = @JoinColumn(name = "role_id"))
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
  private Set<Role> roles;

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

  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }

  public void addRole(Role role) {
    getRoles().add(role);
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
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
      return HexUtil.bytesToHex(MessageDigest.getInstance("SHA").digest(password.getBytes()));
    } catch(NoSuchAlgorithmException e) {
      e.printStackTrace();
      return password;
    }
  }

  public boolean isActive() {
    return (this.getStatus().equals(Status.ACTIVE));
  }
}

package org.obiba.onyx.core.domain.user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.ImmutableType;

/**
 * Used to persist a @see#Role as a string (SQL VARCHAR type).
 */
public class RoleType extends ImmutableType implements DiscriminatorType {

  private static final long serialVersionUID = 64164164L;

  @Override
  public Object fromStringValue(String xml) {
    return new Role(xml);
  }

  @Override
  public String toString(Object value) {
    return value.toString();
  }

  @Override
  public Object get(ResultSet rs, String name) throws HibernateException, SQLException {
    return new Role(rs.getString(name));
  }

  @Override
  public void set(PreparedStatement ps, Object value, int index) throws HibernateException, SQLException {
    ps.setString(index, value.toString());
  }

  @Override
  public int sqlType() {
    return Types.VARCHAR;
  }

  public Object stringToObject(String str) throws Exception {
    return new Role(str);
  }

  public String getName() {
    return "role";
  }

  public Class<Role> getReturnedClass() {
    return Role.class;
  }

  public String objectToSQLString(Object value, Dialect arg1) throws Exception {
    return '\'' + (String) value.toString() + '\'';
  }
}

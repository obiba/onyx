/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.runtime.upgrade;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.VersionModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class OnyxVersionModifier implements VersionModifier, InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(OnyxVersionModifier.class);

  private DataSource datasource;

  private JdbcTemplate jdbcTemplate;

  private Version version;

  private Version versionBeingInstalled;

  public void setVersionBeingInstalled(Version versionBeingInstalled) {
    this.versionBeingInstalled = versionBeingInstalled;
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
    jdbcTemplate.execute("delete from version");
    jdbcTemplate.update("insert into version ( major, minor, micro, qualifier, version_string ) values (?,?,?,?,?)", new Object[] { version.getMajor(), version.getMinor(), version.getMicro(), version.getQualifier(), version.toString() });
  }

  public void setDatasource(DataSource datasource) {
    this.datasource = datasource;
  }

  public void afterPropertiesSet() throws Exception {
    jdbcTemplate = new JdbcTemplate(datasource);

    try {
      log.info("Attempting to retrieve the currently running version information from the database...");
      List result = jdbcTemplate.queryForList("select * from version");
      if(result != null && result.size() > 0) {
        Map versionInfo = (Map) result.get(0);
        String major = versionInfo.get("major").toString();
        String minor = versionInfo.get("minor").toString();
        String micro = versionInfo.get("micro").toString();
        String qualifier = versionInfo.get("qualifier").toString();
        Version version = new Version(Integer.parseInt(major), Integer.parseInt(minor), Integer.parseInt(micro), qualifier);

        log.info("The current version is {} ", version.toString());
        setVersion(version);
      }
    } catch(DataAccessException couldNotRetrieveCurrentVersion) {
      log.warn("Could not retrieve the current version.  This looks like a new installation of Onyx, so no upgrades are needed.");
      log.info("Attempting to create the Version table in the database...");
      try {
        jdbcTemplate.execute("create table version (major int, minor int, micro int, qualifier varchar(50),version_string varchar(50))");
        log.info("Successfully created the Version table.");
        log.info("Setting version to : {}", versionBeingInstalled.toString());
        setVersion(versionBeingInstalled);
      } catch(DataAccessException couldNotCreateVersionTable) {
        throw new RuntimeException("Could not create version table", couldNotCreateVersionTable);
      }
    }

  }
}

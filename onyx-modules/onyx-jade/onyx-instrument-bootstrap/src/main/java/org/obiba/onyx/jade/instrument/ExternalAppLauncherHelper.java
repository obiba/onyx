/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for launching an external application.
 */
public class ExternalAppLauncherHelper {

  private static final Logger log = LoggerFactory.getLogger(ExternalAppLauncherHelper.class);

  // Working directory for external software.
  protected String workDir;

  // Executable of external software.
  protected String executable;

  // Parameters of external software command
  protected String parameterStr;

  // separator to split executable and/or parameters
  protected String tokenizer;

  protected boolean windowsCommand = true;

  public void launch() {
    if(isSotfwareAlreadyStarted()) {
      JOptionPane.showMessageDialog(null,
          getExecutable() + " already lock for execution.  Please make sure that another instance is not running.",
          "Cannot start application!", JOptionPane.ERROR_MESSAGE);
    } else {
      launchExternalSoftware();
    }
  }

  public void launchExternalSoftware() {

    List<String> command = new ArrayList<String>();
    if (windowsCommand) {
      command.add("cmd");
      command.add("/c");
    }

    if (tokenizer != null) {
      for (String token : executable.split(tokenizer)) {
        command.add(token);
      }
    } else
      command.add(executable);

    if(parameterStr != null) {
      if (tokenizer != null) {
        for (String token : parameterStr.split(tokenizer)) {
          command.add(token);
        }
      } else
        command.add(parameterStr);
    }
    log.info("Launching {} (Work directory={})", command, getWorkDir());

    ProcessBuilder builder = new ProcessBuilder(command);
    builder.directory(new File(getWorkDir()));

    Process wProcess = null;
    try {
      wProcess = builder.start();
    } catch(IOException e) {
      JOptionPane.showMessageDialog(null, "Could not create external process for: " + getWorkDir() + getExecutable(),
          "Cannot start application!", JOptionPane.ERROR_MESSAGE);
      throw new RuntimeException(e);
    }

    OutputPurger wOutputErrorPurger = new OutputPurger(wProcess.getErrorStream());
    OutputPurger wOutputPurger = new OutputPurger(wProcess.getInputStream());

    wOutputErrorPurger.start();
    wOutputPurger.start();

    try {
      wProcess.waitFor();
    } catch(InterruptedException e) {
      throw new RuntimeException(e);
    }

  }

  public boolean isSotfwareAlreadyStarted() {
    return isSotfwareAlreadyStarted(getExecutable());
  }

  public boolean isSotfwareAlreadyStarted(String lockName) {

    File wFile = new File(System.getProperty("java.io.tmpdir"), lockName + ".lock");

    try {

      FileChannel channel = new RandomAccessFile(wFile, "rw").getChannel();
      FileLock collectorLock = null;

      try {
        collectorLock = channel.tryLock();
      } catch(OverlappingFileLockException wEx) {
        return true;
      }

      if(collectorLock == null) {
        return true;
      } else {
        return false;
      }

    } catch(Exception wCouldNotDetermineIfRunning) {
      wCouldNotDetermineIfRunning.printStackTrace();
      return true;
    }

  }

  public void setWindowsCommand(boolean windowsCommand) {
    this.windowsCommand = windowsCommand;
  }

  public boolean isWindowsCommand() {
    return windowsCommand;
  }

  public String getExecutable() {
    return executable;
  }

  public void setExecutable(String executable) {
    this.executable = executable;
  }

  public void setTokenizer(String tokenizer) {
    this.tokenizer = tokenizer;
  }

  public String getTokenizer() {
    return tokenizer;
  }

  public String getParameterStr() {
    return parameterStr;
  }

  public void setParameterStr(String parameterStr) {
    this.parameterStr = parameterStr;
  }

  public String getWorkDir() {
    return workDir;
  }

  public void setWorkDir(String workDir) {
    this.workDir = workDir;
  }

  private static class OutputPurger extends Thread {

    InputStream inputStream;

    OutputPurger(InputStream pInputStream) {
      inputStream = pInputStream;
    }

    @Override
    public void run() {
      try {
        InputStreamReader wReader = new InputStreamReader(inputStream);
        BufferedReader wBuffReader = new BufferedReader(wReader);

        while(wBuffReader.readLine() != null) {
        }

      } catch(IOException wEx) {
      }
    }
  }

}

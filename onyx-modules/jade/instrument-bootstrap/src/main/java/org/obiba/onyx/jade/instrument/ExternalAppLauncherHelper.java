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

public class ExternalAppLauncherHelper {

  // Working directory for external software.
  protected String workDir;

  // Executable of external software.
  protected String executable;

  public void launchExternalSoftware() {

    class OuputPurger extends Thread {

      InputStream inputStream;

      OuputPurger(InputStream pInputStream) {
        inputStream = pInputStream;
      }

      public void run() {
        try {
          InputStreamReader wReader = new InputStreamReader(inputStream);
          BufferedReader wBuffReader = new BufferedReader(wReader);

          while((wBuffReader.readLine()) != null) {
          }

        } catch(IOException wEx) {
        }
      }

    }

    List<String> command = new ArrayList<String>();
    command.add("cmd");
    command.add("/c");
    command.add(getExecutable());

    ProcessBuilder builder = new ProcessBuilder(command);
    builder.directory(new File(getWorkDir()));

    Process wProcess = null;
    try {
      wProcess = builder.start();
    } catch(IOException wCouldNotCreateProcess) {
      JOptionPane.showMessageDialog(null, "Could not create external process for: " + getWorkDir() + getExecutable(), "Cannot start application!", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }

    OuputPurger wOutputErrorPurger = new OuputPurger(wProcess.getErrorStream());
    OuputPurger wOutputPurger = new OuputPurger(wProcess.getInputStream());

    wOutputErrorPurger.start();
    wOutputPurger.start();

    try {
      wProcess.waitFor();
    } catch(InterruptedException wThreadInterrupted) {
      wThreadInterrupted.printStackTrace();
      System.exit(1);
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

  public void launch() {
    if(!isSotfwareAlreadyStarted()) {
      launchExternalSoftware();
    } else {
      JOptionPane.showMessageDialog(null, getExecutable() + " already lock for execution.  Please make sure that another instance is not running.", "Cannot start application!", JOptionPane.ERROR_MESSAGE);
    }
  }

  public String getExecutable() {
    return executable;
  }

  public void setExecutable(String executable) {
    this.executable = executable;
  }

  public String getWorkDir() {
    return workDir;
  }

  public void setWorkDir(String workDir) {
    this.workDir = workDir;
  }

}

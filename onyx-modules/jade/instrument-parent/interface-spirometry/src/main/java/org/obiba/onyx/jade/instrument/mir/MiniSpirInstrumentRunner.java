package org.obiba.onyx.jade.instrument.mir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.obiba.core.util.FileUtil;
import org.obiba.onyx.jade.client.JnlpClient;
import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniSpirInstrumentRunner implements InstrumentRunner {

  private static final Logger log = LoggerFactory.getLogger(JnlpClient.class);
  
	//Injected by spring.
  protected InstrumentExecutionService instrumentExecutionService;
  protected ExternalAppLauncherHelper externalAppHelper;
  private String mirPath;
  private String initdbPath;
  private String externalDbName;
  private String externalInputName;
  private String externalOutputName;
  private String externalImageName;

  public InstrumentExecutionService getInstrumentExecutionService() {
    return instrumentExecutionService;
  }

  public void setInstrumentExecutionService(InstrumentExecutionService instrumentExecutionService) {
    this.instrumentExecutionService = instrumentExecutionService;
  }

  public ExternalAppLauncherHelper getExternalAppHelper() {
    return externalAppHelper;
  }

  public void setExternalAppHelper(ExternalAppLauncherHelper externalAppHelper) {
    this.externalAppHelper = externalAppHelper;
  }

  public String getMirPath() {
    return mirPath;
  }

  public void setMirPath(String mirPath) {
    this.mirPath = mirPath;
  }
  
  public String getInitdbPath() {
    return initdbPath;
  }

  public void setInitdbPath(String initdbPath) {
    this.initdbPath = initdbPath;
  }
  
  public String getExternalDbName() {
    return externalDbName;
  }

  public void setExternalDbName(String externalDbName) {
    this.externalDbName = externalDbName;
  }
  
  public String getExternalInputName() {
    return externalInputName;
  }

  public void setExternalInputName(String externalInputName) {
    this.externalInputName = externalInputName;
  }
  
  public String getExternalOutputName() {
  	return externalOutputName;
  }
  
  public void setExternalOutputName(String externalOutputName) {
  	this.externalOutputName = externalOutputName;
  }
  
  public String getExternalImageName() {
  	return externalImageName;
  }
  
  public void setExternalImageName(String externalImageName) {
  	this.externalImageName = externalImageName;
  }
  
  public void initParticipantData() throws Exception{
  	// normally should retrieve latest data from database with current participant
  	
  	// Write participant data in spirometer input file
  	File externalAppInputFile = new File(getMirPath() + getExternalInputName());
		try {
			//Map<String, Data> inputData = instrumentExecutionService.getInputParametersValue("ID", "LastName");
			
			// Test: to be removed
			Map<String, Data> inputData = new HashMap<String, Data>();
			inputData.put("ID", new Data(DataType.INTEGER, Integer.valueOf("2")));
			inputData.put("LastName", new Data(DataType.TEXT, "Dupond"));
			inputData.put("FirstName", new Data(DataType.TEXT, "Émilie"));
			inputData.put("Gender", new Data(DataType.BOOLEAN, Boolean.valueOf("true")));
			inputData.put("Height", new Data(DataType.DECIMAL, Double.valueOf("169")));
			inputData.put("Weight", new Data(DataType.DECIMAL, Double.valueOf("57")));
			inputData.put("EthnicGroup", new Data(DataType.INTEGER, Integer.valueOf("1")));
			inputData.put("BirthDate", new Data(DataType.TEXT, "04/09/1979"));
			// End Test
			
			BufferedWriter inputFileWriter = new BufferedWriter(new FileWriter(externalAppInputFile));
			inputFileWriter.write( "[Identification]\n" );
			for(String keyStr : inputData.keySet()) {
				inputFileWriter.write( keyStr + "=" + inputData.get(keyStr).getValue().toString() + "\n" );
			}
			inputFileWriter.close();
		} catch ( Exception ex ) {
			throw new Exception( "Error writing spirometer input file: ", ex );
		}
  }
  
  protected void deleteDeviceData() throws Exception {
		File backupDbFile = new File(getInitdbPath() + getExternalDbName()); 
		File currentDbFile = new File(getMirPath() + getExternalDbName()); 
		
		if ( backupDbFile.exists() ) {
			FileUtil.copyFile(backupDbFile, currentDbFile);
		} else {
			new File(getInitdbPath()).mkdir();
			FileUtil.copyFile( currentDbFile, backupDbFile );
		}
	}
  
  private LinkedHashMap<String, Double[]> retrieveDeviceData() throws Exception {
		
  	//INCMOPLETE: Should retrieve all data, unit and image?
  	
		InputStream resultFileStrm = null;
		LinkedHashMap<String, Double[]> outputData = new LinkedHashMap<String, Double[]>();
		try {
			resultFileStrm = new FileInputStream(getMirPath() + getExternalOutputName());
			BufferedReader fileReader = new BufferedReader( new InputStreamReader( resultFileStrm ) );

			StringBuffer wResults = new StringBuffer();
			String line;
			Boolean lastParam = false;
			while ( (line = fileReader.readLine()) != null ) {
				wResults.append( line + "\n" );
				if (line.indexOf("PIF") == 0)
					lastParam = true;
			}
			if (lastParam == false)
				JOptionPane.showMessageDialog(null, "Data is incomplete", "Could not complete process", JOptionPane.ERROR_MESSAGE);
			
			Pattern pattern = Pattern.compile( "(.*)\t(.*)\t(.*)\t(.*)\t(.*)" );
			Matcher matcher = pattern.matcher( wResults.toString().replace( ",", "." ).replace( "/", "_" ) );
			String description = null;
			Double[] data = null;
			
			while(matcher.find()) {
				description = matcher.group(1);
				data = new Double[2];
				data[0] = Double.valueOf( matcher.group(3) );
				data[1] = Double.valueOf( matcher.group(4) );
				outputData.put( description, data );
			}
			
			try {
				resultFileStrm.close();
			} catch ( Exception ex ) {
				log.info("*** Error in closing spirometry output data file stream: " + ex.getStackTrace());
			}			
						
		} catch ( FileNotFoundException fnfEx ) {
			log.info("*** Error: spirometry output data file not found: " + fnfEx.getStackTrace());
			JOptionPane.showMessageDialog(null, "Error: spirometry output data file not found", "Could not complete process", JOptionPane.ERROR_MESSAGE);
		}
		return outputData;
	}

  public void SendDataToServer(LinkedHashMap<String, Double[]> results) throws Exception {
  	Map<String, Data> ouputToSend = new HashMap<String, Data>();
  	
  	for (String keyStr : results.keySet()) {
  		Double[] valueArray = results.get(keyStr);
  		ouputToSend.put(keyStr, new Data(DataType.DECIMAL, valueArray[0]));
  		ouputToSend.put(keyStr + "_pred", new Data(DataType.DECIMAL, valueArray[1]));
  	}
  	
  	// Should we keep FVC Image???
  	/*File FVCFile = new File( getMirPath() + getExternalImageName() );
		FileInputStream FVCInputStream = new FileInputStream( FVCFile );
		ouputToSend.put("FVCImage", new Data(DataType.DATA, FVCInputStream));*/
		
    instrumentExecutionService.addOutputParameterValues(ouputToSend);
  }
  
  public void initialize() {
  	log.info( "*** Initializing MIR Runner ***" );
  	try {
  		deleteDeviceData();				// Delete ancient data in instrument specific database
  		initParticipantData();		// Create file with participant data
  	} catch ( Exception ex ) {
  		log.info("*** EXCEPTION INITIALIZE STEP: " + ex.getStackTrace());
		}
  }

  public void run() {
  	log.info( "*** Running MIR Runner ***" );    
    externalAppHelper.launch();
  }

  public void shutdown() {
  	log.info( "*** Shutdown MIR Runner ***" );
  	// Get data from external app
  	try {
  		LinkedHashMap<String, Double[]> results = retrieveDeviceData();
  		SendDataToServer(results);
  	} catch( Exception ex) {
  		log.info("*** EXCEPTION SHUTDOWN STEP: " + ex.getStackTrace());
  	}
  	
  	// Send data to server => How ???
  }
  
}





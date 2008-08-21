package org.obiba.onyx.jade.instrument.gehealthcare;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import org.obiba.onyx.jade.client.JnlpClient;
import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AchillesExpressInstrumentRunner implements InstrumentRunner {

	protected Connection deviceDatabaseConn;
	protected String deviceDbUrl = "jdbc:odbc:DRIVER=Microsoft Access Driver (*.mdb);DBQ=C:/Program Files/Lunar/OsteoReport/WinOsteo.mdb;DriverId=25;FIL=MSAccess";
	
  private static final Logger log = LoggerFactory.getLogger(JnlpClient.class);

  // Injected by spring.
  protected InstrumentExecutionService instrumentExecutionService;

  protected ExternalAppLauncherHelper externalAppHelper;


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

  public AchillesExpressInstrumentRunner() throws Exception {
	deviceDatabaseConn = getMsAccessConnection( deviceDbUrl );			
  }  
  
	public static Connection getDatabaseConnection( String pDriver, String pUrlForConnection ) throws Exception {
		try {
			Class.forName( pDriver );			
			Connection wDBConnection = DriverManager.getConnection( pUrlForConnection );
			return wDBConnection;
		}
		catch (ClassNotFoundException wInvalidDriverName) {
			throw new RuntimeException( "Invalid driver name: " + pDriver, wInvalidDriverName );
		}
		catch ( SQLException wCouldNotEstablishConnection ) {
			throw new RuntimeException( "Could not establish connection with database: " + pUrlForConnection, wCouldNotEstablishConnection );
		}
	}
	
	public static Connection getMsAccessConnection( String pUrlForConnection ) throws Exception {
		final String MS_ACCESS_DRIVER = "sun.jdbc.odbc.JdbcOdbcDriver";			
		Connection wMsAccessConn = getDatabaseConnection( MS_ACCESS_DRIVER, pUrlForConnection );
		wMsAccessConn.setAutoCommit( false );
		
		return wMsAccessConn;
	}  
  
  public void initParticipantData() throws Exception {
		PreparedStatement wInsertOsteoReportData 
		= deviceDatabaseConn.prepareStatement( "insert into Patients " +
											   "( Chart_Num, FName, LName, " +
											   " DOB, Sex, Foot ) " +
											   "values( ?, ?, ?, ?, ?, ? )" );
		
		// TODO retrieve data from current interview
		//ResultSet wInterviewData = retrieveInterviewData( mainDatabaseConn );			
	
		wInsertOsteoReportData.setInt( 1, 1 );
		wInsertOsteoReportData.setString( 2, "CARTaGENE" );
		wInsertOsteoReportData.setString( 3, "CARTaGENE" );
		
		SimpleDateFormat wDateFormatter = new SimpleDateFormat("yyyy/MM/dd");
		Date wBirthDate = (java.sql.Date)wDateFormatter.parse("1955/10/15");
		wInsertOsteoReportData.setDate( 4, wBirthDate );
		
		String wSexCode = "MALE";
		if ( wSexCode.equals( "MALE" ) ) {
			wInsertOsteoReportData.setString( 5, "M" );	
		} else {
			wInsertOsteoReportData.setString( 5, "F" );	
		}
		
		wInsertOsteoReportData.setString( 6, "R" );
		
		wInsertOsteoReportData.executeUpdate();				
		wInsertOsteoReportData.close();		
		
		deviceDatabaseConn.commit();
  }

	public void deleteData(Connection pDatabaseConnection, String pQuery) throws Exception {
		
		try {
			PreparedStatement wDeleteStatement = pDatabaseConnection.prepareStatement( pQuery );
			wDeleteStatement.executeUpdate();	
		
		} catch ( Exception wSqlEx ) {
			wSqlEx.printStackTrace();
			throw wSqlEx;
		}
		
		//pDatabaseConnection.commit();
	}  
  
protected void deleteDeviceData() throws Exception {
	String wDeleteOsteoReportDataSql2 = "delete from Results";
	String wDeleteOsteoReportDataSql1 = "delete from Patients";
	deleteData( deviceDatabaseConn, wDeleteOsteoReportDataSql1 );
	deleteData( deviceDatabaseConn, wDeleteOsteoReportDataSql2 );
	
	deviceDatabaseConn.commit();
}

public ResultSet retrieveData(Connection pDatabaseConnection, String pQuery) {
	return retrieveData( pDatabaseConnection, pQuery, ResultSet.TYPE_FORWARD_ONLY );
}

public ResultSet retrieveData(Connection pDatabaseConnection, String pQuery, int pResultSetType) {
	
	ResultSet wResult = null;
	
	try {
		Statement wStatement = pDatabaseConnection.createStatement( pResultSetType, ResultSet.CONCUR_READ_ONLY ); 
		wResult = wStatement.executeQuery( pQuery );
		
	} catch ( Exception wSqlEx ) {
		wSqlEx.printStackTrace();
	} 
	
	return wResult;
	
}

public ResultSet retrieveData(Connection pDatabaseConnection, String pQuery, Object[] pParams) {
	
	ResultSet wResult = null;
	
	try {
		PreparedStatement wStatement = pDatabaseConnection.prepareStatement( pQuery ); 
		int x = 1;
		for ( Object pOneParam : pParams ) {
			wStatement.setObject( x++, pOneParam );
		}
		wResult = wStatement.executeQuery( pQuery );
					
	} catch ( Exception wSqlEx ) {
		wSqlEx.printStackTrace();
	} 
	
	return wResult;
	
}

private ResultSet retrieveDeviceData( Integer pPatientId, Connection pOsteoreportDbConnection ) {
	
	final String OSTEOREPORT_QUERY = "select assessment, fxrisk, total, tscore, zscore, agematched,  " +
							   		 "percentnormal, sidescanned, stiffnessindex, patients.chart_num, " +
							   		 "results.SOS, results.BUA, achillesbitmap " +
							   		 "from results, patients " +
							   		 "where results.chart_num = patients.chart_num " +
							   		 "and patients.chart_num = '" + pPatientId +"'";
	
	ResultSet wOsteoreport = retrieveData( pOsteoreportDbConnection, OSTEOREPORT_QUERY );	
	
	return wOsteoreport;
}

  public void SendDataToServer(/*LinkedHashMap<String, Double[]>*/ ResultSet results) {
    /*Map<String, Data> ouputToSend = new HashMap<String, Data>();

    for(String keyStr : results.keySet()) {
      Double[] valueArray = results.get(keyStr);
      ouputToSend.put(keyStr, new Data(DataType.DECIMAL, valueArray[0]));
      ouputToSend.put(keyStr + "_pred", new Data(DataType.DECIMAL, valueArray[1]));
    }

    // Save the FVC image
    File FVCFile = new File(getMirPath() + getExternalImageName());
    String fileContent = Streams.readString(new FileInputStream(FVCFile), "UTF-8");
    ByteArrayInputStream FVCInputStream = new ByteArrayInputStream(fileContent.getBytes("UTF-8"));
    ouputToSend.put("FVCImage", new Data(DataType.DATA, (Serializable) FVCInputStream));

    instrumentExecutionService.addOutputParameterValues(ouputToSend);*/
  }

  public void initialize() {
    log.info("*** Initializing Achilles Express Runner ***");
    try {
      deleteDeviceData(); 
      initParticipantData();
    } catch(Exception ex) {
      log.info("*** EXCEPTION INITIALIZE STEP: " + ex.getStackTrace());
    }
  }

  public void run() {
    log.info("*** Running Achilles Express Runner ***");
    externalAppHelper.launch();
    ResultSet results = retrieveDeviceData( 1, deviceDatabaseConn );
    SendDataToServer(results);    
  }

  public void shutdown() {
    log.info("*** Shutdown Achilles Express Runner ***");
    // Get data from external app
    try {
      // TODO set patient id
      deleteDeviceData();
    } catch(Exception ex) {
      log.info("*** EXCEPTION SHUTDOWN STEP: " + ex.getStackTrace());
    }
  }

}
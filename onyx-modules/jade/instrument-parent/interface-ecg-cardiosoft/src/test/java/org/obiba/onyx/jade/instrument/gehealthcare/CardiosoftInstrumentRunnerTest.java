package org.obiba.onyx.jade.instrument.gehealthcare;

import static org.easymock.EasyMock.*;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.obiba.onyx.jade.util.FileUtil;
import org.obiba.onyx.util.data.Data;

public class CardiosoftInstrumentRunnerTest {

  private ExternalAppLauncherHelper externalAppHelper;

  private CardiosoftInstrumentRunner cardiosoftInstrumentRunner;

  private InstrumentExecutionService instrumentExecutionServiceMock;

  @Before
  public void setUp() throws URISyntaxException {

    cardiosoftInstrumentRunner = new CardiosoftInstrumentRunner();

    // Create a test directory to simulate Cardiosoft software installation path.
    String cardioSoftSimulatedPath = new File( "target", "test-cardiosoft").getPath();
    (new File(cardioSoftSimulatedPath)).mkdir();
    cardiosoftInstrumentRunner.setCardioPath(cardioSoftSimulatedPath);
    
    // Cardiosoft database path.
    File databasePath = new File( cardioSoftSimulatedPath, "DATABASE");
    databasePath.mkdir();
    cardiosoftInstrumentRunner.setDatabasePath(databasePath.getPath());
    
    // Cardiosoft output path.    
    File exportPath = new File( cardioSoftSimulatedPath, "EXPORT");
    exportPath.mkdir();    
    cardiosoftInstrumentRunner.setExportPath(exportPath.getPath()); 
    
    cardiosoftInstrumentRunner.setPdfFileName("Cartagene.pdf");
    cardiosoftInstrumentRunner.setSettingsFileName("CARDIO.INI");
    cardiosoftInstrumentRunner.setXmlFileName("Cartagene.XML");
    
    String resourcesParentDir = new File(getClass().getResource("/initecg/CARDIO.INI").toURI().getPath()).getParent();
    cardiosoftInstrumentRunner.setInitPath(new File( resourcesParentDir).getPath());  
    
    // Cannot mock ExternalAppLauncherHelper (without EasyMock extension!),
    // so for now, use the class itself with the launch method overridden to
    // do nothing.
    externalAppHelper = new ExternalAppLauncherHelper() {
      public void launch() {
        // do nothing
      }
    };
    
    cardiosoftInstrumentRunner.setExternalAppHelper(externalAppHelper);

    // Create a mock instrumentExecutionService for testing.
    instrumentExecutionServiceMock = createMock(InstrumentExecutionService.class);
    cardiosoftInstrumentRunner.setInstrumentExecutionService(instrumentExecutionServiceMock);

  }

  
  @Test
  public void testInitialize() throws FileNotFoundException, IOException, URISyntaxException  {

    simulateResults();        
    
    cardiosoftInstrumentRunner.initialize();

    verifyInitialization();    
    
  }

  @Test
  public void testShutdown() throws FileNotFoundException, IOException, URISyntaxException {
    
    simulateResults();
    
    cardiosoftInstrumentRunner.shutdown();
       
    verifyInitialization(); 

  }  
  
  @SuppressWarnings("unchecked")
  @Test
  public void testRun() throws Exception {
    
    simulateResults();
    
    externalAppHelper.launch();
    
    FileInputStream resultInputStream = new FileInputStream(new File(cardiosoftInstrumentRunner.getExportPath(), cardiosoftInstrumentRunner.getXmlFileName()));
    CardiosoftInstrumentResultParser resultParser = new CardiosoftInstrumentResultParser(resultInputStream);
    
    Object value;
    String paramName;
    
    // Load results properties file for testing.
    Properties testResults = new Properties();
    testResults.load(getClass().getResourceAsStream("/testResults.properties"));
    
    Assert.assertTrue(new File(getClass().getResource("/testResults.properties").toURI()).exists());
    
    // Compare the test results file to the values extracted from the XML result file to make sure they match.
    for(PropertyDescriptor pd : Introspector.getBeanInfo(CardiosoftInstrumentResultParser.class).getPropertyDescriptors()) {
     
      paramName = pd.getName();
      value = pd.getReadMethod().invoke(resultParser);
     
      if (!paramName.equalsIgnoreCase("doc") && !paramName.equalsIgnoreCase("xpath") && !paramName.equalsIgnoreCase("xmldocument") && !paramName.equalsIgnoreCase("class")) {      
        System.out.println("param= " + paramName + ", value= " + value.toString() + ", testvalue= " + testResults.getProperty(paramName));
        Assert.assertTrue(value.toString().equals(testResults.getProperty(paramName)));
      }

    }
        
    instrumentExecutionServiceMock.addOutputParameterValues((Map<String, Data>) anyObject());
    replay(instrumentExecutionServiceMock);

    // Make sure that the results are sent to the server.
    cardiosoftInstrumentRunner.sendDataToServer(resultParser);
    verify(instrumentExecutionServiceMock);

  }  

  private void simulateResults() throws FileNotFoundException, IOException, URISyntaxException {
    
    // Write some arbitrary data to simulate database access by Cardiosoft.
    (new FileOutputStream(new File(cardiosoftInstrumentRunner.getDatabasePath(),"PATIENT.BTR"))).write((byte) 234432141);
    (new FileOutputStream(new File(cardiosoftInstrumentRunner.getDatabasePath(),"EXAMINA.BTR"))).write((byte) 234432141);    
    (new FileOutputStream(new File(cardiosoftInstrumentRunner.getCardioPath(),cardiosoftInstrumentRunner.getSettingsFileName()))).write((byte) 234432141);     
        
    // Copy the results file + PDF file to test directory.
    FileUtil.copyFile(new File(getClass().getResource("/Cartagene.pdf").toURI()), new File(cardiosoftInstrumentRunner.getExportPath(), cardiosoftInstrumentRunner.getPdfFileName()));
    FileUtil.copyFile(new File(getClass().getResource("/Cartagene.XML").toURI()), new File(cardiosoftInstrumentRunner.getExportPath(), cardiosoftInstrumentRunner.getXmlFileName()));
  
  }


  private void verifyInitialization() {
    
    // Verify that the Cardiosoft database content has been cleared successfully.
    Assert.assertEquals(new File(cardiosoftInstrumentRunner.getInitPath(),"PATIENT.BTR").length(), new File(cardiosoftInstrumentRunner.getDatabasePath(),"PATIENT.BTR").length());
    Assert.assertEquals(new File(cardiosoftInstrumentRunner.getInitPath(),"EXAMINA.BTR").length(), new File(cardiosoftInstrumentRunner.getDatabasePath(),"EXAMINA.BTR").length());
   
    // Make sure that Cardiosoft settings have been overwritten (cardio.ini).
    Assert.assertEquals(new File(cardiosoftInstrumentRunner.getInitPath(),cardiosoftInstrumentRunner.getSettingsFileName()).length(), new File(cardiosoftInstrumentRunner.getCardioPath(),"CARDIO.INI").length());
 
    // Make sure result files have been deleted.
    Assert.assertFalse(new File(cardiosoftInstrumentRunner.getExportPath(),cardiosoftInstrumentRunner.getXmlFileName()).exists());
    Assert.assertFalse(new File(cardiosoftInstrumentRunner.getExportPath(),cardiosoftInstrumentRunner.getPdfFileName()).exists());
 
  }
  





}
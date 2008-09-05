package org.obiba.onyx.util;

import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import au.com.bytecode.opencsv.CSVReader;

public class ParticipantCsvToXml {
  //
  // Constants
  //

  private static final SimpleDateFormat CSV_BIRTH_DATE_FORMAT = 
    new SimpleDateFormat("dd/MM/yyyy");
  
  private static final SimpleDateFormat BIRTH_DATE_FORMAT = 
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

  private static final SimpleDateFormat CSV_APPOINTMENT_DATE_FORMAT = 
    new SimpleDateFormat("dd/MM/yyyy H:mm");
  
  private static final SimpleDateFormat APPOINTMENT_DATE_FORMAT = 
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

  //
  // Methods
  //

  /**
   * Reads a participant list in CSV format, transforms it to the required XML format, and writes the result to STDOUT.
   * 
   * NOTE: It is assumed that the CSV file includes exactly one header row.
   * 
   * @param args command-line arguments, as follows: - first (and only) argument is the path to the CSV file
   */
  public static void main(String[] args) throws Exception {
    if(args.length != 1) {
      printUsage();
      System.exit(1);
    }

    CSVReader reader = new CSVReader(new FileReader(args[0]));

    System.out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
    
    System.out.println("<linked-list>");

    // Skip first row (column headers).
    reader.readNext();

    int participantId = 1;

    String[] nextRow;
    while((nextRow = reader.readNext()) != null) {
      processParticipant(nextRow, participantId);
      processAppointment(nextRow, participantId);
      participantId++;
    }

    System.out.println("</linked-list>");
  }

  private static void processParticipant(String[] dataRow, int participantId) throws ParseException {
    System.out.println("  <participant id=\"" + participantId + "\">");
    System.out.println("    <firstName>" + dataRow[5] + "</firstName>");
    System.out.println("    <lastName>" + dataRow[4] + "</lastName>");
    System.out.println("    <gender>" + formatGender(dataRow[6]) + "</gender>");
    System.out.println("    <birthDate>" + formatBirthDate(dataRow[7]) + "</birthDate>");
    System.out.println("    <street>" + dataRow[8] + "</street>");
    // System.out.println(" <apartment>"+dataRow[???]+"</apartment>");
    System.out.println("    <city>" + dataRow[9] + "</city>");
    System.out.println("    <province>" + formatProvince(dataRow[10]) + "</province>");
    System.out.println("    <country>" + dataRow[11] + "</country>");
    System.out.println("    <postalCode>" + dataRow[12] + "</postalCode>");

    if(dataRow[13] != null && dataRow[13].trim().length() != 0) {
      System.out.println("    <phone>" + dataRow[13].substring(0, 12) + "</phone>");
    }

    System.out.println("  </participant>");

  }

  private static void processAppointment(String[] dataRow, int participantId) throws ParseException {
    System.out.println("  <appointment>");
    System.out.println("    <appointmentCode>" + dataRow[3] + "</appointmentCode>");
    System.out.println("    <date>" + formatAppointmentDate(dataRow[0]) + "</date>");
    System.out.println("    <participant reference=\"" + participantId + "\" />");
    System.out.println("  </appointment>");
  }

  private static String formatGender(String gender) {
    return gender.equals("F") ? "FEMALE" : "MALE";  
  }
  
  private static String formatBirthDate(String birthDate) throws ParseException {    
    return BIRTH_DATE_FORMAT.format(CSV_BIRTH_DATE_FORMAT.parse(birthDate))+" GMT";
  }
  
  private static String formatProvince(String province) {    
    String formattedProvince = province;
    
    if (province.equals("Quebec")) {
      formattedProvince = "QC";
    }
    
    return formattedProvince;
  }
  
  private static String formatAppointmentDate(String appointmentDate) throws ParseException {    
    return APPOINTMENT_DATE_FORMAT.format(CSV_APPOINTMENT_DATE_FORMAT.parse(appointmentDate))+" EDT";
  }
  
  private static void printUsage() {
    System.err.println("Usage:");
    System.err.println("  ParticipantCsvToXmlConverter <csv-file>");
  }
}
package org.uiuc;

import java.io.IOException;

import static org.uiuc.AppConstants.ERROR_MSG;

public class Main {
  public static void main(String[] args) {

    String sourceDir = args[0];
    String sourceFileName = args[1];
    String destDir = args[2];
    String destFileName = args[3];
    String testCase = args[4];

    System.out.println("copying " + sourceFileName + "from " + sourceDir + "to " + destDir);

    Process p = null;
    try {
      p = Runtime.getRuntime().exec("cp " + sourceDir + "/" + sourceFileName + " " + destDir + "/" + destFileName);
    } catch (IOException e) {
      System.err.println(ERROR_MSG);
      e.printStackTrace();
    }

    try {
      p = Runtime.getRuntime().exec("mvn test -Dtest=" + testCase + " -DfailIfNoTests=false");
    } catch (IOException e) {
      System.err.println(ERROR_MSG);
      e.printStackTrace();
    }
  }
}
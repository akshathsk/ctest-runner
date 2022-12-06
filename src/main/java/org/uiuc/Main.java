package org.uiuc;

import java.io.*;

import static org.uiuc.AppConstants.*;

public class Main {
  public static void main(String[] args) throws IOException, InterruptedException {

    String sourceDir = args[0];
    String sourceFileName = args[1];
    String module = args[2];
    String destFileName = moduleToFileNameMap.get(module);
    String testCase = args[3];

    System.out.println("copying < " + sourceFileName + " > from < " + sourceDir + " > to < " + moduleToDirMap.get(module) + " >");

    try {
      Runtime.getRuntime().exec("cp " + sourceDir + "/" + sourceFileName + " " + moduleToDirMap.get(module) + "/" + destFileName);
    } catch (IOException e) {
      System.err.println(ERROR_MSG);
      e.printStackTrace();
    }

    Process p = null;
    try {
      p = Runtime.getRuntime().exec("mvn test -pl " + module + " -Dtest=" + testCase + " -DfailIfNoTests=false");
    } catch (IOException e) {
      System.err.println(ERROR_MSG);
      e.printStackTrace();
    }

    copy(p.getInputStream(), System.out);
    p.waitFor();
  }

  public static void copy(InputStream in, OutputStream out) throws IOException {
    while (true) {
      int c = in.read();
      if (c == -1)
        break;
      out.write((char) c);
    }
  }
}
package org.uiuc;

import java.io.*;

import static org.uiuc.AppConstants.*;

public class Main {
  public static void main(String[] args) throws IOException, InterruptedException {

    String sourceDir = args[0];
    String sourceFileName = args[1];
    String destDir = args[2];
    String destFileName = args[3];
    String testCase = args[4];

    System.out.println("copying " + sourceFileName + "from " + sourceDir + "to " + destDir);

    Process p1 = null;
    try {
      p1 = Runtime.getRuntime().exec("cp " + sourceDir + "/" + sourceFileName + " " + destDir + "/" + destFileName);
    } catch (IOException e) {
      System.err.println(ERROR_MSG);
      e.printStackTrace();
    }

    Process p = null;
    try {
      p = Runtime.getRuntime().exec("mvn test -Dtest=" + testCase + " -DfailIfNoTests=false");
    } catch (IOException e) {
      System.err.println(ERROR_MSG);
      e.printStackTrace();
    }

    OutputStream output = new OutputStream() {
      private final StringBuilder string = new StringBuilder();

      @Override
      public void write(int b) {
        this.string.append((char) b);
      }

      public String toString() {
        return this.string.toString();
      }
    };

    copy(p.getInputStream(), output);
    BufferedReader bufReader = new BufferedReader(new StringReader(output.toString()));
    String next = bufReader.readLine();
    while (next != null) {
      System.out.println(next);
      next = bufReader.readLine();
    }
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
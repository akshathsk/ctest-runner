package org.uiuc;

import com.anubhavshukla.p2y.converter.PropertiesToYamlConverter;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.uiuc.AppConstants.*;

public class Main {
  public static void main(String[] args) throws IOException {

    String configDestDir = args[0];
    String testToConfigMapperInputFile = args[1];
    String generatedValueListFile = args[2];

    List<String> arrList = new ArrayList<>();
    try (Stream<String> lines = Files.lines(Paths.get(generatedValueListFile), Charset.defaultCharset())) {
      lines.forEachOrdered(line -> {
        if (!line.isEmpty()) {
          String[] split = line.split("\t");
          if (!split[1].equals("SKIP") || !split[2].equals("SKIP")) {
            arrList.add(split[0] + "=" + split[1]);
            arrList.add(split[0] + "=" + split[2]);
          }
        }
      });
    }
    System.out.println(arrList);
    FileWriter writer = new FileWriter(Main.class.getClass().getResource("/").getPath() + "/output.txt");
    for (String str : arrList) {
      writer.write(str + System.lineSeparator());
    }
    writer.close();

    List<String> overrideConfigFileList = new ArrayList<>();
    int i = 0;
    try (Stream<String> lines = Files.lines(Paths.get(Main.class.getClass().getResource("/output.txt").getPath()), Charset.defaultCharset())) {
      lines.forEachOrdered(line -> {
        try {
          File file = new File("config" + i + ".txt");
          FileUtils.writeStringToFile(file, line, Charset.forName("UTF-8"));
          PropertiesToYamlConverter p = new PropertiesToYamlConverter();
          String yaml = p.fileToYamlString(file);
          if (yaml.contains("[")) {
            yaml = yaml.replaceFirst("\\[[^\\]]+\\]", "").trim().replace("\n ", "\n" + "  -");
          }
          System.out.println(yaml);
          FileUtils.writeStringToFile(new File(configDestDir + "/" + line), yaml, Charset.forName("UTF-8"));
          overrideConfigFileList.add(line);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    }

    StringBuffer mapperInputStr = new StringBuffer();
    try (Stream<String> lines = Files.lines(Paths.get(testToConfigMapperInputFile), Charset.defaultCharset())) {
      lines.forEachOrdered(line ->
              mapperInputStr.append(line)
      );
    }
    System.out.println(mapperInputStr);

    Gson gson = new Gson();
    Map<String, List<String>> testToConfigList = gson.fromJson(mapperInputStr.toString(), Map.class);
    System.out.println(testToConfigList);

    Map<String, String> testCaseToModuleMap = testCases.stream()
            .collect(Collectors.toMap(x -> x.split(">")[1], y -> y.split(">")[0]));

    List<String> finalReport = new ArrayList<>();
    testToConfigList.forEach((testCase, configList) -> {
      configList.forEach(config -> {
        List<String> allMatchingFiles = overrideConfigFileList.stream().filter(x -> x.split("=")[0].equals(config)).collect(Collectors.toList());
        allMatchingFiles.forEach(c -> {
          System.out.println("Module : " + testCaseToModuleMap.get(testCase) + " TestCase : " + testCase + " destFileName : " + c);
          try {
            runTest(configDestDir, c, testCaseToModuleMap.get(testCase), testCase, finalReport);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
      });
    });
    System.out.println(finalReport);

  }

  private static void runTest(String sourceDir, String sourceFileName, String module, String testCase, List<String> finalReport) throws IOException, InterruptedException {

    String destFileName = moduleToFileNameMap.get(module);

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
      if (next.contains("BUILD FAILURE")) {
        finalReport.add(sourceFileName.split("=")[0] + "\t" + testCase + "\t" + sourceFileName.split("=")[1] + "\t" + "f" + "");
      } else if (next.contains("BUILD SUCCESS")) {
        finalReport.add(sourceFileName.split("=")[0] + "\t" + testCase + "\t" + sourceFileName.split("=")[1] + "\t" + "p" + "");
      }
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
package com.flit.protoc.gen;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.javaparser.JavaParser;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.compiler.PluginProtos;
import com.google.protobuf.util.JsonFormat;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.commons.io.FileUtils;

public abstract class BaseGeneratorTest {

  public static PluginProtos.CodeGeneratorRequest loadJson(String resource) throws Exception {
    return loadJson(resource, null);
  }

  public static PluginProtos.CodeGeneratorRequest loadJson(String resource, String parameterOverride) throws Exception {
    try (InputStream is = BaseGeneratorTest.class.getClassLoader().getResource(resource).openStream()) {
      PluginProtos.CodeGeneratorRequest.Builder b = PluginProtos.CodeGeneratorRequest.newBuilder();
      JsonFormat.parser().merge(new InputStreamReader(is), b);
      if (parameterOverride != null) {
        b.setParameter(parameterOverride);
      }
      return b.build();
    }
  }

  // For converting initial .bin dumps over to .json
  public static void saveAsJson(MessageOrBuilder request, String fileName) throws Exception {
    FileUtils.writeStringToFile(new File(fileName), JsonFormat.printer().print(request), UTF_8);
  }

  protected static void assertParses(PluginProtos.CodeGeneratorResponse.File file) {
    try {
      JavaParser.parse(file.getContent());
    } catch (Exception e) {
      throw new RuntimeException("Could not parse " + file.getName(), e);
    }
  }

}

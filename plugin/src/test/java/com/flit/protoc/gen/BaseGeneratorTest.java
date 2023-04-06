package com.flit.protoc.gen;

import com.github.javaparser.StaticJavaParser;
import com.google.protobuf.compiler.PluginProtos;
import com.google.protobuf.util.JsonFormat;
import java.io.InputStream;
import java.io.InputStreamReader;

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

  protected static void assertParses(PluginProtos.CodeGeneratorResponse.File file) {
    try {
      StaticJavaParser.parse(file.getContent());
    } catch (Exception e) {
      throw new RuntimeException("Could not parse " + file.getName(), e);
    }
  }

}

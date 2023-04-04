package com.flit.protoc.gen.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.protobuf.DescriptorProtos;
import com.squareup.javapoet.ClassName;
import org.junit.jupiter.api.Test;

public class TypeMapperTest {

  private static final String PROTO_PACKAGE = "flit.test";
  private static final String JAVA_PACKAGE = "com.flit.test";

  private static final DescriptorProtos.DescriptorProto MAP_MESSAGE = DescriptorProtos.DescriptorProto
      .newBuilder()
      .setName("Map")
      .build();

  private static final DescriptorProtos.DescriptorProto MAPPER_MESSAGE = DescriptorProtos.DescriptorProto
      .newBuilder()
      .setName("Mapper")
      .build();

  @Test
  public void protoPackage() {
    DescriptorProtos.FileOptions options = DescriptorProtos.FileOptions.newBuilder()
        .setJavaMultipleFiles(false)
        .build();

    DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto.newBuilder()
        .setPackage(PROTO_PACKAGE)
        .setName("mapper.proto")
        .setOptions(options)
        .addMessageType(MAP_MESSAGE)
        .build();

    TypeMapper mapper = new TypeMapper();
    mapper.add(proto);

    ClassName result = mapper.get(".flit.test.Map");
    assertEquals(PROTO_PACKAGE, result.packageName());
    assertEquals("Mapper", result.enclosingClassName().simpleName());
    assertEquals("Map", result.simpleName());
  }

  @Test
  public void protoPackageNameCollision() {
    DescriptorProtos.FileOptions options = DescriptorProtos.FileOptions.newBuilder()
        .setJavaMultipleFiles(false)
        .build();

    DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto.newBuilder()
        .setPackage(PROTO_PACKAGE)
        .setName("mapper.proto")
        .setOptions(options)
        .addMessageType(MAPPER_MESSAGE)
        .build();

    TypeMapper mapper = new TypeMapper();
    mapper.add(proto);

    ClassName result = mapper.get(".flit.test.Mapper");
    assertEquals(PROTO_PACKAGE, result.packageName());
    assertEquals("MapperOuterClass", result.enclosingClassName().simpleName());
    assertEquals("Mapper", result.simpleName());
  }

  @Test
  public void protoPackageWithOuterClass() {
    DescriptorProtos.FileOptions options = DescriptorProtos.FileOptions.newBuilder()
        .setJavaMultipleFiles(false)
        .setJavaOuterClassname("Mapper")
        .build();

    DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto.newBuilder()
        .setPackage(PROTO_PACKAGE)
        .setName("mapper.proto")
        .setOptions(options)
        .addMessageType(MAP_MESSAGE)
        .build();

    TypeMapper mapper = new TypeMapper();
    mapper.add(proto);

    ClassName result = mapper.get(".flit.test.Map");
    assertEquals(PROTO_PACKAGE, result.packageName());
    assertEquals("Mapper", result.enclosingClassName().simpleName());
    assertEquals("Map", result.simpleName());
  }

  @Test
  public void javaPackage() {
    DescriptorProtos.FileOptions options = DescriptorProtos.FileOptions.newBuilder()
        .setJavaMultipleFiles(false)
        .setJavaPackage(JAVA_PACKAGE)
        .build();

    DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto.newBuilder()
        .setPackage(PROTO_PACKAGE)
        .setName("mapper.proto")
        .setOptions(options)
        .addMessageType(MAP_MESSAGE)
        .build();

    TypeMapper mapper = new TypeMapper();
    mapper.add(proto);

    ClassName result = mapper.get(".flit.test.Map");
    assertEquals(JAVA_PACKAGE, result.packageName());
    assertEquals("Mapper", result.enclosingClassName().simpleName());
    assertEquals("Map", result.simpleName());
  }

  @Test
  public void javaPackageNameCollision() {
    DescriptorProtos.FileOptions options = DescriptorProtos.FileOptions.newBuilder()
        .setJavaMultipleFiles(false)
        .setJavaPackage(JAVA_PACKAGE)
        .build();

    DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto.newBuilder()
        .setPackage(PROTO_PACKAGE)
        .setName("mapper.proto")
        .setOptions(options)
        .addMessageType(MAPPER_MESSAGE)
        .build();

    TypeMapper mapper = new TypeMapper();
    mapper.add(proto);

    ClassName result = mapper.get(".flit.test.Mapper");
    assertEquals(JAVA_PACKAGE, result.packageName());
    assertEquals("MapperOuterClass", result.enclosingClassName().simpleName());
    assertEquals("Mapper", result.simpleName());
  }

  @Test
  public void javaPackageWithOuterClass() {
    DescriptorProtos.FileOptions options = DescriptorProtos.FileOptions.newBuilder()
        .setJavaMultipleFiles(false)
        .setJavaOuterClassname("Mapper")
        .setJavaPackage(JAVA_PACKAGE)
        .build();

    DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto.newBuilder()
        .setPackage(PROTO_PACKAGE)
        .setName("mapper.proto")
        .setOptions(options)
        .addMessageType(MAP_MESSAGE)
        .build();

    TypeMapper mapper = new TypeMapper();
    mapper.add(proto);

    ClassName result = mapper.get(".flit.test.Map");
    assertEquals(JAVA_PACKAGE, result.packageName());
    assertEquals("Mapper", result.enclosingClassName().simpleName());
    assertEquals("Map", result.simpleName());
  }

  @Test
  public void javaPackageWithOuterClassEmpty() {
    DescriptorProtos.FileOptions options = DescriptorProtos.FileOptions.newBuilder()
        .setJavaMultipleFiles(false)
        .setJavaOuterClassname("")
        .setJavaPackage(JAVA_PACKAGE)
        .build();

    DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto.newBuilder()
        .setPackage(PROTO_PACKAGE)
        .setName("mapper.proto")
        .setOptions(options)
        .addMessageType(MAP_MESSAGE)
        .build();

    TypeMapper mapper = new TypeMapper();
    assertThrows(IllegalArgumentException.class, () -> mapper.add(proto));
  }

  @Test
  public void javaPackageWithOuterClassMultiFile() {
    DescriptorProtos.FileOptions options = DescriptorProtos.FileOptions.newBuilder()
        .setJavaMultipleFiles(true)
        .setJavaOuterClassname("Mapper")
        .setJavaPackage(JAVA_PACKAGE)
        .build();

    DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto.newBuilder()
        .setPackage(PROTO_PACKAGE)
        .setName("mapper.proto")
        .setOptions(options)
        .addMessageType(MAP_MESSAGE)
        .build();

    TypeMapper mapper = new TypeMapper();
    mapper.add(proto);

    ClassName result = mapper.get(".flit.test.Map");
    assertEquals(JAVA_PACKAGE, result.packageName());
    assertNull(result.enclosingClassName());
    assertEquals("Map", result.simpleName());
  }
}

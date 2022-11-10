package com.flit.protoc.gen.server;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

public abstract class BaseGenerator {

  protected final DescriptorProtos.FileDescriptorProto proto;
  protected final DescriptorProtos.ServiceDescriptorProto service;
  protected final String javaPackage;
  protected final TypeMapper mapper;

  protected BaseGenerator(DescriptorProtos.FileDescriptorProto proto, DescriptorProtos.ServiceDescriptorProto s, TypeMapper mapper) {
    this.proto = proto;
    this.service = s;
    this.javaPackage = proto.getOptions().getJavaPackage();
    this.mapper = mapper;
  }

  public abstract List<PluginProtos.CodeGeneratorResponse.File> getFiles();

  protected String getFileName(String className) {
    return javaPackage.replace(".", "/") + "/" + className + ".java";
  }

  /** Returns the {@code Rpc${Service}} synchronous interface. */
  protected ClassName getServiceInterface() {
    return ClassName.get(javaPackage, "Rpc" + service.getName());
  }

  /** Returns the {@code Rpc${Service}} synchronous interface. */
  protected ClassName getAuthServiceInterface() {
    return ClassName.get(javaPackage, "RpcAuth" + service.getName());
  }

  protected static String getContext(String context) {
    if (context == null) {
      return "/twirp";
    } else {
      context = context.trim();
      if (context.equals("") || context.equals("/")) {
        return ""; // empty route - i.e. top level "/"
      } else if (context.startsWith("/")) {
        return context;
      } else {
        return "/" + context;
      }
    }
  }

  /** Assumes name+type is a top-level type and turns it into the protobuf output file type. */
  protected static PluginProtos.CodeGeneratorResponse.File toFile(ClassName name, TypeSpec type) {
    return PluginProtos.CodeGeneratorResponse.File.newBuilder()
      .setName(name.toString().replace(".", "/") + ".java")
      .setContent(JavaFile.builder(name.packageName(), type).build().toString())
      .build();
  }
}

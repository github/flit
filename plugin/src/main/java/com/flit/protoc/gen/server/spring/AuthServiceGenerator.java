package com.flit.protoc.gen.server.spring;

import static com.flit.protoc.gen.server.spring.RpcGenerator.HttpServletRequest;

import com.flit.protoc.gen.server.BaseGenerator;
import com.flit.protoc.gen.server.TypeMapper;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;

public class AuthServiceGenerator extends BaseGenerator {

  private final TypeSpec.Builder rpcInterface;

  public AuthServiceGenerator(DescriptorProtos.FileDescriptorProto proto, DescriptorProtos.ServiceDescriptorProto s, TypeMapper mapper) {
    super(proto, s, mapper);
    rpcInterface = TypeSpec.interfaceBuilder(getAuthServiceInterface());
    rpcInterface.addModifiers(Modifier.PUBLIC);
    service.getMethodList().forEach(this::addHandleMethod);
  }

  private void addHandleMethod(DescriptorProtos.MethodDescriptorProto m) {
    rpcInterface.addMethod(MethodSpec.methodBuilder("handle" + m.getName())
        .addParameter(HttpServletRequest, "request")
        .addParameter(mapper.get(m.getInputType()), "in")
        .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
        .addCode(CodeBlock.builder().addStatement("return true").build())
        .returns(TypeName.BOOLEAN)
        .build());
  }

  @Override public List<PluginProtos.CodeGeneratorResponse.File> getFiles() {
    return Collections.singletonList(toFile(getAuthServiceInterface(), rpcInterface.build()));
  }
}

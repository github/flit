package com.flit.protoc.gen.server;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;

/**
 * Generates the `Rpc${SerivceName}` interface.
 *
 * Currently, this is the same interface across both undertow and spring.
 */
public class ServiceGenerator extends BaseGenerator {

  private final TypeSpec.Builder rpcInterface;
  private final boolean passRequest;
  private final TypeName httpRequestType;

  public ServiceGenerator(
      DescriptorProtos.FileDescriptorProto proto,
      DescriptorProtos.ServiceDescriptorProto s,
      TypeMapper mapper,
      boolean passRequest,
      TypeName httpRequestType
  ) {
    super(proto, s, mapper);
    this.passRequest = passRequest;
    this.httpRequestType = httpRequestType;
    rpcInterface = TypeSpec.interfaceBuilder(ClassName.get(javaPackage, "Rpc" + service.getName()));
    rpcInterface.addModifiers(Modifier.PUBLIC);
    service.getMethodList().forEach(this::addHandleMethod);
  }

  private void addHandleMethod(DescriptorProtos.MethodDescriptorProto m) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("handle" + m.getName());
    if (passRequest) {
      builder.addParameter(httpRequestType, "request");
    }
    builder
      .addParameter(mapper.get(m.getInputType()), "in")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .returns(mapper.get(m.getOutputType()));
    rpcInterface.addMethod(builder.build());
  }

  @Override public List<PluginProtos.CodeGeneratorResponse.File> getFiles() {
    return Collections.singletonList(toFile(getServiceInterface(), rpcInterface.build()));
  }
}

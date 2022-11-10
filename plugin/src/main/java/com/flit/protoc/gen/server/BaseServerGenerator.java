package com.flit.protoc.gen.server;

import static com.flit.protoc.Parameter.PARAM_CONTEXT;

import com.flit.protoc.Parameter;
import com.flit.protoc.gen.Generator;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import com.squareup.javapoet.TypeName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implements the basic "generate a service interface + impl dispatcher".
 *
 * Subclasses need to provide their own {@code getRpcGenerator} for their
 * Spring/Undertow/etc.-specific dispatcher logic.
 */
public abstract class BaseServerGenerator implements Generator {

  private final List<String> requestServices;

  protected BaseServerGenerator(List<String> requestServices) {
    this.requestServices = requestServices;
  }

  @Override public List<CodeGeneratorResponse.File> generate(CodeGeneratorRequest request, Map<String, Parameter> params) {
    List<CodeGeneratorResponse.File> files = new ArrayList<>();
    String context = getContext(params);
    TypeMapper mapper = new TypeMapper(request.getProtoFileList());
    request.getProtoFileList().forEach(proto -> {
      proto.getServiceList().forEach(s -> {
        files.addAll(new ServiceGenerator(proto, s, mapper, isRequestBasedClass(s), getHttpRequestTypeName()).getFiles());
        files.addAll(getRpcGenerator(proto, s, context, mapper).getFiles());
      });
    });
    return files;
  }

  private static String getContext(Map<String, Parameter> params) {
    if (params.containsKey(PARAM_CONTEXT)) {
      return params.get(PARAM_CONTEXT).getValue();
    }
    return null;
  }

  protected boolean isRequestBasedClass(ServiceDescriptorProto service) {
    return requestServices.contains(service.getName());
  }

  protected abstract BaseGenerator getRpcGenerator(FileDescriptorProto proto, ServiceDescriptorProto service, String context, TypeMapper mapper);

  protected abstract TypeName getHttpRequestTypeName();
}

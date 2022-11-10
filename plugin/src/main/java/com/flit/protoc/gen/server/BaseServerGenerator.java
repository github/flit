package com.flit.protoc.gen.server;

import com.flit.protoc.Parameter;
import com.flit.protoc.gen.Generator;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.flit.protoc.Parameter.PARAM_CONTEXT;

/**
 * Implements the basic "generate a service interface + impl dispatcher".
 *
 * Subclasses need to provide their own {@code getRpcGenerator} for their
 * Spring/Undertow/etc.-specific dispatcher logic.
 */
public abstract class BaseServerGenerator implements Generator {

  @Override public List<CodeGeneratorResponse.File> generate(CodeGeneratorRequest request, Map<String, Parameter> params) {
    List<CodeGeneratorResponse.File> files = new ArrayList<>();
    String context = getContext(params);
    TypeMapper mapper = new TypeMapper(request.getProtoFileList());
    request.getProtoFileList().forEach(proto -> {
      proto.getServiceList().forEach(s -> {
        files.addAll(new ServiceGenerator(proto, s, mapper).getFiles());
        getGenerators(proto, s, context, mapper).forEach(g -> files.addAll(g.getFiles()));
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

  protected abstract List<BaseGenerator> getGenerators(FileDescriptorProto proto, ServiceDescriptorProto service, String context, TypeMapper mapper);
}

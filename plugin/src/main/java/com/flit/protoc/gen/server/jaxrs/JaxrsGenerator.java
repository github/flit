package com.flit.protoc.gen.server.jaxrs;

import static com.flit.protoc.gen.server.jaxrs.RpcGenerator.HttpServletRequest;

import com.flit.protoc.gen.server.BaseGenerator;
import com.flit.protoc.gen.server.BaseServerGenerator;
import com.flit.protoc.gen.server.TypeMapper;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.squareup.javapoet.TypeName;
import java.util.List;

public class JaxrsGenerator extends BaseServerGenerator {

  public JaxrsGenerator(List<String> requestServices) {
    super(requestServices);
  }

  @Override
  protected BaseGenerator getRpcGenerator(FileDescriptorProto proto, ServiceDescriptorProto service,
      String context, TypeMapper mapper) {
    return new RpcGenerator(proto, service, context, mapper, isRequestBasedClass(service));
  }

  @Override
  protected TypeName getHttpRequestTypeName() {
    return HttpServletRequest;
  }
}

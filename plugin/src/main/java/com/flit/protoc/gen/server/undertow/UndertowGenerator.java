package com.flit.protoc.gen.server.undertow;

import static com.flit.protoc.gen.server.Types.HttpServerExchange;

import com.flit.protoc.gen.server.BaseGenerator;
import com.flit.protoc.gen.server.BaseServerGenerator;
import com.flit.protoc.gen.server.TypeMapper;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.squareup.javapoet.TypeName;
import java.util.List;

public class UndertowGenerator extends BaseServerGenerator {

  public UndertowGenerator(List<String> requestServices) {
    super(requestServices);
  }

  @Override
  protected BaseGenerator getRpcGenerator(
    FileDescriptorProto proto, ServiceDescriptorProto service, String context, TypeMapper mapper) {
    return new RpcGenerator(proto, service, context, mapper, isRequestBasedClass(service));
  }

  @Override
  protected TypeName getHttpRequestTypeName() {
    return HttpServerExchange;
  }
}

package com.flit.protoc.gen.server.jaxrs;

import com.flit.protoc.gen.server.BaseGenerator;
import com.flit.protoc.gen.server.BaseServerGenerator;
import com.flit.protoc.gen.server.TypeMapper;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import java.util.Collections;
import java.util.List;

public class JaxrsGenerator extends BaseServerGenerator {

  @Override
  protected List<BaseGenerator> getGenerators(FileDescriptorProto proto,
      ServiceDescriptorProto service, String context, TypeMapper mapper) {
    return Collections.singletonList(new RpcGenerator(proto, service, context, mapper));
  }
}

package com.flit.protoc.gen.server.spring;

import com.flit.protoc.gen.server.BaseGenerator;
import com.flit.protoc.gen.server.BaseServerGenerator;
import com.flit.protoc.gen.server.TypeMapper;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring specific generator that will output MVC style routes.
 */
public class SpringGenerator extends BaseServerGenerator {

  @Override
  protected List<BaseGenerator> getGenerators(FileDescriptorProto proto,
      ServiceDescriptorProto service, String context, TypeMapper mapper) {
    List<BaseGenerator> result = new ArrayList<>();
    result.add(new RpcGenerator(proto, service, context, mapper));
    result.add(new AuthServiceGenerator(proto, service, mapper));
    return result;
  }
}

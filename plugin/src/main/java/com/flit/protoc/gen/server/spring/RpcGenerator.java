package com.flit.protoc.gen.server.spring;

import com.flit.protoc.gen.server.BaseGenerator;
import com.flit.protoc.gen.server.TypeMapper;
import com.flit.protoc.gen.server.Types;
import com.google.common.net.MediaType;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.Collections;
import java.util.List;

class RpcGenerator extends BaseGenerator {

  public static final ClassName RestController = ClassName.bestGuess("org.springframework.web.bind.annotation.RestController");
  public static final ClassName Autowired = ClassName.bestGuess("org.springframework.beans.factory.annotation.Autowired");
  public static final ClassName PostMapping = ClassName.bestGuess("org.springframework.web.bind.annotation.PostMapping");
  public static final ClassName HttpServletRequest = ClassName.bestGuess("javax.servlet.http.HttpServletRequest");
  public static final ClassName HttpServletResponse = ClassName.bestGuess("javax.servlet.http.HttpServletResponse");

  private final String context;
  private final TypeSpec.Builder rpcController;
  private final boolean passRequest;

  RpcGenerator(
      DescriptorProtos.FileDescriptorProto proto,
      DescriptorProtos.ServiceDescriptorProto service,
      String context,
      TypeMapper mapper,
      boolean passRequest
  ) {
    super(proto, service, mapper);
    this.context = getContext(context);
    this.passRequest = passRequest;
    rpcController = TypeSpec.classBuilder(getControllerName()).addModifiers(Modifier.PUBLIC).addAnnotation(RestController);
    addInstanceFields();
    service.getMethodList().forEach(this::addHandleMethod);
  }

  private void addInstanceFields() {
    rpcController.addField(FieldSpec.builder(getServiceInterface(), "service").addAnnotation(Autowired).addModifiers(Modifier.PRIVATE).build());
  }

  private void addHandleMethod(DescriptorProtos.MethodDescriptorProto m) {
    ClassName inputType = mapper.get(m.getInputType());
    ClassName outputType = mapper.get(m.getOutputType());
    String route = context + "/" + (proto.hasPackage() ? proto.getPackage() + "." : "") + service.getName() + "/" + m.getName();
    rpcController.addMethod(MethodSpec.methodBuilder("handle" + m.getName())
      .addModifiers(Modifier.PUBLIC)
      .addParameter(HttpServletRequest, "request")
      .addParameter(HttpServletResponse, "response")
      .addException(Types.Exception)
      // the spring mapping
      .addAnnotation(AnnotationSpec.builder(PostMapping).addMember("value", "$S", route).build())
      .addStatement("boolean json = false")
      .addStatement("final $T data", inputType)
      .beginControlFlow("if (request.getContentType().equals($S))", MediaType.PROTOBUF.toString())
      .addStatement("data = $T.parseFrom(request.getInputStream())", inputType)
      .nextControlFlow("else if (request.getContentType().startsWith($S))", "application/json")
      .addStatement("json = true")
      .addStatement("$T.Builder builder = $T.newBuilder()", inputType, inputType)
      .addStatement("$T.parser().merge(new $T(request.getInputStream(), $T.UTF_8), builder)",
        Types.JsonFormat,
        Types.InputStreamReader,
        Types.StandardCharsets)
      .addStatement("data = builder.build()")
      .nextControlFlow("else")
      .addStatement("response.setStatus(415)")
      .addStatement("return")
      .endControlFlow()
      // route to the service
      .addStatement(getRouteToService(), outputType, m.getName())
      .addStatement("response.setStatus(200)")
      // send the response
      .beginControlFlow("if (json)")
      .addStatement("response.setContentType($S)", MediaType.JSON_UTF_8.toString())
      .addStatement("response.getOutputStream().write($T.printer().omittingInsignificantWhitespace().print(retval).getBytes($T.UTF_8))",
        Types.JsonFormat,
        Types.StandardCharsets)
      .nextControlFlow("else")
      .addStatement("response.setContentType($S)", MediaType.PROTOBUF.toString())
      .addStatement("retval.writeTo(response.getOutputStream())")
      .endControlFlow()
      .build());
  }

  private String getRouteToService() {
    if (passRequest) {
      return "$T retval = service.handle$L(request, data)";
    } else {
      return "$T retval = service.handle$L(data)";
    }
  }

  private ClassName getControllerName() {
    return ClassName.get(javaPackage, "Rpc" + service.getName() + "Controller");
  }

  @Override public List<PluginProtos.CodeGeneratorResponse.File> getFiles() {
    return Collections.singletonList(toFile(getControllerName(), rpcController.build()));
  }
}

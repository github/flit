package com.flit.protoc.gen.server.jaxrs;

import com.flit.protoc.gen.server.BaseGenerator;
import com.flit.protoc.gen.server.TypeMapper;
import com.flit.protoc.gen.server.Types;
import com.google.common.net.MediaType;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;

public class RpcGenerator extends BaseGenerator {

  public static final ClassName PATH = ClassName.bestGuess("javax.ws.rs.Path");
  public static final ClassName POST = ClassName.bestGuess("javax.ws.rs.POST");
  public static final ClassName PRODUCES = ClassName.bestGuess("javax.ws.rs.Produces");
  public static final ClassName CONSUMES = ClassName.bestGuess("javax.ws.rs.Consumes");
  public static final ClassName CONTEXT = ClassName.bestGuess("javax.ws.rs.core.Context");
  public static final ClassName HttpServletRequest = ClassName.bestGuess("javax.servlet.http.HttpServletRequest");
  public static final ClassName HttpServletResponse = ClassName.bestGuess("javax.servlet.http.HttpServletResponse");
  private final String context;
  private final Builder rpcResource;
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
    this.rpcResource = TypeSpec.classBuilder(getResourceName(service))
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(
            AnnotationSpec.builder(PATH).addMember("value", "$S",
                this.context + "/" + (proto.hasPackage() ? proto.getPackage() + "." : "") + service
                    .getName()).build());
    addInstanceFields();
    addConstructor();
    service.getMethodList().forEach(this::addHandleMethod);
  }

  private void addConstructor() {
    rpcResource.addMethod(MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(getServiceInterface(), "service")
        .addStatement("this.service = service").build());
  }

  private void addHandleMethod(MethodDescriptorProto mdp) {
    ClassName inputType = mapper.get(mdp.getInputType());
    ClassName outputType = mapper.get(mdp.getOutputType());
    rpcResource.addMethod(MethodSpec.methodBuilder("handle" + mdp.getName())
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(POST)
        .addAnnotation(AnnotationSpec.builder(PATH)
            .addMember("value", "$S", "/" + mdp.getName())
            .build())
        .addParameter(ParameterSpec.builder(HttpServletRequest, "request")
            .addAnnotation(CONTEXT).build())
        .addParameter(ParameterSpec.builder(HttpServletResponse, "response")
            .addAnnotation(CONTEXT).build())
        .addException(Types.Exception)
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
        .addStatement("response.flushBuffer()")
        .addStatement("return")
        .endControlFlow()
        // route to the service
        .addStatement(getRouteToService(), outputType, mdp.getName())
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
        .addStatement("response.flushBuffer()")
        .build());
  }

  private String getRouteToService() {
    if (passRequest) {
      return "$T retval = service.handle$L(request, data)";
    } else {
      return "$T retval = service.handle$L(data)";
    }
  }

  private ClassName getResourceName(DescriptorProtos.ServiceDescriptorProto service) {
    return ClassName.get(javaPackage, "Rpc" + service.getName() + "Resource");
  }

  private void addInstanceFields() {
    rpcResource.addField(FieldSpec.builder(getServiceInterface(), "service")
        .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build());
  }

  @Override
  public List<File> getFiles() {
    return Collections.singletonList(toFile(getResourceName(service), rpcResource.build()));
  }
}

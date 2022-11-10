package com.flit.protoc.gen.server.undertow;

import static com.flit.protoc.gen.server.Types.ErrorCode;
import static com.flit.protoc.gen.server.Types.ErrorWriter;
import static com.flit.protoc.gen.server.Types.Exception;
import static com.flit.protoc.gen.server.Types.FlitException;
import static com.flit.protoc.gen.server.Types.FlitHandler;
import static com.flit.protoc.gen.server.Types.Headers;
import static com.flit.protoc.gen.server.Types.HttpServerExchange;
import static com.flit.protoc.gen.server.Types.InputStreamReader;
import static com.flit.protoc.gen.server.Types.JsonFormat;
import static com.flit.protoc.gen.server.Types.Logger;
import static com.flit.protoc.gen.server.Types.LoggerFactory;
import static com.flit.protoc.gen.server.Types.Override;
import static com.flit.protoc.gen.server.Types.StandardCharsets;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import com.flit.protoc.gen.server.BaseGenerator;
import com.flit.protoc.gen.server.TypeMapper;
import com.flit.protoc.gen.server.Types;
import com.google.common.net.MediaType;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.Collections;
import java.util.List;

class RpcGenerator extends BaseGenerator {

  private final String context;
  private final TypeSpec.Builder rpcHandler;
  private final boolean passRequest;

  RpcGenerator(
      DescriptorProtos.FileDescriptorProto proto,
      DescriptorProtos.ServiceDescriptorProto service,
      String context, TypeMapper mapper,
      boolean passRequest
  ) {
    super(proto, service, mapper);
    this.context = getContext(context);
    this.passRequest = passRequest;
    rpcHandler = TypeSpec.classBuilder(getHandlerName(service))
      .addModifiers(PUBLIC)
      .addSuperinterface(ClassName.bestGuess("io.undertow.server.HttpHandler"));
    addStaticFields();
    addInstanceFields();
    addConstructor();
    writeHandleRequest();
    service.getMethodList().forEach(this::writeHandleMethod);
  }

  private ClassName getHandlerName(DescriptorProtos.ServiceDescriptorProto service) {
    return ClassName.get(javaPackage, "Rpc" + service.getName() + "Handler");
  }

  private void addStaticFields() {
    // add a logger
    rpcHandler.addField(FieldSpec.builder(Logger, "LOGGER")
      .addModifiers(PRIVATE, STATIC, FINAL)
      .initializer("$T.getLogger($L.class)", LoggerFactory, getHandlerName(service).simpleName())
      .build());
    // add the static route name
    rpcHandler.addField(FieldSpec.builder(Types.String, "ROUTE")
      .addModifiers(PUBLIC, STATIC, FINAL)
      .initializer("\"$L/$L$L\"", context, proto.hasPackage() ? proto.getPackage() + "." : "", service.getName())
      .build());
  }

  private void addInstanceFields() {
    // add service instance and error
    rpcHandler.addField(FieldSpec.builder(getServiceInterface(), "service").addModifiers(PRIVATE, FINAL).build());
    rpcHandler.addField(FieldSpec.builder(ErrorWriter, "errorWriter").addModifiers(PRIVATE, FINAL).build());
  }

  private void addConstructor() {
    rpcHandler.addMethod(MethodSpec.constructorBuilder()
      .addModifiers(PUBLIC)
      .addParameter(getServiceInterface(), "service")
      .addStatement("this.service = service")
      .addStatement("this.errorWriter = new $T()", ErrorWriter)
      .build());
  }

  // write the handleRequest routing method table
  private void writeHandleRequest() {
    rpcHandler.addMethod(MethodSpec.methodBuilder("handleRequest")
      .addAnnotation(Override)
      .addModifiers(PUBLIC)
      .addParameter(HttpServerExchange, "exchange")
      .addException(Exception)
      .beginControlFlow("if (exchange.isInIoThread())")
      .addStatement("exchange.dispatch(this)")
      .addStatement("return")
      .endControlFlow()
      // once we're not on I/O thread, we can block and dispatch
      .addStatement("exchange.startBlocking()")
      .addStatement("String method = exchange.getAttachment($T.KEY_METHOD)", FlitHandler)
      .beginControlFlow("try")
      // add a "case: ..." for each method
      .beginControlFlow("switch (method)")
      .addCode(service.getMethodList()
        .stream()
        .map(m -> CodeBlock.builder().addStatement("case $S: handle$L(exchange); break", m.getName(), m.getName()).build())
        .collect(CodeBlock.joining("")))
      // and blow up if the case didn't match
      .addCode("default: throw $T.builder().withErrorCode($T.BAD_ROUTE).withMessage($S).build();\n", FlitException, ErrorCode, "No such route")
      .endControlFlow()
      .nextControlFlow("catch ($T e)", FlitException)
      .addStatement("errorWriter.write(e, exchange)")
      .nextControlFlow("catch ($T e)", Exception)
      .addStatement("LOGGER.error($S, e.getMessage(), e)", "Exception caught at handler: {}")
      .addStatement("errorWriter.write(e, exchange)")
      .endControlFlow()
      .build());
  }

  private void writeHandleMethod(DescriptorProtos.MethodDescriptorProto m) {
    ClassName inputType = mapper.get(m.getInputType());
    ClassName outputType = mapper.get(m.getOutputType());
    rpcHandler.addMethod(MethodSpec.methodBuilder("handle" + m.getName())
      .addModifiers(PRIVATE)
      .addParameter(HttpServerExchange, "exchange")
      .addException(Exception)
      .addStatement("boolean json = false")
      .addStatement("final $T data", inputType)
      .addStatement("final String contentType = exchange.getRequestHeaders().get($T.CONTENT_TYPE).getFirst()", Headers)
      .beginControlFlow("if (contentType.equals($S))", MediaType.PROTOBUF.toString())
      .addStatement("data = $T.parseFrom(exchange.getInputStream())", inputType)
      .nextControlFlow("else if (contentType.startsWith($S))", "application/json")
      .addStatement("json = true")
      .addStatement("$T.Builder builder = $T.newBuilder()", inputType, inputType)
      .addStatement("$T.parser().merge(new $T(exchange.getInputStream(), $T.UTF_8), builder)", JsonFormat, InputStreamReader, StandardCharsets)
      .addStatement("data = builder.build()")
      .nextControlFlow("else")
      .addStatement("exchange.setStatusCode(415)")
      .addStatement("return")
      .endControlFlow()
      // data is populated, now route to the service
      .addStatement(getRouteToService(), outputType, m.getName())
      .addStatement("exchange.setStatusCode(200)")
      // put the result on the wire
      .beginControlFlow("if (json)")
      .addStatement("exchange.getResponseHeaders().put($T.CONTENT_TYPE, $S)", Headers, MediaType.JSON_UTF_8.toString())
      .addStatement("exchange.getResponseSender().send($T.printer().omittingInsignificantWhitespace().print(response))", JsonFormat)
      .nextControlFlow("else")
      .addStatement("exchange.getResponseHeaders().put($T.CONTENT_TYPE, $S)", Headers, MediaType.PROTOBUF.toString())
      .addStatement("response.writeTo(exchange.getOutputStream())")
      .endControlFlow()
      .build());
  }

  private String getRouteToService() {
    if (passRequest) {
      return "$T response = service.handle$L(exchange, data)";
    } else {
      return "$T response = service.handle$L(data)";
    }
  }

  @Override public List<PluginProtos.CodeGeneratorResponse.File> getFiles() {
    return Collections.singletonList(toFile(getHandlerName(service), rpcHandler.build()));
  }
}

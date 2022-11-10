package com.flit.protoc.gen.server.spring;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.flit.protoc.Plugin;
import com.flit.protoc.gen.BaseGeneratorTest;
import com.google.protobuf.compiler.PluginProtos;
import org.approvaltests.Approvals;
import org.junit.Test;

public class HelloworldGeneratorTest extends BaseGeneratorTest {

  @Test public void test_Generate() throws Exception {
    PluginProtos.CodeGeneratorRequest request = loadJson("helloworld.spring.json");

    Plugin plugin = new Plugin(request);
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertNotNull(response);
    assertEquals(2, response.getFileCount());
    assertEquals(response.getFile(0).getName(), "com/example/helloworld/RpcHelloWorld.java");
    assertEquals(response.getFile(1).getName(), "com/example/helloworld/RpcHelloWorldController.java");

    Approvals.verifyAll("", response.getFileList().stream().map(f -> f.getContent()).collect(toList()));
    response.getFileList().forEach(f -> assertParses(f));
  }

  @Test public void test_GenerateWithRequest() throws Exception {
    PluginProtos.CodeGeneratorRequest request = loadJson("helloworld.spring.json", "target=server,type=spring,request=HelloWorld");

    Plugin plugin = new Plugin(request);
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertNotNull(response);
    assertEquals(2, response.getFileCount());
    assertEquals(response.getFile(0).getName(), "com/example/helloworld/RpcHelloWorld.java");
    assertEquals(response.getFile(1).getName(), "com/example/helloworld/RpcHelloWorldController.java");

    Approvals.verifyAll("", response.getFileList().stream().map(f -> f.getContent()).collect(toList()));
    response.getFileList().forEach(f -> assertParses(f));
  }

}

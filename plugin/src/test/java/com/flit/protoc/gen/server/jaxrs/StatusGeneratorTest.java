package com.flit.protoc.gen.server.jaxrs;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.flit.protoc.Plugin;
import com.flit.protoc.gen.BaseGeneratorTest;
import com.google.protobuf.compiler.PluginProtos;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;

/**
 * Tests the generation of a service that has core definition imported from another file
 */
public class StatusGeneratorTest extends BaseGeneratorTest {

  @Test
  public void test_Generate() throws Exception {
    PluginProtos.CodeGeneratorRequest request = loadJson("status.jaxrs.json");

    Plugin plugin = new Plugin(request);
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertNotNull(response);
    assertEquals(2, response.getFileCount());

    assertEquals(response.getFile(0).getName(), "com/example/helloworld/RpcStatus.java");
    assertEquals(response.getFile(1).getName(), "com/example/helloworld/RpcStatusResource.java");

    Approvals.verifyAll("", response.getFileList().stream().map(File::getContent).collect(toList()));
    response.getFileList().forEach(BaseGeneratorTest::assertParses);
  }

}

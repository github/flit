package com.flit.protoc.gen.server.jakarta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.flit.protoc.Plugin;
import com.flit.protoc.gen.BaseGeneratorTest;
import com.google.protobuf.compiler.PluginProtos;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Tests the generation of a service that has core definition imported from another file
 */
public class ContextGeneratorTest extends BaseGeneratorTest {

  @Test
  public void test_GenerateWithMissingRoot() throws Exception {
    test_Route("context.missing.jakarta.json", "/twirp/com.example.context.NullService");
  }

  @Test
  public void test_GenerateWithEmptyRoot() throws Exception {
    test_Route("context.empty.jakarta.json", "/twirp/com.example.context.NullService");
  }

  @Test
  public void test_GenerateWithSlashOnlyRoot() throws Exception {
    test_Route("context.slash.jakarta.json", "/com.example.context.NullService");
  }

  @Test
  public void test_GenerateWithSlashRoot() throws Exception {
    test_Route("context.root.jakarta.json", "/root/com.example.context.NullService");
  }

  @Test
  public void test_GenerateWithNameRoot() throws Exception {
    test_Route("context.name.jakarta.json", "/fibble/com.example.context.NullService");
  }

  private void test_Route(String file, String route) throws Exception {
    PluginProtos.CodeGeneratorRequest request = loadJson(file);

    Plugin plugin = new Plugin(request);
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertNotNull(response);
    assertEquals(2, response.getFileCount());

    Map<String, File> files = response.getFileList()
        .stream()
        .collect(Collectors
            .toMap(File::getName, Function.identity()));

    assertTrue(files.containsKey("com/example/context/rpc/RpcNullService.java"));
    assertTrue(files.containsKey("com/example/context/rpc/RpcNullServiceResource.java"));

    assertTrue(files.get("com/example/context/rpc/RpcNullServiceResource.java")
        .getContent()
        .contains(String.format("@Path(\"%s\")", route)));
  }
}

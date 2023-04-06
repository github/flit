package com.flit.protoc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.compiler.PluginProtos;
import org.junit.jupiter.api.Test;

public class PluginTest {

  @Test
  public void test_NoParameters() {
    Plugin plugin = new Plugin(PluginProtos.CodeGeneratorRequest.newBuilder().build());
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertTrue(response.hasError(), "Expected an error for no parameters");
    assertEquals(
        "Usage: --flit_out=target=server,type=[spring|undertow|jaxrs][,request=[class(es)]]:<PATH>",
        response.getError());
  }

  @Test
  public void test_NoTargetSpecified() {
    Plugin plugin = new Plugin(
        PluginProtos.CodeGeneratorRequest.newBuilder().setParameter("unknown=unknown").build());
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertTrue(response.hasError(), "Expected an error for unknown target type");
    assertEquals("No argument specified for target", response.getError());
  }

  @Test
  public void test_UnknownTargetType() {
    Plugin plugin = new Plugin(
        PluginProtos.CodeGeneratorRequest.newBuilder().setParameter("target=unknown,type=boot")
            .build());
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertTrue(response.hasError(), "Expected an error for unknown target type");
    assertEquals("Unknown target type: unknown", response.getError());
  }

  @Test
  public void test_EmptyTargetType() {
    Plugin plugin = new Plugin(
        PluginProtos.CodeGeneratorRequest.newBuilder().setParameter("target=").build());
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertTrue(response.hasError(), "Expected an error for unknown target type");
    assertEquals("No argument specified for target", response.getError());
  }

  @Test
  public void test_MissingTargetType() {
    Plugin plugin = new Plugin(
        PluginProtos.CodeGeneratorRequest.newBuilder().setParameter("target=server").build());
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertTrue(response.hasError(), "Expected an error for unknown server type");
    assertEquals("No argument specified for type", response.getError());
  }

  @Test
  public void test_UnknownServerType() {
    Plugin plugin = new Plugin(
        PluginProtos.CodeGeneratorRequest.newBuilder().setParameter("target=server,type=unknown")
            .build());
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertTrue(response.hasError(), "Expected an error for unknown server type");
    assertEquals("Unknown server type: unknown", response.getError());
  }

  @Test
  public void test_MissingServerType() {
    Plugin plugin = new Plugin(
        PluginProtos.CodeGeneratorRequest.newBuilder().setParameter("target=server,type=").build());
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertTrue(response.hasError(), "Expected an error for unknown server type");
    assertEquals("No argument specified for type", response.getError());
  }

  @Test
  public void test_EmptyProtoList() {
    Plugin plugin = new Plugin(
        PluginProtos.CodeGeneratorRequest.newBuilder().setParameter("target=server,type=boot")
            .build());
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertFalse(response.hasError(), "No error expected for empty file list");
    assertEquals(0, response.getFileCount());
  }

}

package com.flit.protoc;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.protobuf.compiler.PluginProtos;
import org.junit.Test;

public class PluginTest {

  @Test public void test_NoParameters() {
    Plugin plugin = new Plugin(PluginProtos.CodeGeneratorRequest.newBuilder().build());
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertTrue("Expected an error for no parameters", response.hasError());
    assertEquals("Incorrect error message", "Usage: --flit_out=target=server,type=[spring|undertow|jaxrs][,request=[class(es)]]:<PATH>", response.getError());
  }

  @Test public void test_NoTargetSpecified() {
    Plugin plugin = new Plugin(PluginProtos.CodeGeneratorRequest.newBuilder().setParameter("unknown=unknown").build());
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertTrue("Expected an error for unknown target type", response.hasError());
    assertEquals("Incorrect error message", "No argument specified for target", response.getError());
  }

  @Test public void test_UnknownTargetType() {
    Plugin plugin = new Plugin(PluginProtos.CodeGeneratorRequest.newBuilder().setParameter("target=unknown,type=boot").build());
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertTrue("Expected an error for unknown target type", response.hasError());
    assertEquals("Incorrect error message", "Unknown target type: unknown", response.getError());
  }

  @Test public void test_EmptyTargetType() {
    Plugin plugin = new Plugin(PluginProtos.CodeGeneratorRequest.newBuilder().setParameter("target=").build());
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertTrue("Expected an error for unknown target type", response.hasError());
    assertEquals("Incorrect error message", "No argument specified for target", response.getError());
  }

  @Test public void test_MissingTargetType() {
    Plugin plugin = new Plugin(PluginProtos.CodeGeneratorRequest.newBuilder().setParameter("target=server").build());
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertTrue("Expected an error for unknown server type", response.hasError());
    assertEquals("Incorrect error message", "No argument specified for type", response.getError());
  }

  @Test public void test_UnknownServerType() {
    Plugin plugin = new Plugin(PluginProtos.CodeGeneratorRequest.newBuilder().setParameter("target=server,type=unknown").build());
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertTrue("Expected an error for unknown server type", response.hasError());
    assertEquals("Incorrect error message", "Unknown server type: unknown", response.getError());
  }

  @Test public void test_MissingServerType() {
    Plugin plugin = new Plugin(PluginProtos.CodeGeneratorRequest.newBuilder().setParameter("target=server,type=").build());
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertTrue("Expected an error for unknown server type", response.hasError());
    assertEquals("Incorrect error message", "No argument specified for type", response.getError());
  }

  @Test public void test_EmptyProtoList() {
    Plugin plugin = new Plugin(PluginProtos.CodeGeneratorRequest.newBuilder().setParameter("target=server,type=boot").build());
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertFalse("No error expected for empty file list", response.hasError());
    assertEquals("Expected no files generated", 0, response.getFileCount());
  }

}

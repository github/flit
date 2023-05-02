package com.flit.runtime.jakarta;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvalidProtobufExceptionMapperTest {

  @Mock
  private HttpServletRequest request;

  @InjectMocks
  private InvalidProtobufExceptionMapper mapper = new InvalidProtobufExceptionMapper();

  @Test
  void testToResponse() {
    InvalidProtocolBufferException exception = new InvalidProtocolBufferException(
        "something happened");
    Response response = mapper.toResponse(exception);
    assertEquals(response.getStatus(), 400);
    Map<String, Object> expectedResult = Map
        .of("msg", "something happened", "code", "invalid_argument");
    assertEquals(response.getEntity(), expectedResult);
    assertEquals(response.getMediaType(), MediaType.APPLICATION_JSON_TYPE);
  }
}

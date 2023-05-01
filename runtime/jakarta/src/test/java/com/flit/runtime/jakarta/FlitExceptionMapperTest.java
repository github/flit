package com.flit.runtime.jakarta;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.flit.runtime.ErrorCode;
import com.flit.runtime.FlitException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FlitExceptionMapperTest {

  @Mock
  private HttpServletRequest request;

  @InjectMocks
  private FlitExceptionMapper flitExceptionMapper = new FlitExceptionMapper();

  @Test
  void testToResponse() {
    FlitException flit = FlitException.builder()
        .withCause(new RuntimeException("Failed"))
        .withErrorCode(ErrorCode.INTERNAL)
        .withMessage("with this message")
        .build();
    Response response = flitExceptionMapper.toResponse(flit);
    assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
    Map<String, Object> expectedResult = Map.of("msg", "with this message", "code", "internal");
    assertEquals(response.getEntity(), expectedResult);
    assertEquals(response.getMediaType(), MediaType.APPLICATION_JSON_TYPE);
  }
}
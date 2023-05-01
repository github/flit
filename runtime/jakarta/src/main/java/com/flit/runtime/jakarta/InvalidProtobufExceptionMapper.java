package com.flit.runtime.jakarta;

import com.flit.runtime.ErrorCode;
import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvalidProtobufExceptionMapper implements
    ExceptionMapper<InvalidProtocolBufferException> {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(InvalidProtobufExceptionMapper.class);

  @Context private HttpServletRequest request;

  @Override
  public Response toResponse(InvalidProtocolBufferException exception) {
    LOGGER.error("InvalidProtocolBufferException: request = {}, method = {}, msg= {}",
        request.getRequestURI(), request.getMethod(), exception.getMessage(), exception);

    Map<String, Object> response = new HashMap<>();
    response.put("code", ErrorCode.INVALID_ARGUMENT.getErrorCode());
    response.put("msg", exception.getMessage());
    return Response.status(ErrorCode.INVALID_ARGUMENT.getHttpStatus())
        .type(MediaType.APPLICATION_JSON)
        .entity(response)
        .build();
  }
}

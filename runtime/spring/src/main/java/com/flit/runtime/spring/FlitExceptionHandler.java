package com.flit.runtime.spring;

import com.flit.runtime.ErrorCode;
import com.flit.runtime.FlitException;
import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Component
public class FlitExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlitExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(HttpServletRequest request, Exception e) {
        LOGGER.error(
            "Exception: request = {}, method = {}, msg = {}",
            request.getRequestURI(), request.getMethod(), e.getMessage(), e
        );

        Map<String, Object> response = new HashMap<>();
        response.put("code", ErrorCode.INTERNAL);
        response.put("msg", "An internal error has occurred");


        return ResponseEntity
            .status(ErrorCode.INTERNAL.getHttpStatus())
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    @ExceptionHandler(InvalidProtocolBufferException.class)
    public ResponseEntity<?> handleInvalidProtocolBufferException(HttpServletRequest request, Exception e) {
      LOGGER.error("InvalidProtocolBufferException: request = {}, method = {}, msg= {}",
          request.getRequestURI(), request.getMethod(), e.getMessage(), e);

      Map<String, Object> response = new HashMap<>();
      response.put("code", ErrorCode.INVALID_ARGUMENT);
      response.put("msg", e.getMessage());

      return ResponseEntity
          .status(ErrorCode.INVALID_ARGUMENT.getHttpStatus())
          .contentType(MediaType.APPLICATION_JSON)
          .body(response);
    }

    @ExceptionHandler(FlitException.class)
    public ResponseEntity<?> handleFlitException(HttpServletRequest request, FlitException e) {

        LOGGER.error(
            "Flit exception: request = {}, method = {}, code = {}, msg = {}",
            request.getRequestURI(), request.getMethod(), e.getMessage(), e.getErrorCode(), e.getMessage(), e
        );

        Map<String, Object> response = new HashMap<>();
        response.put("code", e.getErrorCode().getErrorCode());
        response.put("msg", e.getMessage());

        if (e.hasMeta()) {
            response.put("meta", e.getMeta());
        }

        return ResponseEntity
            .status(e.getErrorCode().getHttpStatus())
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }
}

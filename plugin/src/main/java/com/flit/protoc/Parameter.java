package com.flit.protoc;

import com.flit.protoc.gen.GeneratorException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString public class Parameter {

  public static final String PARAM_TARGET = "target";
  public static final String PARAM_CLIENT = "client";
  public static final String PARAM_TYPE = "type";
  public static final String PARAM_CONTEXT = "context";
  public static final String PARAM_REQUEST = "request";

  private final String key;
  private final String value;

  public Parameter(String[] strings) {
    this.key = strings[0];
    this.value = strings[1];
  }

  public static Map<String, Parameter> of(String value) {
    if (value == null || (value = value.trim()).isEmpty()) {
      throw new GeneratorException("Empty value passed to parameter builder");
    }

    return Arrays.stream(value.split(","))
      .map(p -> p.split("=", 2))
      .filter(s -> s.length == 2 && !s[0].trim().isEmpty() && !s[1].trim().isEmpty())
      .map(Parameter::new)
      .collect(Collectors.toMap(Parameter::getKey, Function.identity()));
  }
}

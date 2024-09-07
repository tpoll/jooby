/*
 * Jooby https://jooby.io
 * Apache License Version 2.0 https://jooby.io/LICENSE.txt
 * Copyright 2014 Edgar Espina
 */
package io.jooby.avaje.validator.app;

import io.jooby.Jooby;
import io.jooby.StatusCode;
import io.jooby.avaje.validator.AvajeValidatorModule;
import io.jooby.avaje.validator.ConstraintViolationHandler;
import io.jooby.jackson.JacksonModule;
import jakarta.validation.ConstraintViolationException;

public class App extends Jooby {

  private static final StatusCode STATUS_CODE = StatusCode.UNPROCESSABLE_ENTITY;
  public static final String DEFAULT_TITLE = "Validation failed";

  {
    install(new JacksonModule());
    install(new AvajeValidatorModule());

    mvc(new Controller());

    error(
        ConstraintViolationException.class,
        new ConstraintViolationHandler(STATUS_CODE, DEFAULT_TITLE));
  }
}
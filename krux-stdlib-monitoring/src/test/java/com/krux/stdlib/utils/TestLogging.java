package com.krux.stdlib.utils;

import com.krux.stdlib.KruxStdLib;
import joptsimple.OptionSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;

@DisplayName("KruxStdLib Logging")
class TestLogging {
  KruxStdLib stdlib = KruxStdLib.get();
  OptionSet options;
  Logger LOGGER = LoggerFactory.getLogger(TestLogging.class);

  @Test
  @DisplayName("can output logs")
  void canOutputLogs() {
    LOGGER.info("Test INFO log");
  }
}

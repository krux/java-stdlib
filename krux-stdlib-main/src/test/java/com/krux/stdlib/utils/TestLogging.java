package com.krux.stdlib.utils;

// For capturing logger output
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.krux.stdlib.KruxStdLib;
import joptsimple.OptionSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.junit.jupiter.api.Assertions.fail;

import static org.assertj.core.api.Assertions.*;

@DisplayName("KruxStdLib Logging")
class TestLogging {
  KruxStdLib stdlib = KruxStdLib.get();
  OptionSet options;
  Logger LOGGER = LoggerFactory.getLogger(TestLogging.class);

  private PrintStream systemOut = System.out;
  private ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();

  @BeforeEach
  void captureStdout() {
    PrintStream captureStream = new PrintStream(capturedOut);
    System.setOut(captureStream);
  }

  @AfterEach
  void resetStdout() {
    System.setOut(systemOut);
  }

  @Test
  @DisplayName("can output logs")
  void canOutputLogs() {
    String test_info_log = "Test INFO log";
    LOGGER.info(test_info_log);
    assertThat(capturedOut.toString()).contains(test_info_log);

  }
}

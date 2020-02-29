package com.krux.stdlib.utils;

import com.krux.stdlib.KruxStdLib;
import io.github.netmikey.logunit.api.LogCapturer;
import joptsimple.OptionSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

// I think this would be preferred for log capture, but I
// had to punt.
//import org.apache.logging.log4j.test.appender.ListAppender;

@DisplayName("KruxStdLib Logging")
class TestLogging {
  Logger LOG = LoggerFactory.getLogger(TestLogging.class);

  @RegisterExtension
  LogCapturer capturedLogs = LogCapturer.create().captureForType(TestLogging.class);

  KruxStdLib stdlib = KruxStdLib.get();

  @Test
  @DisplayName("can output logs")
  void canOutputLogs() {
    String test_error_log = "Test ERROR log";
    LOG.info(test_error_log);
    assertThat(capturedLogs.size()).isEqualTo(1);
    capturedLogs.assertContains(test_error_log);
  }

  @Test
  @DisplayName("does not output logs below configured level")
  void doesNotOutputLogsBelowConfiguredLevel() {
    String test_debug_log = "Test DEBUG log";
    String test_error_log = "Test ERROR log";
    LOG.error(test_error_log);
    LOG.debug(test_debug_log);
    capturedLogs.assertContains(test_error_log);
    capturedLogs.assertDoesNotContain(test_debug_log);
  }
}

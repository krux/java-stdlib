package com.krux.stdlib.utils;

import com.krux.stdlib.KruxStdLib;
import joptsimple.OptionSet;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("the options parser")
public class TestOptions {
  @Nested
  @DisplayName("given args: --env testEnv --app-name TestAppName")
  class givenEnvAndAppName {
    private final String[] args = {"--env", "testEnv", "--app-name", "TestAppName"};
    private OptionSet options;
    private KruxStdLib stdlib;
    private Logger logger = LogManager.getRootLogger();

    @BeforeEach
    void parseArgs() {
      stdlib = new KruxStdLib();
      options = stdlib.parse(args);
    }

    @Test
    @DisplayName("sets the environment to 'testEnv'")
    void setsEnvironment() {
      assertThat(options.has("env"));
      assertThat(stdlib.getEnv()).isEqualTo("testEnv");
    }

    @Test
    @DisplayName("sets the app name to 'TestAppName'")
    void setsAppName() {
      assertThat(options.has("app-name"));
      assertThat(stdlib.getAppName()).isEqualTo("TestAppName");
    }

    @Test
    @DisplayName("does NOT set the log level to DEBUG")
    void doesNotSetLogLevelDEBUG() {
      assertThat(logger.getLevel()).isLessThan(Level.DEBUG);
    }
  }

  @Nested
  @DisplayName("given args: --env testEnv --log-level DEBUG")
  class GivenLogLevelDEBUG {
    private final String[] args = {"--env",  "testEnv", "--log-level", "DEBUG"};
    private OptionSet options;
    private KruxStdLib stdlib;
    private Logger logger = LogManager.getRootLogger();

    @BeforeEach
    void parseArgs() {
      stdlib = new KruxStdLib();
      options = stdlib.parse(args);
    }

    @Test
    @DisplayName("does NOT set the log level in the logger to DEBUG")
    void doesNotSetLoggerLogLevel() {
      assertThat(logger.getLevel()).isLessThan(Level.DEBUG);
    }

    @Test
    @DisplayName("does NOT set the log level in KruxStdLib to DEBUG")
    void doesNotSetStdlibLogLevel() {
      assertThat(stdlib.getLogLevel()).isEqualTo("WARN");
    }

    // Would like to have this but jopt-simple does not support
    // removing options from the OptionSet.
    //
    // @Test
    // @DisplayName("sets log level in Options")
    // void doesNotSetOptionsLogLevel() {
    //   assertThat(options.has("log-level")).isFalse();
    // }
  }
}

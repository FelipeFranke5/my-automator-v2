package dev.franke.felipee.braspag_automator_v2.api_30_retrieve_merchant_data.service.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProcessExecutionAPI30 {

  private static final Logger LOG = LoggerFactory.getLogger(ProcessExecutionAPI30.class);

  @Value("${braspag.prod.login}")
  private String login;

  @Value("${braspag.prod.password}")
  private String password;

  public String run(String ec) throws IOException {
    LOG.info("Starting process for Python execution");
    ProcessBuilder processBuilder = getProcessBuilder(ec);

    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();
    LOG.info("Process has started with PID {}", process.pid());

    InputStreamReader inputStreamReader =
        new InputStreamReader(process.getInputStream(), Charset.defaultCharset());
    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
    String lastLine = "";
    String currentLine;

    LOG.info("Going through each line for process with PID {}", process.pid());
    while ((currentLine = bufferedReader.readLine()) != null) {
      lastLine = currentLine;
    }

    LOG.info("Last line for PID {} was: {}", process.pid(), lastLine);
    return lastLine;
  }

  private ProcessBuilder getProcessBuilder(String ec) {
    LOG.info("Defining the process");
    String fullCommand = getScriptForMakingScriptExecutable() + " && " + getExecutableString(ec);
    LOG.info("Process defined");
    return new ProcessBuilder("/bin/sh", "-c", fullCommand);
  }

  private String getScriptForMakingScriptExecutable() {
    return "chmod +x python/prod_get_merchant_script.sh";
  }

  private String getExecutableString(String ec) {
    return "python/prod_get_merchant_script.sh " + login + " " + password + " " + ec;
  }
}

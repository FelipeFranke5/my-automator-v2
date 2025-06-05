package dev.franke.felipee.braspag_automator_v2.checkout_enable_3ds.service.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProcessExecutionEnable3DS {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessExecutionEnable3DS.class);

    @Value("${braspag.prod.login}")
    private String login;

    @Value("${braspag.prod.password}")
    private String password;

    // Show number of Python processes running
    public byte getNumberOfPythonProcesses() {
        LOG.info("Counting the number of Python processes running");
        final String[] processCommand = {"/bin/sh", "-c", "ps -ef | grep python | grep -v grep | wc -l"};
        final ProcessBuilder processBuilder = new ProcessBuilder(processCommand);
        processBuilder.redirectErrorStream(true);

        try {
            final Process process = processBuilder.start();
            final InputStreamReader inputStreamReader =
                    new InputStreamReader(process.getInputStream(), Charset.defaultCharset());
            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            final String line = bufferedReader.readLine();
            LOG.info("Number of Python processes running: {}", line);
            return Byte.parseByte(line.trim());
        } catch (final IOException ioException) {
            LOG.error("Error while counting Python processes", ioException);
            return -1; // Return -1 to indicate an error
        } catch (final NumberFormatException numberFormatException) {
            LOG.error("Error parsing the number of Python processes", numberFormatException);
            return -2; // Return -2 to indicate an error
        }
    }

    public String run(final String ec) throws IOException {
        LOG.info("[{}] Starting process for Python execution", ec);
        final ProcessBuilder processBuilder = getProcessBuilder(ec);

        processBuilder.redirectErrorStream(true);
        final Process process = processBuilder.start();
        LOG.info("[{}] Process has started with PID {}", ec, process.pid());

        final InputStreamReader inputStreamReader =
                new InputStreamReader(process.getInputStream(), Charset.defaultCharset());
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String lastLine = "";
        String currentLine;

        LOG.info("[{}] Going through each line for process with PID {}", ec, process.pid());
        while ((currentLine = bufferedReader.readLine()) != null) {
            lastLine = currentLine;
        }

        LOG.info("[{}] Last line for PID {} was: {}", ec, process.pid(), lastLine);
        return lastLine;
    }

    private ProcessBuilder getProcessBuilder(final String ec) {
        LOG.info("[{}] Defining the process", ec);
        final String fullCommand = getScriptForMakingScriptExecutable() + " && " + getExecutableString(ec);
        LOG.info("[{}] Process defined", ec);
        return new ProcessBuilder("/bin/sh", "-c", fullCommand);
    }

    private String getScriptForMakingScriptExecutable() {
        return "chmod +x python/prod_checkout_enable_3ds.sh";
    }

    private String getExecutableString(final String ec) {
        return "python/prod_checkout_enable_3ds.sh " + login + " " + password + " " + ec;
    }
}

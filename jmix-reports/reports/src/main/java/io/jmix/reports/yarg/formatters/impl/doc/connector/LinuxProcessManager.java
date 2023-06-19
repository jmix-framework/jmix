/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.reports.yarg.formatters.impl.doc.connector;

import io.jmix.reports.yarg.formatters.impl.doc.connector.JavaProcessManager;
import io.jmix.reports.yarg.formatters.impl.doc.connector.ProcessManager;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinuxProcessManager extends JavaProcessManager implements ProcessManager {
    protected static final Pattern PS_OUTPUT_LINE = Pattern.compile("^\\s*(\\d+)\\s+(.*)$");

    protected String[] psCommand() {
        return new String[]{"/bin/ps", "-e", "-o", "pid,args"};
    }

    @Override
    public List<Long> findPid(String host, int port) {
        try {
            String regex = Pattern.quote(host) + ".*" + port;
            Pattern commandPattern = Pattern.compile(regex);
            List<String> lines = execute(psCommand());
            List<Long> result = new ArrayList<Long>();

            for (String line : lines) {
                Matcher lineMatcher = PS_OUTPUT_LINE.matcher(line);
                if (lineMatcher.matches()) {
                    String command = lineMatcher.group(2);
                    Matcher commandMatcher = commandPattern.matcher(command);
                    if (commandMatcher.find()) {
                        result.add(Long.parseLong(lineMatcher.group(1)));
                    }
                }
            }

            return result;
        } catch (IOException e) {
            log.error("An error occured while searching for soffice PID in linux system", e);
        }

        return Collections.singletonList(PID_UNKNOWN);
    }

    public void kill(Process process, List<Long> pids) {
        log.info("Linux office process manager is going to kill following processes " + pids);
        for (Long pid : pids) {
            try {
                if (PID_UNKNOWN != pid) {
                    execute("/bin/kill", "-KILL", Long.toString(pid));
                } else {
                    log.warn("Fail to kill open office process with platform dependent manager - PID not found.");
                    super.kill(process, Collections.singletonList(pid));
                }
            } catch (Exception e) {
                log.error(String.format("An error occurred while killing process %d in linux system. Process.destroy() will be called.", pid), e);
                super.kill(process, Collections.singletonList(pid));
            }
        }
    }

    protected List<String> execute(String... args) throws IOException {
        Process process = new ProcessBuilder(args).start();
        @SuppressWarnings("unchecked")
        List<String> lines = IOUtils.readLines(process.getInputStream());
        return lines;
    }

}

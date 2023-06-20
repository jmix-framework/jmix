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


import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class WinProcessManager extends JavaProcessManager implements ProcessManager {
    protected static final Logger log = LoggerFactory.getLogger(WinProcessManager.class);

    protected static final String KILL_COMMAND = "taskkill /f /PID %s";
    protected static final String FIND_PID_COMMAND = "cmd /c netstat -a -n -o -p TCP|findstr \"%s\"";
    protected static final Pattern NETSTAT_PATTERN =
            Pattern.compile("^.*?(\\d+\\.\\d+\\.\\d+\\.\\d+)[\\.\\:](\\d+)\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)[\\.\\:](\\d+)\\s+\\w+\\s+(\\d+)");
    protected static final String LOCAL_HOST = "127.0.0.1";

    @Override
    public List<Long> findPid(String host, int port) {
        try {
            if ("localhost".equalsIgnoreCase(host))
                host = LOCAL_HOST;
            Process process = Runtime.getRuntime().exec(String.format(FIND_PID_COMMAND, port));
            List<String> r = IOUtils.readLines(process.getInputStream(), StandardCharsets.UTF_8);
            for (String output : r) {
                NetStatInfo info = new NetStatInfo(output);
                if (info.localPort == port && Objects.equals(host, info.localAddress))
                    return Collections.singletonList(info.pid);
            }
        } catch (IOException e) {
            log.warn(String.format("Unable to find PID for OO process on host:port  %s:%s", host, port), e);
        }
        log.warn(String.format("Unable to find PID for OO process on host:port %s:%s", host, port));
        return Collections.singletonList(PID_UNKNOWN);
    }

    @Override
    public void kill(Process process, List<Long> pids) {
        log.info("Windows office process manager is going to kill following processes " + pids);
        for (Long pid : pids) {
            try {
                if (PID_UNKNOWN != pid) {
                    String command = String.format(KILL_COMMAND, pid);
                    Runtime.getRuntime().exec(command);
                } else {
                    log.warn("Fail to kill open office process with platform dependent manager - PID not found.");
                    super.kill(process, Collections.singletonList(pid));
                }
            } catch (IOException e) {
                log.error(String.format("An error occurred while killing process %d in windows system. Process.destroy() will be called.", pid), e);
                super.kill(process, Collections.singletonList(pid));
            }
        }
    }

    protected static class NetStatInfo {
        protected String localAddress;
        protected int localPort;
        protected long pid;

        protected NetStatInfo(String output) {
            Matcher matcher = NETSTAT_PATTERN.matcher(output);
            if (matcher.matches()) {
                localAddress = matcher.group(1);
                String value = matcher.group(2);
                if (isNotBlank(value))
                    localPort = Integer.parseInt(value);
                value = matcher.group(5);
                if (isNotBlank(value))
                    pid = Long.parseLong(value);
            }
        }
    }
}

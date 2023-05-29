/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.haulmont.cuba.core.app;

import com.haulmont.bali.db.QueryRunner;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.UuidSource;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.events.AppContextStartedEvent;
import com.haulmont.cuba.core.sys.events.AppContextStoppedEvent;
import io.jmix.core.TimeSource;
import io.jmix.data.persistence.DbTypeConverter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.haulmont.cuba.core.global.Events.HIGHEST_PLATFORM_PRECEDENCE;

@Component(ServerInfoAPI.NAME)
public class ServerInfo implements ServerInfoAPI, Ordered {

    public static final String CUBA_RELEASE_NUMBER_PATH = "/com/haulmont/cuba/core/global/release.number";
    public static final String CUBA_RELEASE_TIMESTAMP_PATH = "/com/haulmont/cuba/core/global/release.timestamp";

    private static final Logger log = LoggerFactory.getLogger(ServerInfo.class);

    protected String releaseNumber = "?";
    protected String releaseTimestamp = "?";

    protected Configuration configuration;

    protected volatile String serverId;

    protected SchedulingConfig globalConfig;

    @Inject
    protected TimeSource timeSource;

    @Inject
    protected Persistence persistence;

    @Inject
    protected UuidSource uuidSource;

    protected Timer infoUpdateTimer;

    @Inject
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;

        InputStream stream = getClass().getResourceAsStream(CUBA_RELEASE_NUMBER_PATH);
        if (stream != null) {
            try {
                releaseNumber = IOUtils.toString(stream, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.warn("Unable to read release number", e);
            }
        }

        stream = getClass().getResourceAsStream(CUBA_RELEASE_TIMESTAMP_PATH);
        if (stream != null) {
            try {
                releaseTimestamp = IOUtils.toString(stream, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.warn("Unable to read release timestamp", e);
            }
        }

        globalConfig = configuration.getConfig(SchedulingConfig.class);
    }

    @Override
    public String getReleaseNumber() {
        return releaseNumber;
    }

    @Override
    public String getReleaseTimestamp() {
        return releaseTimestamp;
    }

    @Override
    public String getServerId() {
        if (serverId == null) {
            SchedulingConfig globalConfig = configuration.getConfig(SchedulingConfig.class);
            serverId = globalConfig.getWebHostName() + ":" + globalConfig.getWebPort() + "/" + globalConfig.getWebContextName();
        }
        return serverId;
    }

    @EventListener(AppContextStartedEvent.class)
    public void applicationStarted() {
        if (!globalConfig.getTestMode()) {
            infoUpdateTimer = new Timer(true);
            infoUpdateTimer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            Thread.currentThread().setName("ServerInfoTimer");

                            if (AppContext.isStarted()) {
                                updateCurrentServer();
                            }
                        }
                    },
                    30000,
                    60000
            );
        }
    }

    @EventListener(AppContextStoppedEvent.class)
    public void applicationStopped() {
        try {
            infoUpdateTimer.cancel();
            infoUpdateTimer.purge();

            log.trace("Updating server information in the database");

            DbTypeConverter types = persistence.getDbTypeConverter();
            Object tsObj = types.getSqlObject(timeSource.currentTimestamp());
            int tsType = types.getSqlType(Date.class);
            Object falseObj = types.getSqlObject(Boolean.FALSE);
            int boolType = types.getSqlType(Boolean.class);

            QueryRunner runner = new QueryRunner(persistence.getDataSource());
            runner.update(
                    "update SYS_SERVER set UPDATE_TS = ?, IS_RUNNING = ? where NAME = ?",
                    new Object[]{tsObj, falseObj, getServerId()},
                    new int[]{tsType, boolType, Types.VARCHAR}
            );
        } catch (Exception e) {
            log.error("Unable to update SYS_SERVER: {}", e);
        }
    }

    protected void updateCurrentServer() {
        try {
            log.trace("Updating server information in the database");
            String serverId = getServerId();

            DbTypeConverter types = persistence.getDbTypeConverter();
            Object tsObj = types.getSqlObject(timeSource.currentTimestamp());
            int tsType = types.getSqlType(Date.class);
            Object trueObj = types.getSqlObject(Boolean.TRUE);
            int boolType = types.getSqlType(Boolean.class);

            QueryRunner runner = new QueryRunner(persistence.getDataSource());

            int updated = runner.update(
                    "update SYS_SERVER set UPDATE_TS = ?, IS_RUNNING = ? where NAME = ?",
                    new Object[]{tsObj, trueObj, serverId},
                    new int[]{tsType, boolType, Types.VARCHAR}
            );
            if (updated == 0) {
                Object id = types.getSqlObject(uuidSource.createUuid());
                int idType = types.getSqlType(UUID.class);
                runner.update(
                        "insert into SYS_SERVER (ID, CREATE_TS, UPDATE_TS, NAME, IS_RUNNING) " +
                                "values (?, ?, ?, ?, ?)",
                        new Object[]{id, tsObj, tsObj, serverId, trueObj},
                        new int[]{idType, tsType, tsType, Types.VARCHAR, boolType}
                );
            }
        } catch (Exception e) {
            log.error("Unable to update SYS_SERVER: {}", e.toString());
        }
    }

    @Override
    public int getOrder() {
        return HIGHEST_PLATFORM_PRECEDENCE + 10;
    }
}
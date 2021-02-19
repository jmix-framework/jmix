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

package io.jmix.data.impl.liquibase;

import liquibase.integration.spring.SpringLiquibase;
import liquibase.integration.spring.SpringResourceAccessor;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class JmixLiquibase extends SpringLiquibase {
    private static final String MASTER_CHANGELOG_NAME = "inmemory:jmix_master.xml";
    private String masterLog = null;

    @Override
    protected SpringResourceAccessor createResourceOpener() {
        return new JmixResourceAccessor(resourceLoader);

    }

    public void setChangeLogContent(String masterLog) {
        this.masterLog = Objects.requireNonNull(masterLog);
        setChangeLog(MASTER_CHANGELOG_NAME);
    }


    public class JmixResourceAccessor extends SpringResourceAccessor {

        public JmixResourceAccessor(ResourceLoader resourceLoader) {
            super(resourceLoader);
        }


        @Override
        public InputStream openStream(String relativeTo, String streamPath) throws IOException {
            if (MASTER_CHANGELOG_NAME.equals(streamPath)) {
                if (masterLog != null) {
                    return new ByteArrayInputStream(masterLog.getBytes(StandardCharsets.UTF_8));
                } else {
                    throw new RuntimeException("Changelog name set to '" + MASTER_CHANGELOG_NAME
                            + "' but masterChangeLog has not been specified. See JmixLiquibase#setMasterChangeLog(String)");
                }
            }
            return super.openStream(relativeTo, streamPath);
        }
    }
}

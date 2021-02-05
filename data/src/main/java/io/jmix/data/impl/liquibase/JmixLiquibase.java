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

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.integration.spring.SpringResourceAccessor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
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

        // Workaround for https://github.com/liquibase/liquibase/issues/1657 based on https://github.com/liquibase/liquibase/pull/1665
        //---- START ----

        /**
         * Returns the lookup path to the given resource.
         */
        @Override
        protected String getResourcePath(Resource resource) {
            if (resource instanceof ContextResource) {
                return ((ContextResource) resource).getPathWithinContext();
            }
            if (resource instanceof ClassPathResource) {
                return ((ClassPathResource) resource).getPath();
            }

            //have to fall back to figuring out the path as best we can
            try {
                String url = resource.getURL().toExternalForm();
                if (url.contains("!")) {
                    return url.replaceFirst(".*!", "");
                } else {
                    while (!resourceLoader.getResource("classpath:" + url).exists()) {
                        String newUrl = url.replaceFirst("^/?.*?/", "");
                        if (newUrl.equals(url)) {
                            throw new UnexpectedLiquibaseException("Could determine path for " + resource.getURL().toExternalForm());
                        }
                        url = newUrl;
                    }

                    return url;
                }
            } catch (IOException e) {
                //the path gets stored in the databasechangelog table, so if it gets returned incorrectly it will cause future problems.
                //so throw a breaking error now rather than wait for bigger problems down the line
                throw new UnexpectedLiquibaseException("Cannot determine resource path for " + resource.getDescription());
            }
        }

        @Override
        protected String finalizeSearchPath(String searchPath) {
            return super.finalizeSearchPath(searchPath.replaceAll("classpath\\*:", "classpath:"));
        }

        //---- END ----
    }
}

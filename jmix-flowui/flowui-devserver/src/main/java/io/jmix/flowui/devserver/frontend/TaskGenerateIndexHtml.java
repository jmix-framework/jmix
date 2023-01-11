/*
 * Copyright 2000-2022 Vaadin Ltd.
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

package io.jmix.flowui.devserver.frontend;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Generate <code>index.html</code> if it is missing in frontend folder.
 */
public class TaskGenerateIndexHtml extends AbstractTaskClientGenerator {

    private final File indexHtml;

    /**
     * Create a task to generate <code>index.html</code> if necessary.
     *
     * @param frontendDirectory frontend directory is to check if the file already exists
     *                          there.
     */
    TaskGenerateIndexHtml(File frontendDirectory) {
        indexHtml = new File(frontendDirectory, FrontendUtils.INDEX_HTML);
    }

    @Override
    protected String getFileContent() throws IOException {
        InputStream indexStream = FrontendUtils.getResourceAsStream(
                FrontendUtils.INDEX_HTML, TaskGenerateIndexHtml.class.getClassLoader()
        );
        return IOUtils.toString(indexStream, UTF_8);
    }

    @Override
    protected File getGeneratedFile() {
        return indexHtml;
    }

    @Override
    protected boolean shouldGenerate() {
        return !indexHtml.exists();
    }
}


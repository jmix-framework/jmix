/*
 * Copyright 2026 Haulmont.
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

package io.jmix.core.impl;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JavaClassLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void testDisabledHotDeploySkipsFilesystemClassLoading() throws Exception {
        writeInvalidClassFile();

        JavaClassLoader classLoader = createClassLoader(false);
        try {
            assertThat(classLoader.loadClass(String.class.getName(), false)).isSameAs(String.class);
        } finally {
            classLoader.destroy();
        }
    }

    @Test
    void testEnabledHotDeployLoadsFilesystemClassLoading() throws Exception {
        writeInvalidClassFile();

        JavaClassLoader classLoader = createClassLoader(true);
        try {
            assertThatThrownBy(() -> classLoader.loadClass(String.class.getName(), false))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("Prohibited package name");
        } finally {
            classLoader.destroy();
        }
    }

    private JavaClassLoader createClassLoader(boolean hotDeployEnabled) {
        JavaClassLoader classLoader = new JavaClassLoader(
                Thread.currentThread().getContextClassLoader(),
                tempDir.toString(),
                Set.of(tempDir.toString()),
                new SpringBeanLoader(),
                hotDeployEnabled
        );
        classLoader.meterRegistry = new SimpleMeterRegistry();
        return classLoader;
    }

    private void writeInvalidClassFile() throws Exception {
        Path classFile = tempDir.resolve("java/lang/String.class");
        Files.createDirectories(classFile.getParent());
        Files.write(classFile, new byte[]{0x00, 0x01, 0x02});
    }
}

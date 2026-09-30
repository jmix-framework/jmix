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

package temp_dir;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.CoreProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import test_support.addon1.TestAddon1Configuration;
import test_support.app.TestAppConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TempDirTest {

    @TempDir
    Path testDir;

    @Test
    void tempDir_defaultNotWritable_fallsBackToJavaTmpDir() throws IOException {
        // The default temp dir is ${user.dir}/.jmix/temp. It cannot be created under a regular file.
        Path userDirFile = Files.createFile(testDir.resolve("user-dir-file"));

        Path tempDir = Path.of(startAndGetTempDir(Map.of("user.dir", userDirFile.toString())));

        assertThat(tempDir).isDirectory().isWritable();
        assertThat(tempDir).startsWith(Path.of(System.getProperty("java.io.tmpdir")));
    }

    @Test
    void tempDir_defaultWritable_keepsDefault() {
        String tempDir = startAndGetTempDir(Map.of("user.dir", testDir.toString()));

        assertThat(Path.of(tempDir)).isEqualTo(testDir.resolve(".jmix/temp"));
    }

    @Test
    void tempDir_explicitNotWritable_keepsExplicitValue() throws IOException {
        Path file = Files.createFile(testDir.resolve("some-file"));
        String explicitTempDir = file.resolve("temp").toString();

        String tempDir = startAndGetTempDir(Map.of("jmix.core.temp-dir", explicitTempDir));

        assertThat(tempDir).isEqualTo(explicitTempDir);
    }

    @Test
    void tempDir_explicitCamelCaseNotWritable_keepsExplicitValue() throws IOException {
        Path file = Files.createFile(testDir.resolve("some-file"));
        String explicitTempDir = file.resolve("temp").toString();

        String tempDir = startAndGetTempDir(Map.of("jmix.core.tempDir", explicitTempDir));

        assertThat(tempDir).isEqualTo(explicitTempDir);
    }

    private String startAndGetTempDir(Map<String, Object> properties) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
            context.register(CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class);
            context.refresh();
            return context.getBean(CoreProperties.class).getTempDir();
        }
    }
}

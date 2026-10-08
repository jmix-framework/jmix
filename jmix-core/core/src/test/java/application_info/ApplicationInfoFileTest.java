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

package application_info;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jmix.core.ApplicationInfoProvider;
import io.jmix.core.CoreProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import test_support.TestCoreProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationInfoFileTest {

    @TempDir
    Path tempDir;

    @Test
    void testDetectsTestFrameworkLaunch() {
        TestableApplicationInfoProvider provider = createProvider(true);

        assertThat(provider.detectsTestLaunch()).isTrue();
    }

    @Test
    void testFileNotCreatedWhenLaunchedFromTest() {
        TestableApplicationInfoProvider provider = createProvider(true);

        startApplication(provider);

        assertThat(fileLocation()).doesNotExist();
        assertThat(provider.hookRegistrations).isZero();
    }

    @Test
    void testFileCreatedWithInstanceIdWhenAppLaunchedNormally() throws IOException {
        TestableApplicationInfoProvider provider = createProvider(true);
        provider.testLaunchOverride = false;

        startApplication(provider);

        assertThat(fileLocation()).exists();
        assertThat(provider.hookRegistrations).isEqualTo(1);

        JsonObject root = readFileJson();
        assertThat(root.get("instanceId").getAsString()).isNotBlank();
        JsonObject general = root.getAsJsonObject("general");
        assertThat(general.get("host").getAsString()).isEqualTo("localhost");
        assertThat(general.get("port").getAsInt()).isEqualTo(8080);
    }

    @Test
    void testFileNotCreatedWhenDisabled() {
        TestableApplicationInfoProvider provider = createProvider(false);
        provider.testLaunchOverride = false;

        startApplication(provider);

        assertThat(fileLocation()).doesNotExist();
        assertThat(provider.hookRegistrations).isZero();
    }

    @Test
    void testShutdownRemovesOwnFile() {
        TestableApplicationInfoProvider provider = createProvider(true);
        provider.testLaunchOverride = false;
        startApplication(provider);

        provider.runShutdownHook();

        assertThat(fileLocation()).doesNotExist();
    }

    @Test
    void testShutdownKeepsFileOfAnotherInstance() throws IOException {
        TestableApplicationInfoProvider provider = createProvider(true);
        provider.testLaunchOverride = false;
        startApplication(provider);

        writeFileContent("{\"instanceId\": \"another-instance\", \"general\": {}}");

        provider.runShutdownHook();

        assertThat(fileLocation()).exists();
    }

    @Test
    void testShutdownKeepsUnparsableFile() throws IOException {
        TestableApplicationInfoProvider provider = createProvider(true);
        provider.testLaunchOverride = false;
        startApplication(provider);

        writeFileContent("not a json");

        provider.runShutdownHook();

        assertThat(fileLocation()).exists();
    }

    @Test
    void testWriteFailureRegistersNoHookAndDoesNotFail() throws IOException {
        // A regular file in place of the parent directory makes the write fail with an IOException
        // on any OS, standing in for real-world failures like missing permissions in a container.
        Path blocker = tempDir.resolve("blocker");
        Files.createFile(blocker);

        CoreProperties properties = TestCoreProperties.builder().build();
        TestableApplicationInfoProvider provider =
                new TestableApplicationInfoProvider(blocker.resolve("app-info.json"), properties);
        provider.testLaunchOverride = false;

        startApplication(provider);

        assertThat(provider.hookRegistrations).isZero();
    }

    @Test
    void testShutdownWithoutCreatedFileKeepsExistingFile() throws IOException {
        writeFileContent("{\"instanceId\": \"another-instance\", \"general\": {}}");

        TestableApplicationInfoProvider provider = createProvider(false);
        provider.testLaunchOverride = false;
        startApplication(provider);

        provider.runShutdownHook();

        assertThat(fileLocation()).exists();
    }

    private TestableApplicationInfoProvider createProvider(boolean applicationInfoFileEnabled) {
        CoreProperties properties = TestCoreProperties.builder()
                .setApplicationInfoFileEnabled(applicationInfoFileEnabled)
                .build();
        return new TestableApplicationInfoProvider(fileLocation(), properties);
    }

    private void startApplication(TestableApplicationInfoProvider provider) {
        provider.onApplicationReady(new ApplicationReadyEvent(
                new SpringApplication(Object.class), new String[0], new StaticApplicationContext(), Duration.ZERO));
    }

    private Path fileLocation() {
        return tempDir.resolve("app-info.json");
    }

    private JsonObject readFileJson() throws IOException {
        String content = Files.readString(fileLocation(), StandardCharsets.UTF_8);
        return JsonParser.parseString(content).getAsJsonObject();
    }

    private void writeFileContent(String content) throws IOException {
        Files.writeString(fileLocation(), content, StandardCharsets.UTF_8);
    }

    private static class TestableApplicationInfoProvider extends ApplicationInfoProvider {

        final Path fileLocation;
        int hookRegistrations;
        Boolean testLaunchOverride;

        TestableApplicationInfoProvider(Path fileLocation, CoreProperties properties) {
            this.fileLocation = fileLocation;
            this.environment = new MockEnvironment();
            this.coreProperties = properties;
        }

        @Override
        protected Path resolveAppInfoFileLocation() {
            return fileLocation;
        }

        @Override
        protected void registerShutdownHook() {
            hookRegistrations++;
        }

        @Override
        protected boolean isLaunchedFromTest() {
            return testLaunchOverride != null ? testLaunchOverride : super.isLaunchedFromTest();
        }

        boolean detectsTestLaunch() {
            return super.isLaunchedFromTest();
        }

        void runShutdownHook() {
            shutdownHookOperation();
        }
    }
}

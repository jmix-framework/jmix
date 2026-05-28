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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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

    @Test
    void testHotDeployReloadsClassAfterFileChange() throws Exception {
        String className = "hot.Sample";
        Path classFile = tempDir.resolve("hot/Sample.class");
        Files.createDirectories(classFile.getParent());

        Files.write(classFile, generateEmptyClassBytes(className));
        // Ensure the first load uses a strictly earlier modification time than the rewrite below.
        classFile.toFile().setLastModified(System.currentTimeMillis() - 5000);

        JavaClassLoader classLoader = createClassLoader(true);
        try {
            Class<?> first = classLoader.loadClass(className, false);

            Files.write(classFile, generateEmptyClassBytes(className));
            classFile.toFile().setLastModified(System.currentTimeMillis());

            Class<?> second = classLoader.loadClass(className, false);

            assertThat(second).isNotSameAs(first);
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
        ReflectionTestUtils.setField(classLoader, "timeSource", new TimeSourceImpl());
        return classLoader;
    }

    /**
     * Generates bytes for a minimal class file with the given fully-qualified name (no methods, no fields,
     * extends java.lang.Object).
     */
    private byte[] generateEmptyClassBytes(String fqn) throws Exception {
        String internalName = fqn.replace('.', '/');
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeInt(0xCAFEBABE);
            out.writeShort(0);          // minor version
            out.writeShort(52);         // major version (Java 8)
            out.writeShort(7);          // constant pool count (size 6 + 1)
            // #1 Utf8 "<class internal name>"
            out.writeByte(1);
            out.writeUTF(internalName);
            // #2 Class -> #1
            out.writeByte(7);
            out.writeShort(1);
            // #3 Utf8 "java/lang/Object"
            out.writeByte(1);
            out.writeUTF("java/lang/Object");
            // #4 Class -> #3
            out.writeByte(7);
            out.writeShort(3);
            // #5 Utf8 "<init>"
            out.writeByte(1);
            out.writeUTF("<init>");
            // #6 Utf8 "()V"
            out.writeByte(1);
            out.writeUTF("()V");
            out.writeShort(0x0021);     // access flags: ACC_PUBLIC | ACC_SUPER
            out.writeShort(2);          // this_class -> #2
            out.writeShort(4);          // super_class -> #4
            out.writeShort(0);          // interfaces count
            out.writeShort(0);          // fields count
            out.writeShort(0);          // methods count
            out.writeShort(0);          // attributes count
        }
        return baos.toByteArray();
    }

    private void writeInvalidClassFile() throws Exception {
        Path classFile = tempDir.resolve("java/lang/String.class");
        Files.createDirectories(classFile.getParent());
        Files.write(classFile, new byte[]{0x00, 0x01, 0x02});
    }
}

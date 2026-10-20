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

package io.jmix.flowui.theme.bundler;

/**
 * Thrown when a theme stylesheet cannot be bundled. The message starts with the offending file's path relative to
 * the themes root and the line number of the offending statement: {@code jmix-lumo/jmix-lumo.css:12: ...}.
 */
public class ThemeCssBundleException extends RuntimeException {

    public ThemeCssBundleException(String file, int line, String message) {
        super(file + ":" + line + ": " + message);
    }
}

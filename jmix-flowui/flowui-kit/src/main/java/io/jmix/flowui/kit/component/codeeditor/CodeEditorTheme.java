/*
 * Copyright 2023 Haulmont.
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
package io.jmix.flowui.kit.component.codeeditor;

import jakarta.annotation.Nullable;

import java.util.Objects;

public enum CodeEditorTheme {

    AMBIANCE("ambiance"),
    CHAOS("chaos"),
    CHROME("chrome"),
    CLOUD_9_DAY("cloud9_day"),
    CLOUD_9_NIGHT("cloud9_night"),
    CLOUD_9_NIGHT_LOW_COLOR("cloud9_night_low_color"),
    CLOUDS("clouds"),
    CLOUDS_MIDNIGHT("clouds_midnight"),
    COBALT("cobalt"),
    CRIMSON_EDITOR("crimson_editor"),
    DAWN("dawn"),
    DRACULA("dracula"),
    DREAMWEAVER("dreamweaver"),
    ECLIPSE("eclipse"),
    GITHUB("github"),
    GOB("gob"),
    GRUVBOX("gruvbox"),
    GRUVBOX_DARK_HARD("gruvbox_dark_hard"),
    GRUVBOX_LIGHT_HARD("gruvbox_light_hard"),
    IDLE_FINGERS("idle_fingers"),
    IPLASTIC("iplastic"),
    KATZENMILCH("katzenmilch"),
    KR_THEME("kr_theme"),
    KUROIR("kuroir"),
    MERBIVORE("merbivore"),
    MERBIVORE_SOFT("merbivore_soft"),
    MONO_INDUSTRIAL("mono_industrial"),
    MONOKAI("monokai"),
    NORD_DARK("nord_dark"),
    ONE_DARK("one_dark"),
    PASTEL_ON_DARK("pastel_on_dark"),
    SOLARIZED_DARK("solarized_dark"),
    SOLARIZED_LIGHT("solarized_light"),
    SQLSERVER("sqlserver"),
    TERMINAL("terminal"),
    TEXTMATE("textmate"),
    TOMORROW("tomorrow"),
    TOMORROW_NIGHT("tomorrow_night"),
    TOMORROW_NIGHT_BLUE("tomorrow_night_blue"),
    TOMORROW_NIGHT_BRIGHT("tomorrow_night_bright"),
    TOMORROW_NIGHT_EIGHTIES("tomorrow_night_eighties"),
    TWILIGHT("twilight"),
    VIBRANT_INK("vibrant_ink"),
    XCODE("xcode");

    private final String id;

    CodeEditorTheme(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    @Nullable
    public static CodeEditorTheme fromId(String id) {
        for (CodeEditorTheme theme : values()) {
            if (Objects.equals(theme.getId(), id)) {
                return theme;
            }
        }

        return null;
    }
}

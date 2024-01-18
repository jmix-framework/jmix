/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.server.frontend.TypeScriptBootstrapModifier;
import com.vaadin.flow.server.frontend.scanner.FrontendDependenciesScanner;
import com.vaadin.flow.theme.ThemeDefinition;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.jmix.flowui.devserver.frontend.FrontendUtils.BOOTSTRAP_FILE_NAME;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.FEATURE_FLAGS_FILE_NAME;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.GENERATED;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.INDEX_JS;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.INDEX_TS;
import static io.jmix.flowui.devserver.frontend.FrontendUtils.INDEX_TSX;

/**
 * A task for generating the bootstrap file
 * {@link FrontendUtils#BOOTSTRAP_FILE_NAME} during `package` Maven goal.
 */
public class TaskGenerateBootstrap extends AbstractTaskClientGenerator {

    static final String DEV_TOOLS_IMPORT = String.format(
            "import '%svaadin-dev-tools.js';%n",
            FrontendUtils.JAR_RESOURCES_IMPORT + "vaadin-dev-tools/");
    private final FrontendDependenciesScanner frontDeps;

    private final Options options;
    private List<TypeScriptBootstrapModifier> modifiers;
    private final ThemeDefinition themeDef;

    TaskGenerateBootstrap(FrontendDependenciesScanner frontDeps,
                          Options options,
                          ThemeDefinition themeDef) {
        this.frontDeps = frontDeps;
        this.options = options;
        this.themeDef = themeDef;
        this.modifiers = new ArrayList<>();
        for (Class<? extends TypeScriptBootstrapModifier> modifierClass : options
                .getClassFinder()
                .getSubTypesOf(TypeScriptBootstrapModifier.class)) {
            try {
                this.modifiers
                        .add(modifierClass.getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException
                     | IllegalArgumentException | InvocationTargetException
                     | NoSuchMethodException | SecurityException e) {
                String msg = "Failed to instantiate TypeScriptBootstrapModifier";
                FrontendUtils.logInFile(msg + "\n" + e);
                LoggerFactory.getLogger(TaskGenerateBootstrap.class).error(msg, e);
            }
        }
    }

    @Override
    protected String getFileContent() {
        List<String> lines = new ArrayList<>();
        lines.add(String.format("import './%s';%n", FEATURE_FLAGS_FILE_NAME));
        lines.add(String.format("import '%s';%n", getIndexTsEntryPath()));
        if (false) { // Hide vaadin dev tools
            lines.add(DEV_TOOLS_IMPORT);
        }
        lines.addAll(getThemeLines());

        for (TypeScriptBootstrapModifier modifier : modifiers) {
            modifier.modify(lines, options.isProductionMode());
        }
        return String.join(System.lineSeparator(), lines);
    }

    @Override
    protected File getGeneratedFile() {
        File frontendGeneratedDirectory = new File(
                options.getFrontendDirectory(), GENERATED);
        return new File(frontendGeneratedDirectory, BOOTSTRAP_FILE_NAME);
    }

    @Override
    protected boolean shouldGenerate() {
        return frontDeps != null;
    }

    private String getIndexTsEntryPath() {
        File frontendDirectory = options.getFrontendDirectory();
        boolean hasCustomIndexFile = new File(frontendDirectory, INDEX_TS)
                .exists() || new File(frontendDirectory, INDEX_JS).exists()
                || new File(frontendDirectory, INDEX_TSX).exists();
        if (hasCustomIndexFile) {
            return "../index";
        } else {
            return "./index";
        }
    }

    private Collection<String> getThemeLines() {
        Collection<String> lines = new ArrayList<>();
        if (themeDef != null && !"".equals(themeDef.getName())) {
            lines.add("import './theme-" + themeDef.getName() + ".global.generated.js';");
            lines.add("import { applyTheme } from './theme.js';");
            lines.add("applyTheme(document);");
            lines.add("");
        }

        return lines;
    }
}

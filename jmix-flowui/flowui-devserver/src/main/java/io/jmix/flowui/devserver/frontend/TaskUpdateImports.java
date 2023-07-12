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

import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.frontend.FrontendTools;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;
import com.vaadin.flow.server.frontend.scanner.FrontendDependenciesScanner;
import com.vaadin.flow.theme.AbstractTheme;
import com.vaadin.flow.theme.ThemeDefinition;
import io.jmix.flowui.devserver.frontend.scanner.ThemeWrapper;
import org.slf4j.Logger;

/**
 * An updater that it's run when the servlet context is initialised in dev-mode
 * or when flow-maven-plugin goals are run in order to update Flow imports file
 * and "jar-resources" contents by visiting all classes with JsModule and Theme annotations.
 */
public class TaskUpdateImports extends NodeUpdater {

    private final AbstractTheme theme;

    private class UpdateMainImportsFile extends AbstractUpdateImports {
        UpdateMainImportsFile(ClassFinder classFinder, Options options,
                              FrontendDependenciesScanner scanner) {
            super(options, scanner, classFinder, theme);
        }

        @Override
        protected Logger getLogger() {
            return log();
        }

        @Override
        protected String getImportsNotFoundMessage() {
            return getAbsentPackagesMessage();
        }

    }

    /**
     * Create an instance of the updater given all configurable parameters.
     */
    TaskUpdateImports(ClassFinder finder,
                      FrontendDependenciesScanner frontendDepScanner,
                      Options options, ThemeDefinition themeDefinition) {
        super(finder, frontendDepScanner, options);

        AbstractTheme abstractTheme = null;
        try {
            abstractTheme = themeDefinition != null ? new ThemeWrapper(themeDefinition.getTheme()) : null;
        } catch (InstantiationException | IllegalAccessException ignored) {
        }
        this.theme = abstractTheme;
    }

    @Override
    public void execute() {
        UpdateMainImportsFile mainUpdate = new UpdateMainImportsFile(finder,
                options, frontDeps);
        mainUpdate.run();
    }

    private String getAbsentPackagesMessage() {
        String lockFile = options.isEnablePnpm() ? "pnpm-lock.yaml"
                : Constants.PACKAGE_LOCK_JSON;
        String command = options.isEnablePnpm() ? "pnpm" : "npm";
        String note = "";
        if (options.isEnablePnpm()) {
            note = "\nMake sure first that `pnpm` command is installed, otherwise you should install it using npm: `npm add -g pnpm@"
                    + FrontendTools.DEFAULT_PNPM_VERSION + "`";
        }
        return String.format(
                "If the build fails, check that npm packages are installed.\n\n"
                        + "  To fix the build remove `%s` and `node_modules` directory to reset modules.\n"
                        + "  In addition you may run `%s install` to fix `node_modules` tree structure.%s",
                lockFile, command, note);
    }
}


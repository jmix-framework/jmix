/*
 * Copyright 2022 Haulmont.
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

/**
 * NOTICE: this is an auto-generated file
 *
 * This file has been generated for `pnpm install` task.
 * It is used to pin client side dependencies.
 * This file will be overwritten on every run.
 */

const fs = require('fs');

const versionsFile = '[to-be-generated-by-flow]';

if (!fs.existsSync(versionsFile)) {
    return;
}
const versions = JSON.parse(fs.readFileSync(versionsFile, 'utf-8'));

module.exports = {
    hooks: {
        readPackage
    }
};

function readPackage(pkg) {
    const { dependencies } = pkg;

    if (dependencies) {
        for (let k in versions) {
            if (dependencies[k] && dependencies[k] !== versions[k]) {
                pkg.dependencies[k] = versions[k];
            }
        }
    }

    // Forcing chokidar version for now until new babel version is available
    // check out https://github.com/babel/babel/issues/11488
    if (pkg.dependencies.chokidar) {
        pkg.dependencies.chokidar = '^3.4.0';
    }

    return pkg;
}

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

const loaderUtils = require('loader-utils');
const fs = require('fs');
const path = require('path');
const { rewriteCssUrls } = require('./theme-loader-utils');

/**
 * This custom loader handles rewriting urls for the application theme css files.
 * URLs starting with ./ or ../ are checked against the filesystem and converted if a file exists.
 * URLs going outside of the application theme folder are not accepted and will not be rewritten.
 *
 * @param source file contents to handle
 * @param map source map for file
 */
module.exports = function (source, map) {
  const options = loaderUtils.getOptions(this);
  const handledResourceFolder = path.dirname(this._module.resource);
  const logger = this.getLogger('theme-loader');

  let themeFolder = handledResourceFolder;
  // Recurse up until we find the themes folder or don't have 'themes' on the path.
  while (themeFolder.indexOf('themes') > 1 && path.basename(path.resolve(themeFolder, '..')) !== 'themes') {
    themeFolder = path.resolve(themeFolder, '..');
  }
  // If we have found no themes folder return without doing anything.
  if (path.basename(path.resolve(themeFolder, '..')) !== 'themes') {
    this.callback(null, source, map);
    return;
  }

  logger.log("Using '", themeFolder, "' for the application theme base folder.");

  source = rewriteCssUrls(source, handledResourceFolder, themeFolder, logger, options);
  this.callback(null, source, map);
};

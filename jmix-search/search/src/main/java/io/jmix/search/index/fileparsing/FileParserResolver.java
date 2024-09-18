/*
 * Copyright 2024 Haulmont.
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

package io.jmix.search.index.fileparsing;

import io.jmix.core.FileRef;
import org.apache.tika.parser.Parser;

import java.util.List;

/**
 * Is a part of the extendable engine the gives an ability to implement custom file parser resolvers and to support
 * custom file types or to modify behavior of existing file parser resolvers.
 */
public interface FileParserResolver {

    /**
     * This method should return the description that describes the constraints or the constraint for the files
     * that are supported with this resolver. This message is used for generating the log message that
     * is written into the log while no one of the resolvers supports the processed file.
     *
     * @return criteria description
     */
    String getCriteriaDescription();

    /**
     * Returns an instance of a file parser for the supported file types.
     *
     * @return an instance of a file parser
     */
    Parser getParser();

    /**
     * This method should implement the logic for checking
     * if the file with given fileRef is supported by the resolver or not.
     *
     * @param fileRef object with the file information
     * @return the given FileRef's checking result
     */
    boolean supports(FileRef fileRef);
}

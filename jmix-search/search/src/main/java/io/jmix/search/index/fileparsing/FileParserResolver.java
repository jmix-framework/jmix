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

/**
 * Interface to be implemented for adding a custom file parser resolver
 * or modifying the behavior of the existing file parser resolvers. It gives an ability to define the exact parser
 * for the exact file types with a custom implementation of the file checking logic. These parsers are used to extract
 * file content for sending it to the search server and indexing.
 */
public interface FileParserResolver {

    /**
     * Returns the description of the criteria for the files that are supported with this resolver.
     * This message is used for generating the log message that is written into the log
     * while no one of the resolvers supports the processing file.
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
     * Returns the result of the checking if the file with the given fileRef is supported by the resolver or not.
     *
     * @param fileRef object with the file information
     * @return the given FileRef's checking result
     */
    boolean supports(FileRef fileRef);
}

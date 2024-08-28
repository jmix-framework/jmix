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

package io.jmix.search.index.impl;

public class IndexMappingComparatorTestCase {
    private final String name;
    private final String folderWithFiles;
    private final IndexMappingComparator.MappingComparingResult expectedResult;

    public static IndexMappingComparatorTestCase testCase(String name, String folderWithFiles, IndexMappingComparator.MappingComparingResult expectedResult){
        return new IndexMappingComparatorTestCase(name, folderWithFiles, expectedResult);
    }

    private IndexMappingComparatorTestCase(String name, String folderWithFiles, IndexMappingComparator.MappingComparingResult expectedResult) {
        this.name = name;
        this.folderWithFiles = folderWithFiles;
        this.expectedResult = expectedResult;
    }

    public String getName() {
        return name;
    }

    public String getFolderWithFiles() {
        return folderWithFiles;
    }

    public IndexMappingComparator.MappingComparingResult getExpectedResult() {
        return expectedResult;
    }

    @Override
    public String toString() {
        return name;
    }
}

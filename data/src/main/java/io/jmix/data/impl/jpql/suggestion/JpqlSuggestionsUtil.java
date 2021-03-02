/*
 * Copyright 2021 Haulmont.
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

package io.jmix.data.impl.jpql.suggestion;

import io.jmix.data.impl.jpql.ErrorRec;
import io.jmix.data.impl.jpql.InferredType;
import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JpqlSuggestionsUtil {

    private static final String[] ARITHMETIC_OPERATIONS = {"+", "-", "*", "/"};
    private static final Pattern COLLECTION_MEMBER_PATTERN =
            Pattern.compile(".*\\sin\\s*[(]\\s*[a-zA-Z0-9]+[.][a-zA-Z0-9.]*$", Pattern.DOTALL);
    private static final Pattern JOIN_PATTERN =
            Pattern.compile(".*\\sjoin\\s*[a-zA-Z0-9]+[.][a-zA-Z0-9.]*$", Pattern.DOTALL);
    private static final Pattern ALIAS_PATTERN = Pattern.compile("as\\s+\"?([\\w|\\d|_|\\.]+)\"?\\s*");
    private static final String FROM_KEYWORD = "from";

    private JpqlSuggestionsUtil() {
    }

    /**
     * Returns word in query denoting entity or field parameter user have requested hint for
     *
     * @param query         query string
     * @param caretPosition caret position
     * @return matched word or empty string
     */
    static String getLastWord(String query, int caretPosition) {
        if (caretPosition < 0)
            return "";

        if (Character.isSpaceChar(query.charAt(caretPosition))) {
            return "";
        }

        String[] words = query.substring(0, caretPosition + 1).split("\\s");
        String result = words[words.length - 1];

        if (StringUtils.isBlank(result)) {
            return result;
        }

        int leftBracketsIdx = result.lastIndexOf('(');
        if (leftBracketsIdx >= 0 && leftBracketsIdx < result.length()) {
            result = result.substring(leftBracketsIdx + 1);
        }

        result = getLastWordWithArithmeticOperation(result);

        return result;
    }

    private static String getLastWordWithArithmeticOperation(String word) {
        if (!word.contains("'")) {
            int operationIdx = StringUtils.lastIndexOfAny(word, ARITHMETIC_OPERATIONS);
            if (operationIdx >= 0 && operationIdx < word.length()) {
                return word.substring(operationIdx + 1);
            }
        }
        return word;
    }

    static Set<InferredType> getExpectedTypes(String query, int position) {
        if (position >= 0 && position < query.length() && query.charAt(position) == ' ') {
            return EnumSet.of(InferredType.Any);
        }

        String matchingInput = query.substring(0, position + 1);

        Matcher matcher = COLLECTION_MEMBER_PATTERN.matcher(matchingInput);
        if (matcher.matches()) {
            return EnumSet.of(InferredType.Collection, InferredType.Entity);
        }

        matcher = JOIN_PATTERN.matcher(matchingInput);
        if (matcher.matches()) {
            return EnumSet.of(InferredType.Collection, InferredType.Entity);
        }

        return EnumSet.of(InferredType.Any);
    }

    static boolean isSuggestionByField(String lastWord) {
        return lastWord.contains(".");
    }

    static boolean isSuggestionByParameter(String lastWord) {
        return Objects.equals(lastWord, ":");
    }

    static List<String> formatErrorMessages(List<ErrorRec> errorRecs) {
        List<String> errorMessages = new ArrayList<>();
        for (ErrorRec errorRec : errorRecs) {
            StringBuilder queryPart = new StringBuilder();
            for (Object child : errorRec.node.getChildren()) {
                CommonTree childNode = (CommonTree) child;
                queryPart.append(childNode.getText());
            }
            errorMessages.add(String.format("Error near: \"{%s}\"", queryPart));
        }
        return errorMessages;
    }

    static QueryWithPosition removeAliases(String query, int position) {
        String resultQuery = query;
        int resultPosition = position;

        int fromIndex = StringUtils.indexOfIgnoreCase(query, FROM_KEYWORD);

        Matcher matcher = ALIAS_PATTERN.matcher(query);
        while (matcher.find()) {
            String alias = matcher.group();

            int regionStart = matcher.start();
            int regionEnd = matcher.end();

            if (regionEnd <= fromIndex || fromIndex == -1) {
                if (resultPosition > regionEnd) {
                    resultPosition = resultPosition - alias.length();
                    resultQuery = matcher.replaceFirst("");
                    matcher.reset(resultQuery);
                } else if (resultPosition > regionStart) {
                    resultPosition = regionStart - 1;
                    resultQuery = matcher.replaceFirst("");
                    matcher.reset(resultQuery);
                } else {
                    resultQuery = matcher.replaceFirst("");
                    matcher.reset(resultQuery);
                }
                fromIndex = StringUtils.indexOfIgnoreCase(query, FROM_KEYWORD);
            }
        }

        return new QueryWithPosition(resultQuery, resultPosition);
    }

    static class QueryWithPosition {
        private final String query;
        private final int position;

        public QueryWithPosition(String query, int position) {
            this.query = query;
            this.position = position;
        }

        public String getQuery() {
            return query;
        }

        public int getPosition() {
            return position;
        }
    }
}

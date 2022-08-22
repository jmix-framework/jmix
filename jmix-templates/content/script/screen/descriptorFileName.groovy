def appendHyphen(char[] chars, int length, StringBuilder result, int i) {
    if (i == 0) return

    char prevChar = chars[i - 1]
    if (prevChar == ('_' as char)) return

    if (Character.isLowerCase(prevChar) || (i != length - 1 && Character.isLowerCase(chars[i + 1]))) {
        result.append('-')
    }
}

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

char[] chars = entity.className.toCharArray()
int length = chars.length
StringBuilder result = new StringBuilder()
for (int i = 0; i < length; i++) {
    char currChar = chars[i]
    if (Character.isUpperCase(currChar)) {
        appendHyphen(chars, length, result, i)
        result.append(Character.toLowerCase(currChar))
    } else {
        result.append(currChar)
    }
}

return result.toString()
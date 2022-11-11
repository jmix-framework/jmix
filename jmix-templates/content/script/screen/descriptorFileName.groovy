def appendHyphen(char[] chars, int length, StringBuilder result, int i) {
    if (i == 0) return

    char prevChar = chars[i - 1]
    if (prevChar == ('_' as char)) return

    if (Character.isLowerCase(prevChar) || (i != length - 1 && Character.isLowerCase(chars[i + 1]))) {
        result.append('-')
    }
}

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
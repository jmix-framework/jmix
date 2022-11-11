String result = "caption"

messages.find { message ->
    String messageText = message.key.tokenize('/')[-1]
    if (messageText.toLowerCase().endsWith("caption")) {
        result = message.key
        return true
    }
    return false
}
return result
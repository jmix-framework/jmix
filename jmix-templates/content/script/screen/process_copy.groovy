String result = content

messages.each { message ->
    String messageText = message.key.tokenize('/')[-1]
    result = result.replaceAll("msg://".concat(messageText), "msg://".concat(message.key))
}
return result
const { messages } = require("./sub/foo/module5")

exports.messages = {
    ...messages,
    'module3': 'module 3'
}
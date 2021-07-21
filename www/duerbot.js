/*global cordova, module*/

module.exports = {
    register: function (botId, signatureKey, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "DuerBotPlugin", "register", [botId, signatureKey]);
    },
    speak: function (txt, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "DuerBotPlugin", "speak", [txt]);
    },
    addDuerBotHandler: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "DuerBotPlugin", "addDuerBotHandler", []);
    },
    removeDuerBotHandler: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "DuerBotPlugin", "removeDuerBotHandler", []);
    }
};

//调用方式  window.AppUpdate.checkAppUpdate();
var exec = require('cordova/exec');

exports.checkAppUpdate = function(success, error, updateUrl) {
    updateUrl = updateUrl ? [updateUrl] : [];
    exec(success, error, "AppUpdate", "checkAppUpdate", updateUrl);
};
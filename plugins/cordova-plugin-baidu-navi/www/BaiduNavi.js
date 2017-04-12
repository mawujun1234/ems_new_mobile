var exec = require('cordova/exec');

exports.navi = function(success,error,str) {
    cordova.exec(success, error, "BaiduNavi", "navi", [str]);
};
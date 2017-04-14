var exec = require('cordova/exec');

exports.navi = function(success,error,paramArray) {
    cordova.exec(success, error, "BaiduNavi", "navi", paramArray);
};
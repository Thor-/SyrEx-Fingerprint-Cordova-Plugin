/**
 * Status codes:
 * UNKNOWN = -100;
 * OK = 0;
 * CANCEL = 1;
 * ERROR = -1; //DATA_CHECK_ERR //data check is error
 * DATA_CHECK_ERR       = -1; //data check is error
 * DATA_NOT_END_ERR     = -2; //data without finish mark
 * CMD_ERR_OR_BUSY      = -3; //command return error or transfer busy
 * BT_RECONNECT_OR_BUSY = -4; //bluetooth disconnect, reconnect or transfer busy
 * DATA_LENGTH_ERR       = -5; //data length is error
 * ALGORITHM_VERIFY_ERR = -6; //algorithm check error
 * GEN_TEMPLATE_ERR      = -7; //fingerprint image synthetise template is failed
 * GET_FEATURE_ERR       = -8; //fingerprint image extract
 */

var exec = require('cordova/exec');

function Fingerprint() {
}

Fingerprint.prototype.getTemplate = function(success, error){
    return exec(success, error, "Fingerprint", 'getTemplate', []);
};

Fingerprint.prototype.getFeature = function(success, error){
    return exec(success, error, "Fingerprint", 'getFeature', []);
};

Fingerprint.prototype.closeBluetooth = function(success, error){
    return exec(success, error, "Fingerprint", 'closeBluetooth', []);
};

Fingerprint.prototype.match = function(success, error, feature, template){
    return exec(success, error, "Fingerprint", 'match', [feature, template]);
};

var fingerprint = new Fingerprint(); 
module.exports = fingerprint

window.cordova.plugins.fingerprint = fingerprint
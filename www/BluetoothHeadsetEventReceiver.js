var exec = require( 'cordova/exec' );

exports.registerReceiver = function( success, error ) {
    exec( success, error, 'BluetoothHeadsetEventReceiver', 'registerReceiver', [] );
};

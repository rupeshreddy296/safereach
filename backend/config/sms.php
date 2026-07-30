<?php
// backend/config/sms.php

return [
    // Active gateway: 'twilio', 'fast2sms', 'msg91', or 'sns'
    'gateway' => 'twilio',

    // Twilio Settings
    'twilio' => [
        'sid' => '',       // Put your real Twilio Account SID here
        'token' => '',     // Put your real Twilio Auth Token here
        'from' => ''       // Put your Twilio virtual number here (e.g. +1877XXXXXXX)
    ],

    // Fast2SMS Settings
    'fast2sms' => [
        'api_key' => ''
    ],

    // MSG91 Settings
    'msg91' => [
        'auth_key' => '',
        'template_id' => ''
    ]
];
?>

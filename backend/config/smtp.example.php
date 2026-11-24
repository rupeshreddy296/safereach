<?php
// backend/config/smtp.example.php
// Template for SafeReach SMTP Email Configuration

return [
    'enabled'    => true,
    'host'       => 'smtp.gmail.com',
    'port'       => 587,                   // 587 (TLS) or 465 (SSL)
    'secure'     => 'tls',                 // 'tls' or 'ssl'
    'username'   => 'your-email@gmail.com',
    'password'   => 'your-16-char-app-password',
    'from_email' => 'your-email@gmail.com',
    'from_name'  => 'SafeReach Emergency Response',
    'timeout'    => 5,
];

<?php
// backend/helpers/sms.php

require_once __DIR__ . '/../config/db.php';

class SMSGateway {
    /**
     * Sends an SMS message to a specific phone number using the configured gateway.
     * Logs the transaction details in the sms_logs table.
     *
     * @param string $phoneNumber Recipient phone number (e.g. +919999999999)
     * @param string $message Message content
     * @return array Result summary: ['success' => bool, 'gateway' => string, 'status' => string, 'response' => string]
     */
    public static function send($phoneNumber, $message) {
        $configFile = __DIR__ . '/../config/sms.php';
        $config = [];
        if (file_exists($configFile)) {
            $config = include $configFile;
        }

        $gateway = isset($config['gateway']) ? $config['gateway'] : 'twilio';
        $status = 'failed';
        $responseRaw = '';

        if ($gateway === 'twilio') {
            $sid = isset($config['twilio']['sid']) ? trim($config['twilio']['sid']) : '';
            $token = isset($config['twilio']['token']) ? trim($config['twilio']['token']) : '';
            $from = isset($config['twilio']['from']) ? trim($config['twilio']['from']) : '';

            if (!empty($sid) && !empty($token) && !empty($from)) {
                $url = "https://api.twilio.com/2010-04-01/Accounts/$sid/Messages.json";
                $postData = [
                    'To' => $phoneNumber,
                    'From' => $from,
                    'Body' => $message
                ];

                $ch = curl_init();
                curl_setopt($ch, CURLOPT_URL, $url);
                curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
                curl_setopt($ch, CURLOPT_POST, true);
                curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($postData));
                curl_setopt($ch, CURLOPT_USERPWD, "$sid:$token");
                curl_setopt($ch, CURLOPT_TIMEOUT, 10);
                curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false); // For local XAMPP SSL configurations

                $responseRaw = curl_exec($ch);
                $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
                curl_close($ch);

                if ($httpCode >= 200 && $httpCode < 300) {
                    $status = 'sent';
                } else {
                    $status = 'failed';
                }
            } else {
                $responseRaw = 'Twilio settings not configured in backend/config/sms.php. Mocking SMS dispatch.';
                $status = 'failed';
            }
        } elseif ($gateway === 'fast2sms') {
            $apiKey = isset($config['fast2sms']['api_key']) ? trim($config['fast2sms']['api_key']) : '';
            if (!empty($apiKey)) {
                $url = "https://www.fast2sms.com/dev/bulkV2";
                // Fast2SMS accepts route: 'q' (quick), numbers, message
                $postData = [
                    'route' => 'q',
                    'message' => $message,
                    'language' => 'english',
                    'numbers' => str_replace('+', '', $phoneNumber) // Remove + for local numbers
                ];

                $ch = curl_init();
                curl_setopt($ch, CURLOPT_URL, $url);
                curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
                curl_setopt($ch, CURLOPT_POST, true);
                curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($postData));
                curl_setopt($ch, CURLOPT_HTTPHEADER, [
                    "authorization: $apiKey",
                    "Content-Type: application/json"
                ]);
                curl_setopt($ch, CURLOPT_TIMEOUT, 10);
                curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);

                $responseRaw = curl_exec($ch);
                $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
                curl_close($ch);

                if ($httpCode >= 200 && $httpCode < 300) {
                    $status = 'sent';
                } else {
                    $status = 'failed';
                }
            } else {
                $responseRaw = 'Fast2SMS API key not configured. Mocking SMS dispatch.';
                $status = 'failed';
            }
        } else {
            $responseRaw = 'Unknown or unsupported gateway: ' . $gateway;
            $status = 'failed';
        }

        // Always log to sms_logs table
        try {
            $database = new Database();
            $db = $database->getConnection();
            $logQuery = "INSERT INTO sms_logs (phone_number, message, gateway, status, response) VALUES (?, ?, ?, ?, ?)";
            $logStmt = $db->prepare($logQuery);
            $logStmt->execute([$phoneNumber, $message, $gateway, $status, $responseRaw]);
        } catch (Exception $e) {
            // Keep going even if DB logging fails
            error_log("Failed to write to sms_logs: " . $e->getMessage());
        }

        return [
            'success' => ($status === 'sent'),
            'gateway' => $gateway,
            'status' => $status,
            'response' => $responseRaw
        ];
    }
}
?>

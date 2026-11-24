<?php
// backend/helpers/mailer.php

class Mailer {
    /**
     * Send an OTP email using configured SMTP settings.
     *
     * @param string $recipientEmail Target email address
     * @param string $otpCode 6-digit verification code
     * @param string $purpose e.g. "Password Reset", "Signup Verification", "Login"
     * @param string $recipientName Optional user name
     * @return array ['success' => bool, 'message' => string, 'details' => string]
     */
    public static function sendOTP($recipientEmail, $otpCode, $purpose = "Verification", $recipientName = "User") {
        $configFile = __DIR__ . '/../config/smtp.php';
        if (!file_exists($configFile)) {
            return [
                'success' => false,
                'message' => 'SMTP configuration file not found.',
                'otp'     => $otpCode
            ];
        }

        $config = include $configFile;
        if (empty($config['enabled'])) {
            return [
                'success' => false,
                'message' => 'SMTP is disabled in configuration.',
                'otp'     => $otpCode
            ];
        }

        $subject = "SafeReach — Your $purpose Code: $otpCode";

        $htmlBody = "
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset='utf-8'>
          <title>$subject</title>
        </head>
        <body style='margin:0;padding:0;background-color:#0f172a;font-family:Arial,sans-serif;color:#f8fafc;'>
          <div style='max-width:540px;margin:30px auto;background:#1e293b;border-radius:16px;border:1px solid #334155;overflow:hidden;box-shadow:0 10px 25px rgba(0,0,0,0.5);'>
            <div style='background:#dc2626;padding:24px;text-align:center;'>
              <h1 style='margin:0;color:#ffffff;font-size:24px;font-weight:800;letter-spacing:1px;'>SAFEREACH</h1>
              <p style='margin:4px 0 0;color:#fee2e2;font-size:12px;font-weight:600;letter-spacing:0.5px;'>EMERGENCY AMBULANCE DISPATCH &amp; TRACKING</p>
            </div>
            <div style='padding:32px 24px;text-align:center;'>
              <h2 style='margin:0 0 12px;color:#ffffff;font-size:18px;'>$purpose Verification</h2>
              <p style='margin:0 0 24px;color:#94a3b8;font-size:14px;line-height:1.5;'>
                Hello <strong>" . htmlspecialchars($recipientName) . "</strong>,<br>
                Use the one-time verification code below to complete your $purpose request.
              </p>
              <div style='background:#0f172a;border:2px dashed #dc2626;border-radius:12px;padding:18px;margin:0 auto 24px;max-width:260px;'>
                <span style='font-size:32px;font-weight:800;letter-spacing:8px;color:#ef4444;font-family:monospace;'>$otpCode</span>
              </div>
              <p style='margin:0 0 12px;color:#94a3b8;font-size:12px;'>
                ⏱️ This code is valid for <strong>10 minutes</strong>. Do not share it with anyone.
              </p>
              <p style='margin:0;color:#64748b;font-size:11px;'>
                If you did not request this verification code, please ignore this email.
              </p>
            </div>
            <div style='background:#0f172a;padding:16px;text-align:center;border-top:1px solid #334155;'>
              <p style='margin:0;color:#475569;font-size:11px;'>
                &copy; " . date('Y') . " SafeReach Response Network. All rights reserved.
              </p>
            </div>
          </div>
        </body>
        </html>";

        $textBody = "SafeReach - Your $purpose Verification Code is: $otpCode (Valid for 10 minutes).\nDo not share this code with anyone.";

        return self::sendMail($recipientEmail, $subject, $htmlBody, $textBody, $config);
    }

    /**
     * Send email via native PHP SMTP socket.
     */
    public static function sendMail($to, $subject, $htmlBody, $textBody, $config) {
        $host     = $config['host'];
        $port     = $config['port'];
        $username = $config['username'];
        $password = $config['password'];
        $from     = $config['from_email'];
        $fromName = $config['from_name'];
        $timeout  = isset($config['timeout']) ? (int)$config['timeout'] : 5;

        // Try direct SSL connection on 465 or STARTTLS on 587
        $portsToTry = ($port == 465) ? [465, 587] : [587, 465];
        $lastError = "";

        foreach ($portsToTry as $currentPort) {
            try {
                $isSSL = ($currentPort == 465);
                $protocol = $isSSL ? "ssl://" : "tcp://";

                $context = stream_context_create([
                    'ssl' => [
                        'verify_peer'       => false,
                        'verify_peer_name'  => false,
                        'allow_self_signed' => true
                    ]
                ]);

                $socket = @stream_socket_client($protocol . $host . ':' . $currentPort, $errno, $errstr, $timeout, STREAM_CLIENT_CONNECT, $context);

                if (!$socket) {
                    $lastError = "Port $currentPort: $errstr ($errno)";
                    continue;
                }

                stream_set_timeout($socket, $timeout);

                $response = self::readSocket($socket);

                // Handshake
                fwrite($socket, "EHLO localhost\r\n");
                $response = self::readSocket($socket);

                // STARTTLS if using port 587
                if ($currentPort == 587) {
                    fwrite($socket, "STARTTLS\r\n");
                    $response = self::readSocket($socket);
                    if (substr($response, 0, 3) !== '220') {
                        $lastError = "STARTTLS failed: $response";
                        fclose($socket);
                        continue;
                    }
                    if (!@stream_socket_enable_crypto($socket, true, STREAM_CRYPTO_METHOD_TLSv1_2_CLIENT | STREAM_CRYPTO_METHOD_TLSv1_3_CLIENT)) {
                        $lastError = "Crypto negotiation failed on port 587";
                        fclose($socket);
                        continue;
                    }
                    fwrite($socket, "EHLO localhost\r\n");
                    $response = self::readSocket($socket);
                }

                // Authentication
                fwrite($socket, "AUTH LOGIN\r\n");
                $response = self::readSocket($socket);

                fwrite($socket, base64_encode($username) . "\r\n");
                $response = self::readSocket($socket);

                fwrite($socket, base64_encode($password) . "\r\n");
                $response = self::readSocket($socket);

                if (substr($response, 0, 3) !== '235') {
                    $lastError = "SMTP Auth Failed: $response";
                    fclose($socket);
                    continue;
                }

                // Mail From & Rcpt To
                fwrite($socket, "MAIL FROM: <$from>\r\n");
                $response = self::readSocket($socket);

                fwrite($socket, "RCPT TO: <$to>\r\n");
                $response = self::readSocket($socket);

                // Data
                fwrite($socket, "DATA\r\n");
                $response = self::readSocket($socket);

                $boundary = "----=_NextPart_" . md5(uniqid(time()));
                $headers  = "From: =?UTF-8?B?" . base64_encode($fromName) . "?= <$from>\r\n";
                $headers .= "To: <$to>\r\n";
                $headers .= "Subject: =?UTF-8?B?" . base64_encode($subject) . "?=\r\n";
                $headers .= "MIME-Version: 1.0\r\n";
                $headers .= "Content-Type: multipart/alternative; boundary=\"$boundary\"\r\n";
                $headers .= "Date: " . date('r') . "\r\n";
                $headers .= "\r\n";

                $content  = "--$boundary\r\n";
                $content .= "Content-Type: text/plain; charset=UTF-8\r\n\r\n";
                $content .= $textBody . "\r\n\r\n";
                $content .= "--$boundary\r\n";
                $content .= "Content-Type: text/html; charset=UTF-8\r\n\r\n";
                $content .= $htmlBody . "\r\n\r\n";
                $content .= "--$boundary--\r\n";
                $content .= "\r\n.\r\n";

                fwrite($socket, $headers . $content);
                $response = self::readSocket($socket);

                fwrite($socket, "QUIT\r\n");
                fclose($socket);

                if (substr($response, 0, 3) === '250') {
                    return [
                        'success' => true,
                        'message' => "Email sent successfully to $to via SMTP."
                    ];
                } else {
                    $lastError = "DATA response: $response";
                }
            } catch (Exception $e) {
                $lastError = $e->getMessage();
            }
        }

        // Log fallback in case network firewall blocks outbound SMTP
        self::logFallback($to, $subject, $lastError);

        return [
            'success' => false,
            'message' => "SMTP connection failed ($lastError). Network firewall may restrict outbound SMTP.",
            'details' => $lastError
        ];
    }

    private static function readSocket($socket) {
        $data = "";
        while ($line = fgets($socket, 512)) {
            $data .= $line;
            if (substr($line, 3, 1) === ' ') break;
        }
        return $data;
    }

    private static function logFallback($to, $subject, $error) {
        $logDir = __DIR__ . '/../logs';
        if (!is_dir($logDir)) {
            @mkdir($logDir, 0777, true);
        }
        $logFile = $logDir . '/mail.log';
        $entry = "[" . date('Y-m-d H:i:s') . "] TO: $to | SUBJECT: $subject | ERROR: $error\n";
        @file_put_contents($logFile, $entry, FILE_APPEND);
    }
}


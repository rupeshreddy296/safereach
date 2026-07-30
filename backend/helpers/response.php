<?php
// backend/helpers/response.php

class Response {
    public static function initHeaders() {
        header("Access-Control-Allow-Origin: *");
        header("Content-Type: application/json; charset=UTF-8");
        header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
        header("Access-Control-Max-Age: 3600");
        header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

        // Handle OPTIONS preflight request
        if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
            http_response_code(200);
            exit();
        }
    }

    public static function send($success, $message, $data = null, $http_code = 200) {
        self::initHeaders();
        http_response_code($http_code);
        
        echo json_encode([
            "success" => (bool)$success,
            "message" => $message,
            "data" => $data
        ]);
        exit();
    }
}

// Automatically initialize headers for any API including this file
Response::initHeaders();
?>

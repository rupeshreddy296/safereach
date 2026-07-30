<?php
// backend/api/requests/feedback.php

require_once '../../config/db.php';
require_once '../../helpers/auth.php';
require_once '../../helpers/response.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::send(false, "Method not allowed. Use POST.", null, 405);
}

// Authenticate user
$userPayload = Auth::getAuthenticatedUser();
$userId = $userPayload['user_id'];

// Get raw JSON input
$input = json_decode(file_get_contents("php://input"), true);

$requestId = isset($input['request_id']) ? (int)$input['request_id'] : 0;
$rating = isset($input['rating']) ? (int)$input['rating'] : 0;
$comments = isset($input['comments']) ? trim($input['comments']) : null;

if ($requestId <= 0 || $rating < 1 || $rating > 5) {
    Response::send(false, "Valid request_id and rating (1-5) are required fields.", null, 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    // 1. Verify that the request exists and belongs to the authenticated user
    $checkQuery = "SELECT user_id, status FROM emergency_requests WHERE request_id = ? LIMIT 1";
    $checkStmt = $db->prepare($checkQuery);
    $checkStmt->execute([$requestId]);
    $request = $checkStmt->fetch();

    if (!$request) {
        Response::send(false, "Emergency request not found.", null, 404);
    }

    if ((int)$request['user_id'] !== $userId) {
        Response::send(false, "Unauthorized. You can only submit feedback for your own requests.", null, 403);
    }

    // 2. Check if feedback was already submitted
    $fbQuery = "SELECT feedback_id FROM request_feedback WHERE request_id = ? LIMIT 1";
    $fbStmt = $db->prepare($fbQuery);
    $fbStmt->execute([$requestId]);
    if ($fbStmt->rowCount() > 0) {
        Response::send(false, "Feedback has already been submitted for this request.", null, 409);
    }

    // 3. Insert feedback
    $insertQuery = "INSERT INTO request_feedback (request_id, rating, comments) VALUES (?, ?, ?)";
    $insertStmt = $db->prepare($insertQuery);
    $insertStmt->execute([$requestId, $rating, $comments]);

    Response::send(true, "Feedback submitted successfully.", null, 201);

} catch (Exception $e) {
    Response::send(false, "Failed to submit feedback: " . $e->getMessage(), null, 500);
}
?>

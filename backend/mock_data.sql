USE safereach; 
INSERT INTO emergency_requests (user_id, patient_name, patient_phone, pickup_address, pickup_latitude, pickup_longitude, requested_ambulance_type, emergency_type, assigned_ambulance_id, destination_hospital_id, status, created_at, completed_at) VALUES 
(1, 'John Doe', '9876543210', '123 Main St, Chennai', 13.0827, 80.2707, 'BLS', 'Cardiac Arrest', 2, 1, 'completed', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 'Jane Smith', '9876543211', '456 Tech Park, Adyar', 13.0012, 80.2565, 'ALS', 'Accident Trauma', 2, 2, 'completed', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO request_feedback (request_id, rating, comments, created_at) 
SELECT request_id, 5, 'Very fast response, excellent paramedic.', completed_at FROM emergency_requests WHERE assigned_ambulance_id = 2 AND patient_name = 'John Doe';

INSERT INTO request_feedback (request_id, rating, comments, created_at) 
SELECT request_id, 4, 'Good driving, reached safely.', completed_at FROM emergency_requests WHERE assigned_ambulance_id = 2 AND patient_name = 'Jane Smith';

INSERT INTO emergency_requests (user_id, patient_name, patient_phone, pickup_address, pickup_latitude, pickup_longitude, requested_ambulance_type, emergency_type, assigned_ambulance_id, destination_hospital_id, status, created_at) VALUES 
(3, 'Alex Johnson', '9876543212', '789 OMR Road, Sholinganallur', 12.8981, 80.2241, 'CCU', 'Severe Stroke', 2, 1, 'accepted', DATE_SUB(NOW(), INTERVAL 15 MINUTE));

UPDATE ambulances SET experience_years = 5, license_number = 'TN-DL-2024-00123' WHERE driver_id = 16;


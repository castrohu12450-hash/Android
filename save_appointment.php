<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization");

if ($_SERVER['REQUEST_METHOD'] == 'OPTIONS') {
    exit(0);
}

require_once "config.php";

$data = json_decode(file_get_contents("php://input"), true);

if (!$data) {
    echo json_encode(["success" => false, "message" => "No data received"]);
    exit;
}

$appointment_id = $data['appointment_id'] ?? '';
$patient_id = $data['patient_id'] ?? '';
$patient_name = $data['patient_name'] ?? '';
$doctor_name = $data['doctor_name'] ?? '';
$department = $data['department'] ?? '';
$appointment_date = $data['appointment_date'] ?? '';
$appointment_time = $data['appointment_time'] ?? '';
$reason = $data['reason'] ?? '';
$status = $data['status'] ?? 'Pending';

if (empty($appointment_id) || empty($patient_id) || empty($doctor_name)) {
    echo json_encode(["success" => false, "message" => "Missing required fields"]);
    exit;
}

// Check if appointment already exists
$stmt = $conn->prepare("SELECT id FROM appointments WHERE appointment_id = ?");
$stmt->bind_param("s", $appointment_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    // Update existing
    $sql = "UPDATE appointments SET 
                patient_name = ?, 
                doctor_name = ?, 
                department = ?, 
                appointment_date = ?, 
                appointment_time = ?, 
                reason = ?, 
                status = ? 
            WHERE appointment_id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ssssssss", $patient_name, $doctor_name, $department, $appointment_date, $appointment_time, $reason, $status, $appointment_id);
    $message = "Appointment updated";
} else {
    // Insert new
    $sql = "INSERT INTO appointments (appointment_id, patient_id, patient_name, doctor_name, department, appointment_date, appointment_time, reason, status) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("sssssssss", $appointment_id, $patient_id, $patient_name, $doctor_name, $department, $appointment_date, $appointment_time, $reason, $status);
    $message = "Appointment created";
}

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => $message]);
} else {
    echo json_encode(["success" => false, "message" => "Database error: " . $stmt->error]);
}

$stmt->close();
$conn->close();
?>
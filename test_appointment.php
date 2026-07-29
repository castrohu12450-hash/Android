<?php
$testData = [
    "appointment_id" => "TEST-001",
    "patient_id" => "P-123",
    "patient_name" => "Test Patient",
    "doctor_name" => "Dr. Test",
    "department" => "General Medicine",
    "appointment_date" => "2026-06-15",
    "appointment_time" => "10:00 AM",
    "reason" => "Test appointment",
    "status" => "Pending"
];

$ch = curl_init("http://localhost/hospital_api/save_appointment.php");
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($testData));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

$response = curl_exec($ch);
echo "Response: " . $response;
curl_close($ch);
?>
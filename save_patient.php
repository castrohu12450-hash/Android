<?php
require_once "config.php";

$data = json_decode(file_get_contents("php://input"), true);

$id = $data["id"] ?? "";
$name = $data["name"] ?? "";
$phone = $data["phone"] ?? "";
$email = $data["email"] ?? "";
$date_of_birth = $data["date_of_birth"] ?? "";
$gender = $data["gender"] ?? "";
$address = $data["address"] ?? "";
$blood_type = $data["blood_type"] ?? "";
$allergies = $data["allergies"] ?? "";
$chronic_conditions = $data["chronic_conditions"] ?? "";
$primary_doctor = $data["primary_doctor"] ?? "";
$emergency_contact_name = $data["emergency_contact_name"] ?? "";
$emergency_contact_relationship = $data["emergency_contact_relationship"] ?? "";
$emergency_contact_phone = $data["emergency_contact_phone"] ?? "";

if (empty($id) || empty($name) || empty($phone)) {
    echo json_encode(["error" => "Missing required fields"]);
    exit;
}

// Check if patient exists
$stmt = $conn->prepare("SELECT id FROM patients WHERE phone = ?");
$stmt->bind_param("s", $phone);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    // Update
    $sql = "UPDATE patients SET name=?, email=?, date_of_birth=?, gender=?, address=?, blood_type=?, allergies=?, chronic_conditions=?, primary_doctor=?, emergency_contact_name=?, emergency_contact_relationship=?, emergency_contact_phone=? WHERE phone=?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("sssssssssssss", $name, $email, $date_of_birth, $gender, $address, $blood_type, $allergies, $chronic_conditions, $primary_doctor, $emergency_contact_name, $emergency_contact_relationship, $emergency_contact_phone, $phone);
    $stmt->execute();
    echo json_encode(["success" => true, "message" => "Patient updated"]);
} else {
    // Insert
    $sql = "INSERT INTO patients (id, name, phone, email, date_of_birth, gender, address, blood_type, allergies, chronic_conditions, primary_doctor, emergency_contact_name, emergency_contact_relationship, emergency_contact_phone) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ssssssssssssss", $id, $name, $phone, $email, $date_of_birth, $gender, $address, $blood_type, $allergies, $chronic_conditions, $primary_doctor, $emergency_contact_name, $emergency_contact_relationship, $emergency_contact_phone);
    $stmt->execute();
    echo json_encode(["success" => true, "message" => "Patient created"]);
}

$stmt->close();
$conn->close();
?>
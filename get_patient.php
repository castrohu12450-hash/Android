<?php
require_once "config.php";

$phone = $_GET["phone"] ?? "";
if (empty($phone)) {
    echo json_encode(["error" => "Phone number required"]);
    exit;
}

$stmt = $conn->prepare("SELECT * FROM patients WHERE phone = ?");
$stmt->bind_param("s", $phone);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    echo json_encode($row);
} else {
    echo json_encode(["error" => "Patient not found"]);
}

$stmt->close();
$conn->close();
?>
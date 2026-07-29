<?php
// config_web.php – for HTML views (no JSON header)
$host = "localhost";
$user = "root";
$pass = "";
$db = "hospital_db";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die("Database connection failed: " . $conn->connect_error);
}
?>
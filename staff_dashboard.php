<?php
require_once "config_web.php";

$patientCount = $conn->query("SELECT COUNT(*) as count FROM patients")->fetch_assoc()['count'];
$appointmentCount = $conn->query("SELECT COUNT(*) as count FROM appointments")->fetch_assoc()['count'];
$pendingAppointments = $conn->query("SELECT COUNT(*) as count FROM appointments WHERE LOWER(status) = 'pending'")->fetch_assoc()['count'];
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kiminini Hospital – Staff Dashboard</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f0f4f8; padding: 30px 20px; display: flex; justify-content: center; align-items: flex-start; }
        .container { max-width: 1200px; width: 100%; background: #ffffff; border-radius: 20px; box-shadow: 0 8px 40px rgba(0,0,0,0.08); padding: 30px 30px 40px; }
        .header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px; padding-bottom: 20px; border-bottom: 2px solid #e8edf2; margin-bottom: 24px; }
        .header-left { display: flex; align-items: center; gap: 16px; }
        .hospital-icon { font-size: 34px; }
        .header h1 { font-size: 26px; font-weight: 700; color: #0b2b4a; }
        .header h1 span { color: #1a73e8; }
        .header-badge { background: #e8f0fe; color: #1a73e8; padding: 6px 16px; border-radius: 30px; font-size: 13px; font-weight: 600; }
        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 16px; margin-bottom: 28px; }
        .stat-card { background: #f8faff; padding: 16px 18px; border-radius: 14px; border-left: 4px solid #1a73e8; text-align: center; }
        .stat-card .stat-value { font-size: 28px; font-weight: 700; color: #0b2b4a; }
        .stat-card .stat-label { font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; color: #5f6b7a; font-weight: 600; }
        .stat-card.pending { border-left-color: #f59e0b; }
        .quick-actions { display: flex; gap: 16px; flex-wrap: wrap; margin-top: 12px; padding-top: 16px; border-top: 1px solid #e8edf2; }
        .quick-btn { background: #f8faff; padding: 12px 24px; border-radius: 30px; text-decoration: none; color: #1a73e8; font-weight: 600; font-size: 14px; border: 1px solid #e0e6ed; transition: 0.2s; display: inline-flex; align-items: center; gap: 8px; }
        .quick-btn:hover { background: #e8f0fe; border-color: #1a73e8; }
        .footer { margin-top: 24px; text-align: center; font-size: 13px; color: #8a9aa8; border-top: 1px solid #e8edf2; padding-top: 20px; display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px; }
        .footer a { color: #1a73e8; text-decoration: none; font-weight: 500; }
        .footer a:hover { text-decoration: underline; }
        @media (max-width: 600px) { .container { padding: 16px; } .header h1 { font-size: 20px; } .stats-grid { grid-template-columns: repeat(2, 1fr); } }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <div class="header-left">
            <span class="hospital-icon">🏥</span>
            <h1>Kiminini <span>Hospital</span></h1>
        </div>
        <div class="header-badge">👩‍⚕️ Staff Dashboard</div>
    </div>
    <div class="stats-grid">
        <div class="stat-card"><div class="stat-value"><?= $patientCount ?></div><div class="stat-label">Total Patients</div></div>
        <div class="stat-card pending"><div class="stat-value"><?= $pendingAppointments ?></div><div class="stat-label">Pending Appointments</div></div>
        <div class="stat-card"><div class="stat-value"><?= $appointmentCount ?></div><div class="stat-label">Total Appointments</div></div>
        <div class="stat-card"><div class="stat-value">0</div><div class="stat-label">Waiting Queue</div></div>
    </div>
    <div class="quick-actions">
        <a href="view_patients.php" class="quick-btn">👤 View Patients</a>
        <a href="view_appointments.php" class="quick-btn">📋 View Appointments</a>
        <a href="queue_tickets.php" class="quick-btn">🚶 View Queue</a>
        <a href="search_patient.php" class="quick-btn">🔍 Search Patient</a>
    </div>
    <div class="footer">
        <span>© <?= date('Y') ?> Kiminini Hospital – Patient Management System</span>
        <span><a href="dashboard.php">Dashboard</a> &middot; <a href="view_patients.php">Patients</a> &middot; <a href="view_appointments.php">Appointments</a></span>
    </div>
</div>
</body>
</html>
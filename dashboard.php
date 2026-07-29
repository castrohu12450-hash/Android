<?php
require_once "config_web.php";

// Get counts
$patientCount = $conn->query("SELECT COUNT(*) as count FROM patients")->fetch_assoc()['count'];
$appointmentCount = $conn->query("SELECT COUNT(*) as count FROM appointments")->fetch_assoc()['count'];
$pendingAppointments = $conn->query("SELECT COUNT(*) as count FROM appointments WHERE LOWER(status) = 'pending'")->fetch_assoc()['count'];
$completedAppointments = $conn->query("SELECT COUNT(*) as count FROM appointments WHERE LOWER(status) = 'completed'")->fetch_assoc()['count'];
$cancelledAppointments = $conn->query("SELECT COUNT(*) as count FROM appointments WHERE LOWER(status) = 'cancelled'")->fetch_assoc()['count'];
$maleCount = $conn->query("SELECT COUNT(*) as count FROM patients WHERE LOWER(gender) = 'male'")->fetch_assoc()['count'];
$femaleCount = $conn->query("SELECT COUNT(*) as count FROM patients WHERE LOWER(gender) = 'female'")->fetch_assoc()['count'];

$recentPatients = $conn->query("SELECT * FROM patients ORDER BY created_at DESC LIMIT 5");
$recentAppointments = $conn->query("SELECT * FROM appointments ORDER BY created_at DESC LIMIT 5");
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kiminini Hospital – Dashboard</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f0f4f8; padding: 30px 20px; min-height: 100vh; display: flex; justify-content: center; align-items: flex-start; }
        .container { max-width: 1300px; width: 100%; background: #ffffff; border-radius: 20px; box-shadow: 0 8px 40px rgba(0,0,0,0.08); overflow: hidden; padding: 30px 30px 40px; }
        .header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px; padding-bottom: 20px; border-bottom: 2px solid #e8edf2; margin-bottom: 24px; }
        .header-left { display: flex; align-items: center; gap: 16px; }
        .hospital-icon { font-size: 34px; }
        .header h1 { font-size: 26px; font-weight: 700; color: #0b2b4a; letter-spacing: -0.3px; }
        .header h1 span { color: #1a73e8; }
        .header-badge { background: #e8f0fe; color: #1a73e8; padding: 6px 16px; border-radius: 30px; font-size: 13px; font-weight: 600; }
        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 16px; margin-bottom: 28px; }
        .stat-card { background: #f8faff; padding: 16px 18px; border-radius: 14px; border-left: 4px solid #1a73e8; text-align: center; }
        .stat-card .stat-value { font-size: 28px; font-weight: 700; color: #0b2b4a; }
        .stat-card .stat-label { font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; color: #5f6b7a; font-weight: 600; margin-top: 2px; }
        .stat-card.pending { border-left-color: #f59e0b; }
        .stat-card.completed { border-left-color: #10b981; }
        .stat-card.cancelled { border-left-color: #ef4444; }
        .stat-card.male { border-left-color: #1a73e8; }
        .stat-card.female { border-left-color: #ec4899; }
        .two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-top: 8px; }
        @media (max-width: 800px) { .two-col { grid-template-columns: 1fr; } }
        .section-title { font-size: 18px; font-weight: 600; color: #0b2b4a; margin-bottom: 12px; display: flex; align-items: center; gap: 10px; }
        .section-title .badge { background: #e8f0fe; color: #1a73e8; font-size: 12px; padding: 0 12px; border-radius: 30px; font-weight: 600; }
        .table-wrapper { overflow-x: auto; border-radius: 12px; border: 1px solid #e8edf2; }
        table { width: 100%; border-collapse: collapse; font-size: 13px; min-width: 350px; }
        thead { background: #f8faff; }
        th { text-align: left; padding: 10px 14px; font-weight: 600; color: #1f2a3a; border-bottom: 2px solid #e0e6ed; font-size: 11px; text-transform: uppercase; letter-spacing: 0.3px; }
        td { padding: 10px 14px; border-bottom: 1px solid #eef2f7; color: #1f2a3a; }
        tbody tr:hover { background: #f8faff; }
        .status-badge { display: inline-block; padding: 2px 12px; border-radius: 30px; font-size: 11px; font-weight: 600; }
        .status-badge.pending { background: #fef3c7; color: #b45309; }
        .status-badge.completed { background: #d1fae5; color: #065f46; }
        .status-badge.cancelled { background: #fee2e2; color: #991b1b; }
        .gender-badge { display: inline-block; padding: 2px 10px; border-radius: 30px; font-size: 11px; font-weight: 600; }
        .gender-badge.male { background: #dbeafe; color: #1e40af; }
        .gender-badge.female { background: #fce7f3; color: #9d174d; }
        .gender-badge.other { background: #ede9fe; color: #5b21b6; }
        .view-link { color: #1a73e8; text-decoration: none; font-weight: 500; font-size: 13px; }
        .view-link:hover { text-decoration: underline; }
        .quick-links { display: flex; gap: 16px; flex-wrap: wrap; margin-top: 20px; padding-top: 20px; border-top: 1px solid #e8edf2; }
        .quick-link-btn { background: #f8faff; padding: 10px 24px; border-radius: 30px; text-decoration: none; color: #1a73e8; font-weight: 600; font-size: 14px; border: 1px solid #e0e6ed; transition: all 0.2s; }
        .quick-link-btn:hover { background: #e8f0fe; border-color: #1a73e8; }
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
        <div class="header-badge">📊 Dashboard</div>
    </div>
    <div class="stats-grid">
        <div class="stat-card"><div class="stat-value"><?= $patientCount ?></div><div class="stat-label">Total Patients</div></div>
        <div class="stat-card male"><div class="stat-value"><?= $maleCount ?></div><div class="stat-label">Male</div></div>
        <div class="stat-card female"><div class="stat-value"><?= $femaleCount ?></div><div class="stat-label">Female</div></div>
        <div class="stat-card"><div class="stat-value"><?= $appointmentCount ?></div><div class="stat-label">Total Appointments</div></div>
        <div class="stat-card pending"><div class="stat-value"><?= $pendingAppointments ?></div><div class="stat-label">Pending</div></div>
        <div class="stat-card completed"><div class="stat-value"><?= $completedAppointments ?></div><div class="stat-label">Completed</div></div>
        <div class="stat-card cancelled"><div class="stat-value"><?= $cancelledAppointments ?></div><div class="stat-label">Cancelled</div></div>
    </div>
    <div class="two-col">
        <div>
            <div class="section-title">👤 Recent Patients <span class="badge">Last 5</span></div>
            <div class="table-wrapper">
                <table>
                    <thead><tr><th>Name</th><th>Phone</th><th>Gender</th></tr></thead>
                    <tbody>
                        <?php if ($recentPatients && $recentPatients->num_rows > 0): ?>
                            <?php while ($row = $recentPatients->fetch_assoc()): $gender = strtolower(trim($row['gender'] ?? '')); ?>
                                <tr>
                                    <td><strong><?= htmlspecialchars($row['name']) ?></strong></td>
                                    <td><?= htmlspecialchars($row['phone']) ?></td>
                                    <td><span class="gender-badge <?= $gender ?>"><?= ucfirst($gender) ?></span></td>
                                </tr>
                            <?php endwhile; ?>
                        <?php else: ?>
                            <tr><td colspan="3" style="text-align:center; color:#8a9aa8;">No patients</td></tr>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>
            <div style="margin-top: 8px; text-align: right;"><a href="view_patients.php" class="view-link">View All Patients →</a></div>
        </div>
        <div>
            <div class="section-title">📋 Recent Appointments <span class="badge">Last 5</span></div>
            <div class="table-wrapper">
                <table>
                    <thead><tr><th>Patient</th><th>Doctor</th><th>Date</th><th>Status</th></tr></thead>
                    <tbody>
                        <?php if ($recentAppointments && $recentAppointments->num_rows > 0): ?>
                            <?php while ($row = $recentAppointments->fetch_assoc()): $status = strtolower(htmlspecialchars($row['status'] ?? 'pending')); ?>
                                <tr>
                                    <td><strong><?= htmlspecialchars($row['patient_name']) ?></strong></td>
                                    <td><?= htmlspecialchars($row['doctor_name']) ?></td>
                                    <td><?= date('d M Y', strtotime($row['appointment_date'])) ?></td>
                                    <td><span class="status-badge <?= $status ?>"><?= ucfirst($status) ?></span></td>
                                </tr>
                            <?php endwhile; ?>
                        <?php else: ?>
                            <tr><td colspan="4" style="text-align:center; color:#8a9aa8;">No appointments</td></tr>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>
            <div style="margin-top: 8px; text-align: right;"><a href="view_appointments.php" class="view-link">View All Appointments →</a></div>
        </div>
    </div>
    <div class="quick-links">
        <a href="view_patients.php" class="quick-link-btn">👤 Patients</a>
        <a href="view_appointments.php" class="quick-link-btn">📋 Appointments</a>
        <a href="search_patient.php" class="quick-link-btn">🔍 Search Patient</a>
        <a href="queue_tickets.php" class="quick-link-btn">🚶 Queue</a>
    </div>
    <div class="footer">
        <span>© <?= date('Y') ?> Kiminini Hospital – Patient Management System</span>
        <span><a href="dashboard.php">Dashboard</a> &middot; <a href="view_patients.php">Patients</a> &middot; <a href="view_appointments.php">Appointments</a></span>
    </div>
</div>
</body>
</html>
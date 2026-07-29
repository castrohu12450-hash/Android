<?php
require_once "config.php";

// Reset content type to HTML (config.php sets it to JSON)
header("Content-Type: text/html; charset=UTF-8");

$result = $conn->query("SELECT * FROM appointments ORDER BY appointment_date DESC, appointment_time ASC");

if (!$result) {
    die("Query failed: " . $conn->error);
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kiminini Hospital – Appointments</title>
    <style>
        /* ─── Reset & Base ─── */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f0f4f8;
            padding: 30px 20px;
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: flex-start;
        }

        .container {
            max-width: 1300px;
            width: 100%;
            background: #ffffff;
            border-radius: 20px;
            box-shadow: 0 8px 40px rgba(0, 0, 0, 0.08);
            overflow: hidden;
            padding: 30px 30px 40px;
        }

        /* ─── Header ─── */
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 16px;
            padding-bottom: 20px;
            border-bottom: 2px solid #e8edf2;
            margin-bottom: 24px;
        }

        .header-left {
            display: flex;
            align-items: center;
            gap: 16px;
        }

        .hospital-icon {
            font-size: 34px;
        }

        .header h1 {
            font-size: 26px;
            font-weight: 700;
            color: #0b2b4a;
            letter-spacing: -0.3px;
        }

        .header h1 span {
            color: #1a73e8;
        }

        .header-badge {
            background: #e8f0fe;
            color: #1a73e8;
            padding: 6px 16px;
            border-radius: 30px;
            font-size: 13px;
            font-weight: 600;
            letter-spacing: 0.3px;
        }

        .header-badge .count {
            background: #1a73e8;
            color: #fff;
            padding: 0 10px;
            border-radius: 20px;
            margin-left: 6px;
        }

        /* ─── Stats Bar ─── */
        .stats-bar {
            display: flex;
            gap: 20px;
            flex-wrap: wrap;
            margin-bottom: 24px;
        }

        .stat-card {
            background: #f8faff;
            padding: 12px 22px;
            border-radius: 12px;
            border-left: 4px solid #1a73e8;
            flex: 1;
            min-width: 140px;
        }

        .stat-card .stat-label {
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #5f6b7a;
            font-weight: 600;
        }

        .stat-card .stat-value {
            font-size: 22px;
            font-weight: 700;
            color: #0b2b4a;
            margin-top: 2px;
        }

        .stat-card.pending { border-left-color: #f59e0b; }
        .stat-card.completed { border-left-color: #10b981; }
        .stat-card.cancelled { border-left-color: #ef4444; }

        /* ─── Table Wrapper ─── */
        .table-wrapper {
            overflow-x: auto;
            border-radius: 14px;
            border: 1px solid #e8edf2;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
            min-width: 700px;
        }

        thead {
            background: #f8faff;
        }

        th {
            text-align: left;
            padding: 14px 16px;
            font-weight: 600;
            color: #1f2a3a;
            border-bottom: 2px solid #e0e6ed;
            font-size: 13px;
            text-transform: uppercase;
            letter-spacing: 0.3px;
        }

        td {
            padding: 14px 16px;
            border-bottom: 1px solid #eef2f7;
            color: #1f2a3a;
            vertical-align: middle;
        }

        tbody tr:hover {
            background: #f8faff;
        }

        tbody tr:last-child td {
            border-bottom: none;
        }

        /* ─── Status Badges ─── */
        .status-badge {
            display: inline-block;
            padding: 4px 14px;
            border-radius: 30px;
            font-size: 12px;
            font-weight: 600;
        }

        .status-badge.pending {
            background: #fef3c7;
            color: #b45309;
        }

        .status-badge.completed {
            background: #d1fae5;
            color: #065f46;
        }

        .status-badge.cancelled {
            background: #fee2e2;
            color: #991b1b;
        }

        .status-badge.confirmed {
            background: #dbeafe;
            color: #1e40af;
        }

        /* ─── Footer ─── */
        .footer {
            margin-top: 24px;
            text-align: center;
            font-size: 13px;
            color: #8a9aa8;
            border-top: 1px solid #e8edf2;
            padding-top: 20px;
            display: flex;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 8px;
        }

        .footer a {
            color: #1a73e8;
            text-decoration: none;
            font-weight: 500;
        }

        .footer a:hover {
            text-decoration: underline;
        }

        /* ─── Responsive ─── */
        @media (max-width: 700px) {
            .container { padding: 18px; }
            .header h1 { font-size: 20px; }
            .stat-card { min-width: 100px; }
            th, td { padding: 10px 12px; font-size: 13px; }
        }

        @media (max-width: 500px) {
            .header { flex-direction: column; align-items: flex-start; }
            .stats-bar { flex-direction: column; }
        }
    </style>
</head>
<body>

<div class="container">

    <!-- Header -->
    <div class="header">
        <div class="header-left">
            <span class="hospital-icon">🏥</span>
            <h1>Kiminini <span>Hospital</span></h1>
        </div>
        <div class="header-badge">
            📋 Appointments
            <span class="count"><?= $result->num_rows ?></span>
        </div>
    </div>

    <!-- Stats Bar -->
    <?php
    // Count statuses
    $pending = 0;
    $completed = 0;
    $cancelled = 0;
    $result->data_seek(0);
    while ($row = $result->fetch_assoc()) {
        $status = strtolower($row['status']);
        if ($status == 'pending') $pending++;
        elseif ($status == 'completed') $completed++;
        elseif ($status == 'cancelled') $cancelled++;
    }
    $result->data_seek(0);
    ?>

    <div class="stats-bar">
        <div class="stat-card">
            <div class="stat-label">Total</div>
            <div class="stat-value"><?= $result->num_rows ?></div>
        </div>
        <div class="stat-card pending">
            <div class="stat-label">Pending</div>
            <div class="stat-value"><?= $pending ?></div>
        </div>
        <div class="stat-card completed">
            <div class="stat-label">Completed</div>
            <div class="stat-value"><?= $completed ?></div>
        </div>
        <div class="stat-card cancelled">
            <div class="stat-label">Cancelled</div>
            <div class="stat-value"><?= $cancelled ?></div>
        </div>
    </div>

    <!-- Table -->
    <div class="table-wrapper">
        <table>
            <thead>
                <tr>
                    <th>#</th>
                    <th>Patient</th>
                    <th>Doctor</th>
                    <th>Department</th>
                    <th>Date</th>
                    <th>Time</th>
                    <th>Reason</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                <?php if ($result->num_rows > 0): ?>
                    <?php while ($row = $result->fetch_assoc()): ?>
                        <tr>
                            <td><strong><?= htmlspecialchars($row['id']) ?></strong></td>
                            <td><?= htmlspecialchars($row['patient_name']) ?></td>
                            <td><?= htmlspecialchars($row['doctor_name']) ?></td>
                            <td><?= htmlspecialchars($row['department']) ?></td>
                            <td><?= date('d M Y', strtotime($row['appointment_date'])) ?></td>
                            <td><?= htmlspecialchars($row['appointment_time']) ?></td>
                            <td><?= htmlspecialchars($row['reason']) ?></td>
                            <td>
                                <?php
                                $status = strtolower(htmlspecialchars($row['status']));
                                $class = $status;
                                ?>
                                <span class="status-badge <?= $class ?>"><?= ucfirst($status) ?></span>
                            </td>
                        </tr>
                    <?php endwhile; ?>
                <?php else: ?>
                    <tr>
                        <td colspan="8" style="text-align: center; padding: 40px; color: #8a9aa8;">
                            No appointments found.
                        </td>
                    </tr>
                <?php endif; ?>
            </tbody>
        </table>
    </div>

    <!-- Footer -->
    <div class="footer">
        <span>© <?= date('Y') ?> Kiminini Hospital – Patient Management System</span>
        <span>
            <a href="view_patients.php">Patients</a> &middot;
            <a href="view_appointments.php">Appointments</a>
        </span>
    </div>

</div>

</body>
</html>
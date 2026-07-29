<?php
require_once "config_web.php";

$result = $conn->query("SELECT * FROM patients ORDER BY created_at DESC");

if (!$result) {
    die("Query failed: " . $conn->error);
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kiminini Hospital – Patients</title>
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
            max-width: 1400px;
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
            gap: 16px;
            flex-wrap: wrap;
            margin-bottom: 24px;
        }

        .stat-card {
            background: #f8faff;
            padding: 14px 24px;
            border-radius: 12px;
            border-left: 4px solid #1a73e8;
            flex: 1;
            min-width: 100px;
            text-align: center;
        }

        .stat-card .stat-value {
            font-size: 24px;
            font-weight: 700;
            color: #0b2b4a;
        }

        .stat-card .stat-label {
            font-size: 11px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #5f6b7a;
            font-weight: 600;
        }

        .stat-card.male { border-left-color: #1a73e8; }
        .stat-card.female { border-left-color: #ec4899; }
        .stat-card.other { border-left-color: #8b5cf6; }

        /* ─── Table ─── */
        .table-wrapper {
            overflow-x: auto;
            border-radius: 14px;
            border: 1px solid #e8edf2;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 13px;
            min-width: 900px;
        }

        thead {
            background: #f8faff;
        }

        th {
            text-align: center;
            padding: 14px 12px;
            font-weight: 600;
            color: #1f2a3a;
            border-bottom: 2px solid #e0e6ed;
            font-size: 11px;
            text-transform: uppercase;
            letter-spacing: 0.3px;
            white-space: nowrap;
        }

        td {
            text-align: center;
            padding: 12px 12px;
            border-bottom: 1px solid #eef2f7;
            color: #1f2a3a;
            vertical-align: middle;
        }

        td.name-column,
        th.name-column {
            text-align: left;
        }

        td.email-column,
        th.email-column {
            text-align: left;
        }

        tbody tr:hover {
            background: #f8faff;
        }

        tbody tr:last-child td {
            border-bottom: none;
        }

        /* ─── Badges ─── */
        .blood-badge {
            display: inline-block;
            padding: 2px 12px;
            border-radius: 30px;
            font-size: 12px;
            font-weight: 600;
            background: #f1f4f9;
            color: #1f2a3a;
        }
        .blood-badge.a-positive { background: #dbeafe; color: #1e40af; }
        .blood-badge.a-negative { background: #dbeafe; color: #1e40af; }
        .blood-badge.b-positive { background: #fce7f3; color: #9d174d; }
        .blood-badge.b-negative { background: #fce7f3; color: #9d174d; }
        .blood-badge.o-positive { background: #d1fae5; color: #065f46; }
        .blood-badge.o-negative { background: #d1fae5; color: #065f46; }
        .blood-badge.ab-positive { background: #fef3c7; color: #b45309; }
        .blood-badge.ab-negative { background: #fef3c7; color: #b45309; }

        .gender-badge {
            display: inline-block;
            padding: 2px 12px;
            border-radius: 30px;
            font-size: 12px;
            font-weight: 600;
        }
        .gender-badge.male { background: #dbeafe; color: #1e40af; }
        .gender-badge.female { background: #fce7f3; color: #9d174d; }
        .gender-badge.other { background: #ede9fe; color: #5b21b6; }

        .patient-id-code {
            background: #f1f4f9;
            padding: 2px 10px;
            border-radius: 6px;
            font-size: 12px;
            font-family: 'Courier New', monospace;
            font-weight: 600;
            color: #1a73e8;
            display: inline-block;
        }

        .email-column a {
            color: #1a73e8;
            text-decoration: none;
        }
        .email-column a:hover {
            text-decoration: underline;
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
            .stat-card { min-width: 80px; padding: 10px 14px; }
            th, td { padding: 8px 8px; font-size: 12px; }
        }
        @media (max-width: 500px) {
            .header { flex-direction: column; align-items: flex-start; }
            .stats-bar { flex-direction: column; }
        }
    </style>
</head>
<body>

<div class="container">

    <!-- ─── Header ─── -->
    <div class="header">
        <div class="header-left">
            <span class="hospital-icon">🏥</span>
            <h1>Kiminini <span>Hospital</span></h1>
        </div>
        <div class="header-badge">
            👤 Patients
            <span class="count"><?= $result->num_rows ?></span>
        </div>
    </div>

    <!-- ─── Stats ─── -->
    <?php
    $male = 0; $female = 0; $other = 0;
    $result->data_seek(0);
    while ($row = $result->fetch_assoc()) {
        $g = strtolower(trim($row['gender'] ?? ''));
        if ($g == 'male') $male++;
        elseif ($g == 'female') $female++;
        elseif (!empty($g)) $other++;
    }
    $result->data_seek(0);
    ?>

    <div class="stats-bar">
        <div class="stat-card">
            <div class="stat-value"><?= $result->num_rows ?></div>
            <div class="stat-label">Total</div>
        </div>
        <div class="stat-card male">
            <div class="stat-value"><?= $male ?></div>
            <div class="stat-label">Male</div>
        </div>
        <div class="stat-card female">
            <div class="stat-value"><?= $female ?></div>
            <div class="stat-label">Female</div>
        </div>
        <div class="stat-card other">
            <div class="stat-value"><?= $other ?></div>
            <div class="stat-label">Other</div>
        </div>
    </div>

    <!-- ─── Table ─── -->
    <div class="table-wrapper">
        <table>
            <thead>
                <tr>
                    <th>#</th>
                    <th>Patient ID</th>
                    <th class="name-column">Name</th>
                    <th>Phone</th>
                    <th class="email-column">Email</th>
                    <th>DOB</th>
                    <th>Gender</th>
                    <th>Blood Type</th>
                    <th>Allergies</th>
                    <th>Registered</th>
                </tr>
            </thead>
            <tbody>
                <?php if ($result->num_rows > 0): ?>
                    <?php $counter = 1; while ($row = $result->fetch_assoc()): 
                        $gender = strtolower(trim($row['gender'] ?? ''));
                        $bt = strtoupper($row['blood_type'] ?? '-');
                        $btClass = strtolower(str_replace('+', '-positive', str_replace('-', '-negative', $bt)));
                        if (strpos($bt, 'A') !== false) $btClass = 'a-positive';
                        if (strpos($bt, 'B') !== false) $btClass = 'b-positive';
                        if (strpos($bt, 'O') !== false) $btClass = 'o-positive';
                        if (strpos($bt, 'AB') !== false) $btClass = 'ab-positive';
                        if (strpos($bt, '-') !== false) $btClass = str_replace('positive', 'negative', $btClass);
                    ?>
                        <tr>
                            <td><?= $counter++ ?></td>
                            <td><span class="patient-id-code"><?= htmlspecialchars($row['id'] ?? '-') ?></span></td>
                            <td class="name-column"><strong><?= htmlspecialchars($row['name']) ?></strong></td>
                            <td><?= htmlspecialchars($row['phone']) ?></td>
                            <td class="email-column">
                                <?php if (!empty($row['email'])): ?>
                                    <a href="mailto:<?= htmlspecialchars($row['email']) ?>"><?= htmlspecialchars($row['email']) ?></a>
                                <?php else: ?>
                                    -
                                <?php endif; ?>
                            </td>
                            <td><?= htmlspecialchars($row['date_of_birth'] ?? '-') ?></td>
                            <td>
                                <span class="gender-badge <?= $gender ?>">
                                    <?= ucfirst($gender) ?>
                                </span>
                            </td>
                            <td>
                                <span class="blood-badge <?= $btClass ?>">
                                    <?= htmlspecialchars($bt) ?>
                                </span>
                            </td>
                            <td><?= htmlspecialchars($row['allergies'] ?? 'None') ?></td>
                            <td><?= date('d M Y', strtotime($row['created_at'] ?? 'now')) ?></td>
                        </tr>
                    <?php endwhile; ?>
                <?php else: ?>
                    <tr><td colspan="10" style="text-align:center; padding:40px; color:#8a9aa8;">No patients registered yet.</td></tr>
                <?php endif; ?>
            </tbody>
        </table>
    </div>

    <!-- ─── Footer ─── -->
    <div class="footer">
        <span>© <?= date('Y') ?> Kiminini Hospital – Patient Management System</span>
        <span>
            <a href="dashboard.php">Dashboard</a> &middot;
            <a href="view_patients.php">Patients</a> &middot;
            <a href="view_appointments.php">Appointments</a>
        </span>
    </div>

</div>

</body>
</html>
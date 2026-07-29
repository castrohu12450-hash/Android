<?php
require_once "config_web.php";

$search = isset($_GET['search']) ? trim($_GET['search']) : '';
$results = [];

if (!empty($search)) {
    // Search by id (patient ID), name, or phone
    $stmt = $conn->prepare("SELECT * FROM patients WHERE id LIKE ? OR name LIKE ? OR phone LIKE ? ORDER BY created_at DESC");
    $like = "%$search%";
    $stmt->bind_param("sss", $like, $like, $like);
    $stmt->execute();
    $results = $stmt->get_result();
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kiminini Hospital – Search Patient</title>
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
        .search-box { display: flex; gap: 12px; margin-bottom: 24px; flex-wrap: wrap; }
        .search-box input { flex: 1; padding: 12px 18px; border: 2px solid #e0e6ed; border-radius: 30px; font-size: 15px; outline: none; transition: 0.2s; min-width: 200px; }
        .search-box input:focus { border-color: #1a73e8; }
        .search-box button { padding: 12px 32px; background: #1a73e8; color: #fff; border: none; border-radius: 30px; font-weight: 600; cursor: pointer; transition: 0.2s; }
        .search-box button:hover { background: #0d47a1; }
        .search-box .clear-btn { background: #e8edf2; color: #1f2a3a; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; padding: 12px 32px; border-radius: 30px; font-weight: 600; }
        .search-box .clear-btn:hover { background: #d0d5db; }
        .result-count { font-size: 14px; color: #5f6b7a; margin-bottom: 16px; }
        .table-wrapper { overflow-x: auto; border-radius: 12px; border: 1px solid #e8edf2; }
        table { width: 100%; border-collapse: collapse; font-size: 13px; min-width: 700px; }
        thead { background: #f8faff; }
        th { text-align: left; padding: 12px 14px; font-weight: 600; color: #1f2a3a; border-bottom: 2px solid #e0e6ed; font-size: 11px; text-transform: uppercase; letter-spacing: 0.3px; }
        td { padding: 12px 14px; border-bottom: 1px solid #eef2f7; color: #1f2a3a; }
        tbody tr:hover { background: #f8faff; }
        .no-results { text-align: center; padding: 40px; color: #8a9aa8; }
        .gender-badge { display: inline-block; padding: 2px 10px; border-radius: 30px; font-size: 11px; font-weight: 600; }
        .gender-badge.male { background: #dbeafe; color: #1e40af; }
        .gender-badge.female { background: #fce7f3; color: #9d174d; }
        .gender-badge.other { background: #ede9fe; color: #5b21b6; }
        .footer { margin-top: 24px; text-align: center; font-size: 13px; color: #8a9aa8; border-top: 1px solid #e8edf2; padding-top: 20px; display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px; }
        .footer a { color: #1a73e8; text-decoration: none; font-weight: 500; }
        .footer a:hover { text-decoration: underline; }
        .patient-id-code { background: #f1f4f9; padding: 2px 10px; border-radius: 6px; font-size: 12px; font-family: 'Courier New', monospace; font-weight: 600; color: #1a73e8; display: inline-block; }
        @media (max-width: 600px) { .container { padding: 16px; } .header h1 { font-size: 20px; } }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <div class="header-left">
            <span class="hospital-icon">🏥</span>
            <h1>Kiminini <span>Hospital</span></h1>
        </div>
        <div class="header-badge">🔍 Patient Search</div>
    </div>
    <form method="GET" class="search-box">
        <input type="text" name="search" placeholder="Search by name, phone, or patient ID..." value="<?= htmlspecialchars($search) ?>">
        <button type="submit">🔍 Search</button>
        <?php if (!empty($search)): ?>
            <a href="search_patient.php" class="clear-btn">Clear</a>
        <?php endif; ?>
    </form>
    <?php if (!empty($search)): ?>
        <div class="result-count">
            Found <?= $results ? $results->num_rows : 0 ?> result(s) for "<strong><?= htmlspecialchars($search) ?></strong>"
        </div>
        <div class="table-wrapper">
            <table>
                <thead>
                    <tr>
                        <th>Patient ID</th>
                        <th>Name</th>
                        <th>Phone</th>
                        <th>Email</th>
                        <th>Gender</th>
                        <th>Blood Type</th>
                        <th>Registered</th>
                    </tr>
                </thead>
                <tbody>
                    <?php if ($results && $results->num_rows > 0): ?>
                        <?php while ($row = $results->fetch_assoc()): 
                            $gender = strtolower(trim($row['gender'] ?? ''));
                        ?>
                            <tr>
                                <td><span class="patient-id-code"><?= htmlspecialchars($row['id'] ?? '-') ?></span></td>
                                <td><strong><?= htmlspecialchars($row['name']) ?></strong></td>
                                <td><?= htmlspecialchars($row['phone']) ?></td>
                                <td><?= htmlspecialchars($row['email'] ?? '-') ?></td>
                                <td>
                                    <span class="gender-badge <?= $gender ?>">
                                        <?= ucfirst($gender) ?>
                                    </span>
                                </td>
                                <td><?= htmlspecialchars($row['blood_type'] ?? '-') ?></td>
                                <td><?= date('d M Y', strtotime($row['created_at'] ?? 'now')) ?></td>
                            </tr>
                        <?php endwhile; ?>
                    <?php else: ?>
                        <tr>
                            <td colspan="7" class="no-results">
                                No patients found matching "<strong><?= htmlspecialchars($search) ?></strong>"
                            </td>
                        </tr>
                    <?php endif; ?>
                </tbody>
            </table>
        </div>
    <?php else: ?>
        <div style="text-align:center; padding:60px 20px; color:#8a9aa8;">
            <div style="font-size:48px; margin-bottom:16px;">🔍</div>
            <h2 style="color:#1f2a3a; font-weight:600;">Search for a Patient</h2>
            <p>Enter a name, phone number, or patient ID above to find a patient record.</p>
        </div>
    <?php endif; ?>
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
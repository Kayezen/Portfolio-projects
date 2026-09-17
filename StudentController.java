import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;

public class StudentController {

    private static final String FILE_PATH = "studentrecords.txt";

    private final StudentGUI gui;
    private final StudentBST tree = new StudentBST();

    StudentController(StudentGUI gui) {
        this.gui = gui;
        loadFromFile();
        attachListeners();
        refreshDashboard();
        refreshTable();
        gui.treePanel.refreshTree(tree);
    }

    // ---- connects every button/nav click to its action ----
    private void attachListeners() {

        // sidebar navigation
        gui.navDashboard.addActionListener(e -> {
            refreshDashboard();
            gui.showPage("DASHBOARD");
        });
        gui.navRecords.addActionListener(e -> {
            refreshTable();
            gui.showPage("RECORDS");
        });
        gui.navAddStudent.addActionListener(e -> gui.showPage("ADD_STUDENT"));
        gui.navBSTView.addActionListener(e -> {
            gui.treePanel.refreshTree(tree);
            gui.showPage("BST_VIEW");
        });

        // add student form: live preview as user types
        gui.idField.getDocument().addDocumentListener(new SimpleDocListener(this::updatePreview));
        gui.nameField.getDocument().addDocumentListener(new SimpleDocListener(this::updatePreview));
        gui.courseField.getDocument().addDocumentListener(new SimpleDocListener(this::updatePreview));
        gui.gradeField.getDocument().addDocumentListener(new SimpleDocListener(this::updatePreview));

        // add student form: submit button
        gui.addStudentBtn.addActionListener(e -> handleAddStudent());
        gui.cancelBtn.addActionListener(e -> {
            clearAddForm();
            gui.showPage("DASHBOARD");
        });

        // records page: search bar
        gui.searchField.getDocument().addDocumentListener(new SimpleDocListener(this::handleSearch));
    }

    // ---- ADD STUDENT button logic ----
    private void handleAddStudent() {
        String idText = gui.idField.getText().trim();
        String name = gui.nameField.getText().trim();
        String course = gui.courseField.getText().trim();
        String gradeText = gui.gradeField.getText().trim();

        if (idText.isEmpty() || name.isEmpty() || course.isEmpty() || gradeText.isEmpty()) {
            JOptionPane.showMessageDialog(gui, "Please fill in all fields.");
            return;
        }

        int rollNumber;
        try {
            rollNumber = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(gui, "Student ID must be a number.");
            return;
        }

        if (tree.rollNumberExists(rollNumber)) {
            JOptionPane.showMessageDialog(gui, "This Student ID already exists.");
            return;
        }

        tree.insert(rollNumber, name, course + "|" + gradeText);
        saveToFile();

        clearAddForm();
        refreshDashboard();
        refreshTable();
        gui.treePanel.refreshTree(tree);
        gui.showPage("RECORDS");
    }

    // ---- live preview panel on Add Student page ----
    private void updatePreview() {
        gui.previewId.setText(orDash(gui.idField.getText()));
        gui.previewName.setText(orDash(gui.nameField.getText()));
        gui.previewCourse.setText(orDash(gui.courseField.getText()));
        gui.previewGrade.setText(orDash(gui.gradeField.getText()));
    }

    private String orDash(String text) {
        return text.trim().isEmpty() ? "\u2014" : text.trim();
    }

    private void clearAddForm() {
        gui.idField.setText("");
        gui.nameField.setText("");
        gui.courseField.setText("");
        gui.gradeField.setText("");
        updatePreview();
    }

    // ---- SEARCH bar logic (filters table by id or name) ----
    private void handleSearch() {
        String query = gui.searchField.getText().trim();
        if (query.isEmpty()) {
            refreshTable();
            return;
        }
        gui.tableModel.setRowCount(0);

        // try matching by roll number first
        try {
            int roll = Integer.parseInt(query);
            StudentBST.Node found = tree.searchByRoll(roll);
            if (found != null) {
                addRowToTable(found);
            }
        } catch (NumberFormatException ex) {
            // not a number, search by name instead
            List<StudentBST.Node> matches = tree.searchByName(query);
            for (StudentBST.Node n : matches) {
                addRowToTable(n);
            }
        }
        gui.recordCountLabel.setText(gui.tableModel.getRowCount() + " students found");
    }

    // ---- VIEW button logic (per row, shown as a details dialog) ----
    private void handleView(int rollNumber) {
        StudentBST.Node n = tree.searchByRoll(rollNumber);
        if (n == null) return;
        String[] parts = n.grade.split("\\|");
        String course = parts[0];
        String grade = parts.length > 1 ? parts[1] : "";
        JOptionPane.showMessageDialog(gui,
                "Student ID: " + n.rollNumber + "\n" +
                        "Name: " + n.name + "\n" +
                        "Course: " + course + "\n" +
                        "Grade: " + grade,
                "Student Details", JOptionPane.INFORMATION_MESSAGE);
    }

    // ---- DELETE button logic (per row) ----
    private void handleDelete(int rollNumber) {
        int confirm = JOptionPane.showConfirmDialog(gui,
                "Are you sure you want to delete this record?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        tree.deleteByRoll(rollNumber);
        saveToFile();
        refreshDashboard();
        refreshTable();
        gui.treePanel.refreshTree(tree);
    }

    // ---- refreshes dashboard stat cards + recent students list ----
    private void refreshDashboard() {
        List<StudentBST.Node> all = tree.inorder();
        gui.totalStudentsValue.setText(String.valueOf(all.size()));

        if (all.isEmpty()) {
            gui.avgGradeValue.setText("0.0");
            gui.topGradeValue.setText("0");
            gui.topGradeName.setText("-");
        } else {
            double sum = 0;
            int topGrade = -1;
            String topName = "-";
            for (StudentBST.Node n : all) {
                int grade = extractGrade(n.grade);
                sum += grade;
                if (grade > topGrade) {
                    topGrade = grade;
                    topName = n.name;
                }
            }
            gui.avgGradeValue.setText(String.format("%.1f", sum / all.size()));
            gui.topGradeValue.setText(String.valueOf(topGrade));
            gui.topGradeName.setText(topName);
        }

        // recent students = last 3 added (end of insertion order approximated via inorder tail)
        gui.recentStudentsPanel.removeAll();
        int count = 0;
        for (int i = all.size() - 1; i >= 0 && count < 3; i--, count++) {
            gui.recentStudentsPanel.add(buildRecentRow(all.get(i)));
        }
        gui.recentStudentsPanel.revalidate();
        gui.recentStudentsPanel.repaint();
    }

    private JPanel buildRecentRow(StudentBST.Node n) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, StudentGUI.BORDER),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        // fixed height so BoxLayout.Y_AXIS doesn't stretch this row to fill leftover space
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] parts = n.grade.split("\\|");
        String course = parts[0];
        String grade = parts.length > 1 ? parts[1] : "";

        JLabel left = new JLabel(n.name + "  -  " + course);
        JLabel right = new JLabel(n.rollNumber + "   " + grade);
        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    // ---- refreshes the full records table ----
    private void refreshTable() {
        gui.tableModel.setRowCount(0);
        List<StudentBST.Node> all = tree.inorder();
        for (StudentBST.Node n : all) {
            addRowToTable(n);
        }
        gui.recordCountLabel.setText(all.size() + " students found");
    }

    private void addRowToTable(StudentBST.Node n) {
        String[] parts = n.grade.split("\\|");
        String course = parts[0];
        String grade = parts.length > 1 ? parts[1] : "";

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        actionPanel.setOpaque(false);
        JButton viewBtn = new JButton("View");
        JButton deleteBtn = new JButton("Delete");
        viewBtn.addActionListener(e -> handleView(n.rollNumber));
        deleteBtn.addActionListener(e -> handleDelete(n.rollNumber));
        actionPanel.add(viewBtn);
        actionPanel.add(deleteBtn);

        gui.tableModel.addRow(new Object[] {
                n.rollNumber, n.name, course, grade, actionPanel
        });
    }

    private int extractGrade(String stored) {
        String[] parts = stored.split("\\|");
        try {
            return Integer.parseInt(parts.length > 1 ? parts[1] : parts[0]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ---- FILE persistence: save all records ----
    private void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (StudentBST.Node n : tree.inorder()) {
                writer.write(n.rollNumber + "," + n.name + "," + n.grade);
                writer.newLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ---- FILE persistence: load records on startup ----
    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 3);
                if (data.length < 3) continue;
                int roll = Integer.parseInt(data[0]);
                tree.insert(roll, data[1], data[2]);
            }
        } catch (IOException | NumberFormatException ex) {
            ex.printStackTrace();
        }
    }
}

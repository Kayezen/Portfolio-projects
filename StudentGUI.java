import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StudentGUI extends JFrame {

    // ===== Colors (from Figma) =====
    static final Color BLUE = new Color(37, 99, 235);
    static final Color LIGHT_BG = new Color(246, 247, 250);
    static final Color SIDEBAR_BG = Color.WHITE;
    static final Color BORDER = new Color(228, 231, 235);
    static final Color TEXT_DARK = new Color(30, 30, 30);
    static final Color TEXT_GRAY = new Color(110, 116, 122);
    static final Color GREEN = new Color(22, 163, 74);
    static final Color RED = new Color(220, 53, 69);

    // ===== Layout containers =====
    CardLayout cardLayout = new CardLayout();
    JPanel mainPanel = new JPanel(cardLayout);

    // ===== Sidebar buttons =====
    JButton navDashboard, navRecords, navAddStudent, navBSTView;

    // ===== BST View page =====
    BSTVisualizerPanel treePanel;

    // ===== Dashboard page components =====
    JLabel totalStudentsValue, avgGradeValue, topGradeValue, topGradeName;
    JPanel recentStudentsPanel;

    // ===== Add Student page components =====
    JTextField idField, nameField, courseField, gradeField;
    JLabel previewId, previewName, previewCourse, previewGrade;
    JButton addStudentBtn, cancelBtn;

    // ===== Student Records page components =====
    JTextField searchField;
    DefaultTableModel tableModel;
    JTable table;
    JLabel recordCountLabel;

    StudentGUI() {
        setTitle("Student Records Management System");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);

        mainPanel.add(buildDashboardPage(), "DASHBOARD");
        mainPanel.add(buildAddStudentPage(), "ADD_STUDENT");
        mainPanel.add(buildRecordsPage(), "RECORDS");
        mainPanel.add(buildBSTViewPage(), "BST_VIEW");
        add(mainPanel, BorderLayout.CENTER);

        showPage("DASHBOARD");
    }

    // Lets the controller switch visible page
    void showPage(String name) {
        cardLayout.show(mainPanel, name);
    }

    // ================= SIDEBAR =================
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // header/logo area
        JPanel header = new JPanel();
        header.setBackground(SIDEBAR_BG);
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 20));
        header.setMaximumSize(new Dimension(220, 60));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel logo = new JLabel("\uD83D\uDC64"); // simple person icon placeholder
        logo.setOpaque(true);
        logo.setBackground(BLUE);
        logo.setForeground(Color.WHITE);
        logo.setHorizontalAlignment(JLabel.CENTER);
        logo.setPreferredSize(new Dimension(36, 36));
        JPanel titleBox = new JPanel();
        titleBox.setBackground(SIDEBAR_BG);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Student Records");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel subtitle = new JLabel("Management System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(TEXT_GRAY);
        titleBox.add(title);
        titleBox.add(subtitle);
        header.add(logo);
        header.add(titleBox);
        sidebar.add(header);

        // SIDEBAR_INSET is the single left-padding value shared by MENU label and every nav button,
        // so everything lines up under the header instead of drifting from mismatched paddings
        int SIDEBAR_INSET = 15;

        JLabel menuLabel = new JLabel("MENU");
        menuLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        menuLabel.setForeground(TEXT_GRAY);
        menuLabel.setBorder(BorderFactory.createEmptyBorder(10, SIDEBAR_INSET, 10, 0));
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(menuLabel);

        navDashboard = createNavButton("Dashboard", SIDEBAR_INSET);
        navRecords = createNavButton("Student Records", SIDEBAR_INSET);
        navAddStudent = createNavButton("Add Student", SIDEBAR_INSET);
        navBSTView = createNavButton("BST Visualizer", SIDEBAR_INSET);

        sidebar.add(navDashboard);
        sidebar.add(navRecords);
        sidebar.add(navAddStudent);
        sidebar.add(navBSTView);
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JButton createNavButton(String text, int leftInset) {
        JButton btn = new JButton(text);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        // stretch to full sidebar width instead of a fixed cap that clips longer labels
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        // single source of left padding (matches header/MENU inset) instead of stacked margin + text spaces
        btn.setBorder(BorderFactory.createEmptyBorder(8, leftInset, 8, 5));
        return btn;
    }

    // ================= DASHBOARD PAGE =================
    private JPanel buildDashboardPage() {
        JPanel page = new JPanel();
        page.setBackground(LIGHT_BG);
        page.setLayout(new BorderLayout());
        page.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // -- header --
        JPanel headerBox = new JPanel(new GridLayout(2, 1));
        headerBox.setBackground(LIGHT_BG);
        JLabel h1 = new JLabel("Dashboard");
        h1.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JLabel h2 = new JLabel("Overview of your student records");
        h2.setForeground(TEXT_GRAY);
        headerBox.add(h1);
        headerBox.add(h2);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(LIGHT_BG);
        top.add(headerBox, BorderLayout.NORTH);

        // -- stat cards --
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(LIGHT_BG);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        totalStudentsValue = new JLabel("0");
        avgGradeValue = new JLabel("0.0");
        topGradeValue = new JLabel("0");
        topGradeName = new JLabel("-");

        statsPanel.add(statCard("Total Students", totalStudentsValue, "enrolled records", BLUE));
        statsPanel.add(statCard("Average Grade", avgGradeValue, "class average", GREEN));
        statsPanel.add(statCard("Top Grade", topGradeValue, topGradeName, "Top student", BLUE));

        top.add(statsPanel, BorderLayout.CENTER);
        page.add(top, BorderLayout.NORTH);

        // -- recent students box --
        JPanel recentBox = new JPanel(new BorderLayout());
        recentBox.setBackground(Color.WHITE);
        recentBox.setBorder(BorderFactory.createLineBorder(BORDER));

        JPanel recentHeader = new JPanel(new BorderLayout());
        recentHeader.setBackground(Color.WHITE);
        recentHeader.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        JLabel recentTitle = new JLabel("Recent Students");
        recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        recentHeader.add(recentTitle, BorderLayout.WEST);
        recentBox.add(recentHeader, BorderLayout.NORTH);

        recentStudentsPanel = new JPanel();
        recentStudentsPanel.setLayout(new BoxLayout(recentStudentsPanel, BoxLayout.Y_AXIS));
        recentStudentsPanel.setBackground(Color.WHITE);
        recentBox.add(recentStudentsPanel, BorderLayout.CENTER);

        page.add(recentBox, BorderLayout.CENTER);

        // -- bottom action buttons --
        JPanel bottomButtons = new JPanel(new GridLayout(1, 2, 15, 0));
        bottomButtons.setBackground(LIGHT_BG);
        bottomButtons.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        JButton addBtn = new JButton("+ Add Student");
        styleFilledButton(addBtn);
        addBtn.addActionListener(e -> showPage("ADD_STUDENT"));
        JButton viewBtn = new JButton("View Records");
        styleOutlineButton(viewBtn);
        viewBtn.addActionListener(e -> showPage("RECORDS"));
        bottomButtons.add(addBtn);
        bottomButtons.add(viewBtn);
        page.add(bottomButtons, BorderLayout.SOUTH);

        return page;
    }

    private JPanel statCard(String label, JLabel valueLabel, String sub, Color accent) {
        return statCard(label, valueLabel, new JLabel(sub), sub, accent);
    }

    private JPanel statCard(String label, JLabel valueLabel, JLabel subLabel, String subText, Color accent) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(BORDER));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel top = new JLabel(label);
        top.setForeground(TEXT_GRAY);
        top.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(accent);

        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLabel.setForeground(TEXT_GRAY);

        card.add(top);
        card.add(Box.createVerticalStrut(5));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(3));
        card.add(subLabel);
        return card;
    }

    // ================= ADD STUDENT PAGE =================
    private JPanel buildAddStudentPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(LIGHT_BG);
        page.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JPanel headerBox = new JPanel(new GridLayout(2, 1));
        headerBox.setBackground(LIGHT_BG);
        JLabel h1 = new JLabel("Add Student");
        h1.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JLabel h2 = new JLabel("Fill in the form to register a new student");
        h2.setForeground(TEXT_GRAY);
        headerBox.add(h1);
        headerBox.add(h2);
        page.add(headerBox, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 20, 0));
        content.setBackground(LIGHT_BG);
        content.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // -- form box --
        JPanel formBox = new JPanel();
        formBox.setBackground(Color.WHITE);
        formBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));

        JLabel formTitle = new JLabel("Student Information");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formBox.add(formTitle);
        formBox.add(Box.createVerticalStrut(15));

        idField = new JTextField();
        nameField = new JTextField();
        courseField = new JTextField();
        gradeField = new JTextField();

        formBox.add(formField("STUDENT ID / ROLL NUMBER", idField, "e.g. 2024006"));
        formBox.add(formField("FULL NAME", nameField, "e.g. Pedro Santos"));
        formBox.add(formField("COURSE / PROGRAM", courseField, "e.g. BS Computer Science"));
        formBox.add(formField("GRADE (0-100)", gradeField, "e.g. 88"));

        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        formButtons.setBackground(Color.WHITE);
        formButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
        formButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        addStudentBtn = new JButton("Add Student");
        styleFilledButton(addStudentBtn);
        cancelBtn = new JButton("Cancel");
        styleOutlineButton(cancelBtn);
        formButtons.add(addStudentBtn);
        formButtons.add(cancelBtn);
        formBox.add(formButtons);

        // -- preview box --
        JPanel previewBox = new JPanel();
        previewBox.setLayout(new BoxLayout(previewBox, BoxLayout.Y_AXIS));
        previewBox.setBackground(LIGHT_BG);

        JPanel previewCard = new JPanel();
        previewCard.setBackground(Color.WHITE);
        previewCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        previewCard.setLayout(new BoxLayout(previewCard, BoxLayout.Y_AXIS));

        JLabel previewTitle = new JLabel("Preview");
        previewTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        previewCard.add(previewTitle);
        previewCard.add(Box.createVerticalStrut(15));

        previewId = new JLabel("\u2014");
        previewName = new JLabel("\u2014");
        previewCourse = new JLabel("\u2014");
        previewGrade = new JLabel("\u2014");

        previewCard.add(previewRow("Student ID", previewId));
        previewCard.add(previewRow("Name", previewName));
        previewCard.add(previewRow("Course", previewCourse));
        previewCard.add(previewRow("Grade", previewGrade));

        JPanel infoBox = new JPanel();
        infoBox.setLayout(new BoxLayout(infoBox, BoxLayout.Y_AXIS));
        infoBox.setBackground(new Color(235, 242, 255));
        infoBox.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel infoTitle = new JLabel("How it works");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextArea infoText = new JTextArea(
                "Records are stored in a Binary Search Tree keyed by Student ID. "
                        + "This supports fast insert, search, and delete operations.");
        infoText.setLineWrap(true);
        infoText.setWrapStyleWord(true);
        infoText.setEditable(false);
        infoText.setBackground(new Color(235, 242, 255));
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoBox.add(infoTitle);
        infoBox.add(Box.createVerticalStrut(5));
        infoBox.add(infoText);

        previewBox.add(previewCard);
        previewBox.add(Box.createVerticalStrut(15));
        previewBox.add(infoBox);

        content.add(formBox);
        content.add(previewBox);
        page.add(content, BorderLayout.CENTER);

        return page;
    }

    private JPanel formField(String label, JTextField field, String placeholderTooltip) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(Color.WHITE);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_GRAY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setToolTipText(placeholderTooltip);
        // stretch to fill the form width instead of floating center in the BoxLayout
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        field.setPreferredSize(new Dimension(300, 34));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        box.add(lbl);
        box.add(Box.createVerticalStrut(4));
        box.add(field);
        return box;
    }

    private JPanel previewRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        JLabel lbl = new JLabel(label);
        lbl.setForeground(TEXT_GRAY);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        row.add(lbl, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    // ================= RECORDS PAGE =================
    private JPanel buildRecordsPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(LIGHT_BG);
        page.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(LIGHT_BG);

        JPanel headerBox = new JPanel(new GridLayout(2, 1));
        headerBox.setBackground(LIGHT_BG);
        JLabel h1 = new JLabel("Student Records");
        h1.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JLabel h2 = new JLabel("Manage student information");
        h2.setForeground(TEXT_GRAY);
        headerBox.add(h1);
        headerBox.add(h2);
        top.add(headerBox, BorderLayout.WEST);

        JButton addBtn = new JButton("+ Add Student");
        styleFilledButton(addBtn);
        addBtn.addActionListener(e -> showPage("ADD_STUDENT"));
        JPanel addBtnWrap = new JPanel();
        addBtnWrap.setBackground(LIGHT_BG);
        addBtnWrap.add(addBtn);
        top.add(addBtnWrap, BorderLayout.EAST);

        page.add(top, BorderLayout.NORTH);

        JPanel centerBox = new JPanel(new BorderLayout());
        centerBox.setBackground(LIGHT_BG);
        centerBox.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        searchField = new JTextField();
        searchField.setToolTipText("Search student...");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setBackground(LIGHT_BG);
        searchWrap.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        searchWrap.add(searchField, BorderLayout.CENTER);
        centerBox.add(searchWrap, BorderLayout.NORTH);

        String[] columns = { "Student ID", "Name", "Course", "Grade", "" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(44);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(224, 234, 255));
        table.setSelectionForeground(TEXT_DARK);

        // grid lines between rows/columns instead of a flat blank table
        table.setShowGrid(true);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(1, 1));

        // header styling
        table.getTableHeader().setBackground(new Color(249, 250, 252));
        table.getTableHeader().setForeground(TEXT_GRAY);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        // zebra striping so rows are easy to scan instead of a plain white block
        DefaultTableCellRenderer stripedRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 252));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount() - 1; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(stripedRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        centerBox.add(scrollPane, BorderLayout.CENTER);

        recordCountLabel = new JLabel("0 students found");
        recordCountLabel.setForeground(TEXT_GRAY);
        recordCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        recordCountLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        centerBox.add(recordCountLabel, BorderLayout.SOUTH);

        page.add(centerBox, BorderLayout.CENTER);

        return page;
    }

    // ================= BST VIEW PAGE =================
    private JPanel buildBSTViewPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(LIGHT_BG);
        page.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JPanel headerBox = new JPanel(new GridLayout(2, 1));
        headerBox.setBackground(LIGHT_BG);
        JLabel h1 = new JLabel("BST Visualizer");
        h1.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JLabel h2 = new JLabel("How the students are arranged in the tree");
        h2.setForeground(TEXT_GRAY);
        headerBox.add(h1);
        headerBox.add(h2);
        page.add(headerBox, BorderLayout.NORTH);

        // treePanel starts empty; controller calls treePanel.refreshTree(tree) whenever data changes
        treePanel = new BSTVisualizerPanel(null);
        JScrollPane scrollPane = new JScrollPane(treePanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        page.add(scrollPane, BorderLayout.CENTER);

        return page;
    }

    // ================= shared button styles =================
    void styleFilledButton(JButton btn) {
        btn.setBackground(BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }

    void styleOutlineButton(JButton btn) {
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }
}

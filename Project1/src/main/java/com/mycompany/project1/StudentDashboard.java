/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.project1;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentDashboard extends JFrame {
    private JTable gradesTable;
    private JButton backButton;
    private JButton notificationsButton;
    private JLabel gpaLabel; // لعرض المعدل التراكمي
    private JPanel mainPanel;

    public StudentDashboard() {
        initComponents();
        loadGrades();
    }

    private void initComponents() {
        setTitle("Student Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        gradesTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(gradesTable);

        backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            this.dispose(); // Close the dashboard
            new LoginFrame().setVisible(true); // Go back to login screen
        });

        notificationsButton = new JButton("Show Notifications");
        notificationsButton.addActionListener(e -> showNotifications());

        gpaLabel = new JLabel("GPA: "); // لعرض المعدل التراكمي

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Bottom panel for buttons and GPA
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        bottomPanel.add(gpaLabel); // إضافة خانة GPA
        bottomPanel.add(notificationsButton);
        bottomPanel.add(backButton);

        // Add components to the main panel
        mainPanel.add(scrollPane, BorderLayout.CENTER); // جدول الدرجات في المنتصف
        mainPanel.add(bottomPanel, BorderLayout.SOUTH); // الأزرار في الأسفل

        add(mainPanel);
    }

    private void loadGrades() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // استعلام لجلب الدرجات
            String query = """
                SELECT c.course_name AS [Course],
                       g.assignment_score AS [Assignment],
                       g.quiz_score AS [Quiz],
                       g.exam_score AS [Exam],
                       g.final_score AS [Final Grade]
                FROM grades g
                JOIN courses c ON g.course_id = c.id
                JOIN users u ON g.student_id = u.id
                WHERE u.username = ?
            """;

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, Session.getUsername()); // تمرير اسم المستخدم النشط
            ResultSet resultSet = preparedStatement.executeQuery();

            // إعداد الجدول لعرض النتائج
            DefaultTableModel tableModel = new DefaultTableModel();
            tableModel.addColumn("Course");
            tableModel.addColumn("Assignment");
            tableModel.addColumn("Quiz");
            tableModel.addColumn("Exam");
            tableModel.addColumn("Final Grade");
            tableModel.addColumn("Grade Letter"); // عمود الدرجة بالحرف

            double totalGPA = 0; // مجموع النقاط لحساب المعدل
            int courseCount = 0; // عدد الدورات

            while (resultSet.next()) {
                double finalGrade = resultSet.getDouble("Final Grade");
                String gradeLetter = calculateGradeLetter(finalGrade);
                double gpa = calculateGPA(finalGrade);

                tableModel.addRow(new Object[]{
                    resultSet.getString("Course"),
                    resultSet.getDouble("Assignment"),
                    resultSet.getDouble("Quiz"),
                    resultSet.getDouble("Exam"),
                    finalGrade,
                    gradeLetter
                });

                totalGPA += gpa; // جمع النقاط لحساب المعدل
                courseCount++;
            }

            gradesTable.setModel(tableModel); // تعيين البيانات إلى الجدول

            // حساب المعدل التراكمي وعرضه في خانة GPA
            if (courseCount > 0) {
                double gpa = totalGPA / courseCount;
                gpaLabel.setText("GPA: " + String.format("%.2f", gpa)); // تحديث النص في الخانة
            } else {
                gpaLabel.setText("GPA: N/A");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading grades: " + e.getMessage());
        }
    }

   private void showNotifications() {
    try (Connection connection = DatabaseConnection.getConnection()) {
        // استعلام لجلب التنبيهات غير المقروءة
        String query = """
            SELECT n.message AS [Notification]
            FROM notifications n
            JOIN users u ON n.user_id = u.id
            WHERE u.username = ? AND (n.is_read IS NULL OR n.is_read = 0)
        """;

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, Session.getUsername());
        ResultSet resultSet = preparedStatement.executeQuery();

        StringBuilder notifications = new StringBuilder("Notifications:\n");
        while (resultSet.next()) {
            notifications.append("- ").append(resultSet.getString("Notification")).append("\n");
        }

        if (notifications.toString().equals("Notifications:\n")) {
            notifications.append("No new notifications.");
        }

        // عرض التنبيهات باستخدام JOptionPane
        JOptionPane.showMessageDialog(this, notifications.toString(), "Notifications", JOptionPane.INFORMATION_MESSAGE);

        // تحديث الإشعارات إلى "تمت قراءتها"
        String markAsReadQuery = """
            UPDATE notifications
            SET is_read = 1
            WHERE user_id = (SELECT id FROM users WHERE username = ?)
        """;
        PreparedStatement markAsReadStatement = connection.prepareStatement(markAsReadQuery);
        markAsReadStatement.setString(1, Session.getUsername());
        markAsReadStatement.executeUpdate();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading notifications: " + e.getMessage());
    }
}

   private String calculateGradeLetter(double finalGrade) {
        if (finalGrade >= 95) {
               return "A+";
            } else if (finalGrade >= 90) {
               return "A";
            } else if (finalGrade >= 85) {
              return "B+";
            } else if (finalGrade >= 80) {
                return "B";
            } else if (finalGrade >= 75) {
               return "C+";
            }else if (finalGrade >= 70) {
             return "C";
        }   else if (finalGrade >= 65) {
                return "D+";
        }else if (finalGrade >= 60) {
                return "D";
        }else {
                return "F";
            }
    }

    private double calculateGPA(double finalGrade) {
        if (finalGrade >= 95) {
            return 5.0;
        } else if (finalGrade >= 90) {
            return 4.50;
        } else if (finalGrade >= 85) {
            return 4.0;
        } else if (finalGrade >= 80) {
            return 3.50;
        }else if (finalGrade >= 75) {
            return 3.0;
        }else if (finalGrade >= 70) {
            return 2.50;
        } else if (finalGrade >= 65) {
            return 2.0;
        }else if (finalGrade >= 60) {
            return 1.50;
        }else {
            return 0.0;
        }
    }
}

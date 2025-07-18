/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.project1;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class ManageGradesFrame extends JFrame {
    private JComboBox<String> studentComboBox, courseComboBox;
    private JTextField assignmentField, quizField, examField;
    private JButton saveButton, calculateButton, backButton;

    public ManageGradesFrame() {
        setTitle("Manage Grades");
        setSize(600, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // العنوان
        JPanel headerPanel = new JPanel();
        JLabel titleLabel = new JLabel("<html><h1>Manage Grades</h1></html>", SwingConstants.CENTER);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // لوحة الإدخال
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(6, 2, 10, 10));

        inputPanel.add(new JLabel("Select Student:"));
        studentComboBox = new JComboBox<>();
        loadStudents();
        inputPanel.add(studentComboBox);

        inputPanel.add(new JLabel("Select Course:"));
        courseComboBox = new JComboBox<>();
        loadCourses();
        inputPanel.add(courseComboBox);

        inputPanel.add(new JLabel("Assignment Score:"));
        assignmentField = new JTextField();
        inputPanel.add(assignmentField);

        inputPanel.add(new JLabel("Quiz Score:"));
        quizField = new JTextField();
        inputPanel.add(quizField);

        inputPanel.add(new JLabel("Exam Score:"));
        examField = new JTextField();
        inputPanel.add(examField);

        add(inputPanel, BorderLayout.CENTER);

        // لوحة الأزرار
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 10, 10));

        saveButton = new JButton("Save Grades");
        saveButton.addActionListener(e -> saveGrades());
        buttonPanel.add(saveButton);

        calculateButton = new JButton("Calculate Final Grade");
        calculateButton.addActionListener(e -> calculateFinalGrade());
        buttonPanel.add(calculateButton);

        backButton = new JButton("Back");
        backButton.addActionListener(e -> dispose());
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void loadStudents() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT username FROM users WHERE role = 'student'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                studentComboBox.addItem(resultSet.getString("username"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());
        }
    }

    private void loadCourses() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT course_name FROM courses";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                courseComboBox.addItem(resultSet.getString("course_name"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage());
        }
        
    }
   private Map<String, Double> parseGradingPolicy(String gradingPolicy) {
    Map<String, Double> policyMap = new HashMap<>();
    try {
        // إزالة الأقواس إذا وجدت
        gradingPolicy = gradingPolicy.replace("{", "").replace("}", "").replace("\"", "");
        String[] parts = gradingPolicy.split(",");

        for (String part : parts) {
            String[] keyValue = part.split(":");
            String key = keyValue[0].trim(); // اسم العنصر (assignment, quiz, exam)
            double value = Double.parseDouble(keyValue[1].trim()); // القيمة
            policyMap.put(key, value);
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error parsing grading policy: " + e.getMessage());
    }
    return policyMap;
}

private Map<String, Double> getGradingPolicy(String courseName) {
    Map<String, Double> gradingPolicy = new HashMap<>();
    try (Connection connection = DatabaseConnection.getConnection()) {
        String query = "SELECT grading_policy FROM courses WHERE course_name = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, courseName);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            String policyText = resultSet.getString("grading_policy");
            if (policyText == null || policyText.isEmpty()) {
                throw new IllegalArgumentException("Grading policy is missing for course: " + courseName);
            }
            gradingPolicy = parseGradingPolicy(policyText);
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error fetching grading policy: " + e.getMessage());
    }
    return gradingPolicy;
}
    private boolean validateScores(double assignmentScore, double quizScore, double examScore, String courseName) {
        Map<String, Double> gradingPolicy = getGradingPolicy(courseName);

        double maxAssignmentScore = gradingPolicy.getOrDefault("assignment", 0.0);
        double maxQuizScore = gradingPolicy.getOrDefault("quiz", 0.0);
        double maxExamScore = gradingPolicy.getOrDefault("exam", 0.0);

        if (assignmentScore > maxAssignmentScore || quizScore > maxQuizScore || examScore > maxExamScore) {
            JOptionPane.showMessageDialog(this, "Error: Scores exceed the allowed distribution!");
            return false;
        }

        return true;
    }
  private void saveGrades() {
    try (Connection connection = DatabaseConnection.getConnection()) {
        // جلب القيم من الحقول
        String studentUsername = (String) studentComboBox.getSelectedItem();
        String courseName = (String) courseComboBox.getSelectedItem();
        double assignmentScore = Double.parseDouble(assignmentField.getText());
        double quizScore = Double.parseDouble(quizField.getText());
        double examScore = Double.parseDouble(examField.getText());
        // التحقق من صحة القيم
        if (!validateScores(assignmentScore, quizScore, examScore, courseName)) {
            return; // إذا لم تكن القيم صالحة، يتم الإنهاء
        }
        double finalScore = calculateFinalScore(assignmentScore, quizScore, examScore);

        // التحقق إذا كانت البيانات موجودة مسبقاً
        String checkQuery = """
            SELECT COUNT(*) FROM grades
            WHERE student_id = (SELECT id FROM users WHERE username = ?)
              AND course_id = (SELECT id FROM courses WHERE course_name = ?)
        """;

        PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
        checkStatement.setString(1, studentUsername);
        checkStatement.setString(2, courseName);
        ResultSet resultSet = checkStatement.executeQuery();

        boolean recordExists = false;
        if (resultSet.next()) {
            recordExists = resultSet.getInt(1) > 0;
        }

        // استعلام لإضافة أو تحديث الدرجات
        String updateGradesQuery;
        if (recordExists) {
            updateGradesQuery = """
                UPDATE grades
                SET assignment_score = ?, quiz_score = ?, exam_score = ?, final_score = ?
                WHERE student_id = (SELECT id FROM users WHERE username = ?)
                  AND course_id = (SELECT id FROM courses WHERE course_name = ?)
            """;
        } else {
            updateGradesQuery = """
                INSERT INTO grades (student_id, course_id, assignment_score, quiz_score, exam_score, final_score)
                VALUES (
                    (SELECT id FROM users WHERE username = ?),
                    (SELECT id FROM courses WHERE course_name = ?),
                    ?, ?, ?, ?
                )
            """;
        }

        PreparedStatement gradesStatement = connection.prepareStatement(updateGradesQuery);

        if (recordExists) {
            gradesStatement.setDouble(1, assignmentScore);
            gradesStatement.setDouble(2, quizScore);
            gradesStatement.setDouble(3, examScore);
            gradesStatement.setDouble(4, finalScore);
            gradesStatement.setString(5, studentUsername);
            gradesStatement.setString(6, courseName);
        } else {
            gradesStatement.setString(1, studentUsername);
            gradesStatement.setString(2, courseName);
            gradesStatement.setDouble(3, assignmentScore);
            gradesStatement.setDouble(4, quizScore);
            gradesStatement.setDouble(5, examScore);
            gradesStatement.setDouble(6, finalScore);
        }

        gradesStatement.executeUpdate();

        // إضافة تنبيه للطالب
        String notificationQuery = """
            INSERT INTO notifications (user_id, message)
            VALUES (
                (SELECT id FROM users WHERE username = ?),
                'Your grades for the course ' & ? & ' have been updated.'
            )
        """;

        PreparedStatement notificationStatement = connection.prepareStatement(notificationQuery);
        notificationStatement.setString(1, studentUsername);
        notificationStatement.setString(2, courseName);
        notificationStatement.executeUpdate();

        JOptionPane.showMessageDialog(this, "Grades saved and notification sent to the student.");
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error saving grades: " + e.getMessage());
    }
}
private double calculateFinalScore(double assignmentScore, double quizScore, double examScore) {
    return assignmentScore  + quizScore  + examScore ; // مثال لحساب الدرجة النهائية
}

    private void calculateFinalGrade() {
    String student = (String) studentComboBox.getSelectedItem();
    String course = (String) courseComboBox.getSelectedItem();

    try (Connection connection = DatabaseConnection.getConnection()) {
        // جلب الدرجات وسياسة التقييم
        String query = """
            SELECT g.assignment_score, g.quiz_score, g.exam_score, c.grading_policy
            FROM grades g
            JOIN courses c ON g.course_id = c.id
            WHERE g.student_id = (SELECT id FROM users WHERE username = ?)
            AND g.course_id = (SELECT id FROM courses WHERE course_name = ?)
        """;

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, student);
        preparedStatement.setString(2, course);

        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            double assignmentScore = resultSet.getDouble("assignment_score");
            double quizScore = resultSet.getDouble("quiz_score");
            double examScore = resultSet.getDouble("exam_score");
            String gradingPolicy = resultSet.getString("grading_policy");

            // معالجة سياسة التقييم
            gradingPolicy = gradingPolicy.replace("{", "").replace("}", "").replace("\"", "");
            String[] policies = gradingPolicy.split(",");

            double assignmentWeight = 0, quizWeight = 0, examWeight = 0;
            for (String part : policies) {
                String[] keyValue = part.split(":");
                switch (keyValue[0].trim()) {
                    case "assignment" -> assignmentWeight = Double.parseDouble(keyValue[1].trim().replace(",", "."));
                    case "quiz" -> quizWeight = Double.parseDouble(keyValue[1].trim().replace(",", "."));
                    case "exam" -> examWeight = Double.parseDouble(keyValue[1].trim().replace(",", "."));
                }
            }

            // حساب الدرجة النهائية
            double finalGrade = (assignmentScore) +
                                (quizScore) +
                                (examScore);

            // تحديد الحرف (Letter Grade)
            String gradeLetter;
            if (finalGrade >= 95) {
                gradeLetter = "A+";
            } else if (finalGrade >= 90) {
                gradeLetter = "A";
            } else if (finalGrade >= 85) {
                gradeLetter = "B+";
            } else if (finalGrade >= 80) {
                gradeLetter = "B";
            } else if (finalGrade >= 75) {
                gradeLetter = "C+";
            }else if (finalGrade >= 70) {
             gradeLetter = "C";
        }   else if (finalGrade >= 65) {
                gradeLetter = "D+";
        }else if (finalGrade >= 60) {
                gradeLetter = "D";
        }else {
                gradeLetter = "F";
            }

            // تحديث الدرجة النهائية وحرف التقدير في الجدول
            String updateQuery = """
                UPDATE grades
                SET final_score = ?, grade_letter = ?
                WHERE student_id = (SELECT id FROM users WHERE username = ?)
                AND course_id = (SELECT id FROM courses WHERE course_name = ?)
            """;
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            updateStatement.setDouble(1, finalGrade);
            updateStatement.setString(2, gradeLetter);
            updateStatement.setString(3, student);
            updateStatement.setString(4, course);
            updateStatement.executeUpdate();

            JOptionPane.showMessageDialog(this, "Final Grade: " + finalGrade + " (" + gradeLetter + ")");
        } else {
            JOptionPane.showMessageDialog(this, "No grades found for the selected student and course.");
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error calculating final grade: " + e.getMessage());
    }
}
}

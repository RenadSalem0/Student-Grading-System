/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.project1;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.awt.event.*;
public class EnrollStudentFrame extends JFrame {
    private JComboBox<String> studentComboBox;
    private JComboBox<String> coursesComboBox;
    private JButton enrollButton,backButton;
    private Connection connection;

    public EnrollStudentFrame() {
        setTitle("Enroll Student in Courses");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(3, 1,10,10)); // تعديل الشبكة لتتناسب مع عدد العناصر

        try {
            // إنشاء اتصال بقاعدة البيانات
            connection = DatabaseConnection.getConnection();

            studentComboBox = new JComboBox<>();
            coursesComboBox = new JComboBox<>();

            // تعبئة البيانات
            populateStudentComboBox();
            populateCoursesComboBox();

            // إضافة العناصر إلى واجهة المستخدم
            add(new JLabel("Select Student:"));
            add(studentComboBox);
            add(new JLabel("Select Course:"));
            add(coursesComboBox);

            // زر التسجيل
            enrollButton = new JButton("Enroll");
            enrollButton.addActionListener(e -> enrollStudent());
            add(enrollButton);
            
            backButton = new JButton("Back");
            backButton.addActionListener(e -> dispose());
            add(backButton);

            
            setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error initializing the frame: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateStudentComboBox() throws SQLException {
        String query = "SELECT username FROM users WHERE role = 'student'";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                studentComboBox.addItem(rs.getString("username"));
            }
        }
    }

    private void populateCoursesComboBox() throws SQLException {
        String query = "SELECT course_name FROM courses";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                coursesComboBox.addItem(rs.getString("course_name"));
            }
        }
    }

    private void enrollStudent() {
        String studentName = (String) studentComboBox.getSelectedItem();
        String courseName = (String) coursesComboBox.getSelectedItem();

        if (studentName == null || courseName == null) {
            JOptionPane.showMessageDialog(this, "Please select both a student and a course.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // استرجاع معرف الطالب
            String getStudentIdQuery = "SELECT id FROM users WHERE username = ?";
            int studentId;
            try (PreparedStatement ps = connection.prepareStatement(getStudentIdQuery)) {
                ps.setString(1, studentName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentId = rs.getInt("id");
                    } else {
                        throw new SQLException("Student not found.");
                    }
                }
            }

            // استرجاع معرف الدورة
            String getCourseIdQuery = "SELECT id FROM courses WHERE course_name = ?";
            int courseId;
            try (PreparedStatement ps = connection.prepareStatement(getCourseIdQuery)) {
                ps.setString(1, courseName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        courseId = rs.getInt("id");
                    } else {
                        throw new SQLException("Course not found.");
                    }
                }
            }

            // إدخال بيانات التسجيل
            String insertEnrollmentQuery = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(insertEnrollmentQuery)) {
                ps.setInt(1, studentId);
                ps.setInt(2, courseId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Student successfully enrolled in " + courseName + ".");
                dispose();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error enrolling student: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}

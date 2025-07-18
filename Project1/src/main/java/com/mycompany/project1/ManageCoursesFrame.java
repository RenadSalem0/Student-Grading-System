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

public class ManageCoursesFrame extends JFrame {
    private JTextField courseNameField, assignmentField, quizField, examField;
    private JButton addCourseButton, updateCourseButton, backButton;
    private JComboBox<String> courseListComboBox;

    public ManageCoursesFrame() {
        setTitle("Manage Courses");
        setSize(500, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // إنشاء لوحة الإدخال
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(5, 2, 10, 10));

        inputPanel.add(new JLabel("Course Name:"));
        courseNameField = new JTextField();
        inputPanel.add(courseNameField);

        inputPanel.add(new JLabel("Assignment %:"));
        assignmentField = new JTextField();
        inputPanel.add(assignmentField);

        inputPanel.add(new JLabel("Quiz %:"));
        quizField = new JTextField();
        inputPanel.add(quizField);

        inputPanel.add(new JLabel("Exam %:"));
        examField = new JTextField();
        inputPanel.add(examField);

        inputPanel.add(new JLabel("Select Course:"));
        courseListComboBox = new JComboBox<>();
        loadCourses();
        courseListComboBox.addActionListener(e -> loadSelectedCourse());
        inputPanel.add(courseListComboBox);

        add(inputPanel, BorderLayout.CENTER);

        // إنشاء لوحة الأزرار
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 10, 10));

        addCourseButton = new JButton("Add Course");
        addCourseButton.addActionListener(e -> addCourse());
        buttonPanel.add(addCourseButton);

        updateCourseButton = new JButton("Update Course");
        updateCourseButton.addActionListener(e -> updateCourse());
        buttonPanel.add(updateCourseButton);

        backButton = new JButton("Back");
        backButton.addActionListener(e -> dispose());
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // ضبط التنسيق
        JPanel headerPanel = new JPanel();
        headerPanel.add(new JLabel("<html><h2>Manage Courses</h2></html>"));
        add(headerPanel, BorderLayout.NORTH);

        setVisible(true);
    }

    private void loadCourses() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT course_name FROM courses";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            courseListComboBox.removeAllItems();
            while (resultSet.next()) {
                courseListComboBox.addItem(resultSet.getString("course_name"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage());
        }
    }

    private void loadSelectedCourse() {
        String selectedCourse = (String) courseListComboBox.getSelectedItem();
        if (selectedCourse == null) return;

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT grading_policy FROM courses WHERE course_name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, selectedCourse);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String policyJson = resultSet.getString("grading_policy");
                String[] parts = policyJson.replace("{", "").replace("}", "").replace("\"", "").split(",");
                for (String part : parts) {
                    String[] keyValue = part.split(":");
                    switch (keyValue[0].trim()) {
                        case "assignment" -> assignmentField.setText(keyValue[1].trim());
                        case "quiz" -> quizField.setText(keyValue[1].trim());
                        case "exam" -> examField.setText(keyValue[1].trim());
                    }
                }
                courseNameField.setText(selectedCourse);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading course details: " + e.getMessage());
        }
    }

    private void addCourse() {
        String courseName = courseNameField.getText();
        int assignment, quiz, exam;

        // التحقق من صحة الإدخال
        try {
            assignment = Integer.parseInt(assignmentField.getText());
            quiz = Integer.parseInt(quizField.getText());
            exam = Integer.parseInt(examField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for the grading percentages.");
            return;
        }

        if (assignment + quiz + exam != 100) {
            JOptionPane.showMessageDialog(this, "Grading policy percentages must sum to 100.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO courses (course_name, grading_policy) VALUES (?, ?)";
            String gradingPolicy = String.format("{\"assignment\": %d, \"quiz\": %d, \"exam\": %d}", assignment, quiz, exam);

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courseName);
            preparedStatement.setString(2, gradingPolicy);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Course added successfully!");
                loadCourses(); // تحديث قائمة الكورسات
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add course.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding course: " + e.getMessage());
        }
    }

    private void updateCourse() {
        String courseName = courseNameField.getText();
        int assignment, quiz, exam;

        // التحقق من صحة الإدخال
        try {
            assignment = Integer.parseInt(assignmentField.getText());
            quiz = Integer.parseInt(quizField.getText());
            exam = Integer.parseInt(examField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for the grading percentages.");
            return;
        }

        if (assignment + quiz + exam != 100) {
            JOptionPane.showMessageDialog(this, "Grading policy percentages must sum to 100.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "UPDATE courses SET grading_policy = ? WHERE course_name = ?";
            String gradingPolicy = String.format("{\"assignment\": %d, \"quiz\": %d, \"exam\": %d}", assignment, quiz, exam);

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, gradingPolicy);
            preparedStatement.setString(2, courseName);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Course updated successfully!");
                loadCourses(); // تحديث قائمة الكورسات
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update course.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating course: " + e.getMessage());
        }
    }
}
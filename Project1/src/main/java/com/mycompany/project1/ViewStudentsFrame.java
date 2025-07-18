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
import javax.swing.table.DefaultTableModel;

public class ViewStudentsFrame extends JFrame {
    private JComboBox<String> courseComboBox;
    private JTable studentsTable;
    private JButton loadButton, backButton;

    public ViewStudentsFrame() {
        setTitle("View Enrolled Students");
        setSize(600, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(1, 3));

        courseComboBox = new JComboBox<>();
        loadCourses();

        loadButton = new JButton("Load Students");
        loadButton.addActionListener(e -> loadStudents());

        backButton = new JButton("Back");
        backButton.addActionListener(e -> dispose());

        topPanel.add(new JLabel("Select Course:"));
        topPanel.add(courseComboBox);
        topPanel.add(loadButton);

        add(topPanel, BorderLayout.NORTH);

        studentsTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(studentsTable);
        add(scrollPane, BorderLayout.CENTER);

        add(backButton, BorderLayout.SOUTH);

        setVisible(true);
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
    }private void loadStudents() {
    String selectedCourse = (String) courseComboBox.getSelectedItem();

    try (Connection connection = DatabaseConnection.getConnection()) {
        // الاستعلام المعدل
        String query = """
            SELECT u.username AS [Student Name],
                   g.assignment_score AS [Assignment],
                   g.quiz_score AS [Quiz],
                   g.exam_score AS [Exam],
                   g.final_score AS [Final Grade]
            FROM grades g
            JOIN users u ON g.student_id = u.id
            JOIN courses c ON g.course_id = c.id
            WHERE c.course_name = ?
        """;

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, selectedCourse);

        ResultSet resultSet = preparedStatement.executeQuery();

        // معالجة البيانات وتحميلها في الجدول
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Student Name");
        tableModel.addColumn("Assignment");
        tableModel.addColumn("Quiz");
        tableModel.addColumn("Exam");
        tableModel.addColumn("Final Grade");

        while (resultSet.next()) {
            tableModel.addRow(new Object[]{
                resultSet.getString("Student Name"),
                resultSet.getDouble("Assignment"),
                resultSet.getDouble("Quiz"),
                resultSet.getDouble("Exam"),
                resultSet.getDouble("Final Grade")
            });
        }

        studentsTable.setModel(tableModel);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());
    }
}
}
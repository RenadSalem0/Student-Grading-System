/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.project1;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class ManageStudentsFrame extends JFrame {
    private JTable studentTable;
    private DefaultTableModel tableModel;

    public ManageStudentsFrame() {
        setTitle("Manage Students");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // إنشاء نموذج الجدول
        String[] columnNames = {"ID", "Username", "Email", "Role"};
        tableModel = new DefaultTableModel(columnNames, 0);
        studentTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(studentTable);
        add(scrollPane, BorderLayout.CENTER);

        // تحميل الطلاب من قاعدة البيانات
        loadStudents();

        // لوحة الأزرار
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 10, 10));

        JButton addButton = new JButton("Add Student");
        addButton.addActionListener(e -> addStudent());
        buttonPanel.add(addButton);

        JButton editButton = new JButton("Edit Student");
        editButton.addActionListener(e -> editStudent());
        buttonPanel.add(editButton);

        JButton deleteButton = new JButton("Delete Student");
        deleteButton.addActionListener(e -> deleteStudent());
        buttonPanel.add(deleteButton);
        
         JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> dispose());
        buttonPanel.add(backButton);
        
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void loadStudents() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT id, username, email, role FROM users WHERE role = 'student'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            tableModel.setRowCount(0); // تفريغ الجدول قبل التحميل
            while (resultSet.next()) {
                Object[] row = {
                    resultSet.getInt("id"),
                    resultSet.getString("username"),
                    resultSet.getString("email"),
                    resultSet.getString("role")
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());
        }
    }

    private void addStudent() {
        String username = JOptionPane.showInputDialog(this, "Enter Username:");
        String email = JOptionPane.showInputDialog(this, "Enter Email:");
        String password = JOptionPane.showInputDialog(this, "Enter Password:");

        if (username == null ||  password == null) {
            return; // تم الإلغاء
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, 'student')";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, password);
            preparedStatement.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student added successfully.");
            loadStudents();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding student: " + e.getMessage());
        }
    }

    private void editStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to edit.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        String email = (String) tableModel.getValueAt(selectedRow, 2);

        String newUsername = JOptionPane.showInputDialog(this, "Edit Username:", username);
        String newEmail = JOptionPane.showInputDialog(this, "Edit Email:", email);

        if (newUsername == null || newEmail == null) {
            return; // تم الإلغاء
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "UPDATE users SET username = ?, email = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, newUsername);
            preparedStatement.setString(2, newEmail);
            preparedStatement.setInt(3, id);
            preparedStatement.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student updated successfully.");
            loadStudents();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating student: " + e.getMessage());
        }
    }

    private void deleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this student?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM users WHERE id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, id);
                preparedStatement.executeUpdate();

                JOptionPane.showMessageDialog(this, "Student deleted successfully.");
                loadStudents();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting student: " + e.getMessage());
            }
        }
    }
}

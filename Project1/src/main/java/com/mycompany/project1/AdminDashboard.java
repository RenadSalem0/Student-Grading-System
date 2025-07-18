/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.project1;
import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {
    private JLabel welcomeLabel;
    private JButton manageStudentsButton, manageCoursesButton, manageGradingButton,enrollStudentButton, logoutButton;

    public AdminDashboard() {
        setTitle("Admin Dashboard - " + Session.getUsername());
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(6, 1, 10, 10)); // تعديل التخطيط ليشمل الوظائف الجديدة

        // رسالة الترحيب
        welcomeLabel = new JLabel("Welcome, " + Session.getUsername(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(welcomeLabel);

        // زر إدارة ملفات الطلاب
        manageStudentsButton = new JButton("Manage Students");
        manageStudentsButton.addActionListener(e -> new ManageStudentsFrame());
        add(manageStudentsButton);

        // زر إدارة الدورات
        manageCoursesButton = new JButton("Manage Courses");
        manageCoursesButton.addActionListener(e -> new ManageCoursesFrame());
        add(manageCoursesButton);
        
        // زر التسجيل في المواد
        enrollStudentButton = new JButton("Enroll Student in Course");
        enrollStudentButton.addActionListener(e -> new EnrollStudentFrame());
        add(enrollStudentButton);
        // زر تسجيل الخروج
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            Session.clear();
            new LoginFrame();
            dispose();
        });
        add(logoutButton);

        setVisible(true);
    }
}
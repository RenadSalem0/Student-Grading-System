/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.project1;
import javax.swing.*;
import java.awt.*;

public class TeacherDashboard extends JFrame {
    private JLabel welcomeLabel;
    private JButton manageGradesButton, viewStudentsButton, logoutButton;

    public TeacherDashboard() {
        setTitle("Teacher Dashboard - " + Session.getUsername());
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1));

        // الترحيب
        welcomeLabel = new JLabel("Welcome, " + Session.getUsername(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(welcomeLabel);

        // إدارة الدرجات
        manageGradesButton = new JButton("Manage Grades");
        manageGradesButton.addActionListener(e -> new ManageGradesFrame());
        add(manageGradesButton);

        // عرض الطلاب
        viewStudentsButton = new JButton("View Enrolled Students");
        viewStudentsButton.addActionListener(e -> new ViewStudentsFrame());
        add(viewStudentsButton);

        // تسجيل الخروج
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
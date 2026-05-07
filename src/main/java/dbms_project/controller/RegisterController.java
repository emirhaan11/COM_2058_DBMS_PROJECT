package dbms_project.controller;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

import dbms_project.database.JDBCConnectivity;

import java.io.IOException;
import java.sql.*;

import org.mindrot.jbcrypt.BCrypt;

public class RegisterController {

    @FXML public TextField empCompanyName;
    @FXML private VBox employerForm;
    @FXML private VBox seekerForm;
    @FXML private TextField empMail, empTitle, seekMail, seekFirst, seekLast;
    @FXML private PasswordField empPass, seekPass;

    @FXML
    public void showEmployerForm() {
        if (!employerForm.isVisible()) {
            employerForm.setVisible(true);
            seekerForm.setVisible(false);
            animateForm(employerForm, 300); // Sağdan geliyor
        }
    }

    @FXML
    public void showSeekerForm() {
        if (!seekerForm.isVisible()) {
            seekerForm.setVisible(true);
            employerForm.setVisible(false);
            animateForm(seekerForm, -300); // Soldan geliyor
        }
    }

    private void animateForm(VBox form, double fromX) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), form);
        tt.setFromX(fromX);
        tt.setToX(0);
        tt.play();
    }

    @FXML
    protected void registerEmployer() {
        String email = empMail.getText();
        String password = empPass.getText();
        String jobTitle = empTitle.getText();
        String companyName = empCompanyName.getText();

        if (companyName.isEmpty()) {
            showAlert("Error", "Company name cannot be left blank.");
            return;
        }

        saveEmployer(email, password, jobTitle, companyName);
    }

    @FXML
    protected void registerSeeker() {
        String email = seekMail.getText();
        String password = seekPass.getText();
        String fname = seekFirst.getText();
        String lname = seekLast.getText();

        saveSeeker(email, password, fname, lname);
    }

    private void saveEmployer(String email, String password, String jobTitle, String companyName) {
        String userSql = "INSERT INTO user (Email, Password_Hash, User_Type, Registration_Date) VALUES (?, ?, 'Employer', NOW())";
        String empSql = "INSERT INTO employer (User_ID, Job_Title) VALUES (?, ?)";
        String compSql = "INSERT INTO company (Name, User_ID) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = JDBCConnectivity.getConnection();
            conn.setAutoCommit(false); // Transaction başlat

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            // USER tablosuna ekle
            PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, email);
            userStmt.setString(2, hashedPassword);
            userStmt.executeUpdate();

            ResultSet rs = userStmt.getGeneratedKeys();
            int userId = 0;
            if (rs.next()) {
                userId = rs.getInt(1);
            }

            // EMPLOYER a ekle
            PreparedStatement empStmt = conn.prepareStatement(empSql);
            empStmt.setInt(1, userId);
            empStmt.setString(2, jobTitle);
            empStmt.executeUpdate();

            // COMPANY e ekle
            PreparedStatement compStmt = conn.prepareStatement(compSql);
            compStmt.setString(1, companyName);
            compStmt.setInt(2, userId);
            compStmt.executeUpdate();

            conn.commit();
            System.out.println("Employer Registration Successful!");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful!");
            alert.setHeaderText(null);
            alert.setContentText("Your employer and company registration has been successfully completed!");
            alert.showAndWait();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    private void saveSeeker(String email, String password, String fname, String lname) {
        String userSql = "INSERT INTO user (Email, Password_Hash, User_Type, Registration_Date) VALUES (?, ?, 'JobSeeker', NOW())";
        String seekerSql = "INSERT INTO jobseeker (User_ID, FirstName, LastName) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = JDBCConnectivity.getConnection();
            conn.setAutoCommit(false); // Transaction başlat

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            // 1. USER a ekle
            PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, email);
            userStmt.setString(2, hashedPassword);
            userStmt.executeUpdate();

            ResultSet rs = userStmt.getGeneratedKeys();
            int userId = 0;
            if (rs.next()) {
                userId = rs.getInt(1);
            }

            // JOBSEEKER a ekle
            PreparedStatement seekerStmt = conn.prepareStatement(seekerSql);
            seekerStmt.setInt(1, userId);
            seekerStmt.setString(2, fname);
            seekerStmt.setString(3, lname);
            seekerStmt.executeUpdate();

            conn.commit();
            System.out.println("Seeker Registration Successful!");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful!");
            alert.setHeaderText(null);
            alert.setContentText("Your job seeker registration has been successfully completed!");
            alert.showAndWait();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleBackToLogin(ActionEvent event) {
        try {
            // Login ekranını yükle
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));

            // Mevcut pencereyi (stage) al
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Yeni sahneyi yerleştir
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Hata olursa kullanıcıya bildir
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("An error occurred while returning to the login screen.");
            alert.showAndWait();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
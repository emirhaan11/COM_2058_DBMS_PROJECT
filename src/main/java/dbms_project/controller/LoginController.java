package dbms_project.controller;

import dbms_project.database.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

import dbms_project.database.JDBCConnectivity;

import java.io.IOException;
import java.sql.*;

import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;


public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML
    protected void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (validateLogin(email, password)) {
            System.out.println("Login successful! You are being redirected to the main page.");
            String type = JDBCConnectivity.getDatabaseUserType(email);
            int id = JDBCConnectivity.getDatabaseUserID(email);
            UserSession.setSession(id, type);
            if (type.equals("Employer")) {
                loadScene("/employer_dashboard.fxml");
            } else {
                loadScene("/seeker_dashboard.fxml");
            }
            // Yeni sahneyi (Scene) yükle
        } else {
            // Kullanıcıya hata mesajı göster (Alert veya Label ile)
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setContentText("Invalid email or password.");
            alert.showAndWait();
        }
    }


    public boolean validateLogin(String email, String inputPassword) {
        String sql = "SELECT Password_Hash FROM User WHERE Email = ?";

        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPasswordHash = rs.getString("Password_Hash");

                if (BCrypt.checkpw(inputPassword, storedPasswordHash)) {
                    System.out.println("Login Successful!");
                    return true;
                } else {
                    System.out.println("Wrong Password!");
                    return false;
                }

            } else {
                // Email veritabanında yok
                System.out.println("Error: No registered user found with this email.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    protected void handleRegister() {
        loadScene("/register.fxml");
    }

    private void loadScene(String s) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(s));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
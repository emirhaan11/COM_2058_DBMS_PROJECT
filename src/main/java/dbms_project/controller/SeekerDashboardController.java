package dbms_project.controller;

import dbms_project.database.JDBCConnectivity;
import dbms_project.database.UserSession;
import dbms_project.model.Education;
import dbms_project.model.Experience;
import dbms_project.model.JobPosting;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.sql.*;
import java.io.File;

public class SeekerDashboardController {
    @FXML private TextField fnameField, lnameField;
    @FXML private Label cvPathLabel;
    @FXML private TextField instField, degreeField;
    @FXML private TextField expCompField, expRoleField;

    // eğitim tablosu
    @FXML private TableView<Education> educationTable;
    @FXML private TableColumn<Education, String> colInstitution;
    @FXML private TableColumn<Education, String> colDegree;

    // deneyim tablosu
    @FXML private TableView<Experience> experienceTable;
    @FXML private TableColumn<Experience, String> colExpCompany;
    @FXML private TableColumn<Experience, String> colRole;

    // iş ilanları
    @FXML private TableView<JobPosting> jobPostingTable;
    @FXML private TableColumn<JobPosting, String> colCompany;
    @FXML private TableColumn<JobPosting, String> colTitle;

    private final int userId = UserSession.getUserId();

    @FXML
    public void initialize() {
        // Sütunları modellerin değişkenleri ile eşitleme
        colInstitution.setCellValueFactory(new PropertyValueFactory<>("institution"));
        colDegree.setCellValueFactory(new PropertyValueFactory<>("degree"));

        colExpCompany.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        colCompany.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));

        loadSeekerData();
        loadEducationData();
        loadExperienceData();
        loadJobPostings();
    }

    // veri tabanındanm kullanıcı verilerini çekme
    private void loadSeekerData() {
        String sql = "SELECT FirstName, LastName FROM JobSeeker WHERE User_ID = ?";
        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                fnameField.setText(rs.getString("FirstName"));
                lnameField.setText(rs.getString("LastName"));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    // eğitim bilgilerini çekme
    private void loadEducationData() {
        ObservableList<Education> list = FXCollections.observableArrayList();
        String sql = "SELECT EducationID, Institution, Degree FROM Education WHERE Seeker_ID = ?";

        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Education(
                        rs.getInt("EducationID"), rs.getString("Institution"), rs.getString("Degree")
                ));
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        educationTable.setItems(list);
    }

    // experience
    private void loadExperienceData() {
        ObservableList<Experience> list = FXCollections.observableArrayList();
        String sql = "SELECT ExperienceID, CompanyName, Role FROM Experience WHERE Seeker_ID = ?";

        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Experience(
                        rs.getInt("ExperienceID"), rs.getString("CompanyName"), rs.getString("Role")
                ));
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        experienceTable.setItems(list);
    }

    // Active job posts
    private void loadJobPostings() {
        ObservableList<JobPosting> list = FXCollections.observableArrayList();
        String sql = "SELECT j.JobID, c.Name as CompanyName, j.Title " +
                "FROM JobPosting j JOIN Company c ON j.CompanyID = c.CompanyID " +
                "WHERE j.IsActive = 1";

        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new JobPosting(
                        rs.getInt("JobID"), rs.getString("CompanyName"), rs.getString("Title"), "", "", "Aktif"
                    ));
                }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        jobPostingTable.setItems(list);
    }

    @FXML
    protected void handleUpdateProfile() {
        String sql = "UPDATE JobSeeker SET FirstName = ?, LastName = ? WHERE User_ID = ?";
        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, fnameField.getText());
                pstmt.setString(2, lnameField.getText());
                pstmt.setInt(3, userId);
                pstmt.executeUpdate();

                showAlert("Başarılı", "Profiliniz başarıyla güncellendi.");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void handleSaveJob(ActionEvent actionEvent) {
        // tablodan seçili ilanı al
        JobPosting selectedJob = jobPostingTable.getSelectionModel().getSelectedItem();

        if (selectedJob == null) {
            showAlert("Warning", "Please select a post from the list to save.");
            return;
        }

        String sql = "INSERT INTO SavedJob (Seeker_ID, JobID, Saved_Date) VALUES (?, ?, NOW())";
        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, selectedJob.getJobId());
            pstmt.executeUpdate();

            showAlert("Successful", "The post was saved successfully!");
        } catch (SQLException e) {
            showAlert("Error", "This post may already be saved or an error occurred..");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleApplyJob(ActionEvent actionEvent) {
        JobPosting selectedJob = jobPostingTable.getSelectionModel().getSelectedItem();

        if (selectedJob == null) {
            showAlert("Warning", "Please select a post from the list to apply.");
            return;
        }

        String sql = "INSERT INTO Application (Seeker_ID, JobID, Application_Date, Status) VALUES (?, ?, NOW(), 'Pending')";

        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, selectedJob.getJobId());
                pstmt.executeUpdate();

                showAlert("Successful", "You have successfully applied for the job posting!");
        } catch (SQLException e) {
            showAlert("Error", "You may have already applied for this job.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddEducation() {
        if (instField.getText().isEmpty() || degreeField.getText().isEmpty()) return;

        String sql = "INSERT INTO Education (Seeker_ID, Institution, Degree) VALUES (?, ?, ?)";
        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, instField.getText());
                pstmt.setString(3, degreeField.getText());
                pstmt.executeUpdate();

                instField.clear();
                degreeField.clear();
                loadEducationData();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void handleAddExperience() {
        if (expCompField.getText().isEmpty() || expRoleField.getText().isEmpty()) return;

        String sql = "INSERT INTO Experience (Seeker_ID, CompanyName, Role) VALUES (?, ?, ?)";
        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, expCompField.getText());
                pstmt.setString(3, expRoleField.getText());
                pstmt.executeUpdate();

                expCompField.clear();
                expRoleField.clear();
                loadExperienceData();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void handleUploadCV(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CV (in PDF format)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            cvPathLabel.setText(selectedFile.getName());

            String sql = "UPDATE JobSeeker SET CVFile = ? WHERE User_ID = ?";
            try (Connection conn = JDBCConnectivity.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, selectedFile.getAbsolutePath());
                    pstmt.setInt(2, userId);
                    pstmt.executeUpdate();

                    showAlert("Successful", "Your CV has been successfully uploaded to the system!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void handleLogout(ActionEvent event) {
        UserSession.setSession(0, null);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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
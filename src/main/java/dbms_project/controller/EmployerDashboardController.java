package dbms_project.controller;

import dbms_project.database.JDBCConnectivity;
import dbms_project.database.UserSession;
import dbms_project.model.Application;
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

import java.io.IOException;
import java.sql.*;

public class EmployerDashboardController {
    @FXML private TextField jobTitleField, salaryField;
    @FXML private TextArea jobDescArea;

    // Kaydedilen iş ilanları
    @FXML private TableView<JobPosting> myJobsTable;
    @FXML private TableColumn<JobPosting, String> colMyTitle;
    @FXML private TableColumn<JobPosting, String> colMyStatus;
    @FXML private TableColumn<JobPosting, String> colMyDate;

    // Tüm iş ilanları
    @FXML private TableView<JobPosting> allJobsTable;
    @FXML private TableColumn<JobPosting, String> colAllCompany;
    @FXML private TableColumn<JobPosting, String> colAllTitle;
    @FXML private TableColumn<JobPosting, String> colAllSalary;

    // İş ilanına olan başvurular
    @FXML private TableView<Application> applicationTable;
    @FXML private TableColumn<Application, String> colSeekerName;
    @FXML private TableColumn<Application, String> colJobTitle;
    @FXML private TableColumn<Application, String> colStatus;

    private final int userId = UserSession.getUserId();

    @FXML
    public void initialize() {
        // Sütunları modellerdeki değişkenler ile eşitleme
        colMyTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colMyStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colMyDate.setCellValueFactory(new PropertyValueFactory<>("postedDate"));

        colAllCompany.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        colAllTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAllSalary.setCellValueFactory(new PropertyValueFactory<>("salaryRange"));

        colSeekerName.setCellValueFactory(new PropertyValueFactory<>("seekerName"));
        colJobTitle.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadTableData();
    }

    private void loadTableData() {
        loadMyJobs();
        loadAllJobs();
        loadApplications();
    }

    // Database'den işverenin kendi ilanlarını çekmesi
    private void loadMyJobs() {
        ObservableList<JobPosting> list = FXCollections.observableArrayList();
        int companyId = getCompanyIdFromEmployer(userId);

        String sql = "SELECT JobID, Title, IsActive, PostedDate FROM JobPosting WHERE CompanyID = ?";
        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, companyId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String status = rs.getBoolean("IsActive") ? "Active" : "Passive";
                 list.add(new JobPosting(rs.getInt("JobID"), "", rs.getString("Title"), "", rs.getString("PostedDate"), status
                ));
                }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        myJobsTable.setItems(list);
    }

    // Database den aktif olan bütün ilanları gösterir
    private void loadAllJobs() {
        ObservableList<JobPosting> list = FXCollections.observableArrayList();
        String sql = "SELECT j.JobID, c.Name as CompanyName, j.Title, j.SalaryRange " +
                "FROM JobPosting j JOIN Company c ON j.CompanyID = c.CompanyID " +
                "WHERE j.IsActive = 1";

        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new JobPosting(
                        rs.getInt("JobID"), rs.getString("CompanyName"), rs.getString("Title"), rs.getString("SalaryRange"), "", "Aktif"
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        allJobsTable.setItems(list);
    }

    // İlana yapılan bütün başvurularıu getirir
    private void loadApplications() {
        ObservableList<Application> list = FXCollections.observableArrayList();
        int companyId = getCompanyIdFromEmployer(userId);

        String sql = "SELECT a.Application_ID, CONCAT(s.FirstName, ' ', s.LastName) as SeekerName, j.Title, a.Status " +
                "FROM Application a " +
                "JOIN JobSeeker s ON a.Seeker_ID = s.User_ID " +
                "JOIN JobPosting j ON a.JobID = j.JobID " +
                "WHERE j.CompanyID = ?";

        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, companyId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    list.add(new Application(
                        rs.getInt("Application_ID"), rs.getString("SeekerName"), rs.getString("Title"), rs.getString("Status")
                    ));
                }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        applicationTable.setItems(list);
    }

    @FXML
    protected void handlePostJob() {
        int companyId = getCompanyIdFromEmployer(userId);

        if (companyId == -1) {
            showAlert("Error", "No company was found linked to this account. Please create a company profile first.");
            return;
        }

        String sql = "INSERT INTO JobPosting (CompanyID, Title, Description, SalaryRange, PostedDate, IsActive) VALUES (?, ?, ?, ?, NOW(), 1)";

        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, companyId);
                pstmt.setString(2, jobTitleField.getText());
                pstmt.setString(3, jobDescArea.getText());
                pstmt.setString(4, salaryField.getText());
                pstmt.executeUpdate();

                showAlert("Successful", "The job post was published succesfully!");

                jobTitleField.clear();
                jobDescArea.clear();
                salaryField.clear();
                loadTableData();

            }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private int getCompanyIdFromEmployer(int userId) {
        String sql = "SELECT CompanyID FROM Company WHERE User_ID = ?";
        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("CompanyID");
                }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return -1;
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
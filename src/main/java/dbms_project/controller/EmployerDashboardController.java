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

    // yetenekler
    @FXML private TextField empSkillNameField;
    @FXML private CheckBox mandatoryCheck;

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

        String sql = "SELECT a.Application_ID, CONCAT(s.FirstName, ' ', s.LastName) as SeekerName, j.Title, a.Status, j.JobID " +
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
                            rs.getInt("Application_ID"),
                            rs.getString("SeekerName"),
                            rs.getString("Title"),
                            rs.getString("Status"),
                            rs.getInt("JobID")
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
    public void handleAddJobSkill(ActionEvent actionEvent) {
        JobPosting selectedJob = myJobsTable.getSelectionModel().getSelectedItem();
        if (selectedJob == null) {
            showAlert("Warning", "Please select a job posting from the list above to add a skill..");
            return;
        }

        String skillName = empSkillNameField.getText().trim();
        if (skillName.isEmpty()) {
            showAlert("Warning", "Please enter skill.");
            return;
        }

        boolean isMandatory = mandatoryCheck.isSelected();
        Connection conn = null;

        try {
            conn = JDBCConnectivity.getConnection();
            conn.setAutoCommit(false); // Transaction başlat

            // yetenek havuzunu kontrol et
            String checkSkillSql = "SELECT SkillID FROM Skill WHERE SkillName = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSkillSql);
            checkStmt.setString(1, skillName);
            ResultSet rs = checkStmt.executeQuery();

            int skillId = -1;
            if (rs.next()) {
                skillId = rs.getInt("SkillID");
            } else {
                String insertSkillSql = "INSERT INTO Skill (SkillName) VALUES (?)";
                PreparedStatement insertSkillStmt = conn.prepareStatement(insertSkillSql, Statement.RETURN_GENERATED_KEYS);
                insertSkillStmt.setString(1, skillName);
                insertSkillStmt.executeUpdate();

                ResultSet generatedKeys = insertSkillStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    skillId = generatedKeys.getInt(1);
                }
            }

            // tabloyu doldurma
            String insertJobSkillSql = "INSERT INTO JobSkill (JobID, SkillID, IsMandatory) VALUES (?, ?, ?)";
            PreparedStatement insertJsStmt = conn.prepareStatement(insertJobSkillSql);
            insertJsStmt.setInt(1, selectedJob.getJobId());
            insertJsStmt.setInt(2, skillId);
            insertJsStmt.setBoolean(3, isMandatory);
            insertJsStmt.executeUpdate();

            conn.commit();
            showAlert("Successful", skillName + " skill was added successfully!");

            empSkillNameField.clear();
            mandatoryCheck.setSelected(false);

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            showAlert("Error", "This skill may have already been added to your profile or there is an error.");
            e.printStackTrace();
        }
    }


    //  accept application
    @FXML
    public void handleAcceptApplication(ActionEvent actionEvent) {
        updateApplicationStatus("Accepted");
    }

    // refuse application
    @FXML
    public void handleRejectApplication(ActionEvent actionEvent) {
        updateApplicationStatus("Rejected");
    }

    // change the state of application
    private void updateApplicationStatus(String newStatus) {
        Application selectedApp = applicationTable.getSelectionModel().getSelectedItem();
        if (selectedApp == null) return;

        Connection conn = null;
        try {
            conn = JDBCConnectivity.getConnection();
            conn.setAutoCommit(false); // Transaction başlat

            // update application status
            String updateAppSql = "UPDATE Application SET Status = ? WHERE Application_ID = ?";
            PreparedStatement pstmtApp = conn.prepareStatement(updateAppSql);
            pstmtApp.setString(1, newStatus);
            pstmtApp.setInt(2, selectedApp.getApplicationId());
            pstmtApp.executeUpdate();

            if (newStatus.equals("Accepted")) {
                String deactivateJobSql = "UPDATE JobPosting SET IsActive = 0 WHERE JobID = ?";
                PreparedStatement pstmtJob = conn.prepareStatement(deactivateJobSql);
                pstmtJob.setInt(1, selectedApp.getJobId());
                pstmtJob.executeUpdate();
                showAlert("Information", "The candidate was accepted and the relevant job posting was closed.");
            }

            conn.commit();
            loadApplications();
            loadMyJobs();
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        }
    }

    @FXML
    public void handleDeleteJob() {
        JobPosting selected = myJobsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String sql = "DELETE FROM JobPosting WHERE JobID = ?";
        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selected.getJobId());
            pstmt.executeUpdate();
            showAlert("Successful", "The post was completely removed from the system.");
            loadMyJobs();
        } catch (SQLException e) {
            showAlert("Error", "This post cannot be deleted because there are applications for it. You must clean the applications first.");
        }
    }

    @FXML
    public void handleViewJobSkills() {
        JobPosting selected = myJobsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            selected = allJobsTable.getSelectionModel().getSelectedItem();
        }

        if (selected == null) {
            showAlert("Warning", "Please select a post from the tables.");
            return;
        }

        StringBuilder skillsInfo = new StringBuilder("Skills Wanted:\n\n");
        String sql = "SELECT s.SkillName, js.IsMandatory FROM JobSkill js " +
                "JOIN Skill s ON js.SkillID = s.SkillID WHERE js.JobID = ?";

        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selected.getJobId());
                ResultSet rs = pstmt.executeQuery();

                boolean hasSkills = false;
                 while (rs.next()) {
                    hasSkills = true;
                    String name = rs.getString("SkillName");
                    String mandatory = rs.getBoolean("IsMandatory") ? "[MANDATORY]" : "[NOT MANDATORY]";
                    skillsInfo.append("- ").append(name).append(" ").append(mandatory).append("\n");
                }

                if (!hasSkills) skillsInfo.append("No special talent is mentioned for this post.");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Post details: " + selected.getTitle());
                alert.setHeaderText(selected.getCompanyName() + " - Skill Requirements");
                alert.setContentText(skillsInfo.toString());
                alert.showAndWait();

        } catch (SQLException e) { e.printStackTrace(); }
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
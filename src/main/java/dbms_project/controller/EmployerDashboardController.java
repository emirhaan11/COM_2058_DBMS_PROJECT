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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    @FXML private TableColumn<JobPosting, String> colMyDesc;
    @FXML private TableColumn<JobPosting, String> colAllDesc;

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
        colMyDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        colAllCompany.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        colAllTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAllSalary.setCellValueFactory(new PropertyValueFactory<>("salaryRange"));
        colAllDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

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

    // CompanyID si eşlenen employerların iş ilanlarını çekiyor
    private void loadMyJobs() {
        ObservableList<JobPosting> list = FXCollections.observableArrayList();
        int companyId = getCompanyIdFromEmployer(userId);

        String sql = "SELECT JobID, Title, Description, IsActive, PostedDate FROM JobPosting WHERE CompanyID = ?";
        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, companyId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String status = rs.getBoolean("IsActive") ? "Active" : "Passive";
                    list.add(new JobPosting(
                            rs.getInt("JobID"),
                            "",
                            rs.getString("Title"),
                            rs.getString("Description"),
                            "",
                            rs.getString("PostedDate"),
                            status
                    ));
                }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        myJobsTable.setItems(list);
    }

    // JobPosting den aktif olan bütün ilanları çeker
    private void loadAllJobs() {
        ObservableList<JobPosting> list = FXCollections.observableArrayList();
        String sql = "SELECT j.JobID, c.Name as CompanyName, j.Title, j.Description, j.SalaryRange " +
                "FROM JobPosting j JOIN Company c ON j.CompanyID = c.CompanyID " +
                "WHERE j.IsActive = 1";

        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new JobPosting(
                        rs.getInt("JobID"),
                        rs.getString("CompanyName"),
                        rs.getString("Title"),
                        rs.getString("Description"),
                        rs.getString("SalaryRange"),
                        "",
                        "Active"
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        allJobsTable.setItems(list);
    }

    // Application tablosundaki employerın oluşturduğu iş ilanlarını çeker
    private void loadApplications() {
        ObservableList<Application> list = FXCollections.observableArrayList();
        int companyId = getCompanyIdFromEmployer(userId);

        String sql = "SELECT a.Application_ID, CONCAT(s.FirstName, ' ', s.LastName) as SeekerName, a.Seeker_ID, j.Title, a.Status, j.JobID " +
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
                            rs.getInt("Seeker_ID"),
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

    //  Eğer hesabı oluştururken Company ismi girilmemişse hangi şirket adına ilan oluşturulcağı bilinmediğinden hata verir
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

    // Employerın kayıt olurken oluşturduğu şirketin ID sini çeker (Bir şirket için birden fazla employer olabilir)
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

    //
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
            conn.setAutoCommit(false);

            // eğer daha önce bu isimle kaydedilmiş bir yetenek varsa direkt onun SkillID sini kullanıyor eğer yoksa bir skill oluşturup yeni ID atıyor
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

            // İşe alım kriterlerinde olması gereken skilleri tabloya ekleme
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

    // Employerın başvuru yapanları değerlendirip iş başvurularını kabul edip reddetme
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
    public void handleDeleteApplication(ActionEvent actionEvent) {
        Application selectedApp = applicationTable.getSelectionModel().getSelectedItem();

        if (selectedApp == null) {
            showAlert("Warning", "Please select the application you want to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm");
        confirm.setHeaderText("The application of the candidate named " + selectedApp.getSeekerName() + " will be deleted.");
        confirm.setContentText("You cannot undo this operation. Are you sure?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            String sql = "DELETE FROM Application WHERE Application_ID = ?";

            try (Connection conn = JDBCConnectivity.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, selectedApp.getApplicationId());
                pstmt.executeUpdate();

                showAlert("Successful", "The application was deleted from the system.");

                loadApplications();

            } catch (SQLException e) {
                showAlert("Error", "A database error occurred while deleting the application.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleViewApplicantProfile(ActionEvent actionEvent) {
        Application selectedApp = applicationTable.getSelectionModel().getSelectedItem();
        if (selectedApp == null) {
            showAlert("Warning", "Please select the application whose profile you want to see.");
            return;
        }

        StringBuilder profileInfo = new StringBuilder();
        profileInfo.append("CANDIDATE: ").append(selectedApp.getSeekerName().toUpperCase()).append("\n");
        profileInfo.append("==============================\n\n");

        try (Connection conn = JDBCConnectivity.getConnection()) {
            profileInfo.append("--- EDUCATION ---\n");
            String eduSql = "SELECT Institution, Department, Degree, Graduation_Year FROM Education WHERE Seeker_ID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(eduSql)) {
                pstmt.setInt(1, selectedApp.getSeekerId());
                ResultSet rs = pstmt.executeQuery();
                boolean hasEdu = false;
                while (rs.next()) {
                    hasEdu = true;
                    profileInfo.append("• ").append(rs.getString("Institution"))
                            .append(" | ").append(rs.getString("Department"))
                            .append(" | Degree: ").append(rs.getString("Degree"))
                            .append(" (").append(rs.getString("Graduation_Year")).append(")\n");
                }
                if (!hasEdu) profileInfo.append("No education information entered.\n");
            }

            profileInfo.append("\n--- EXPERIENCE ---\n");
            String expSql = "SELECT CompanyName, Role, Start_Date, End_Date FROM Experience WHERE Seeker_ID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(expSql)) {
                pstmt.setInt(1, selectedApp.getSeekerId());
                ResultSet rs = pstmt.executeQuery();
                boolean hasExp = false;
                while (rs.next()) {
                    hasExp = true;
                    profileInfo.append("• ").append(rs.getString("CompanyName"))
                            .append(" - ").append(rs.getString("Role"))
                            .append(" [").append(rs.getString("Start_Date"))
                            .append(" / ").append(rs.getString("End_Date")).append("]\n");
                }
                if (!hasExp) profileInfo.append("No experience information entered.\n");
            }

            profileInfo.append("\n--- SKILLS ---\n");
            String skillSql = "SELECT s.SkillName, ss.ProficiencyLevel " +
                    "FROM SeekerSkill ss " +
                    "JOIN Skill s ON ss.SkillID = s.SkillID " +
                    "WHERE ss.Seeker_ID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(skillSql)) {
                pstmt.setInt(1, selectedApp.getSeekerId());
                ResultSet rs = pstmt.executeQuery();
                boolean hasSkills = false;
                while (rs.next()) {
                    hasSkills = true;
                    profileInfo.append("• ").append(rs.getString("SkillName"))
                            .append(" (").append(rs.getString("ProficiencyLevel")).append(")\n");
                }
                if (!hasSkills) profileInfo.append("No skills information entered.\n");
            }

            showProfileDialog(selectedApp.getSeekerName() + " - Candidate Profile", profileInfo.toString(), selectedApp);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Warning", "An error occurred while retrieving candidate information.");
        }
    }

    private void showProfileDialog(String title, String content, Application app) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(app.getSeekerName() + " Career Summary");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(400);
        textArea.setPrefWidth(500);

        dialogPane.setContent(textArea);

        ButtonType viewCVButton = new ButtonType("Open CV");
        alert.getButtonTypes().add(viewCVButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == viewCVButton) {
                handleViewCV(null);
            }
        });
    }

    @FXML
    public void handleViewCV(ActionEvent actionEvent) {
        Application selectedApp = applicationTable.getSelectionModel().getSelectedItem();

        if (selectedApp == null) {
            showAlert("Warning", "Please select the candidate whose CV you want to view.");
            return;
        }

        String sql = "SELECT CVFile FROM JobSeeker WHERE User_ID = ?";

        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, selectedApp.getSeekerId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                InputStream is = rs.getBinaryStream("CVFile");

                if (is == null) {
                    showAlert("Information", "This candidate has not uploaded a CV yet.");
                    return;
                }

                File tempFile = File.createTempFile("Candidate_CV_" + selectedApp.getSeekerName().replace(" ", "_"), ".pdf");

                tempFile.deleteOnExit();

                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }

                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(tempFile);
                } else {
                    showAlert("Error", "Your system does not support opening files. File path: " + tempFile.getAbsolutePath());
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while uploading or opening the CV file.");
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
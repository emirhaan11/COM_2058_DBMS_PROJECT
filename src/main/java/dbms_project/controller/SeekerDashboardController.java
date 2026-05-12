package dbms_project.controller;

import dbms_project.database.JDBCConnectivity;
import dbms_project.database.UserSession;
import dbms_project.model.Education;
import dbms_project.model.Experience;
import dbms_project.model.JobPosting;
import dbms_project.model.SeekerSkill;
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

import java.io.FileInputStream;
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
    @FXML private TableColumn<Education, String> colGradYear;
    @FXML private TextField gradYearField;
    @FXML private TableColumn<Education, String> colDepartment;
    @FXML private TextField deptField;

    // deneyim tablosu
    @FXML private TableView<Experience> experienceTable;
    @FXML private TableColumn<Experience, String> colExpCompany;
    @FXML private TableColumn<Experience, String> colRole;
    @FXML private TableColumn<Experience, String> colStartDate;
    @FXML private TableColumn<Experience, String> colEndDate;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    // iş ilanları
    @FXML private TableView<JobPosting> jobPostingTable;
    @FXML private TableColumn<JobPosting, String> colCompany;
    @FXML private TableColumn<JobPosting, String> colTitle;
    @FXML private TableColumn<JobPosting, String> colDescription;

    // yetenek sekmesi
    @FXML private TableView<SeekerSkill> skillTable;
    @FXML private TableColumn<SeekerSkill, String> colSkillName;
    @FXML private TableColumn<SeekerSkill, String> colProficiency;
    @FXML private TextField skillNameField;
    @FXML private ComboBox<String> proficiencyCombo;



    private final int userId = UserSession.getUserId();

    @FXML
    public void initialize() {
        // Sütunları modellerin değişkenleri ile eşitleme
        colInstitution.setCellValueFactory(new PropertyValueFactory<>("institution"));
        colDegree.setCellValueFactory(new PropertyValueFactory<>("degree"));
        colGradYear.setCellValueFactory(new PropertyValueFactory<>("graduationYear"));

        colExpCompany.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        colCompany.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        colSkillName.setCellValueFactory(new PropertyValueFactory<>("skillName"));
        colProficiency.setCellValueFactory(new PropertyValueFactory<>("proficiencyLevel"));
        proficiencyCombo.getItems().addAll("Beginner", "Intermediate", "Advanced", "Expert");

        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));

        loadSeekerData();
        loadEducationData();
        loadExperienceData();
        loadJobPostings();
        loadSkills();
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
        String sql = "SELECT EducationID, Institution, Department, Degree, Graduation_Year FROM Education WHERE Seeker_ID = ?";

        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new Education(
                        rs.getInt("EducationID"),
                        rs.getString("Institution"),
                        rs.getString("Department"),
                        rs.getString("Degree"),
                        rs.getString("Graduation_Year")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        educationTable.setItems(list);
    }

    // experience
    private void loadExperienceData() {
        ObservableList<Experience> list = FXCollections.observableArrayList();
        String sql = "SELECT ExperienceID, CompanyName, Role, Start_Date, End_Date FROM Experience WHERE Seeker_ID = ?";

        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Experience(
                        rs.getInt("ExperienceID"),
                        rs.getString("CompanyName"),
                        rs.getString("Role"),
                        rs.getString("Start_Date"),
                        rs.getString("End_Date")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        experienceTable.setItems(list);
    }

    // Active job posts
    private void loadJobPostings() {
        ObservableList<JobPosting> list = FXCollections.observableArrayList();
        String sql = "SELECT j.JobID, c.Name as CompanyName, j.Title, j.Description " +
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
                        "", "", "Active"
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }

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

                showAlert("Successful", "Your profile has been successfully updated.");
        } catch (SQLException e) { e.printStackTrace(); }
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

                showAlert("Successful", "You have successfully applied for the job!");
        } catch (SQLException e) {
            showAlert("Error", "You may have already applied for this job.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddEducation() {
        if (instField.getText().isEmpty() || deptField.getText().isEmpty()) return;

        String sql = "INSERT INTO Education (Seeker_ID, Institution, Department, Degree, Graduation_Year) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, instField.getText());
            pstmt.setString(3, deptField.getText());
            pstmt.setString(4, degreeField.getText());
            pstmt.setString(5, gradYearField.getText());
            pstmt.executeUpdate();
            loadEducationData();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void handleDeleteEducation() {
        Education selected = educationTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String sql = "DELETE FROM Education WHERE EducationID = ?";
        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selected.getEducationId());
            pstmt.executeUpdate();
            loadEducationData();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void handleAddExperience() {
        if (expCompField.getText().isEmpty() || expRoleField.getText().isEmpty() || startDatePicker.getValue() == null || endDatePicker.getValue() == null) return;

        String sql = "INSERT INTO Experience (Seeker_ID, CompanyName, Role, Start_Date, End_Date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, expCompField.getText());
            pstmt.setString(3, expRoleField.getText());
            pstmt.setString(4, startDatePicker.getValue().toString());
            pstmt.setString(5, endDatePicker.getValue().toString());
            pstmt.executeUpdate();

            expCompField.clear();
            expRoleField.clear();
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            loadExperienceData();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void handleDeleteExperience() {
        Experience selected = experienceTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String sql = "DELETE FROM Experience WHERE ExperienceID = ?";
        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selected.getExperienceId());
            pstmt.executeUpdate();
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
            if (selectedFile.length() > 16 * 1024 * 1024) {
                showAlert("Error", "File size is too large (Max 16MB).");
                return;
            }

            String sql = "UPDATE JobSeeker SET CVFile = ? WHERE User_ID = ?";

            // FileInputStream kullanarak byte akışı olarak dosyalar okunuyor
            try (Connection conn = JDBCConnectivity.getConnection();
                 FileInputStream fis = new FileInputStream(selectedFile);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setBinaryStream(1, fis, (int) selectedFile.length());
                pstmt.setInt(2, userId);

                pstmt.executeUpdate();

                cvPathLabel.setText(selectedFile.getName() + " (Saved to database)");
                showAlert("Successful", "Your CV has been successfully uploaded to the database!");

            } catch (Exception e) {
                showAlert("Error", "An error occurred while uploading the file.");
                e.printStackTrace();
            }
        }
    }

    // adayın skillerini veritabanından çekiyor
    private void loadSkills() {
        ObservableList<SeekerSkill> list = FXCollections.observableArrayList();
        String sql = "SELECT s.SkillName, ss.ProficiencyLevel " +
                "FROM SeekerSkill ss " +
                "JOIN Skill s ON ss.SkillID = s.SkillID " +
                "WHERE ss.Seeker_ID = ?";

        try (Connection conn = JDBCConnectivity.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    list.add(new SeekerSkill(rs.getString("SkillName"), rs.getString("ProficiencyLevel")));
                }
        } catch (SQLException e) { e.printStackTrace(); }

        skillTable.setItems(list);
    }

    // yetenek havuzunun yönetimi
    @FXML
    public void handleAddSkill(ActionEvent actionEvent) {
        String skillName = skillNameField.getText().trim();
        String proficiency = proficiencyCombo.getValue();

        if (skillName.isEmpty() || proficiency == null) {
            showAlert("Warning", "Please write skill and select a level.");
            return;
        }

        Connection conn = null;
        try {
            conn = JDBCConnectivity.getConnection();
            conn.setAutoCommit(false);

            // yeteneğin havuzda olup olmadığını kontrol et
            String checkSkillSql = "SELECT SkillID FROM Skill WHERE SkillName = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSkillSql);
            checkStmt.setString(1, skillName);
            ResultSet rs = checkStmt.executeQuery();

            int skillId = -1;
            if (rs.next()) {
                // havuzda var ise id sini alıyoruz
                skillId = rs.getInt("SkillID");
            } else {
                // havuzda yok ise yeni id oluştur ve veritabanına ekle
                String insertSkillSql = "INSERT INTO Skill (SkillName) VALUES (?)";
                PreparedStatement insertSkillStmt = conn.prepareStatement(insertSkillSql, Statement.RETURN_GENERATED_KEYS);
                insertSkillStmt.setString(1, skillName);
                insertSkillStmt.executeUpdate();

                ResultSet generatedKeys = insertSkillStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    skillId = generatedKeys.getInt(1);
                }
            }

            // elde ettiğimiz id yi tabloya ekleme
            String insertSeekerSkillSql = "INSERT INTO SeekerSkill (Seeker_ID, SkillID, ProficiencyLevel) VALUES (?, ?, ?)";
            PreparedStatement insertSsStmt = conn.prepareStatement(insertSeekerSkillSql);
            insertSsStmt.setInt(1, userId);
            insertSsStmt.setInt(2, skillId);
            insertSsStmt.setString(3, proficiency);
            insertSsStmt.executeUpdate();

            conn.commit();
            showAlert("Successful", "Skill was added your profile!");

            // tabloyu güncelleme
            skillNameField.clear();
            proficiencyCombo.setValue(null);
            loadSkills();

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            showAlert("Warning", "This skill may have already been added to your profile or there is an error.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleViewJobSkills() {
        JobPosting selected = jobPostingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a post.");
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
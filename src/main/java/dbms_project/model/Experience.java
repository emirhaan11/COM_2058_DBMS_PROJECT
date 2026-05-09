package dbms_project.model;

public class Experience {
    private int experienceId;
    private String companyName;
    private String role;
    private String startDate;
    private String endDate;

    public Experience(int experienceId, String companyName, String role, String startDate, String endDate) {
        this.experienceId = experienceId;
        this.companyName = companyName;
        this.role = role;
        this.startDate = startDate;
        this.endDate = endDate;
    }


    public int getExperienceId() {
        return experienceId;
    }
    public void setExperienceId(int experienceId) {
        this.experienceId = experienceId;
    }

    public String getCompanyName() {
        return companyName;
    }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public String getStartDate() {
        return startDate;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}

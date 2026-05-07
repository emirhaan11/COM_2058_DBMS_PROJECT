package dbms_project.model;

public class Experience {
    private int experienceId;
    private String companyName;
    private String role;


    public Experience(int experienceId, String companyName, String role) {
        this.experienceId = experienceId;
        this.companyName = companyName;
        this.role = role;
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
}

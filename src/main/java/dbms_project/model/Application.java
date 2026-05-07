package dbms_project.model;

public class Application {
    private int applicationId;
    private String seekerName;
    private String jobTitle;
    private String status;


    public Application(int applicationId, String seekerName, String jobTitle, String status) {
        this.applicationId = applicationId;
        this.seekerName = seekerName;
        this.jobTitle = jobTitle;
        this.status = status;
    }


    public int getApplicationId() {
        return applicationId;
    }
    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public String getSeekerName() {
        return seekerName;
    }
    public void setSeekerName(String seekerName) {
        this.seekerName = seekerName;
    }

    public String getJobTitle() {
        return jobTitle;
    }
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}

package dbms_project.model;

public class Application {
    private int applicationId;
    private String seekerName;
    private String jobTitle;
    private String status;
    private int jobId;
    private int seekerId;

    public Application(int applicationId, String seekerName, int seekerId, String jobTitle, String status, int jobId) {
        this.applicationId = applicationId;
        this.seekerName = seekerName;
        this.seekerId = seekerId;
        this.jobTitle = jobTitle;
        this.status = status;
        this.jobId = jobId;
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

    public int getJobId() {
        return jobId;
    }
    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getSeekerId() {
        return seekerId;
    }
    public void setSeekerId(int seekerId) {
        this.seekerId = seekerId;
    }
}

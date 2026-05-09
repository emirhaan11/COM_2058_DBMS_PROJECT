package dbms_project.model;

public class JobPosting{
    private int jobId;
    private String companyName;
    private String title;
    private String salaryRange;
    private String postedDate;
    private String status;


    public JobPosting(int jobId, String companyName, String title, String salaryRange, String postedDate, String status) {
        this.jobId = jobId;
        this.companyName = companyName;
        this.title = title;
        this.salaryRange = salaryRange;
        this.postedDate = postedDate;
        this.status = status;
    }


    public int getJobId() {
        return jobId;
    }
    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getCompanyName() {
        return companyName;
    }
    public void setCompanyName(String companyName) {
        companyName = companyName;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getSalaryRange() {
        return salaryRange;
    }
    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }

    public String getPostedDate() {
        return postedDate;
    }
    public void setPostedDate(String postedDate) {
        this.postedDate = postedDate;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
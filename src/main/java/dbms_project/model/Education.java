package dbms_project.model;

public class Education {
    private int educationId;
    private String institution;
    private String degree;


    public Education(int educationId, String institution, String degree) {
        this.educationId = educationId;
        this.institution = institution;
        this.degree = degree;
    }


    public int getEducationId() {
        return educationId;
    }
    public void setEducationId(int educationId) {
        this.educationId = educationId;
    }

    public String getInstitution() {
        return institution;
    }
    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getDegree() {
        return degree;
    }
    public void setDegree(String degree) {
        this.degree = degree;
    }
}

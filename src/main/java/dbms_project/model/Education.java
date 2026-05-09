package dbms_project.model;

public class Education {
    private int educationId;
    private String institution;
    private String department;
    private String degree;
    private String graduationYear;

    public Education(int educationId, String institution, String department, String degree, String graduationYear) {
        this.educationId = educationId;
        this.institution = institution;
        this.department = department;
        this.degree = degree;
        this.graduationYear = graduationYear;
    }

    public int getEducationId() { return educationId; }
    public void setEducationId(int educationId) { this.educationId = educationId; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getGraduationYear() { return graduationYear; }
    public void setGraduationYear(String graduationYear) { this.graduationYear = graduationYear; }
}
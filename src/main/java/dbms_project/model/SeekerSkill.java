package dbms_project.model;

public class SeekerSkill {
    private String skillName;
    private String proficiencyLevel;

    public SeekerSkill(String skillName, String proficiencyLevel) {
        this.skillName = skillName;
        this.proficiencyLevel = proficiencyLevel;
    }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(String proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }
}
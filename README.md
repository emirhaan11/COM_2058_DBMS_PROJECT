**JOB PORTAL APPLICATION**

This project is a desktop application that enables job sharing between job seekers and employers.
The GUI is built with JavaFX, the database with MySQL, and backend operations are implemented in Java. 
The purpose is to develop a database application within the scope of our mandatory course COM2058 (Database Management Systems).


**INSTALLATION**

1. Requirements:
   - Java 17 or higher
   - MySQL 8.0 or higher
   - Maven


2. Database Setup:
   - Open MySQL and run the SQL files in the Dump20260507 folder.


3. Create .env file:
   - Create a .env file in the project root directory.
   - Add the following lines:

   DB_URL=jdbc:mysql://localhost:3306/job_portal (you can change it with your local host IP address)  
   DB_USER=root (your database username)  
   DB_PASSWORD=your_password  
   (Enter the password you set when installing MySQL in the DB_PASSWORD section)


**PROJECT STRUCTURE**

src/main/java/dbms_project/
  - controller/      : UI controllers (login, registration, dashboards)
  - model/           : Data models (JobPosting, Application, etc.)
  - database/        : Database connection and session management
  - Main/            : Main class that starts the application

src/main/resources/
  - *.fxml           : JavaFX UI files
  - style.css        : Style file
  - Images/          : Application images


**DATABASE STRUCTURE**

The project uses 11 tables:

1. USER 
   - Basic information of all users
   - Email, password, user type (Job Seeker / Employer)

2. JOBSEEKER 
   - References the User table
   - First name, last name, CV file path

3. EMPLOYER 
   - References the User table
   - Job title information

4. COMPANY 
   - Company information linked to employers
   - Company name, city, description

5. JOBPOSTING 
   - Job postings belonging to a company
   - Position title, description, salary range, posting date, active status

6. SKILL 
   - List of skills that can be used by the entire system
   - Programming languages, software tools, etc.

7. SEEKERSKILL 
   - Skills possessed by job seekers
   - Proficiency level for each skill (Beginner, Intermediate, Advanced, Expert)

8. JOBSKILL 
   - Required skills for job postings
   - Indicates whether a skill is mandatory

9. EDUCATION 
   - Education information of job seekers
   - Degree, institution, graduation year

10. EXPERIENCE 
    - Work experience of job seekers
    - Company name, role, start and end dates

11. APPLICATION 
    - Applications of job seekers to job postings
    - Application date, status (Pending, Reviewed, Accepted, Rejected)



**TABLE RELATIONSHIPS**

                   USER
                 /      \
           JOBSEEKER    EMPLOYER
            / | \            |
           /  |  \       COMPANY
          /   |   \         |
       SKILL  |    \    JOBPOSTING
        / \   |     \    /   |
       /   \ SEEKERSKILL  JOBSKILL
    SEEKERSKILL |        \   /
      |      EDUCATION    SKILL
      |      |
      |    EXPERIENCE
      |
    APPLICATION


Structure:
- Each job seeker (JOBSEEKER) can have multiple educations (EDUCATION), experiences (EXPERIENCE),
  skills (SEEKERSKILL) and applications (APPLICATION).
- Each employer (EMPLOYER) can have one company (COMPANY) and each company can post multiple job postings (JOBPOSTING).
- Each job posting can require multiple skills (JOBSKILL).
- Each skill can belong to multiple job postings and job seekers.


**USAGE**
1. Open the application
2. Log in with your email and password on the login screen
3. If you are not registered, click the "Register" button
4. During registration, select your user type (Job Seeker / Employer)
5. After logging in, you will be redirected to your personal dashboard
6. Job Seekers: Can view job postings, apply for jobs, edit their own profile
7. Employers: Can post job listings, track applications, redirect them, and view profiles of applicants


**TECHNOLOGIES**

- Java 25
- JavaFX 25: Desktop UI
- MySQL 8.0: Database
- JDBC: Database connectivity
- jBCrypt: Password encryption
- dotenv-java: Configuration from .env file
- Maven: Project management

**QUESTIONS AND TROUBLESHOOTING**

If you experience connection problems:
1. Check if the MySQL server is running
2. Verify that the information in the .env file is correct
3. Verify that the database name is "job_portal"
4. Refresh Maven dependencies (Maven > Reload Projects)

package org.example.store;

import org.example.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSchoolStore implements SchoolStore {
    private final String url;
    private final String user;
    private final String password;

    public JdbcSchoolStore(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        loadDriver();
        initSchema();
    }

    @Override
    public Admin createAdmin(String nome, String username, String password) {
        int id = insertUser(nome, username, password, Role.ADMIN, null);
        return new Admin(id, nome, username, password);
    }

    @Override
    public Student createStudent(String nome, String username, String password) {
        int id = insertUser(nome, username, password, Role.ESTUDANTE, null);
        return new Student(id, nome, username, password);
    }

    @Override
    public Teacher createTeacher(String nome, String username, String password) {
        int id = insertUser(nome, username, password, Role.PROFESSOR, null);
        return new Teacher(id, nome, username, password);
    }

    @Override
    public Course createCourse(String nome) {
        String sql = "INSERT INTO courses(nome) VALUES (?)";
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nome);
            ps.executeUpdate();
            int id = generatedId(ps);
            return new Course(id, nome);
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao criar curso.", e);
        }
    }

    @Override
    public Subject createSubject(String nome, int courseId) {
        String sql = "INSERT INTO subjects(nome, course_id, teacher_id) VALUES (?, ?, NULL)";
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nome);
            ps.setInt(2, courseId);
            ps.executeUpdate();
            int id = generatedId(ps);
            return new Subject(id, nome, courseId);
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao criar disciplina.", e);
        }
    }

    @Override
    public GradeRecord createGrade(int studentId, int subjectId, int teacherId, String avaliacao, double nota) {
        String sql = "INSERT INTO grades(student_id, subject_id, teacher_id, avaliacao, nota) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setInt(3, teacherId);
            ps.setString(4, avaliacao);
            ps.setDouble(5, nota);
            ps.executeUpdate();
            int id = generatedId(ps);
            return new GradeRecord(id, studentId, subjectId, teacherId, avaliacao, nota);
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao publicar nota.", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, nome, username, password, role, course_id FROM users WHERE username = ?";
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(userFromRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao procurar utilizador por username.", e);
        }
    }

    @Override
    public Optional<User> findUser(int userId) {
        String sql = "SELECT id, nome, username, password, role, course_id FROM users WHERE id = ?";
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(userFromRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao procurar utilizador.", e);
        }
    }

    @Override
    public Optional<Student> findStudent(int studentId) {
        return findUser(studentId).filter(Student.class::isInstance).map(Student.class::cast);
    }

    @Override
    public Optional<Teacher> findTeacher(int teacherId) {
        return findUser(teacherId).filter(Teacher.class::isInstance).map(Teacher.class::cast);
    }

    @Override
    public Optional<Course> findCourse(int courseId) {
        String sql = "SELECT id, nome FROM courses WHERE id = ?";
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Course(rs.getInt("id"), rs.getString("nome")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao procurar curso.", e);
        }
    }

    @Override
    public Optional<Subject> findSubject(int subjectId) {
        String sql = "SELECT id, nome, course_id, teacher_id FROM subjects WHERE id = ?";
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(subjectFromRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao procurar disciplina.", e);
        }
    }

    @Override
    public List<User> getUsers() {
        String sql = "SELECT id, nome, username, password, role, course_id FROM users ORDER BY id";
        List<User> users = new ArrayList<>();
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(userFromRow(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao listar utilizadores.", e);
        }
    }

    @Override
    public List<Student> getStudents() {
        String sql = "SELECT id, nome, username, password, course_id FROM users WHERE role = 'ESTUDANTE' ORDER BY id";
        List<Student> students = new ArrayList<>();
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Student s = new Student(rs.getInt("id"), rs.getString("nome"), rs.getString("username"), rs.getString("password"));
                Integer courseId = nullableInt(rs, "course_id");
                s.setCourseId(courseId);
                students.add(s);
            }
            return students;
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao listar estudantes.", e);
        }
    }

    @Override
    public List<Teacher> getTeachers() {
        String sql = "SELECT id, nome, username, password FROM users WHERE role = 'PROFESSOR' ORDER BY id";
        List<Teacher> teachers = new ArrayList<>();
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Teacher t = new Teacher(rs.getInt("id"), rs.getString("nome"), rs.getString("username"), rs.getString("password"));
                t.getSubjectIds().addAll(findSubjectIdsByTeacher(c, t.getId()));
                teachers.add(t);
            }
            return teachers;
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao listar professores.", e);
        }
    }

    @Override
    public List<Course> getCourses() {
        String sql = "SELECT id, nome FROM courses ORDER BY id";
        List<Course> courses = new ArrayList<>();
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                courses.add(new Course(rs.getInt("id"), rs.getString("nome")));
            }
            return courses;
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao listar cursos.", e);
        }
    }

    @Override
    public List<Subject> getSubjects() {
        String sql = "SELECT id, nome, course_id, teacher_id FROM subjects ORDER BY id";
        List<Subject> subjects = new ArrayList<>();
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                subjects.add(subjectFromRow(rs));
            }
            return subjects;
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao listar disciplinas.", e);
        }
    }

    @Override
    public List<Subject> getEnrolledSubjectsByStudent(int studentId) {
        String sql = "SELECT s.id, s.nome, s.course_id, s.teacher_id " +
                "FROM subjects s " +
                "INNER JOIN enrollments e ON e.subject_id = s.id " +
                "WHERE e.student_id = ? " +
                "ORDER BY s.id";
        List<Subject> subjects = new ArrayList<>();
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    subjects.add(subjectFromRow(rs));
                }
            }
            return subjects;
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao listar disciplinas inscritas do estudante.", e);
        }
    }

    @Override
    public List<GradeRecord> getGradesByStudent(int studentId) {
        String sql = "SELECT id, student_id, subject_id, teacher_id, avaliacao, nota FROM grades WHERE student_id = ? ORDER BY id";
        List<GradeRecord> grades = new ArrayList<>();
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    grades.add(new GradeRecord(
                            rs.getInt("id"),
                            rs.getInt("student_id"),
                            rs.getInt("subject_id"),
                            rs.getInt("teacher_id"),
                            rs.getString("avaliacao"),
                            rs.getDouble("nota")
                    ));
                }
            }
            return grades;
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao listar notas do estudante.", e);
        }
    }

    @Override
    public void assignStudentToCourse(int studentId, int courseId) {
        String sql = "UPDATE users SET course_id = ? WHERE id = ? AND role = 'ESTUDANTE'";
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setInt(2, studentId);
            int count = ps.executeUpdate();
            if (count == 0) {
                throw new IllegalArgumentException("Estudante nao encontrado.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao associar estudante ao curso.", e);
        }
    }

    @Override
    public void assignTeacherToSubject(int teacherId, int subjectId) {
        String sql = "UPDATE subjects SET teacher_id = ? WHERE id = ?";
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, subjectId);
            int count = ps.executeUpdate();
            if (count == 0) {
                throw new IllegalArgumentException("Disciplina nao encontrada.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao associar professor a disciplina.", e);
        }
    }

    @Override
    public void enrollStudentInSubject(int studentId, int subjectId) {
        String sql = "INSERT IGNORE INTO enrollments(student_id, subject_id) VALUES (?, ?)";
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao inscrever estudante na disciplina.", e);
        }
    }

    @Override
    public void clearStudentEnrollments(int studentId) {
        String sql = "DELETE FROM enrollments WHERE student_id = ?";
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao limpar inscricoes do estudante.", e);
        }
    }

    @Override
    public boolean isStudentEnrolledInSubject(int studentId, int subjectId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND subject_id = ?";
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao validar inscricao do estudante.", e);
        }
    }

    @Override
    public List<Student> getEnrolledStudentsBySubject(int subjectId) {
        String sql = "SELECT u.id, u.nome, u.username, u.password, u.course_id " +
                "FROM users u " +
                "INNER JOIN enrollments e ON e.student_id = u.id " +
                "WHERE e.subject_id = ? AND u.role = 'ESTUDANTE' " +
                "ORDER BY u.nome";
        List<Student> students = new ArrayList<>();
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student s = new Student(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("username"),
                            rs.getString("password")
                    );
                    s.setCourseId(nullableInt(rs, "course_id"));
                    students.add(s);
                }
            }
            return students;
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao listar estudantes inscritos na disciplina.", e);
        }
    }

    @Override
    public boolean hasAnyUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao verificar utilizadores.", e);
        }
    }

    private int insertUser(String nome, String username, String password, Role role, Integer courseId) {
        String sql = "INSERT INTO users(nome, username, password, role, course_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nome);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.setString(4, role.name());
            if (courseId == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, courseId);
            }
            ps.executeUpdate();
            return generatedId(ps);
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao criar utilizador.", e);
        }
    }

    private User userFromRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nome = rs.getString("nome");
        String username = rs.getString("username");
        String rawPassword = rs.getString("password");
        Role role = Role.valueOf(rs.getString("role"));
        Integer courseId = nullableInt(rs, "course_id");

        if (role == Role.ADMIN) {
            return new Admin(id, nome, username, rawPassword);
        }
        if (role == Role.ESTUDANTE) {
            Student student = new Student(id, nome, username, rawPassword);
            student.setCourseId(courseId);
            return student;
        }

        Teacher teacher = new Teacher(id, nome, username, rawPassword);
        try (Connection c = connection()) {
            teacher.getSubjectIds().addAll(findSubjectIdsByTeacher(c, id));
        }
        return teacher;
    }

    private Subject subjectFromRow(ResultSet rs) throws SQLException {
        Subject subject = new Subject(rs.getInt("id"), rs.getString("nome"), rs.getInt("course_id"));
        Integer teacherId = nullableInt(rs, "teacher_id");
        subject.setTeacherId(teacherId);
        return subject;
    }

    private List<Integer> findSubjectIdsByTeacher(Connection c, int teacherId) throws SQLException {
        String sql = "SELECT id FROM subjects WHERE teacher_id = ?";
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id"));
                }
            }
        }
        return ids;
    }

    private Integer nullableInt(ResultSet rs, String col) throws SQLException {
        int value = rs.getInt(col);
        return rs.wasNull() ? null : value;
    }

    private int generatedId(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new IllegalStateException("ID nao gerado.");
            }
            return keys.getInt(1);
        }
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private void initSchema() {
        try (Connection c = connection();
             Statement st = c.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "nome VARCHAR(120) NOT NULL," +
                    "username VARCHAR(80) NOT NULL UNIQUE," +
                    "password VARCHAR(120) NOT NULL," +
                    "role VARCHAR(20) NOT NULL," +
                    "course_id INT NULL" +
                    ")");
            st.execute("CREATE TABLE IF NOT EXISTS courses (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "nome VARCHAR(120) NOT NULL" +
                    ")");
            st.execute("CREATE TABLE IF NOT EXISTS subjects (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "nome VARCHAR(120) NOT NULL," +
                    "course_id INT NOT NULL," +
                    "teacher_id INT NULL," +
                    "CONSTRAINT fk_subject_course FOREIGN KEY (course_id) REFERENCES courses(id)" +
                    ")");
            st.execute("CREATE TABLE IF NOT EXISTS grades (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "student_id INT NOT NULL," +
                    "subject_id INT NOT NULL," +
                    "teacher_id INT NOT NULL," +
                    "avaliacao VARCHAR(120) NOT NULL," +
                    "nota DECIMAL(5,2) NOT NULL" +
                    ")");
            st.execute("CREATE TABLE IF NOT EXISTS enrollments (" +
                    "student_id INT NOT NULL," +
                    "subject_id INT NOT NULL," +
                    "PRIMARY KEY (student_id, subject_id)," +
                    "CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "CONSTRAINT fk_enrollment_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE" +
                    ")");
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao inicializar schema MySQL.", e);
        }
    }

    private void loadDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver MySQL nao encontrado no classpath.", e);
        }
    }
}

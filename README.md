# Mini SIGA - ZK + Tomcat

Mini sistema academico com 3 perfis:
- `admin`: regista estudantes/professores, cria cursos/disciplinas, faz associacoes
- `professor`: publica notas e avaliacoes
- `estudante`: consulta as suas notas

## Tecnologias

- Java 11
- Apache Tomcat (WAR)
- ZK Framework (UI)
- REST Services (JAX-RS/Jersey)
- Web Service SOAP (JAX-WS/Metro)
- JSON

## Como executar

1. Configurar MySQL (a app cria tabelas automaticamente):
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/mini_siga?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
   export DB_USER="dalton"
   export DB_PASSWORD="veneno"
   ```

2. Garantir que o MySQL esta ativo.

3. Gerar o WAR:
   ```bash
   mvn clean package
   ```
3. O artefacto sera gerado em:
   - `target/zk-siga.war`
4. Deploy no Tomcat (copiar para `webapps/`).

## Credenciais iniciais

- `admin / admin123`
- `marta / 123456` (professor)
- `ana / 123456` (estudante)

## UI (ZK)

- Login: `/zk-siga/login.zul`
- Admin: `/zk-siga/admin.zul`
- Professor: `/zk-siga/teacher.zul`
- Estudante: `/zk-siga/student.zul`

## Endpoints REST

Base: `/zk-siga/api`

- `POST /auth/login`
- `POST /admin/students`
- `POST /admin/teachers`
- `POST /admin/courses`
- `POST /admin/subjects`
- `POST /admin/assign/student-course`
- `POST /admin/assign/teacher-subject`
- `GET /admin/overview`
- `POST /teacher/grades`
- `GET /teacher/subjects`
- `GET /student/grades`
- `GET /student/{studentId}/grades`

Autenticacao REST por header:
- `X-Auth-Token: <token>`

## SOAP Web Service

WSDL:
- `/zk-siga/ws/school?wsdl`

Operacao disponivel:
- `listarNotasDoEstudante(studentId)`

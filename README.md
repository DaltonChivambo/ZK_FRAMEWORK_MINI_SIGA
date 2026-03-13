# Mini SIGA - ZK + Tomcat

Mini sistema academico com 3 perfis:
- `admin`: regista estudantes/professores, cria cursos/disciplinas, faz associacoes
- `professor`: cria avaliacoes por disciplina, lanca e edita notas
- `estudante`: consulta notas e disciplinas inscritas

## Stack

- Java 11+
- Maven 3.8+
- MySQL 8+
- Apache Tomcat 10.1+
- ZK Framework, JAX-RS (REST), JAX-WS (SOAP), JSON

## Pre-requisitos (Ubuntu)

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk maven mysql-server tomcat10
```

Verificar servicos:

```bash
java -version
mvn -version
sudo systemctl status mysql --no-pager
sudo systemctl status tomcat10 --no-pager
```

## Configuracao MySQL

Crie o usuario da app (se ainda nao existir):

```bash
sudo mysql -u root -p -e "CREATE USER IF NOT EXISTS 'dalton'@'localhost' IDENTIFIED BY 'veneno'; GRANT ALL PRIVILEGES ON mini_siga.* TO 'dalton'@'localhost'; FLUSH PRIVILEGES;"
```

Defina as variaveis de ambiente da base:

```bash
export DB_URL="jdbc:mysql://localhost:3306/mini_siga?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USER="dalton"
export DB_PASSWORD="veneno"
```

> A app cria/atualiza tabelas automaticamente no arranque.

## Comandos principais (sequencia completa)

No diretorio do projeto:

```bash
cd "/home/black/Documents/00 - Projects/JAVA PROJECTS/OTHERS/ZK_SIGA"
```

1) Build do WAR:

```bash
mvn clean package
```

2) Deploy limpo no Tomcat:

```bash
sudo rm -rf /var/lib/tomcat10/webapps/zk-siga /var/lib/tomcat10/webapps/zk-siga.war
sudo cp target/zk-siga.war /var/lib/tomcat10/webapps/
sudo systemctl restart tomcat10
```

3) Teste rapido:

```bash
curl -I http://localhost:8080/zk-siga/
curl -I http://localhost:8080/zk-siga/login.zul
```

## Acesso

- App: `http://localhost:8080/zk-siga/`
- Login direto: `http://localhost:8080/zk-siga/login.zul`
- Admin: `http://localhost:8080/zk-siga/admin.zul`
- Professor: `http://localhost:8080/zk-siga/teacher.zul`
- Estudante: `http://localhost:8080/zk-siga/student.zul`

## Credenciais iniciais (seed)

- `admin / admin123`
- `marta / 123456` (professor)
- `ana / 123456` (estudante)

## REST API

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

Header de autenticacao:
- `X-Auth-Token: <token>`

## SOAP

- WSDL: `/zk-siga/ws/school?wsdl`
- Operacao: `listarNotasDoEstudante(studentId)`

## Troubleshooting rapido

- `404 /zk-siga/`: confirme deploy e reinicie Tomcat.
- Erro no startup: veja logs com:
  ```bash
  sudo journalctl -u tomcat10 -n 200 --no-pager
  ```

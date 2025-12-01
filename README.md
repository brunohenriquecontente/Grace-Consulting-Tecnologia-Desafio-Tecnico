# Grace Consulting Tecnologia - Desafio Técnico

API para gerenciamento de cadastro e consulta de números de cartão de crédito com autenticação JWT e armazenamento seguro.

## Objetivo

Desenvolver uma API RESTful que permita:
- Autenticação via JWT
- Cadastro individual de cartões de crédito
- Importação em lote via arquivos TXT
- Consulta de cartões com retorno de identificador único
- Armazenamento seguro com criptografia de dados sensíveis

## Tecnologias

- Java 21
- Spring Boot 3.2
- Spring Security com JWT
- Spring Data JPA
- MySQL 8.0
- Docker & Docker Compose
- Logging com SLF4J/Logback

## Usuário Padrão

A aplicação inicializa com um usuário padrão para testes:
- **Username:** `user`
- **Password:** `user`

---

## Opção 1: Executando com Docker Compose (Recomendado)

### Pré-requisitos
- Docker
- Docker Compose

### Passos

1. Clone o repositório:
```bash
git clone https://github.com/brunohenriquecontente/Grace-Consulting-Tecnologia-Desafio-Tecnico.git
cd Grace-Consulting-Tecnologia-Desafio-Tecnico
```

2. (Opcional) Configure as variáveis de ambiente:
```bash
cp .env.example .env
# Edite o arquivo .env conforme necessário
```

3. Execute com Docker Compose:
```bash
docker compose up --build -d
```

4. A API estará disponível em: `http://localhost:8080`
5. Acesse a documentação Swagger: `http://localhost:8080/swagger-ui.html`

## Opção 2: Executando na IDE 

### Pré-requisitos
- Java 21 (JDK)
- Maven 3.9+
- IntelliJ IDEA
- Docker (apenas para o MySQL)

### Passos

1. Clone o repositório e abra no IntelliJ:
```bash
git clone https://github.com/brunohenriquecontente/Grace-Consulting-Tecnologia-Desafio-Tecnico.git
```
- No IntelliJ: File > Open > Selecione a pasta do projeto

2. Suba apenas o MySQL via Docker:
```bash
docker-compose up -d mysql
```

3. Configure o JDK 21 no IntelliJ:
- File > Project Structure > Project > SDK: selecione Java 21
- File > Project Structure > Project > Language Level: 21

4. Execute a aplicação:
- Navegue até `src/main/java/com/graceconsulting/cardmanagement/CardManagementApplication.java`
- Clique com botão direito > Run 'CardManagementApplication'
- Ou use o atalho: `Shift + F10` (após configurar)

5. A API estará disponível em: `http://localhost:8080`
6. Acesse a documentação Swagger: `http://localhost:8080/swagger-ui.html`

### Configuração Alternativa (Via Maven)

Se preferir executar via terminal:

```bash
# Suba o MySQL
docker-compose up -d mysql

# Execute a aplicação
./mvnw spring-boot:run
```

Ou no Windows:
```bash
mvnw.cmd spring-boot:run
```

---

## Documentação da API

Após iniciar a aplicação, acesse:
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

---

## Referência

Baseado no desafio: https://github.com/hyperativa/back-end

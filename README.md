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

## Pré-requisitos

- Docker
- Docker Compose

## Como executar

1. Clone o repositório:
```bash
git clone https://github.com/brunohenriquecontente/Grace-Consulting-Tecnologia-Desafio-Tecnico.git
cd Grace-Consulting-Tecnologia-Desafio-Tecnico
```

2. Configure as variáveis de ambiente (opcional):
```bash
cp .env.example .env
# Edite o arquivo .env conforme necessário
```

3. Execute com Docker Compose:
```bash
docker-compose up -d
```

4. A API estará disponível em: `http://localhost:8080`

## Parando a aplicação

```bash
docker-compose down
```

Para remover também os volumes (dados do banco):
```bash
docker-compose down -v
```

## Status

🚧 Em desenvolvimento

## Referência

Baseado no desafio: https://github.com/hyperativa/back-end

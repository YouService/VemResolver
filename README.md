# Marketplace de Serviços

Plataforma web para conectar clientes que precisam de serviços com prestadores qualificados.

## 🚀 Tecnologias

### Backend
- Java 17
- Spring Boot 3.2.1
- Spring Security + JWT
- PostgreSQL
- Maven

### Frontend
- Angular 18
- TypeScript
- Tailwind CSS
- RxJS

## 📋 Funcionalidades

### Para Clientes
- ✅ Cadastro e autenticação
- ✅ Criar solicitações de serviço
- ✅ Visualizar propostas de prestadores
- ✅ Dashboard com estatísticas
- ✅ Filtrar solicitações por status

### Para Prestadores
- ✅ Cadastro e autenticação
- ✅ Completar perfil profissional
- ✅ Selecionar categorias de atuação
- ✅ Visualizar solicitações abertas
- ✅ Dashboard com métricas

### Categorias Disponíveis
- Eletricista
- Encanador
- Pintor
- Pedreiro
- Marceneiro
- Jardineiro
- Diarista
- Professor Particular
- Mecânico
- Desenvolvedor

## 🛠️ Instalação e Configuração

### Pré-requisitos
- Java 17+
- Node.js 18+
- PostgreSQL 16+
- Maven 3.6+

### Backend

1. Configure o banco de dados PostgreSQL:
```sql
CREATE DATABASE marketplace_db;
```

2. Configure as credenciais em `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/youservice_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

3. Execute o backend:
```bash
cd backend
mvn spring-boot:run
```

O backend estará disponível em `http://localhost:8080`

### Frontend

1. Instale as dependências:
```bash
cd frontend/marketplace-frontend
npm install
```

2. Execute o frontend:
```bash
npm start
```

O frontend estará disponível em `http://localhost:4200`

## 🔐 Usuários de Teste

Após iniciar o backend, serão criados automaticamente:

**Cliente:**
- Email: joao@cliente.com
- Senha: 123456

**Prestador:**
- Email: maria@prestador.com
- Senha: 123456

## 📚 API Endpoints

### Autenticação
- `POST /api/auth/register` - Cadastro de usuário
- `POST /api/auth/login` - Login

### Categorias
- `GET /api/categorias` - Listar categorias

### Solicitações
- `POST /api/solicitacoes` - Criar solicitação
- `GET /api/solicitacoes/minhas` - Listar minhas solicitações
- `GET /api/solicitacoes/abertas` - Listar solicitações abertas

### Prestadores
- `POST /api/prestadores` - Criar/atualizar perfil
- `GET /api/prestadores/usuario/{id}` - Buscar por usuário
- `GET /api/prestadores` - Buscar prestadores

## 🐳 Docker (Opcional)

Execute com Docker Compose:
```bash
docker-compose up -d
```

## 📝 Licença

Este projeto está sob a licença MIT.

## 👨‍💻 Autor

Felipe Almeida Lima
- GitHub: [@felipealima1984](https://github.com/felipealima1984)

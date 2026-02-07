# 🚀 Início Rápido

## Executar com Docker (Mais Fácil)

```bash
cd marketplace-servicos
docker-compose up -d
```

Aguarde ~3 minutos e acesse:
- Frontend: http://localhost:4200
- Backend: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

## Executar Manualmente

### 1. PostgreSQL
```bash
# Criar banco
psql -U postgres
CREATE DATABASE marketplace_db;
\q
```

### 2. Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 3. Frontend
```bash
cd frontend/marketplace-frontend
npm install
npm start
```

## 👤 Logins de Teste

**Cliente:**
- Email: `joao@cliente.com`
- Senha: `123456`

**Prestador:**
- Email: `maria@prestador.com`
- Senha: `123456`

## 📌 Portas

- Backend: **8080**
- Frontend: **4200**
- PostgreSQL: **5432**

## 🔧 Tecnologias

- **Backend:** Java 21, Spring Boot 3.2, JWT, PostgreSQL
- **Frontend:** Angular 19, Material, Tailwind CSS
- **Docs:** Swagger/OpenAPI

## ✨ Principais Funcionalidades

✅ Autenticação JWT  
✅ Cliente: criar solicitações, receber propostas, avaliar prestadores  
✅ Prestador: buscar serviços, enviar propostas, perfil profissional  
✅ Sistema de avaliações  
✅ Filtros por categoria, localização  
✅ API REST documentada  

## 📚 Documentação Completa

Veja `README.md` para informações detalhadas.

# 🔧 Troubleshooting - Marketplace de Serviços

## Problemas Comuns e Soluções

### 1. Erro de versão do Angular Build

**Erro:**
```
The current version of "@angular/build" supports Angular versions ^21.0.0
```

**Solução:**
```bash
cd frontend/marketplace-frontend
npm install
```

O `package.json` já está configurado com as versões corretas (Angular 19.2.19).

---

### 2. Erro do Tailwind CSS

**Erro:**
```
It looks like you're trying to use `tailwindcss` directly as a PostCSS plugin
```

**Solução:**
O projeto usa Tailwind CSS v3.4.17 (não v4). Certifique-se de executar:
```bash
cd frontend/marketplace-frontend
npm install
```

---

### 3. Erro de @angular/animations

**Erro:**
```
Could not resolve "@angular/animations/browser"
```

**Solução:**
A dependência já está no `package.json`. Execute:
```bash
cd frontend/marketplace-frontend
npm install
```

---

### 4. Porta já em uso (Backend)

**Erro:**
```
Port 8080 is already in use
```

**Solução:**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Ou altere a porta no application.properties
server.port=8081
```

---

### 5. Porta já em uso (Frontend)

**Erro:**
```
Port 4200 is already in use
```

**Solução:**
```bash
# Use outra porta
ng serve --port 4201
```

---

### 6. Erro de conexão com PostgreSQL

**Erro:**
```
Connection refused: localhost:5432
```

**Soluções:**

**a) Verificar se o PostgreSQL está rodando:**
```bash
# Windows
pg_ctl status

# Iniciar PostgreSQL
pg_ctl start
```

**b) Verificar credenciais no `application.properties`:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/marketplace_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

**c) Criar o banco se não existir:**
```sql
psql -U postgres
CREATE DATABASE marketplace_db;
\q
```

---

### 7. Erro de CORS no Frontend

**Erro:**
```
Access to XMLHttpRequest blocked by CORS policy
```

**Solução:**
Verifique se o backend está configurado corretamente no SecurityConfig.java:
```java
configuration.setAllowedOrigins(List.of("http://localhost:4200"));
```

E no frontend, verifique se a URL da API está correta nos services:
```typescript
private apiUrl = 'http://localhost:8080/api/...';
```

---

### 8. Erro de compilação Maven

**Erro:**
```
Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin
```

**Solução:**
```bash
cd backend

# Limpar e recompilar
mvn clean install -DskipTests

# Se persistir, verifique a versão do Java
java -version  # Deve ser Java 21
```

---

### 9. Docker: Container não inicia

**Problema:** Containers não inicializam com `docker-compose up`

**Solução:**
```bash
# Parar tudo
docker-compose down

# Remover volumes (ATENÇÃO: apaga dados do banco)
docker-compose down -v

# Rebuild e restart
docker-compose up --build -d

# Ver logs
docker-compose logs -f
```

---

### 10. NPM: Vulnerabilidades reportadas

**Aviso:**
```
5 vulnerabilities (2 low, 3 high)
```

**Solução:**
Estes são warnings de dependências. Para um projeto de desenvolvimento/teste, são aceitáveis. Para produção:
```bash
npm audit fix
# OU
npm audit fix --force  # CUIDADO: pode quebrar compatibilidade
```

---

### 11. Banco de dados não persiste (Docker)

**Problema:** Dados são perdidos após `docker-compose down`

**Solução:**
Use volumes nomeados (já configurado no `docker-compose.yml`):
```yaml
volumes:
  postgres_data:
```

Para backup:
```bash
docker exec marketplace-postgres pg_dump -U postgres marketplace_db > backup.sql
```

---

### 12. Frontend não carrega depois do login

**Problema:** Tela branca ou erro de navegação

**Soluções:**

**a) Verificar console do navegador (F12)**

**b) Verificar se o token está sendo salvo:**
```javascript
// Console do navegador
localStorage.getItem('token')
```

**c) Verificar se a rota existe:**
```typescript
// app.routes.ts
{ path: 'cliente/dashboard', component: ClienteDashboardComponent }
```

---

### 13. Swagger não abre

**Problema:** http://localhost:8080/swagger-ui.html não funciona

**Solução:**
Tente as URLs alternativas:
- http://localhost:8080/swagger-ui/index.html
- http://localhost:8080/api-docs

Verifique se o backend está rodando:
```bash
curl http://localhost:8080/api-docs
```

---

### 14. Build do frontend muito lento

**Solução:**
```bash
# Use modo de desenvolvimento
ng serve

# Para build de produção otimizado
ng build --configuration production
```

---

## 📞 Ainda com problemas?

1. Verifique os logs:
   - **Backend:** Console onde executou `mvn spring-boot:run`
   - **Frontend:** Console do navegador (F12) e terminal do `ng serve`
   - **Docker:** `docker-compose logs -f`

2. Versões instaladas:
```bash
java -version    # Deve ser 21
node -v          # Deve ser 22.x
npm -v           # Deve ser 10.x
```

3. Reinstalação limpa:
```bash
# Frontend
cd frontend/marketplace-frontend
rm -rf node_modules package-lock.json
npm install

# Backend
cd backend
mvn clean install
```

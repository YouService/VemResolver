# 🚀 GUIA RÁPIDO - Marketplace de Serviços

## ⚠️ IMPORTANTE: Você precisa instalar Maven primeiro!

## 🎯 Passo a Passo Completo

### 1️⃣ Instalar Maven

Escolha UMA das opções:

#### Opção A: Script Automático (Mais Rápido)
```cmd
cd marketplace-servicos
instalar-maven.bat
```

#### Opção B: Comando direto
```cmd
winget install Apache.Maven -e
```

#### Opção C: Download Manual
1. Baixe: https://maven.apache.org/download.cgi
2. Veja instruções: MAVEN-INSTALL.md

**🔴 IMPORTANTE:** Após instalar, **FECHE o terminal atual e ABRA UM NOVO!**

### 2️⃣ Verificar Instalação

```cmd
mvn -version
java -version
node -v
```

Tudo deve funcionar!

---

## 🐳 OU: Usar Docker (SEM INSTALAR MAVEN)

Se você tem Docker instalado:

```cmd
cd marketplace-servicos
docker-compose up
```

Pronto! Funciona sem instalar Maven, PostgreSQL ou configurar nada.

---

## 🚀 Executar o Sistema (Após Instalar Maven)

### Backend (Terminal 1):
```cmd
cd marketplace-servicos\backend
run.bat
```

### Frontend (Terminal 2):
```cmd
cd marketplace-servicos\frontend\marketplace-frontend
npm install
npm start
```

---

## 🌐 Acessar

- **Frontend:** http://localhost:4200
- **Backend API:** http://localhost:8080
- **Swagger:** http://localhost:8080/swagger-ui.html

---

## 👤 Login de Teste

**Cliente:**
- Email: `joao@cliente.com`
- Senha: `123456`

**Prestador:**
- Email: `maria@prestador.com`
- Senha: `123456`

---

## 📁 Arquivos Importantes

- `README.md` - Documentação completa
- `MAVEN-INSTALL.md` - Como instalar Maven
- `TROUBLESHOOTING.md` - Solução de problemas
- `QUICKSTART.md` - Este arquivo
- `instalar-maven.bat` - Script de instalação do Maven
- `backend/run.bat` - Executar backend no Windows
- `backend/run.sh` - Executar backend no Linux/Mac

---

## ❓ Problemas?

1. **Maven não reconhecido:**
   - Veja: `MAVEN-INSTALL.md`
   - Lembre-se de **fechar e abrir novo terminal** após instalar

2. **Java não encontrado:**
   - Instale Java 21 LTS

3. **PostgreSQL:**
   - Instale e crie database `marketplace_db`
   - OU use Docker: `docker-compose up`

4. **Outros erros:**
   - Veja: `TROUBLESHOOTING.md`

---

## 📞 Precisa de Ajuda?

Consulte os arquivos de documentação no projeto!

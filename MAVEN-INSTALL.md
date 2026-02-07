# 🚨 ERRO: Maven não encontrado

## ✅ Solução Rápida (Windows)

### Opção 1: Script Automático (Mais Fácil)
```cmd
cd marketplace-servicos
instalar-maven.bat
```
Depois **feche e abra um novo CMD** e tente novamente.

---

### Opção 2: Instalar Manualmente via Winget
```cmd
winget install Apache.Maven -e
```
Depois **feche e abra um novo CMD**.

---

### Opção 3: Download Manual
1. Acesse: https://maven.apache.org/download.cgi
2. Baixe: `apache-maven-3.9.x-bin.zip`
3. Extraia em: `C:\Program Files\Maven`
4. Adicione ao PATH:
   - Abra: Painel de Controle → Sistema → Configurações Avançadas
   - Variáveis de Ambiente
   - Em "Variáveis do Sistema", edite `Path`
   - Adicione: `C:\Program Files\Maven\bin`
5. **Feche e abra um novo CMD**
6. Teste: `mvn -version`

---

## 🐳 Opção 4: Usar Docker (SEM INSTALAR MAVEN)

Esta é a opção mais simples se você não quer instalar Maven:

```cmd
cd marketplace-servicos
docker-compose up
```

Pronto! Tudo funcionará sem instalar Maven ou PostgreSQL.

---

## ✅ Verificar Instalação

Após instalar, **feche o CMD atual**, abra um novo e teste:

```cmd
mvn -version
```

Deve mostrar algo como:
```
Apache Maven 3.9.x
Java version: 21.x.x
```

---

## 🚀 Executar o Backend

Após instalar Maven:

### Windows:
```cmd
cd marketplace-servicos\backend
run.bat
```

### Ou manualmente:
```cmd
cd marketplace-servicos\backend
mvn spring-boot:run
```

---

## ❓ Ainda com problemas?

### Verificar Java:
```cmd
java -version
```
Deve mostrar Java 21.

### Verificar PATH do Maven:
```cmd
where mvn
```
Deve mostrar o caminho do Maven.

### Se não aparecer:
1. Feche TODOS os terminais CMD/PowerShell
2. Abra um NOVO terminal
3. Teste novamente

---

## 📞 Contato

Se ainda tiver problemas, veja: `TROUBLESHOOTING.md`

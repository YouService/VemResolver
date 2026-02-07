# 📥 INSTALAR MAVEN MANUALMENTE - Guia Completo

## 🎯 Passo a Passo (Windows)

### 1️⃣ Download

1. **Acesse:** https://maven.apache.org/download.cgi

2. **Baixe o arquivo:** `apache-maven-3.9.9-bin.zip` (ou versão mais recente)
   - Procure por "Binary zip archive"
   - Link direto: https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip

### 2️⃣ Extrair

1. Após baixar, **extraia o arquivo ZIP**

2. **Mova a pasta** para: `C:\Program Files\`

3. **Renomeie** a pasta para: `Maven`
   - Caminho final: `C:\Program Files\Maven`

### 3️⃣ Configurar Variáveis de Ambiente

#### Opção A: Interface Gráfica (Mais Fácil)

1. **Abra:** Configurações do Windows (tecla Windows + I)

2. **Pesquise:** "variáveis de ambiente" na busca

3. **Clique em:** "Editar as variáveis de ambiente do sistema"

4. **Na janela que abrir:**
   - Clique em "Variáveis de Ambiente" (botão no canto inferior)

5. **Criar MAVEN_HOME:**
   - Em "Variáveis do sistema" (parte de baixo)
   - Clique em "Novo..."
   - Nome da variável: `MAVEN_HOME`
   - Valor da variável: `C:\Program Files\Maven`
   - Clique em "OK"

6. **Editar PATH:**
   - Ainda em "Variáveis do sistema"
   - Procure e selecione `Path`
   - Clique em "Editar..."
   - Clique em "Novo"
   - Adicione: `%MAVEN_HOME%\bin`
   - Clique em "OK" em todas as janelas

#### Opção B: Via PowerShell (Administrador)

```powershell
# Abra PowerShell como Administrador e execute:
[System.Environment]::SetEnvironmentVariable("MAVEN_HOME", "C:\Program Files\Maven", [System.EnvironmentVariableTarget]::Machine)
$path = [System.Environment]::GetEnvironmentVariable("Path", [System.EnvironmentVariableTarget]::Machine)
[System.Environment]::SetEnvironmentVariable("Path", "$path;%MAVEN_HOME%\bin", [System.EnvironmentVariableTarget]::Machine)
```

### 4️⃣ Verificar Instalação

1. **IMPORTANTE:** Feche TODOS os terminais CMD/PowerShell abertos

2. **Abra um NOVO CMD** (terminal novo)

3. **Execute:**
```cmd
mvn -version
```

4. **Deve aparecer algo como:**
```
Apache Maven 3.9.9
Maven home: C:\Program Files\Maven
Java version: 21.x.x
```

---

## ✅ Pronto! Agora execute o backend:

```cmd
cd marketplace-servicos\backend
run.bat
```

---

## ❌ Se ainda não funcionar...

### Verificação 1: Java está instalado?
```cmd
java -version
```
Se não aparecer nada, instale Java 21:
- https://adoptium.net/temurin/releases/

### Verificação 2: PATH foi configurado corretamente?
```cmd
echo %MAVEN_HOME%
```
Deve mostrar: `C:\Program Files\Maven`

```cmd
echo %PATH%
```
Deve conter: `C:\Program Files\Maven\bin`

### Verificação 3: Arquivo mvn.cmd existe?
```cmd
dir "C:\Program Files\Maven\bin\mvn.cmd"
```
Deve listar o arquivo.

### Ainda com problema?
1. Reinicie o computador (as variáveis de ambiente podem precisar disso)
2. Verifique se extraiu para a pasta correta
3. Verifique se não tem espaços ou caracteres especiais no caminho

---

## 🐳 ALTERNATIVA: Use Docker (SEM INSTALAR MAVEN)

Se estiver com dificuldades, use Docker que já funciona:

```cmd
cd marketplace-servicos
docker-compose up
```

Tudo funcionará sem precisar instalar Maven!

---

## 📸 Imagens de Referência

### Como encontrar as Variáveis de Ambiente:
1. Windows + I (Configurações)
2. Pesquise: "variáveis"
3. Clique em "Editar as variáveis de ambiente do sistema"

### Estrutura de pastas correta:
```
C:\Program Files\Maven\
├── bin\
│   ├── mvn.cmd        ← Este arquivo é essencial!
│   └── ...
├── conf\
├── lib\
└── LICENSE
```

---

## 💡 Dicas

- Use `C:\Program Files\Maven` (sem espaços extras)
- Feche e abra novo terminal depois de configurar
- Se possível, reinicie o computador após configurar
- Verifique se não bloqueou o download (Windows Defender)

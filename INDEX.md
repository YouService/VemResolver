# 📚 ÍNDICE DE DOCUMENTAÇÃO - Marketplace de Serviços

## 🚀 COMEÇAR AQUI

### Você TEM Docker instalado?
✅ **SIM** → Execute: `docker-compose up`  
   Pronto! Não precisa instalar nada.

❌ **NÃO** → Continue lendo abaixo.

---

## 📖 Guias Disponíveis

### 1. Início Rápido
- **INICIO-RAPIDO.md** - Visão geral e passo a passo básico

### 2. Instalar Maven (escolha UMA opção)

#### Opção A: Download Automático ⭐
- **Execute:** `baixar-maven.bat` (como Administrador)
- Baixa e instala automaticamente

#### Opção B: Instalação Manual 📝
- **INSTALAR-MAVEN-MANUAL.md** - Guia completo passo a passo
- **MAVEN-GUIA-VISUAL.txt** - Guia visual simplificado

#### Opção C: Instalador Original
- **Execute:** `instalar-maven.bat`
- Usa winget (pode pedir confirmação)

### 3. Documentação Principal
- **README.md** - Documentação técnica completa do projeto

### 4. Solução de Problemas
- **TROUBLESHOOTING.md** - Problemas comuns e soluções
- **MAVEN-INSTALL.md** - Ajuda específica para Maven

---

## 🎯 FLUXO RECOMENDADO

```
INÍCIO
  │
  ├─> Tem Docker? ────> SIM ──> docker-compose up ──> FIM ✅
  │                              
  └─> NÃO
      │
      ├─> Execute: baixar-maven.bat (como Admin)
      │   │
      │   ├─> Funcionou? ──> SIM ──> backend\run.bat ──> FIM ✅
      │   │
      │   └─> NÃO ──> Consulte: INSTALAR-MAVEN-MANUAL.md
      │               │
      │               └─> Instale manualmente
      │                   │
      │                   └─> backend\run.bat ──> FIM ✅
```

---

## 📁 Estrutura de Arquivos

### Scripts Executáveis (Windows)
```
marketplace-servicos/
├── baixar-maven.bat              ← Download automático Maven
├── instalar-maven.bat            ← Instalar via winget
├── backend/
│   ├── run.bat                   ← Executar backend
│   └── run.sh                    ← Executar backend (Linux/Mac)
└── docker-compose.yml            ← Executar tudo com Docker
```

### Documentação
```
marketplace-servicos/
├── INICIO-RAPIDO.md              ← COMECE AQUI
├── INDEX.md                      ← Este arquivo
├── README.md                     ← Doc completa
├── INSTALAR-MAVEN-MANUAL.md      ← Guia manual Maven
├── MAVEN-GUIA-VISUAL.txt         ← Guia visual Maven
├── MAVEN-INSTALL.md              ← Ajuda Maven
├── TROUBLESHOOTING.md            ← Solução de problemas
└── QUICKSTART.md                 ← Início rápido alternativo
```

---

## 🔥 OPÇÕES MAIS RÁPIDAS

### 1. Com Docker (Mais Fácil)
```cmd
cd marketplace-servicos
docker-compose up
```

### 2. Sem Docker (Instalar Maven)
```cmd
# Como Administrador:
baixar-maven.bat

# Depois feche e abra novo terminal:
cd backend
run.bat
```

---

## ⚡ Acesso Rápido

Depois de tudo rodando:

- **Frontend:** http://localhost:4200
- **Backend:** http://localhost:8080
- **Swagger:** http://localhost:8080/swagger-ui.html

**Login Teste:**
- Cliente: `joao@cliente.com` / `123456`
- Prestador: `maria@prestador.com` / `123456`

---

## ❓ Precisa de Ajuda?

1. **Maven não funciona?** → INSTALAR-MAVEN-MANUAL.md
2. **Erro na execução?** → TROUBLESHOOTING.md
3. **Dúvida geral?** → README.md
4. **Quer usar Docker?** → docker-compose up

---

## 📞 Contato

Se nada funcionar, consulte todos os arquivos de documentação listados acima.
Cada um tem informações específicas para ajudá-lo!

# VemResolver — Backlog de Funcionalidades Futuras

> **Criado em:** 2026-05-19  
> **Analista:** Claude Sonnet 4.6  
> **Contexto:** Documento gerado após a conclusão completa da Prioridade P4 (Competitividade). Lista todos os gaps identificados em relação aos concorrentes (GetNinjas, Cronoshare), requisitos de produção e melhorias de produto que ainda não foram implementados.

---

## Como ler este documento

Cada item tem:
- **Impacto**: benefício percebido pelo usuário ou pelo negócio (Alto / Médio / Baixo)
- **Esforço**: complexidade de implementação (Alto / Médio / Baixo)
- **Prioridade sugerida**: produto do impacto × esforço invertido
- **Dependências**: o que precisa existir antes

---

## P5 — Infraestrutura e Deploy

### P5.1 — CI/CD com GitHub Actions

**Descrição:**  
Pipeline automático que executa testes, compila o JAR, builda as imagens Docker e faz deploy ao fazer push na branch `main`.

**Justificativa:**  
Hoje o deploy é manual (`docker-compose up --build`). Qualquer mudança em produção exige acesso SSH à máquina e conhecimento do processo. CI/CD elimina erros humanos e garante que só código que passa nos testes chegue ao ar.

**Escopo técnico:**
- Workflow `ci.yml`: `mvn test` no backend + `ng test --watch=false` no frontend a cada PR
- Workflow `deploy.yml`: build das imagens Docker + push para Docker Hub (ou GHCR) + SSH na VPS para `docker-compose pull && docker-compose up -d`
- Secrets do GitHub: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `BREVO_API_KEY`, `SSH_KEY`
- Badge de status de build no README

**Impacto:** Alto | **Esforço:** Médio | **Dependência:** P5.2 (ambiente de produção)

---

### P5.2 — Ambiente de Produção (VPS / PaaS)

**Descrição:**  
Provisionamento de um ambiente de produção real, com domínio, HTTPS e banco de dados gerenciado.

**Justificativa:**  
O projeto roda hoje apenas em `localhost`. Sem deploy real não há como validar com usuários reais nem medir métricas de negócio.

**Escopo técnico:**
- Opção A (PaaS — menor esforço): Railway ou Render
  - Backend: serviço Docker com variáveis de ambiente
  - Banco: PostgreSQL gerenciado (Railway Postgres ou Render Postgres)
  - Frontend: Nginx servindo a build Angular via Docker
- Opção B (VPS — mais controle): DigitalOcean Droplet ou Hetzner
  - Docker Compose em produção
  - Nginx reverso com SSL via Let's Encrypt (Certbot)
  - Domínio próprio (`vemresolver.com.br` ou similar)
- `application-prod.yml` já existe — apenas preencher variáveis
- `DataSeeder` já está restrito ao perfil `!prod`

**Impacto:** Alto | **Esforço:** Médio | **Dependência:** nenhuma

---

### P5.3 — Correção das Vulnerabilidades Dependabot

**Descrição:**  
O GitHub reporta 49 vulnerabilidades nas dependências (33 high, 13 moderate, 3 low). Nenhuma foi corrigida até o momento.

**Justificativa:**  
Vulnerabilidades de segurança conhecidas em dependências são o vetor mais comum de ataques em aplicações web. Deve ser resolvido antes de qualquer exposição pública.

**Escopo técnico:**
- Executar `mvn versions:display-dependency-updates` e `npm audit` para mapear as atualizações disponíveis
- Atualizar dependências em lotes (Spring Boot patch, JJWT, Angular minor)
- Rodar a suíte de testes completa após cada lote
- Configurar Dependabot para criar PRs automáticos de atualização

**Impacto:** Alto (segurança) | **Esforço:** Médio | **Dependência:** nenhuma

---

### P5.4 — Observabilidade: Logs Estruturados e Métricas

**Descrição:**  
Configuração de logging estruturado (JSON) e métricas de aplicação com Spring Actuator + Prometheus + Grafana.

**Justificativa:**  
Sem observabilidade não é possível debugar problemas em produção, medir latência de endpoints ou detectar degradação de performance.

**Escopo técnico:**
- `spring-boot-starter-actuator` com endpoints `/health`, `/metrics`, `/info`
- Logback configurado para saída JSON (Logstash encoder)
- Stack opcional: Prometheus scraping `/actuator/prometheus` + Grafana dashboard
- Alertas para: erro 5xx acima de X%, heap acima de 80%, tempo de resposta acima de 2 s

**Impacto:** Médio | **Esforço:** Baixo | **Dependência:** P5.2

---

## P6 — Monetização e Pagamentos

### P6.1 — Sistema de Créditos Pré-Pagos (modelo OLX)

**Descrição:**  
Prestador compra pacotes de créditos. Cada contato com um cliente (aceite de proposta) consome créditos. Cliente nunca paga. Tier gratuito de 1–5 interações.

**Justificativa:**  
O modelo de monetização já está definido. Sem ele o produto não gera receita. É o passo mais importante após o deploy.

**Escopo técnico:**
- Entidade `Pacote` (nome, quantidade de créditos, preço em centavos)
- Entidade `ContaPrestador` (saldo de créditos, total gasto)
- Entidade `TransacaoCredito` (tipo: COMPRA / CONSUMO, valor, data, referência)
- Regra: ao aceitar uma proposta, debitar 1 crédito da conta do prestador
- Regra: se saldo = 0, bloquear o aceite de novas propostas (exceto tier gratuito)
- Tier gratuito: primeiras 3 propostas aceitas são gratuitas (sem débito)
- Migrações Flyway: `V15__add_creditos_prestador.sql`
- Endpoint admin: `GET /api/admin/pacotes`, `POST /api/admin/pacotes`

**Impacto:** Alto | **Esforço:** Médio | **Dependência:** P6.2

**Status:** ✅ Entregue com gateway de pagamento **fictício** (aprova na hora, nunca carrega com o profile `prod`).
- Migração `V23__add_creditos_prestador.sql`: `pacotes_credito`, `contas_credito`, `transacoes_credito` (valores em centavos)
- O envio de proposta também é bloqueado sem créditos, para o cliente não esbarrar em aceite recusado
- Configuração: `CREDITOS_GRATUITOS` (padrão 3), `CREDITOS_ALERTA_SALDO_BAIXO` (padrão 3), `PAGAMENTO_GATEWAY` (padrão `ficticio`)
- Pacotes de exemplo (`[FICTÍCIO]`) criados pelo `DataSeeder` só fora de `prod`; pacotes reais via `POST/PUT /api/admin/pacotes`

---

### P6.2 — Integração com Gateway de Pagamento

**Descrição:**  
Integração com Mercado Pago (ou Stripe) para compra de pacotes de créditos com cartão de crédito, PIX ou boleto.

**Justificativa:**  
Sem gateway de pagamento o sistema de créditos (P6.1) não tem como receber dinheiro real.

**Escopo técnico:**
- SDK do Mercado Pago (MP) ou Stripe no backend Java
- Fluxo: prestador seleciona pacote → backend cria preferência de pagamento MP → frontend redireciona para checkout → webhook MP notifica pagamento aprovado → backend credita saldo
- Webhook endpoint: `POST /api/pagamentos/webhook` (validar assinatura HMAC)
- Tabela `pagamentos`: id, prestadorId, valor, status, gateway_id, dataCriacao
- Frontend: página "Comprar Créditos" com listagem de pacotes e botão de compra
- Testes: mock do webhook para simular pagamento aprovado/recusado

**Como plugar (deixado pronto pelo P6.1):** criar `GatewayPagamentoMercadoPago implements GatewayPagamento`
anotado com `@ConditionalOnProperty(name = "pagamento.gateway", havingValue = "mercadopago")`, retornando
`PENDENTE` + `urlCheckout` em `criarCobranca`; o webhook chama `CreditoService.confirmarPagamento(referencia, aprovado)`
(idempotente). A tabela `transacoes_credito` já guarda `gateway`, `referencia_externa` e `status`, dispensando a tabela `pagamentos`.

**Impacto:** Alto | **Esforço:** Alto | **Dependência:** P6.1, P5.2

---

### P6.3 — Painel Financeiro do Prestador

**Descrição:**  
Aba "Financeiro" no painel do prestador mostrando saldo de créditos, histórico de transações e botão para comprar mais créditos.

**Justificativa:**  
O prestador precisa ter visibilidade do seu saldo para tomar decisões de compra.

**Escopo técnico:**
- `GET /api/prestadores/me/creditos` → saldo atual + histórico paginado
- Frontend: card de saldo no dashboard + aba "Financeiro" com tabela de transações
- Alerta visual quando saldo < 3 créditos

**Impacto:** Médio | **Esforço:** Baixo | **Dependência:** P6.1

**Status:** ✅ Entregue — página `/prestador/financeiro` + card de saldo no painel do prestador.

---

## P7 — Busca e Descoberta

### P7.1 — Busca Textual Full-Text

**Descrição:**  
Permitir que o cliente busque prestadores e solicitações por palavras-chave (ex: "encanador com experiência em condomínio").

**Justificativa:**  
A busca atual filtra apenas por categoria + localização. Usuários chegam com intenção de busca textual (como no Google), e o sistema não atende esse padrão.

**Escopo técnico:**
- PostgreSQL `tsvector` / `tsquery` com `to_tsvector('portuguese', ...)`
- Colunas indexadas: `prestadores.descricao_perfil`, `solicitacoes.descricao`
- Endpoint: `GET /api/prestadores?q=encanador+condominio` com ranking por relevância (`ts_rank`)
- Migração Flyway: `V15__add_fulltext_index.sql` (ou próximo número disponível)
- Frontend: campo de busca na home e na tela de busca de prestadores

**Impacto:** Alto | **Esforço:** Médio | **Dependência:** nenhuma

---

### P7.2 — Hierarquia de Categorias (Pai → Subcategoria)

**Descrição:**  
Expandir o sistema de categorias de 19 para 100+ entradas organizadas em grupos (ex: "Elétrica" → "Instalação", "Manutenção", "Emergência").

**Justificativa:**  
GetNinjas tem 500+ categorias e Cronoshare tem 200+. Com apenas 19 o VemResolver não cobre a maioria dos serviços que usuários buscam.

**Escopo técnico:**
- Adicionar coluna `parent_id` em `categorias` (self-reference FK)
- Seed de categorias expandido (mínimo 80 subcategorias)
- Endpoint: `GET /api/categorias/tree` retorna árvore pai → filhos
- Frontend: dropdown em dois níveis (categoria → subcategoria) na criação de solicitação e no perfil do prestador

**Impacto:** Médio | **Esforço:** Médio | **Dependência:** nenhuma

---

### P7.3 — Recomendação de Prestadores por IA

**Descrição:**  
Ao criar uma solicitação, sugerir os 3 prestadores mais compatíveis com base em categoria, localização, avaliação e histórico de conclusões.

**Justificativa:**  
Reduz o tempo do cliente para encontrar um prestador e aumenta a taxa de conversão de solicitação → proposta aceita.

**Escopo técnico:**
- Score composto: `(avaliacaoMedia × 0.4) + (totalConcluidos × 0.3) + (distanciaKm⁻¹ × 0.2) + (completudePerfil × 0.1)`
- Endpoint: `GET /api/solicitacoes/{id}/prestadores-recomendados` (top 3)
- Pode ser implementado sem IA real — apenas um algoritmo de ranking determinístico
- Frontend: card "Prestadores Recomendados" após criação de solicitação

**Impacto:** Médio | **Esforço:** Baixo | **Dependência:** nenhuma

---

## P8 — Identidade e Confiança

### P8.1 — Verificação de Documento (CPF + Selfie)

**Descrição:**  
Prestador envia foto do CPF/RG e selfie. Admin aprova manualmente (ou via integração com serviço de OCR). Badge "ID Verificado" diferente do badge automático atual.

**Justificativa:**  
O badge "Verificado" atual é automático (≥5 contratos + média ≥4,0). GetNinjas e Cronoshare verificam a identidade real do prestador, aumentando a confiança do cliente.

**Escopo técnico:**
- Entidade `VerificacaoDocumento`: prestadorId, urlDocumento, urlSelfie, status (PENDENTE / APROVADO / RECUSADO), dataEnvio, dataRevisao, observacaoAdmin
- `POST /api/prestadores/me/verificacao` → upload dos documentos
- `GET/PATCH /api/admin/verificacoes` → fila de revisão para o admin
- Armazenamento: volume Docker separado (não exposto publicamente)
- Badge `idVerificado` diferente de `verificado` no perfil público
- Migração Flyway correspondente

**Impacto:** Alto | **Esforço:** Alto | **Dependência:** P5.2 (armazenamento seguro)

---

### P8.2 — Denúncia de Usuários e Conteúdo

**Descrição:**  
Cliente ou prestador pode denunciar um perfil, avaliação ou comportamento inadequado. Admin recebe fila de denúncias no painel.

**Justificativa:**  
Sem moderação, conteúdos abusivos ou perfis falsos degradam a experiência e a reputação da plataforma.

**Escopo técnico:**
- Entidade `Denuncia`: denuncianteId, denunciadoId, tipo (PERFIL / AVALIACAO / COMPORTAMENTO), descricao, status, dataCriacao
- `POST /api/denuncias`
- `GET /api/admin/denuncias` + `PATCH /api/admin/denuncias/{id}/status`
- Frontend: botão "Denunciar" no perfil público e nas avaliações
- Ação automática: suspender perfil após N denúncias aprovadas

**Impacto:** Médio | **Esforço:** Médio | **Dependência:** nenhuma

---

## P9 — Comunicação e Notificações

### P9.1 — Notificações Push (PWA / Service Worker)

**Descrição:**  
Notificações push no navegador mesmo com a aba fechada, via Web Push API e service worker.

**Justificativa:**  
O polling atual de 30 s só funciona com a aba aberta. E-mail tem latência. Push é imediato e não depende da aba estar ativa.

**Escopo técnico:**
- Service worker Angular (`@angular/service-worker`)
- Backend: Web Push via biblioteca `nl.martijndwars:web-push` (Java)
- Tabela `push_subscriptions`: userId, endpoint, p256dh, auth
- `POST /api/notificacoes/push/subscribe` e `/unsubscribe`
- Triggers: mesmos eventos que os e-mails (nova proposta, proposta aceita, serviço concluído)
- `manifest.webmanifest` para instalação como PWA

**Impacto:** Médio | **Esforço:** Alto | **Dependência:** P5.2

---

### P9.2 — Chat com Anexos de Imagem

**Descrição:**  
Permitir que cliente e prestador enviem imagens no chat de proposta (ex: foto do problema a resolver).

**Justificativa:**  
Hoje o chat suporta apenas texto. Para serviços de reforma/manutenção, o prestador precisa ver o problema para dar uma proposta precisa.

**Escopo técnico:**
- Campo `urlAnexo` nullable em `MensagemChat`
- `POST /api/chat/proposta/{id}/imagem` → upload, retorna URL
- Frontend: botão de upload de imagem no chat + preview inline
- Limite: 1 imagem por mensagem, máx 5 MB, JPEG/PNG/WEBP

**Impacto:** Médio | **Esforço:** Baixo | **Dependência:** nenhuma

---

### P9.3 — E-mail de Nova Solicitação Compatível para Prestadores

**Descrição:**  
Quando uma nova solicitação é criada, enviar e-mail para prestadores da mesma categoria e região que ainda não enviaram proposta.

**Justificativa:**  
O item consta no README como entregue, mas o gatilho automático no `SolicitacaoServicoService` precisa ser verificado e testado de ponta a ponta.

**Escopo técnico:**
- Verificar se `SolicitacaoServicoService.criar()` chama `emailService.enviarNovaSolicitacao()` para todos os prestadores compatíveis
- Limitar a 50 prestadores por solicitação (evitar spam)
- Implementar flag `emailNovaSolicitacaoAtivo` na configuração do admin
- Testes de integração para o gatilho

**Impacto:** Alto | **Esforço:** Baixo | **Dependência:** nenhuma (Brevo já configurado)

---

## P10 — Experiência do Usuário (UX)

### P10.1 — Cancelamento de Proposta pelo Prestador

**Descrição:**  
Prestador pode cancelar uma proposta ENVIADA antes de ela ser aceita ou recusada pelo cliente.

**Justificativa:**  
Atualmente só o cliente pode recusar propostas. O prestador não tem como retirar uma proposta enviada por engano ou se mudar de ideia.

**Escopo técnico:**
- `PATCH /api/propostas/{id}/cancelar` — restrito ao prestador dono da proposta
- Status novo: `CANCELADA_PELO_PRESTADOR` (ou reutilizar `RECUSADA` com campo `canceladaPorPrestador`)
- Regra: só pode cancelar se status atual for `ENVIADA`
- Frontend: botão "Cancelar proposta" no painel do prestador → propostas aguardando

**Impacto:** Médio | **Esforço:** Baixo | **Dependência:** nenhuma

---

### P10.2 — Paginação no Chat

**Descrição:**  
Carregar mensagens antigas com scroll para cima (infinite scroll reverso), em vez de exibir todas de uma vez.

**Justificativa:**  
Chats com muitas mensagens carregam tudo na memória. Para conversas longas isso degrada a performance e a usabilidade.

**Escopo técnico:**
- `GET /api/chat/proposta/{id}?page=0&size=20` já suporta paginação (Pageable)
- Frontend: detectar scroll no topo → carregar página anterior → prepend das mensagens
- Manter a posição do scroll ao carregar mais mensagens antigas

**Impacto:** Baixo | **Esforço:** Baixo | **Dependência:** nenhuma

---

### P10.3 — Filtro de Avaliações por Nota no Perfil Público

**Descrição:**  
No perfil público do prestador, permitir filtrar as avaliações exibidas por nota (ex: "Ver apenas 5 estrelas").

**Justificativa:**  
Prestadores com muitas avaliações precisam de filtros para o visitante encontrar o contexto relevante.

**Escopo técnico:**
- `GET /api/avaliacoes/prestador/{id}?nota=5&page=0&size=10`
- Frontend: chips de filtro (1★ a 5★ + "Todas") no card de avaliações do perfil público

**Impacto:** Baixo | **Esforço:** Baixo | **Dependência:** nenhuma

---

### P10.4 — Edição e Resposta a Avaliações

**Descrição:**  
Prestador pode responder publicamente a uma avaliação recebida. Cliente pode editar sua avaliação em até 7 dias.

**Justificativa:**  
Plataformas como Google Maps e Airbnb permitem respostas às avaliações. Isso aumenta o engajamento e dá ao prestador direito de defesa.

**Escopo técnico:**
- Campo `respostaPrestador` (texto) e `dataResposta` em `Avaliacao`
- `PATCH /api/avaliacoes/{id}/resposta` — restrito ao prestador avaliado
- `PATCH /api/avaliacoes/{id}` — edição pelo cliente, só nos primeiros 7 dias
- Frontend: caixa de resposta expansível em cada avaliação do perfil público

**Impacto:** Médio | **Esforço:** Médio | **Dependência:** nenhuma

---

## P11 — Mobile e PWA

### P11.1 — Progressive Web App (PWA)

**Descrição:**  
Configurar o Angular como PWA com `manifest.webmanifest`, ícones e service worker básico de cache offline.

**Justificativa:**  
PWA permite que o usuário instale o app na tela inicial do celular sem passar pela App Store, com carregamento mais rápido via cache.

**Escopo técnico:**
- `ng add @angular/pwa`
- `manifest.webmanifest`: nome, ícones 192×192 e 512×512, cor de tema, display standalone
- `ngsw-config.json`: cache de assets estáticos + API de categorias (read-only)
- Teste de instalabilidade no Chrome DevTools (Lighthouse PWA score ≥ 90)

**Impacto:** Médio | **Esforço:** Baixo | **Dependência:** P5.2

---

### P11.2 — Layout Otimizado para Mobile

**Descrição:**  
Auditoria de todas as telas no viewport mobile (375px) e correção dos problemas de usabilidade encontrados.

**Justificativa:**  
A maioria dos usuários acessa via celular. O layout usa Tailwind responsivo mas nunca foi auditado sistematicamente em mobile.

**Escopo técnico:**
- Auditoria com Chrome DevTools em 375×812 (iPhone SE) e 360×800 (Android médio)
- Correções esperadas: tabelas sem scroll horizontal, botões pequenos demais, formulários sem zoom automático (`font-size ≥ 16px` nos inputs)
- Lighthouse Mobile score: acessibilidade ≥ 90, performance ≥ 70

**Impacto:** Alto | **Esforço:** Médio | **Dependência:** nenhuma

---

## P12 — Segurança Avançada

### P12.1 — Refresh Token Rotation

**Descrição:**  
A cada uso do refresh token, invalidar o antigo e emitir um novo par (access + refresh). Detectar reutilização de token como sinal de comprometimento.

**Justificativa:**  
O refresh token atual não rotaciona. Se for interceptado, o atacante tem acesso permanente até expirar.

**Escopo técnico:**
- Tabela `refresh_tokens`: token (hash), userId, dataExpiracao, usado (boolean)
- `POST /api/auth/refresh`: verifica token → se `usado=true`, revogar todos os tokens do usuário (token theft detected) → senão, marcar como usado e emitir novo par
- TTL do refresh token: 30 dias; access token: manter em 24 h

**Impacto:** Alto (segurança) | **Esforço:** Médio | **Dependência:** nenhuma

---

### P12.2 — Auditoria de Ações Administrativas

**Descrição:**  
Registrar todas as ações do admin (ativar/desativar usuário, alterar status de solicitação, aprovar verificação) em uma tabela de auditoria imutável.

**Justificativa:**  
Sem auditoria não é possível saber quem fez o quê no painel admin — problema legal e de compliance.

**Escopo técnico:**
- Entidade `AuditoriaAdmin`: adminId, acao, entidadeAlvo, entidadeId, dadosAntes (JSON), dadosDepois (JSON), timestamp, ip
- Interceptor ou `@Aspect` que registra automaticamente as chamadas aos endpoints `/api/admin/**`
- `GET /api/admin/auditoria` com filtro por data e tipo de ação

**Impacto:** Médio | **Esforço:** Médio | **Dependência:** nenhuma

---

## Tabela de Prioridades

| # | Funcionalidade | Impacto | Esforço | Prioridade |
|---|---|---|---|---|
| P5.2 | Ambiente de Produção (VPS / PaaS) | Alto | Médio | 🔴 Crítico |
| P5.1 | CI/CD com GitHub Actions | Alto | Médio | 🔴 Crítico |
| P5.3 | Correção Vulnerabilidades Dependabot | Alto | Médio | 🔴 Crítico |
| P6.1 | Sistema de Créditos Pré-Pagos | Alto | Médio | ✅ Entregue (pagamento fictício) |
| P6.2 | Gateway de Pagamento (Mercado Pago) | Alto | Alto | 🟠 Alta |
| P11.2 | Layout Otimizado para Mobile | Alto | Médio | ✅ Entregue |
| P12.1 | Refresh Token Rotation | Alto | Médio | ✅ Entregue |
| P8.1 | Verificação de Documento (CPF + Selfie) | Alto | Alto | 🟡 Média |
| P9.3 | E-mail: Nova Solicitação para Prestadores | Alto | Baixo | 🟡 Média |
| P10.1 | Cancelamento de Proposta pelo Prestador | Médio | Baixo | 🟡 Média |
| P7.2 | Hierarquia de Categorias | Médio | Médio | 🟡 Média |
| P6.3 | Painel Financeiro do Prestador | Médio | Baixo | ✅ Entregue |
| P11.1 | Progressive Web App (PWA) | Médio | Baixo | 🟡 Média |
| P5.4 | Observabilidade (Logs + Métricas) | Médio | Baixo | ✅ Entregue |
| P8.2 | Denúncia de Usuários e Conteúdo | Médio | Médio | ✅ Entregue |
| P10.4 | Edição e Resposta a Avaliações | Médio | Médio | ✅ Entregue |
| P9.1 | Notificações Push (PWA) | Médio | Alto | ✅ Entregue |
| P9.2 | Chat com Anexos de Imagem | Médio | Baixo | ✅ Entregue |
| P7.3 | Recomendação de Prestadores por Score Composto | Médio | Baixo | ✅ Entregue |
| P7.1 | Busca Textual Full-Text | Alto | Médio | ✅ Entregue |
| P12.2 | Auditoria de Ações Administrativas | Médio | Médio | 🟢 Normal |
| P10.2 | Paginação no Chat | Baixo | Baixo | 🔵 Backlog |
| P10.3 | Filtro de Avaliações por Nota | Baixo | Baixo | 🔵 Backlog |

---

## Sequência Recomendada de Implementação

```
Fase 1 — Ir para produção (P5.2 → P5.1 → P5.3)
   ↓
Fase 2 — Gerar receita (P6.1 → P6.3 → P6.2)
   ↓
Fase 3 — Crescimento orgânico (P7.1 → P7.2 → P11.2 → P11.1)
   ↓
Fase 4 — Confiança e retenção (P8.1 → P8.2 → P10.1 → P10.4)
   ↓
Fase 5 — Segurança e compliance (P12.1 → P12.2 → P5.4)
   ↓
Fase 6 — Engajamento avançado (P9.1 → P9.2 → P9.3 → P7.3)
   ↓
Fase 7 — Polish (P10.2 → P10.3)
```

---

*Documento vivo — atualizar à medida que itens forem implementados ou que novos requisitos forem identificados.*

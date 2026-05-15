# Casos de Teste — Prioridade 4 (VemResolver)

> Documento criado em 2026-05-15 com base nos casos de uso definidos em [`CASOS-DE-USO-P4.md`](CASOS-DE-USO-P4.md).  
> Cobre fluxo principal, fluxos alternativos, fluxos de exceção, validações de regras de negócio, casos de borda e segurança.

---

## Convenções

| Campo | Formato |
|---|---|
| ID | `CT-XX-YY` — XX = número do UC, YY = sequência dentro do UC |
| Tipo | `Positivo` / `Negativo` / `Borda` / `Segurança` / `Auditoria` |
| Prioridade | `Alta` / `Média` / `Baixa` |

---

## Índice

- [CT-01 — Chat Interno](#ct-01--chat-interno)
- [CT-02 — Portfólio de Fotos](#ct-02--portfólio-de-fotos)
- [CT-03 — Onboarding Checklist](#ct-03--onboarding-checklist)
- [CT-04 — Perfil Público](#ct-04--perfil-público)
- [CT-05 — Notificações por E-mail](#ct-05--notificações-por-e-mail)
- [CT-06 — Badge Verificado](#ct-06--badge-verificado)
- [CT-07 — Avaliação Multidimensional](#ct-07--avaliação-multidimensional)

---

## CT-01 — Chat Interno

**UC de referência:** UC-01  
**Total de casos:** 11

---

### CT-01-01 — Enviar mensagem com sucesso em contrato EM_ANDAMENTO

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-01, RN-02 |

**Pré-condições:**
- Usuário autenticado como Cliente.
- Existe contrato com status `EM_ANDAMENTO` vinculando esse cliente a um prestador.

**Passos:**
1. Acessar o painel do cliente.
2. Clicar em "Chat" no contrato em andamento.
3. Digitar uma mensagem com até 2.000 caracteres no campo de texto.
4. Clicar em "Enviar".

**Resultado esperado:**
- A mensagem aparece na interface imediatamente, sem recarregar a página.
- A mensagem é persistida com `remetenteId`, `contratoId`, `texto`, `dataEnvio` e `lida = false`.
- Uma notificação in-app é criada para o prestador.
- Nenhum erro é exibido.

---

### CT-01-02 — Prestador responde e mensagens anteriores são marcadas como lidas

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | FA — Prestador responde |

**Pré-condições:**
- Existe ao menos uma mensagem enviada pelo cliente no chat, com `lida = false`.
- Prestador está autenticado.

**Passos:**
1. Prestador acessa o painel → contrato em andamento → aba "Chat".
2. Verificar que mensagens não lidas possuem marcação visual distinta.
3. Digitar resposta e clicar em "Enviar".

**Resultado esperado:**
- Mensagens anteriores do prestador são marcadas como `lida = true`.
- A nova mensagem do prestador aparece na interface imediatamente.
- Uma notificação in-app é criada para o cliente.

---

### CT-01-03 — Chat em contrato CONCLUIDA exibe somente leitura

| Campo | Valor |
|---|---|
| Tipo | Negativo |
| Prioridade | Alta |
| Regra relacionada | RN-01, RN-04 |

**Pré-condições:**
- Existe contrato com status `CONCLUIDA`.
- Usuário autenticado como participante do contrato (cliente ou prestador).

**Passos:**
1. Acessar o chat do contrato concluído.
2. Verificar se o campo de texto está disponível para digitação.
3. Tentar enviar uma mensagem caso o campo esteja acessível.

**Resultado esperado:**
- Campo de texto exibido como desabilitado.
- Aviso exibido: "Este contrato foi encerrado. O chat está disponível apenas para contratos em andamento."
- Histórico de mensagens visível em somente leitura.
- Nenhuma nova mensagem é persistida.

---

### CT-01-04 — Chat em contrato CANCELADA exibe somente leitura

| Campo | Valor |
|---|---|
| Tipo | Negativo |
| Prioridade | Alta |
| Regra relacionada | RN-01, RN-04 |

**Pré-condições:**
- Existe contrato com status `CANCELADA`.
- Usuário autenticado como participante.

**Passos:**
1. Acessar o chat do contrato cancelado.

**Resultado esperado:**
- Mesmo comportamento do CT-01-03: campo desabilitado, aviso exibido, histórico visível em somente leitura.

---

### CT-01-05 — Mensagem vazia não é enviada

| Campo | Valor |
|---|---|
| Tipo | Negativo |
| Prioridade | Alta |
| Regra relacionada | RN-02 |

**Pré-condições:**
- Contrato ativo com status `EM_ANDAMENTO`.
- Usuário autenticado como participante.

**Passos:**
1. Acessar o chat.
2. Deixar o campo de texto vazio.
3. Clicar em "Enviar" (ou pressionar Enter).

**Resultado esperado:**
- Envio bloqueado no frontend (botão desabilitado ou validação de formulário).
- Nenhuma requisição é feita ao backend.
- Nenhuma mensagem é persistida.

---

### CT-01-06 — Mensagem com 2.001 caracteres rejeitada

| Campo | Valor |
|---|---|
| Tipo | Borda |
| Prioridade | Média |
| Regra relacionada | RN-02 |

**Pré-condições:**
- Contrato ativo.
- Usuário autenticado.

**Passos:**
1. Acessar o chat.
2. Digitar uma mensagem com exatamente 2.001 caracteres.
3. Clicar em "Enviar".

**Resultado esperado:**
- Envio rejeitado.
- Mensagem de erro exibida ao usuário indicando o limite de 2.000 caracteres.
- Nenhuma mensagem persistida no backend.

---

### CT-01-07 — Mensagem com exatamente 2.000 caracteres é aceita

| Campo | Valor |
|---|---|
| Tipo | Borda |
| Prioridade | Média |
| Regra relacionada | RN-02 |

**Pré-condições:**
- Contrato ativo.
- Usuário autenticado.

**Passos:**
1. Acessar o chat.
2. Digitar uma mensagem com exatamente 2.000 caracteres.
3. Clicar em "Enviar".

**Resultado esperado:**
- Mensagem enviada e persistida com sucesso.
- Sem mensagem de erro.

---

### CT-01-08 — Usuário não participante do contrato recebe 403

| Campo | Valor |
|---|---|
| Tipo | Segurança |
| Prioridade | Alta |
| Regra relacionada | RN-03 |

**Pré-condições:**
- Existe contrato ativo entre Cliente A e Prestador A.
- Usuário Cliente B está autenticado.

**Passos:**
1. Cliente B tenta acessar diretamente a URL do chat do contrato (ex: `/chat/{contratoId}`).
2. Alternativamente, Cliente B tenta chamar a API `POST /api/chat/{contratoId}/mensagens` com seu token JWT.

**Resultado esperado:**
- Backend retorna HTTP 403 Forbidden.
- Nenhuma mensagem do contrato é exibida a Cliente B.
- Nenhuma mensagem é persistida.

---

### CT-01-09 — Histórico de mensagens persiste após conclusão do contrato

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-04 |

**Pré-condições:**
- Contrato com histórico de mensagens já enviadas.
- Contrato tem status `CONCLUIDA`.

**Passos:**
1. Acessar o chat do contrato concluído como cliente ou prestador.

**Resultado esperado:**
- Todo o histórico de mensagens (enviadas antes da conclusão) está visível.
- Mensagens exibidas em ordem cronológica crescente.

---

### CT-01-10 — Notificação in-app criada ao enviar mensagem

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Critério relacionado | CA — "Notificação in-app é criada para o destinatário" |

**Pré-condições:**
- Contrato ativo.
- Cliente autenticado.
- Prestador com sino de notificações visível no navbar.

**Passos:**
1. Cliente envia uma mensagem no chat.
2. Verificar no banco ou via API de notificações se uma notificação foi criada para o prestador.

**Resultado esperado:**
- Notificação in-app do tipo "chat" criada para o prestador.
- Contador do sino do prestador incrementado em 1.
- Texto da notificação contém o nome do remetente.

---

### CT-01-11 — Falha de rede exibe erro e restaura o texto no campo

| Campo | Valor |
|---|---|
| Tipo | Negativo |
| Prioridade | Média |
| Regra relacionada | FE — Falha de rede |

**Pré-condições:**
- Contrato ativo.
- Backend indisponível ou simulado para retornar erro 500/timeout.

**Passos:**
1. Acessar o chat.
2. Digitar uma mensagem.
3. Clicar em "Enviar" com o backend simulando falha.

**Resultado esperado:**
- Mensagem de erro exibida: "Não foi possível enviar a mensagem. Tente novamente."
- O texto digitado retorna ao campo de texto para reenvio.
- Nenhuma mensagem fantasma permanece na interface.

---

## CT-02 — Portfólio de Fotos

**UC de referência:** UC-02  
**Total de casos:** 13

---

### CT-02-01 — Upload de imagem JPG válida com sucesso

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-01, RN-02 |

**Pré-condições:**
- Prestador autenticado com perfil criado.
- Portfólio com menos de 10 fotos.

**Passos:**
1. Acessar "Meu Perfil" → aba "Portfólio".
2. Clicar em "Adicionar foto".
3. Selecionar arquivo `.jpg` com tamanho menor que 5 MB.

**Resultado esperado:**
- Upload concluído em menos de 3 segundos.
- Foto exibida na galeria sem recarregar a página.
- Opção de adicionar legenda disponível.
- Foto persistida e visível no perfil público.

---

### CT-02-02 — Upload de imagem PNG válida com sucesso

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-02 |

**Pré-condições:** Mesmo do CT-02-01.

**Passos:**
1. Repetir CT-02-01 com arquivo `.png` válido.

**Resultado esperado:** Mesmo do CT-02-01.

---

### CT-02-03 — Upload de imagem WEBP válida com sucesso

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Média |
| Regra relacionada | RN-02 |

**Pré-condições:** Mesmo do CT-02-01.

**Passos:**
1. Repetir CT-02-01 com arquivo `.webp` válido.

**Resultado esperado:** Mesmo do CT-02-01.

---

### CT-02-04 — Upload de arquivo GIF rejeitado com mensagem de erro

| Campo | Valor |
|---|---|
| Tipo | Negativo |
| Prioridade | Alta |
| Regra relacionada | RN-02 |

**Pré-condições:**
- Prestador autenticado com portfólio com menos de 10 fotos.

**Passos:**
1. Acessar "Meu Perfil" → aba "Portfólio".
2. Clicar em "Adicionar foto".
3. Selecionar arquivo `.gif`.

**Resultado esperado:**
- Upload rejeitado.
- Mensagem exibida: "Formato não suportado. Use JPG, PNG ou WEBP até 5 MB."
- Nenhum arquivo persistido.
- Interface não trava.

---

### CT-02-05 — Upload de arquivo acima de 5 MB rejeitado

| Campo | Valor |
|---|---|
| Tipo | Negativo |
| Prioridade | Alta |
| Regra relacionada | RN-02 |

**Pré-condições:**
- Prestador autenticado.

**Passos:**
1. Acessar "Meu Perfil" → aba "Portfólio".
2. Clicar em "Adicionar foto".
3. Selecionar arquivo `.jpg` com tamanho de 5,1 MB.

**Resultado esperado:**
- Upload rejeitado.
- Mensagem exibida: "Formato não suportado. Use JPG, PNG ou WEBP até 5 MB."
- Nenhum arquivo persistido.

---

### CT-02-06 — 10ª foto adicionada com sucesso; botão desabilitado após

| Campo | Valor |
|---|---|
| Tipo | Borda |
| Prioridade | Alta |
| Regra relacionada | RN-01 |

**Pré-condições:**
- Portfólio com exatamente 9 fotos.

**Passos:**
1. Acessar aba "Portfólio".
2. Clicar em "Adicionar foto" e enviar uma imagem válida.

**Resultado esperado:**
- 10ª foto adicionada com sucesso.
- Botão "Adicionar foto" fica desabilitado após o upload.
- Contagem exibida indica "10/10".

---

### CT-02-07 — Tentativa de adicionar 11ª foto bloqueada com mensagem de limite

| Campo | Valor |
|---|---|
| Tipo | Negativo |
| Prioridade | Alta |
| Regra relacionada | RN-01 |

**Pré-condições:**
- Portfólio com exatamente 10 fotos.

**Passos:**
1. Acessar aba "Portfólio".
2. Tentar clicar em "Adicionar foto" (botão deve estar desabilitado).
3. Alternativamente, chamar diretamente o endpoint `POST /api/prestadores/me/portfolio` com autenticação válida.

**Resultado esperado:**
- Frontend: botão desabilitado com mensagem: "Limite de 10 fotos atingido. Remova uma foto para adicionar outra."
- Backend: retorna HTTP 422 ou 400 com mensagem de erro.
- Nenhuma foto adicional persistida.

---

### CT-02-08 — Adicionar legenda com até 150 caracteres com sucesso

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Média |
| Regra relacionada | RN-04 |

**Pré-condições:**
- Foto recém-enviada aguardando legenda.

**Passos:**
1. Após upload bem-sucedido, digitar legenda com exatamente 150 caracteres.
2. Clicar em "Salvar".

**Resultado esperado:**
- Legenda salva e exibida sob a foto na galeria.

---

### CT-02-09 — Legenda com 151 caracteres rejeitada

| Campo | Valor |
|---|---|
| Tipo | Borda |
| Prioridade | Média |
| Regra relacionada | RN-04 |

**Pré-condições:**
- Foto disponível para edição de legenda.

**Passos:**
1. Digitar legenda com 151 caracteres.
2. Tentar salvar.

**Resultado esperado:**
- Campo limitado no frontend (contador de caracteres) ou erro de validação ao salvar.
- Legenda não persistida com mais de 150 caracteres.

---

### CT-02-10 — Reordenar fotos via drag-drop persiste nova ordem

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Média |
| Regra relacionada | RN-03 |

**Pré-condições:**
- Portfólio com pelo menos 2 fotos.

**Passos:**
1. Acessar aba "Portfólio".
2. Arrastar a 2ª foto para a 1ª posição.
3. Verificar no perfil público a nova ordem.

**Resultado esperado:**
- Galeria exibe a nova ordenação imediatamente.
- Após recarregar a página, a ordem persiste.
- A foto movida para o índice 0 passa a ser usada como destaque no card de busca.

---

### CT-02-11 — Remover foto com confirmação remove da galeria

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | FA — Remover foto |

**Pré-condições:**
- Portfólio com pelo menos 1 foto.

**Passos:**
1. Acessar aba "Portfólio".
2. Clicar no ícone de lixeira sobre uma foto.
3. Confirmar a remoção no modal de confirmação.

**Resultado esperado:**
- Foto removida da galeria sem recarregar a página.
- Foto não aparece mais no perfil público.
- Botão "Adicionar foto" reabilitado se o portfólio estiver abaixo de 10 fotos.

---

### CT-02-12 — Galeria visível sem autenticação no perfil público

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Critério relacionado | CA — "Galeria é visível para qualquer visitante do perfil público" |

**Pré-condições:**
- Prestador com pelo menos 1 foto no portfólio.
- Sessão não autenticada no navegador.

**Passos:**
1. Acessar `/prestador/{id}` sem estar logado.

**Resultado esperado:**
- Galeria de fotos exibida.
- Nenhum redirecionamento para login.

---

### CT-02-13 — Foto de índice 0 exibida como destaque no card de busca

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Média |
| Regra relacionada | RN-03 |

**Pré-condições:**
- Prestador com múltiplas fotos no portfólio.

**Passos:**
1. Verificar o card do prestador na listagem de busca.
2. Alterar a posição da primeira foto (drag-drop para segunda posição).
3. Verificar o card de busca novamente.

**Resultado esperado:**
- O card de busca exibe sempre a foto de índice 0 (primeira da ordem de exibição).
- Após reordenação, o card reflete a nova foto de destaque.

---

## CT-03 — Onboarding Checklist

**UC de referência:** UC-03  
**Total de casos:** 10

---

### CT-03-01 — Prestador novo tem todos os itens pendentes e percentual 0%

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-01, RN-02 |

**Pré-condições:**
- Prestador autenticado que acabou de criar o perfil, sem nenhum dos 7 itens preenchidos.

**Passos:**
1. Acessar o painel do prestador.

**Resultado esperado:**
- Barra de progresso exibe 0%.
- Cor vermelha (< 50%).
- Todos os 7 itens exibidos com ícone ⬜ (pendente).
- Cada item possui link para a seção correspondente.

---

### CT-03-02 — Completar foto de perfil incrementa 15% no progresso

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-01, RN-02 |

**Pré-condições:**
- Prestador sem foto de perfil, progresso em 0%.

**Passos:**
1. Clicar no item "Foto de perfil (avatar)" no checklist.
2. Sistema navega para a seção de upload de avatar.
3. Fazer upload de uma foto válida.
4. Salvar.

**Resultado esperado:**
- Checklist atualizado em tempo real: item "Foto de perfil" marcado com ✅.
- Barra de progresso sobe de 0% para 15%.
- Sem recarregamento de página.

---

### CT-03-03 — Completar descrição com 50+ caracteres incrementa 20%

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-01, RN-02 |

**Pré-condições:**
- Prestador sem descrição no perfil.

**Passos:**
1. Acessar edição de perfil.
2. Preencher campo "Descrição" com exatamente 50 caracteres.
3. Salvar.

**Resultado esperado:**
- Item "Descrição do perfil" marcado como ✅.
- Progresso incrementado em 20%.

---

### CT-03-04 — Descrição com 49 caracteres não marca item como concluído

| Campo | Valor |
|---|---|
| Tipo | Borda |
| Prioridade | Alta |
| Regra relacionada | RN-01 |

**Pré-condições:**
- Prestador sem descrição.

**Passos:**
1. Preencher descrição com exatamente 49 caracteres.
2. Salvar.

**Resultado esperado:**
- Item "Descrição do perfil" permanece com ícone ⬜ (pendente).
- Progresso não é incrementado.

---

### CT-03-05 — Completar todos os 7 itens exibe barra verde 100% e mensagem de parabéns

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-02, FA — Perfil 100% completo |

**Pré-condições:**
- Prestador com 6 dos 7 itens concluídos (progresso em 90%).

**Passos:**
1. Completar o último item pendente.
2. Salvar.

**Resultado esperado:**
- Barra de progresso exibe 100% com cor verde.
- Mensagem exibida: "Perfil completo! Você tem mais chances de ser contratado."
- Checklist recolhido ou ocultado automaticamente.

---

### CT-03-06 — Clicar em item pendente navega para a seção correta

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | FA — Clique em item pendente |

**Pré-condições:**
- Prestador com pelo menos 1 item pendente no checklist.

**Passos:**
1. Clicar no item "Pelo menos 1 foto no portfólio".

**Resultado esperado:**
- Sistema navega ou rola até a seção de portfólio ou abre o modal correspondente.

**Verificar para cada item:**
| Item | Destino esperado |
|---|---|
| Foto de perfil | Seção de upload de avatar |
| Descrição | Campo de edição de descrição |
| Categoria | Seletor de categorias |
| Telefone | Campo de telefone no perfil |
| Cidade/Estado | Campos de localização |
| Portfólio | Aba "Portfólio" |
| Raio de atendimento | Campo de geolocalização |

---

### CT-03-07 — Cliente logado não visualiza o checklist

| Campo | Valor |
|---|---|
| Tipo | Segurança |
| Prioridade | Alta |
| Regra relacionada | RN-04 |

**Pré-condições:**
- Usuário autenticado como `ROLE_CLIENTE`.

**Passos:**
1. Acessar o painel do cliente.
2. Verificar se o componente de checklist é renderizado.

**Resultado esperado:**
- Nenhum checklist de onboarding visível no painel do cliente.
- Componente não é carregado para usuários do tipo CLIENTE.

---

### CT-03-08 — Prestador com completude < 50% não aparece nos primeiros resultados de busca

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-03 |

**Pré-condições:**
- Prestador A com completude 40% (abaixo de 50%).
- Prestador B com completude 85% (acima de 80%).
- Ambos na mesma categoria e cidade.

**Passos:**
1. Realizar busca de prestadores por categoria e cidade.
2. Verificar a ordenação dos resultados.

**Resultado esperado:**
- Prestador B aparece antes do Prestador A nos resultados.
- Prestador A pode aparecer, mas apenas após prestadores com completude ≥ 50%.

---

### CT-03-09 — Progresso atualizado em tempo real após salvar item

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Critério relacionado | CA — "Completar um item atualiza a barra sem recarregar a página" |

**Pré-condições:**
- Prestador com checklist visível e pelo menos 1 item pendente.

**Passos:**
1. Completar um item (ex: preencher telefone).
2. Salvar sem sair da página.

**Resultado esperado:**
- Barra de progresso atualizada imediatamente (sem reload).
- Item correspondente marcado com ✅.

---

### CT-03-10 — Cor da barra reflete o percentual correto

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Média |
| Regra relacionada | FP — Visualização do progresso |

**Pré-condições:**
- Prestador com perfil criado.

**Passos e resultados esperados:**

| Percentual | Cor esperada |
|---|---|
| 0% – 49% | Vermelho |
| 50% – 79% | Amarelo |
| 80% – 100% | Verde |

---

## CT-04 — Perfil Público

**UC de referência:** UC-04  
**Total de casos:** 10

---

### CT-04-01 — Visitante acessa perfil válido sem autenticação

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-01 |

**Pré-condições:**
- Existe prestador com `ativo = true` e o `id` é conhecido.
- Sessão não autenticada.

**Passos:**
1. Acessar `/prestador/{id}` diretamente no navegador sem estar logado.

**Resultado esperado:**
- Página carregada com: foto, nome, categorias, cidade/estado, nota média, portfólio e avaliações.
- Nenhum redirecionamento para login.
- Badge "Verificado" exibido se o prestador for elegível.

---

### CT-04-02 — Botão "Solicitar serviço" redireciona não-logado para /register

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | FP — passo 5 |

**Pré-condições:**
- Visitante não autenticado na página do perfil público.

**Passos:**
1. Clicar em "Solicitar serviço".

**Resultado esperado:**
- Redirecionamento para `/register?tipo=CLIENTE`.

---

### CT-04-03 — Botão "Solicitar serviço" redireciona cliente logado para nova solicitação

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | FP — passo 5 |

**Pré-condições:**
- Usuário autenticado como `ROLE_CLIENTE`.

**Passos:**
1. Acessar `/prestador/{id}`.
2. Clicar em "Solicitar serviço".

**Resultado esperado:**
- Redirecionamento para `/cliente/nova-solicitacao`.

---

### CT-04-04 — ID inexistente retorna página 404 amigável

| Campo | Valor |
|---|---|
| Tipo | Negativo |
| Prioridade | Alta |
| Regra relacionada | FE — Perfil não encontrado |

**Pré-condições:**
- ID `99999` não existe na base.

**Passos:**
1. Acessar `/prestador/99999`.

**Resultado esperado:**
- Página amigável de 404 exibida: "Perfil não encontrado."
- Nenhum stack trace ou erro técnico visível ao usuário.
- Status HTTP 404 retornado pelo backend.

---

### CT-04-05 — Prestador com ativo=false retorna 404

| Campo | Valor |
|---|---|
| Tipo | Negativo |
| Prioridade | Alta |
| Regra relacionada | RN-04 |

**Pré-condições:**
- Existe prestador com `ativo = false` (conta deletada via soft delete).
- O `id` é conhecido.

**Passos:**
1. Acessar `/prestador/{id}` do prestador inativo.

**Resultado esperado:**
- Mesmo comportamento do CT-04-04: página 404 amigável.
- Dados do prestador inativo não expostos.

---

### CT-04-06 — Dados sensíveis não aparecem na página pública

| Campo | Valor |
|---|---|
| Tipo | Segurança |
| Prioridade | Alta |
| Regra relacionada | RN-03 |

**Pré-condições:**
- Perfil de prestador com e-mail, telefone e CPF cadastrados.

**Passos:**
1. Acessar `/prestador/{id}` sem autenticação.
2. Inspecionar o HTML da página, o JSON retornado pela API e o código-fonte.

**Resultado esperado:**
- E-mail, telefone e CPF **não** presentes no HTML renderizado.
- E-mail, telefone e CPF **não** presentes no JSON da API pública.

---

### CT-04-07 — Badge "Verificado" exibido apenas para prestadores elegíveis

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-02 |

**Pré-condições:**
- Prestador A com `badgeVerificado = true`.
- Prestador B com `badgeVerificado = false`.

**Passos:**
1. Acessar `/prestador/{idA}`.
2. Acessar `/prestador/{idB}`.

**Resultado esperado:**
- Perfil de Prestador A exibe ícone/badge "Verificado".
- Perfil de Prestador B não exibe o badge.

---

### CT-04-08 — Botão "Copiar link" copia a URL correta para a área de transferência

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Média |
| Regra relacionada | FA — Compartilhar link |

**Pré-condições:**
- Prestador autenticado em seu painel ou perfil público.

**Passos:**
1. Clicar em "Copiar link do perfil".

**Resultado esperado:**
- URL `https://vemresolver.com/prestador/{id}` copiada para a área de transferência.
- Confirmação exibida: "Link copiado! Compartilhe com seus clientes."

---

### CT-04-09 — Últimas 5 avaliações exibidas com dados corretos

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-02 |

**Pré-condições:**
- Prestador com 7 avaliações recebidas.

**Passos:**
1. Acessar o perfil público do prestador.
2. Verificar a seção de avaliações.

**Resultado esperado:**
- Exatamente as 5 avaliações mais recentes são exibidas.
- Cada avaliação contém: nome do cliente, nota (estrelas) e comentário.
- As 2 avaliações mais antigas não são exibidas.

---

### CT-04-10 — Página possui meta tags adequadas para indexação por buscadores

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Média |
| Regra relacionada | RN-05 |

**Pré-condições:**
- Perfil de prestador válido e ativo.

**Passos:**
1. Acessar `/prestador/{id}`.
2. Inspecionar as meta tags do `<head>` da página (`<title>`, `<meta name="description">`, ausência de `<meta name="robots" content="noindex">`).

**Resultado esperado:**
- `<title>` contém nome do prestador e/ou categoria.
- `<meta name="description">` presente com conteúdo relevante.
- **Ausência** de `<meta name="robots" content="noindex">` ou `content="noindex, nofollow"`.

---

## CT-05 — Notificações por E-mail

**UC de referência:** UC-05  
**Total de casos:** 14

---

### CT-05-01 — Proposta enviada dispara e-mail para o cliente

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | FP — tabela de gatilhos |

**Pré-condições:**
- Cliente com e-mail verificado e solicitação aberta.
- Prestador autenticado.

**Passos:**
1. Prestador envia proposta para a solicitação do cliente.

**Resultado esperado:**
- E-mail disparado ao cliente em até 30 segundos.
- Assunto: "Você recebeu uma nova proposta para [título da solicitação]".
- E-mail contém link direto para a tela de propostas recebidas.
- A operação de envio de proposta não é bloqueada ou atrasada pelo envio do e-mail.

---

### CT-05-02 — Proposta aceita dispara e-mail para o prestador

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-06 — e-mail crítico |

**Pré-condições:**
- Prestador com proposta pendente.
- Cliente autenticado.

**Passos:**
1. Cliente aceita a proposta do prestador.

**Resultado esperado:**
- E-mail disparado ao prestador em até 30 segundos.
- Assunto: "Sua proposta foi aceita! Entre em contato com [nome do cliente]".
- E-mail contém link direto para o contrato.

---

### CT-05-03 — Proposta recusada dispara e-mail para o prestador

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-06 — e-mail crítico |

**Pré-condições:**
- Cliente com proposta para recusar.

**Passos:**
1. Cliente recusa a proposta.

**Resultado esperado:**
- E-mail disparado ao prestador.
- Assunto: "Sua proposta para [título] não foi selecionada desta vez".

---

### CT-05-04 — Serviço marcado como concluído dispara e-mail para o prestador

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | FP — tabela de gatilhos |

**Passos:**
1. Cliente marca serviço como concluído.

**Resultado esperado:**
- E-mail disparado ao prestador.
- Assunto: "Serviço concluído — [nome do cliente] marcou o serviço como finalizado".

---

### CT-05-05 — Avaliação recebida dispara e-mail para o avaliado

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | FP — tabela de gatilhos |

**Passos:**
1. Cliente envia avaliação para o prestador.

**Resultado esperado:**
- E-mail disparado ao prestador.
- Assunto: "Você recebeu uma nova avaliação — nota [X]/5".

---

### CT-05-06 — Nova mensagem no chat dispara e-mail para o destinatário

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | FP — tabela de gatilhos |

**Passos:**
1. Cliente envia mensagem no chat de um contrato ativo.

**Resultado esperado:**
- E-mail disparado ao prestador.
- Assunto: "Nova mensagem de [nome]: [preview de 80 chars...]".
- Body contém link direto para o chat.

---

### CT-05-07 — Nova solicitação compatível dispara e-mail para prestador elegível

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-03, RN-04 |

**Pré-condições:**
- Prestador atende à categoria "Eletricista" na cidade "São Paulo".
- Solicitação criada: categoria "Eletricista", cidade "São Paulo".

**Passos:**
1. Cliente cria solicitação de serviço elétrico em São Paulo.

**Resultado esperado:**
- E-mail de "nova oportunidade" disparado ao prestador elegível.
- Assunto: "Nova oportunidade: [título da solicitação] em [cidade]".
- Prestadores de outras categorias ou cidades **não** recebem o e-mail.

---

### CT-05-08 — Segundo e-mail de nova oportunidade dentro de 1h não é enviado (debounce)

| Campo | Valor |
|---|---|
| Tipo | Borda |
| Prioridade | Alta |
| Regra relacionada | RN-04 |

**Pré-condições:**
- Prestador recebeu um e-mail de "nova oportunidade" há 30 minutos.

**Passos:**
1. Criar nova solicitação compatível com o prestador dentro do intervalo de 1 hora.

**Resultado esperado:**
- Segundo e-mail **não** é disparado.
- O evento é registrado no log como "suprimido por debounce".
- Após 1 hora do primeiro envio, o próximo e-mail elegível é enviado normalmente.

---

### CT-05-09 — Preferência de desativação de categoria de e-mail é respeitada

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-05, FA — Usuário desativa e-mails |

**Pré-condições:**
- Prestador com preferência "Nova oportunidade" desativada nas configurações.

**Passos:**
1. Cliente cria solicitação compatível com o prestador.

**Resultado esperado:**
- E-mail de "nova oportunidade" **não** é enviado ao prestador.
- Outros e-mails (ex: proposta aceita) continuam sendo enviados normalmente.

---

### CT-05-10 — E-mails críticos (proposta aceita/recusada) não podem ser desativados

| Campo | Valor |
|---|---|
| Tipo | Negativo |
| Prioridade | Alta |
| Regra relacionada | RN-06 |

**Pré-condições:**
- Usuário tenta desativar o tipo "Proposta aceita" nas configurações.

**Passos:**
1. Acessar "Configurações" → "Notificações por e-mail".
2. Verificar se a opção de desativar "Proposta aceita" ou "Proposta recusada" está disponível.

**Resultado esperado:**
- As categorias críticas estão bloqueadas para desativação (checkbox desabilitado ou ausente).
- Se o frontend tentar burlar via API, o backend ignora a preferência e envia o e-mail normalmente.

---

### CT-05-11 — Falha no envio de e-mail não retorna erro 500 para o usuário

| Campo | Valor |
|---|---|
| Tipo | Negativo |
| Prioridade | Alta |
| Regra relacionada | RN-01, FE — Falha no envio |

**Pré-condições:**
- Servidor de e-mail simulado para retornar timeout.

**Passos:**
1. Realizar ação que dispara e-mail (ex: aceitar proposta).

**Resultado esperado:**
- A ação de negócio (aceitar proposta) é concluída com sucesso para o usuário.
- Nenhum erro 500 exibido.
- Falha registrada no log de auditoria.

---

### CT-05-12 — Retry 3 vezes com backoff após falha

| Campo | Valor |
|---|---|
| Tipo | Negativo |
| Prioridade | Média |
| Regra relacionada | FE — Falha no envio |

**Pré-condições:**
- Servidor de e-mail retornando erro em todas as tentativas.

**Passos:**
1. Disparar evento que gera e-mail com servidor de e-mail falhando.
2. Monitorar o log de tentativas de envio.

**Resultado esperado:**
- Sistema tenta reenvio após 1 minuto (1ª tentativa), 5 minutos (2ª) e 30 minutos (3ª).
- Após 3 falhas, evento marcado como `FALHOU` no log.
- Nenhuma tentativa adicional após a 3ª.

---

### CT-05-13 — Log de auditoria registra todos os disparos com status

| Campo | Valor |
|---|---|
| Tipo | Auditoria |
| Prioridade | Média |
| Critério relacionado | CA — "Log de auditoria registra todos os disparos" |

**Passos:**
1. Disparar eventos que geram e-mails (proposta aceita, avaliação recebida, etc.).
2. Consultar tabela/log de auditoria de e-mails.

**Resultado esperado:**
- Cada disparo registrado com: `timestamp`, `tipoEvento`, `destinatario`, `status` (`ENVIADO` / `FALHOU`).
- Registro disponível para consulta administrativa.

---

### CT-05-14 — Link no e-mail leva diretamente à tela correspondente (deep link)

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-02 |

**Passos:**
1. Receber e-mail de "proposta aceita".
2. Clicar no link do e-mail.

**Resultado esperado para cada tipo:**

| Tipo de e-mail | Tela de destino |
|---|---|
| Proposta recebida | Painel do cliente → aba propostas |
| Proposta aceita | Painel do prestador → contrato em andamento |
| Avaliação recebida | Perfil do usuário → aba avaliações |
| Nova mensagem | Chat do contrato correspondente |
| Nova oportunidade | Detalhe da solicitação |

---

## CT-06 — Badge Verificado

**UC de referência:** UC-06  
**Total de casos:** 11

---

### CT-06-01 — Badge concedido ao atingir os 3 critérios simultaneamente

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-01, RN-02 |

**Pré-condições:**
- Prestador com exatamente 4 contratos concluídos, média 4,2 e completude 85%.

**Passos:**
1. Concluir o 5º contrato.
2. Sistema recalcula critérios.

**Resultado esperado:**
- `badgeVerificado = true` e `dataBadge` gravados no perfil.
- Notificação in-app: "Parabéns! Você conquistou o badge Verificado."
- E-mail de parabéns enviado ao prestador.
- Badge visível no card de busca, perfil público e painel do prestador.

---

### CT-06-02 — Prestador com 4 contratos não recebe badge

| Campo | Valor |
|---|---|
| Tipo | Negativo |
| Prioridade | Alta |
| Regra relacionada | RN-01 |

**Pré-condições:**
- Prestador com apenas 4 contratos concluídos, média 4,5 e completude 90%.

**Passos:**
1. Verificar estado do badge após o 4º contrato concluído.

**Resultado esperado:**
- `badgeVerificado = false`.
- Nenhuma notificação de concessão gerada.

---

### CT-06-03 — Prestador com 5 contratos e média 3,9 não recebe badge

| Campo | Valor |
|---|---|
| Tipo | Negativo |
| Prioridade | Alta |
| Regra relacionada | RN-01 |

**Pré-condições:**
- Prestador com 5 contratos concluídos, média 3,9 e completude 90%.

**Passos:**
1. Verificar estado do badge.

**Resultado esperado:**
- `badgeVerificado = false`.

---

### CT-06-04 — Prestador com completude 79% não recebe badge (caso borda)

| Campo | Valor |
|---|---|
| Tipo | Borda |
| Prioridade | Alta |
| Regra relacionada | RN-01 |

**Pré-condições:**
- Prestador com 5 contratos concluídos, média 4,2 e completude exatamente 79%.

**Passos:**
1. Verificar estado do badge.

**Resultado esperado:**
- `badgeVerificado = false`.
- Badge não concedido com completude < 80%.

---

### CT-06-05 — Avaliação negativa baixa média abaixo de 4,0 e remove o badge

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-03, FA — Remoção do badge |

**Pré-condições:**
- Prestador com `badgeVerificado = true` e média atual de 4,1 calculada sobre muitas avaliações.

**Passos:**
1. Cliente envia avaliação com nota 1 que derruba a média para 3,9.
2. Sistema recalcula critérios.

**Resultado esperado:**
- `badgeVerificado = false` com data de remoção registrada.
- Notificação in-app: "Seu badge Verificado foi removido. Sua média atual é 3,9. Mantenha a qualidade para reconquistá-lo."
- Badge removido do card de busca e perfil público.

---

### CT-06-06 — Notificação in-app gerada na concessão do badge

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Critério relacionado | CA — "Notificação gerada tanto na concessão quanto na remoção" |

**Passos:**
1. Prestador atinge os 3 critérios (conforme CT-06-01).

**Resultado esperado:**
- Notificação in-app criada com texto: "Parabéns! Você conquistou o badge Verificado."
- Notificação aparece no sino do navbar do prestador.

---

### CT-06-07 — Notificação in-app gerada na remoção do badge

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Critério relacionado | CA — "Notificação gerada tanto na concessão quanto na remoção" |

**Passos:**
1. Avaliação negativa remove o badge (conforme CT-06-05).

**Resultado esperado:**
- Notificação in-app criada informando a remoção e a média atual.

---

### CT-06-08 — Badge visível no card de busca de prestadores

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Critério relacionado | CA — "Badge visível no card de busca" |

**Pré-condições:**
- Prestador com `badgeVerificado = true`.

**Passos:**
1. Realizar busca de prestadores.
2. Localizar o card do prestador verificado.

**Resultado esperado:**
- Ícone ou label "✓ Verificado" visível no card do prestador.

---

### CT-06-09 — Filtro "Apenas verificados" retorna somente prestadores com badge

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Critério relacionado | CA — "Filtro 'Apenas verificados' funciona na busca" |

**Pré-condições:**
- Existem prestadores verificados e não verificados na mesma categoria.

**Passos:**
1. Acessar busca de prestadores.
2. Aplicar filtro "Apenas verificados".

**Resultado esperado:**
- Apenas prestadores com `badgeVerificado = true` são exibidos.
- Prestadores sem badge **não** aparecem nos resultados.

---

### CT-06-10 — Histórico de concessão e remoção preservado para auditoria

| Campo | Valor |
|---|---|
| Tipo | Auditoria |
| Prioridade | Média |
| Regra relacionada | RN-05 |

**Passos:**
1. Badge concedido e posteriormente removido para um prestador.
2. Consultar log de auditoria de badges.

**Resultado esperado:**
- Registro de concessão: `prestadorId`, `dataBadgeConcessao`, critérios atendidos.
- Registro de remoção: `prestadorId`, `dataBadgeRemocao`, critério que falhou.
- Ambos os registros persistem independentemente do estado atual.

---

### CT-06-11 — Badge não pode ser concedido manualmente via API de admin

| Campo | Valor |
|---|---|
| Tipo | Segurança |
| Prioridade | Alta |
| Regra relacionada | RN-04 |

**Pré-condições:**
- Usuário autenticado com `ROLE_ADMIN`.

**Passos:**
1. Tentar chamar diretamente `PATCH /api/prestadores/{id}/badge` com `{"badgeVerificado": true}`.

**Resultado esperado:**
- Backend retorna HTTP 404 ou 403 (endpoint não existe ou não autorizado).
- `badgeVerificado` só pode ser alterado pelo processo automático interno.

---

## CT-07 — Avaliação Multidimensional

**UC de referência:** UC-07  
**Total de casos:** 13

---

### CT-07-01 — Avaliação com nota geral e 3 dimensões preenchidas é persistida corretamente

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-01 |

**Pré-condições:**
- Serviço com status `CONCLUIDA`.
- Cliente autenticado que ainda não avaliou esse contrato.

**Passos:**
1. Acessar painel → contrato concluído → "Avaliar".
2. Preencher: nota geral = 5, pontualidade = 4, qualidade = 5, comunicação = 4.
3. Digitar comentário com 100 caracteres.
4. Clicar em "Enviar avaliação".

**Resultado esperado:**
- Avaliação persistida com: `notaGeral = 5`, `notaPontualidade = 4`, `notaQualidade = 5`, `notaComunicacao = 4`, `comentario` preenchido, `dataCriacao` gerada.
- Médias do prestador atualizadas: `avaliacaoMedia`, `mediaPontualidade`, `mediaQualidade`, `mediaComunicacao`.
- Confirmação exibida; redirecionamento ao painel.

---

### CT-07-02 — Avaliação com somente nota geral persiste dimensões como null (retrocompatível)

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-01, RN-04, FA — Avaliação sem dimensões |

**Pré-condições:**
- Serviço concluído.
- Cliente autenticado.

**Passos:**
1. Acessar formulário de avaliação.
2. Preencher apenas a nota geral = 4 (deixar as 3 dimensões em branco).
3. Enviar.

**Resultado esperado:**
- Avaliação persistida com `notaGeral = 4`, `notaPontualidade = null`, `notaQualidade = null`, `notaComunicacao = null`.
- `avaliacaoMedia` do prestador atualizada com base na nota geral.
- Médias dimensionais **não** são afetadas por esta avaliação.

---

### CT-07-03 — Avaliação sem nota geral é rejeitada com erro de validação

| Campo | Valor |
|---|---|
| Tipo | Negativo |
| Prioridade | Alta |
| Regra relacionada | RN-01 |

**Pré-condições:**
- Serviço concluído.

**Passos:**
1. Acessar formulário de avaliação.
2. Deixar a nota geral sem seleção.
3. Preencher uma das dimensões opcionais.
4. Clicar em "Enviar avaliação".

**Resultado esperado:**
- Envio bloqueado com mensagem: "A nota geral é obrigatória."
- Nenhuma avaliação persistida.

---

### CT-07-04 — Nota geral fora do intervalo 1–5 é rejeitada

| Campo | Valor |
|---|---|
| Tipo | Negativo |
| Prioridade | Alta |
| Regra relacionada | RN-01 |

**Passos:**
1. Tentar enviar avaliação com `notaGeral = 0` via API diretamente.
2. Tentar enviar avaliação com `notaGeral = 6` via API diretamente.

**Resultado esperado:**
- Backend retorna HTTP 400 para ambos os casos.
- Mensagem de validação: "Nota deve estar entre 1 e 5."

---

### CT-07-05 — Comentário com 500 caracteres é aceito

| Campo | Valor |
|---|---|
| Tipo | Borda |
| Prioridade | Média |
| Regra relacionada | FP — passo 2 |

**Passos:**
1. Preencher o campo comentário com exatamente 500 caracteres.
2. Enviar avaliação com nota geral preenchida.

**Resultado esperado:**
- Avaliação persistida com o comentário completo de 500 caracteres.

---

### CT-07-06 — Comentário com 501 caracteres é rejeitado

| Campo | Valor |
|---|---|
| Tipo | Borda |
| Prioridade | Média |
| Regra relacionada | FP — passo 2 |

**Passos:**
1. Tentar preencher o campo comentário com 501 caracteres.

**Resultado esperado:**
- Frontend limita a entrada a 500 caracteres (contador visível).
- Se enviado via API com 501 chars, backend retorna HTTP 400.

---

### CT-07-07 — Média dimensional não exibida no perfil com menos de 3 avaliações naquela dimensão

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-03, FA — Visualização das médias |

**Pré-condições:**
- Prestador com 2 avaliações preenchendo "pontualidade" e 0 para "qualidade" e "comunicação".

**Passos:**
1. Acessar o perfil público do prestador.

**Resultado esperado:**
- Dimensão "Pontualidade" **não** exibida (menos de 3 avaliações).
- Dimensões "Qualidade" e "Comunicação" também **não** exibidas.
- Apenas a nota geral (com todas as avaliações) é exibida.

---

### CT-07-08 — Média dimensional exibida no perfil com 3+ avaliações naquela dimensão

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-03, FA — Visualização das médias |

**Pré-condições:**
- Prestador com 3 avaliações preenchendo "pontualidade".

**Passos:**
1. Acessar o perfil público.

**Resultado esperado:**
- "Pontualidade: ⭐ X,X (3 avaliações)" exibida no perfil.
- Média calculada corretamente sobre as 3 avaliações com esta dimensão.

---

### CT-07-09 — avaliacaoMedia geral não é alterada por avaliações sem dimensões

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-04 |

**Pré-condições:**
- Prestador com `avaliacaoMedia = 4,5` baseada em 10 avaliações anteriores (com ou sem dimensões).

**Passos:**
1. Cliente envia avaliação apenas com `notaGeral = 4` (sem dimensões).

**Resultado esperado:**
- `avaliacaoMedia` recalculada considerando a nova nota geral: `(4,5 × 10 + 4) / 11 ≈ 4,45`.
- Médias dimensionais permanecem inalteradas.

---

### CT-07-10 — Prestador avaliando cliente não vê as dimensões no formulário

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Regra relacionada | RN-02 |

**Pré-condições:**
- Serviço concluído.
- Prestador autenticado que ainda não avaliou o cliente.

**Passos:**
1. Acessar formulário de avaliação reversa (prestador avalia cliente).

**Resultado esperado:**
- Formulário exibe apenas: nota geral (obrigatória) + campo de comentário (opcional).
- **Nenhuma** das 3 dimensões (pontualidade, qualidade, comunicação) é exibida.

---

### CT-07-11 — Segunda avaliação para o mesmo contrato pelo mesmo usuário é rejeitada

| Campo | Valor |
|---|---|
| Tipo | Negativo |
| Prioridade | Alta |
| Regra relacionada | UC-07 Pré-condições — unicidade |

**Pré-condições:**
- Cliente já avaliou o prestador neste contrato.

**Passos:**
1. Tentar enviar segunda avaliação via API `POST /api/avaliacoes` para o mesmo `contratoId`.

**Resultado esperado:**
- Backend retorna HTTP 409 Conflict ou 422.
- Mensagem: "Você já avaliou este serviço."
- Nenhuma avaliação duplicada persistida.

---

### CT-07-12 — Badge UC-06 é recalculado após nova avaliação

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Alta |
| Pós-condição relacionada | UC-07 — "Critérios do badge recalculados" |

**Pré-condições:**
- Prestador com `badgeVerificado = false`, 5 contratos concluídos, completude 85%, média atual 3,9 (faltava 0,1 para o badge).

**Passos:**
1. Cliente envia avaliação com `notaGeral = 5`.
2. Nova média calculada: sobe acima de 4,0.

**Resultado esperado:**
- Sistema recalcula critérios do UC-06 imediatamente após a avaliação.
- Badge concedido (`badgeVerificado = true`).
- Notificação de concessão gerada.

---

### CT-07-13 — Formulário distingue visualmente a nota obrigatória das opcionais

| Campo | Valor |
|---|---|
| Tipo | Positivo |
| Prioridade | Média |
| Critério relacionado | CA — "Formulário exibe nota geral obrigatória + 3 dimensões opcionais" |

**Passos:**
1. Acessar o formulário de avaliação como cliente.

**Resultado esperado:**
- Nota geral marcada com indicador de obrigatoriedade (ex: asterisco *, label "Obrigatório").
- As 3 dimensões exibidas com indicador "Opcional".
- Diferença visual clara entre obrigatório e opcional.

---

## Resumo por UC

| UC | Título | Total de CTs | Positivos | Negativos | Borda | Segurança | Auditoria |
|---|---|---|---|---|---|---|---|
| UC-01 | Chat Interno | 11 | 4 | 5 | 2 | 1 | 0 |
| UC-02 | Portfólio de Fotos | 13 | 6 | 4 | 3 | 0 | 0 |
| UC-03 | Onboarding Checklist | 10 | 7 | 1 | 2 | 1 | 0 |
| UC-04 | Perfil Público | 10 | 5 | 2 | 0 | 2 | 0 |
| UC-05 | Notificações por E-mail | 14 | 7 | 4 | 1 | 0 | 2 |
| UC-06 | Badge Verificado | 11 | 5 | 3 | 2 | 1 | 1 |
| UC-07 | Avaliação Multidimensional | 13 | 7 | 4 | 3 | 0 | 0 |
| **Total** | | **82** | **41** | **23** | **13** | **5** | **3** |

---

*Documento criado em 2026-05-15. Atualizar conforme refinamentos durante o sprint de implementação da P4.*

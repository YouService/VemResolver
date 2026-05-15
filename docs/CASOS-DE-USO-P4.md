# Casos de Uso — Prioridade 4 (VemResolver)

> Documento gerado em 2026-05-15 com base na análise competitiva GetNinjas × Cronoshare.  
> Cada caso de uso cobre o fluxo principal, fluxos alternativos, regras de negócio e critérios de aceite.

---

## Índice

- [UC-01 — Chat Interno entre Cliente e Prestador](#uc-01--chat-interno-entre-cliente-e-prestador)
- [UC-02 — Portfólio de Fotos no Perfil do Prestador](#uc-02--portfólio-de-fotos-no-perfil-do-prestador)
- [UC-03 — Onboarding Guiado com Checklist de Perfil](#uc-03--onboarding-guiado-com-checklist-de-perfil)
- [UC-04 — Perfil Público e Compartilhável do Prestador](#uc-04--perfil-público-e-compartilhável-do-prestador)
- [UC-05 — Notificações por E-mail Transacionais](#uc-05--notificações-por-e-mail-transacionais)
- [UC-06 — Badge "Verificado" para Prestadores](#uc-06--badge-verificado-para-prestadores)
- [UC-07 — Avaliação Multidimensional](#uc-07--avaliação-multidimensional)

---

## UC-01 — Chat Interno entre Cliente e Prestador

### Descrição
Permite que cliente e prestador se comuniquem por mensagens de texto dentro da plataforma após a aceitação de uma proposta, sem precisar trocar contato por fora.

### Atores
- **Primário:** Cliente autenticado
- **Primário:** Prestador autenticado
- **Sistema:** VemResolver (entrega mensagens, notifica)

### Pré-condições
- Existe um contrato ativo (solicitação com status `EM_ANDAMENTO`) vinculando o cliente e o prestador.
- Ambos os usuários estão autenticados.

### Fluxo Principal — Cliente envia mensagem

1. Cliente acessa o painel e clica em "Chat" no contrato em andamento.
2. Sistema exibe o histórico de mensagens do contrato em ordem cronológica crescente.
3. Cliente digita a mensagem no campo de texto e clica em "Enviar".
4. Sistema valida que a mensagem não está vazia e não ultrapassa 2.000 caracteres.
5. Sistema persiste a mensagem com: `remetenteId`, `contratoId`, `texto`, `dataEnvio`, `lida = false`.
6. Sistema exibe a mensagem na interface do cliente imediatamente (otimista).
7. Sistema cria uma notificação in-app para o prestador ("Nova mensagem de João Silva").
8. Sistema envia e-mail transacional ao prestador com preview da mensagem e link para o chat.

### Fluxo Alternativo — Prestador responde

1. Prestador recebe notificação no sino ou por e-mail.
2. Prestador acessa o chat pelo painel do prestador → contrato em andamento.
3. Sistema exibe as mensagens não lidas com marcação visual (fundo diferente).
4. Prestador digita resposta e envia.
5. Sistema marca as mensagens anteriores do prestador como `lida = true`.
6. Sistema notifica o cliente da resposta.

### Fluxo Alternativo — Contrato já concluído

1. Cliente ou prestador tenta enviar mensagem em contrato com status `CONCLUIDA` ou `CANCELADA`.
2. Sistema exibe campo de texto desabilitado com aviso: "Este contrato foi encerrado. O chat está disponível apenas para contratos em andamento."
3. Histórico de mensagens permanece visível (somente leitura).

### Fluxo de Exceção — Falha de rede

1. Sistema não consegue persistir a mensagem.
2. Sistema exibe: "Não foi possível enviar a mensagem. Tente novamente."
3. Mensagem volta ao campo de texto para reenvio.

### Pós-condições
- Mensagem persistida e visível para ambas as partes.
- Notificação gerada para o destinatário.

### Regras de Negócio
- **RN-01:** O chat só está disponível em contratos com status `EM_ANDAMENTO`.
- **RN-02:** Mensagem máxima de 2.000 caracteres.
- **RN-03:** Apenas o cliente da solicitação e o prestador da proposta aceita podem trocar mensagens naquele chat.
- **RN-04:** O histórico de mensagens é preservado após a conclusão ou cancelamento do contrato.
- **RN-05:** Nenhum dado de contato (telefone, e-mail, endereço) deve ser indexado ou bloqueado — a plataforma não impede, mas não exige intermediação financeira.

### Critérios de Aceite
- [ ] Mensagem enviada aparece na interface sem recarregar a página.
- [ ] Mensagem recebida aparece para o destinatário em até 30 segundos (polling) ou em tempo real (WebSocket).
- [ ] Notificação in-app é criada para o destinatário.
- [ ] E-mail transacional é disparado ao destinatário.
- [ ] Chat de contrato encerrado exibe histórico em somente leitura.
- [ ] Usuário não participante do contrato não consegue acessar o chat (403).

---

## UC-02 — Portfólio de Fotos no Perfil do Prestador

### Descrição
Permite que o prestador faça upload de fotos de trabalhos realizados, exibidas em galeria no seu perfil público, aumentando a credibilidade e a taxa de conversão de propostas.

### Atores
- **Primário:** Prestador autenticado
- **Secundário:** Cliente (visualiza)
- **Sistema:** VemResolver (armazena, serve imagens)

### Pré-condições
- Prestador autenticado possui perfil de prestador criado (`Prestador` entity existente).

### Fluxo Principal — Upload de foto

1. Prestador acessa "Meu Perfil" → aba "Portfólio".
2. Sistema exibe galeria atual (vazia ou com fotos existentes) e botão "Adicionar foto".
3. Prestador clica em "Adicionar foto" e seleciona um arquivo de imagem.
4. Sistema valida: formato (JPG, PNG, WEBP), tamanho máximo (5 MB), limite de 10 fotos por perfil.
5. Sistema faz upload, gera thumbnail e persiste a URL associada ao prestador.
6. Sistema exibe a nova foto na galeria imediatamente com opção de adicionar legenda.
7. Prestador pode adicionar legenda opcional (máx. 150 caracteres) e salvar.

### Fluxo Alternativo — Reordenar fotos

1. Prestador arrasta uma foto para nova posição na galeria.
2. Sistema persiste a nova ordem (`ordemExibicao`).
3. Perfil público reflete a nova ordem.

### Fluxo Alternativo — Remover foto

1. Prestador clica no ícone de lixeira sobre uma foto.
2. Sistema exibe confirmação: "Remover esta foto do portfólio?"
3. Prestador confirma.
4. Sistema remove a foto da galeria e do armazenamento.

### Fluxo de Exceção — Arquivo inválido

1. Prestador tenta enviar arquivo com formato ou tamanho fora do permitido.
2. Sistema exibe: "Formato não suportado. Use JPG, PNG ou WEBP até 5 MB."
3. Upload não é realizado.

### Fluxo de Exceção — Limite atingido

1. Prestador tenta adicionar foto quando já possui 10 no portfólio.
2. Sistema exibe: "Limite de 10 fotos atingido. Remova uma foto para adicionar outra."
3. Botão "Adicionar foto" fica desabilitado.

### Pós-condições
- Fotos visíveis no perfil público do prestador.
- Checklist de onboarding atualizado (item "Portfólio" marcado como concluído).

### Regras de Negócio
- **RN-01:** Máximo de 10 fotos por perfil de prestador.
- **RN-02:** Formatos aceitos: JPG, PNG, WEBP. Tamanho máximo: 5 MB por arquivo.
- **RN-03:** A foto de índice 0 (primeira da ordem) é usada como destaque no card de busca.
- **RN-04:** Legendas são opcionais e limitadas a 150 caracteres.
- **RN-05:** Imagens são servidas via CDN ou storage (ex: S3/MinIO) — não armazenadas como BLOB no banco.

### Critérios de Aceite
- [ ] Upload de imagem válida aparece na galeria do perfil em menos de 3 segundos.
- [ ] Arquivo acima de 5 MB ou formato inválido exibe erro sem travar a tela.
- [ ] Após 10 fotos, botão de adicionar é desabilitado com mensagem explicativa.
- [ ] Foto removida desaparece da galeria sem recarregar a página.
- [ ] Galeria é visível para qualquer visitante do perfil público (sem autenticação).

---

## UC-03 — Onboarding Guiado com Checklist de Perfil

### Descrição
Exibe para o prestador uma barra de progresso e um checklist com os passos necessários para completar o perfil, incentivando o preenchimento de informações que aumentam a taxa de conversão.

### Atores
- **Primário:** Prestador autenticado
- **Sistema:** VemResolver (calcula progresso, persiste estado)

### Pré-condições
- Prestador autenticado com perfil criado (mesmo que incompleto).

### Fluxo Principal — Visualização do progresso

1. Prestador acessa o painel ou o perfil.
2. Sistema calcula o percentual de completude com base nos itens do checklist.
3. Sistema exibe barra de progresso (ex: "Seu perfil está 40% completo") com cor proporcional ao percentual (vermelho < 50%, amarelo 50–79%, verde ≥ 80%).
4. Sistema exibe lista de itens com ícone ✅ (concluído) ou ⬜ (pendente), cada um com link direto para a seção correspondente.

### Fluxo Alternativo — Clique em item pendente

1. Prestador clica em um item pendente do checklist (ex: "Adicionar foto de perfil").
2. Sistema navega diretamente para a seção ou modal correspondente.
3. Após o prestador preencher e salvar, sistema recalcula o progresso e atualiza a barra em tempo real.

### Fluxo Alternativo — Perfil 100% completo

1. Prestador completa o último item.
2. Sistema exibe barra verde com mensagem: "Perfil completo! Você tem mais chances de ser contratado."
3. Checklist é ocultado ou recolhido automaticamente para não poluir a interface.

### Pós-condições
- Progresso do prestador persistido e atualizado a cada mudança de perfil.

### Regras de Negócio
- **RN-01:** Os 7 itens do checklist e seus pesos:

| Item | Peso |
|------|------|
| Foto de perfil (avatar) | 15% |
| Descrição do perfil (mín. 50 caracteres) | 20% |
| Pelo menos 1 categoria selecionada | 15% |
| Telefone preenchido | 10% |
| Cidade e estado preenchidos | 10% |
| Pelo menos 1 foto no portfólio | 20% |
| Raio de atendimento configurado (lat/lng) | 10% |

- **RN-02:** O percentual é calculado somando os pesos dos itens concluídos.
- **RN-03:** Prestadores com perfil < 50% não aparecem nos primeiros resultados de busca (ordenação por completude).
- **RN-04:** O checklist é exibido apenas para prestadores — clientes não têm este componente.

### Critérios de Aceite
- [ ] Barra de progresso exibe percentual correto ao acessar o painel.
- [ ] Completar um item atualiza a barra sem recarregar a página.
- [ ] Clicar em item pendente navega para a seção correta.
- [ ] Perfil 100% completo exibe mensagem de parabéns e recolhe o checklist.
- [ ] Cliente logado não visualiza o checklist.

---

## UC-04 — Perfil Público e Compartilhável do Prestador

### Descrição
Disponibiliza uma página pública acessível sem autenticação em `/prestador/{id}`, que o prestador pode compartilhar como cartão de visita digital, exibindo seu portfólio, avaliações e um botão para solicitar serviço.

### Atores
- **Primário:** Visitante (não autenticado) ou cliente autenticado
- **Secundário:** Prestador (beneficiário indireto)
- **Sistema:** VemResolver

### Pré-condições
- Existe um perfil de prestador com o `id` informado e `ativo = true`.

### Fluxo Principal — Visitante acessa o perfil

1. Visitante acessa `https://vemresolver.com/prestador/42`.
2. Sistema busca o perfil do prestador pelo ID.
3. Sistema renderiza a página pública com:
   - Foto e nome do prestador.
   - Categorias de serviço atendidas.
   - Cidade e estado.
   - Nota média geral e total de avaliações.
   - Notas médias por dimensão (pontualidade, qualidade, comunicação) se disponíveis.
   - Portfólio de fotos em galeria.
   - Lista das últimas 5 avaliações com nome do cliente, nota e comentário.
   - Badge "Verificado" se aplicável.
   - Botão "Solicitar serviço".
4. Visitante clica em "Solicitar serviço".
5. Sistema redireciona para `/register?tipo=CLIENTE` (se não autenticado) ou para `/cliente/nova-solicitacao` (se cliente logado).

### Fluxo Alternativo — Prestador compartilha o link

1. Prestador acessa seu próprio perfil público ou o painel.
2. Sistema exibe botão "Copiar link do perfil".
3. Prestador clica e o sistema copia a URL para a área de transferência.
4. Sistema exibe confirmação: "Link copiado! Compartilhe com seus clientes."

### Fluxo de Exceção — Perfil não encontrado ou inativo

1. Visitante acessa ID inexistente ou de prestador com `ativo = false`.
2. Sistema retorna 404 com página amigável: "Perfil não encontrado."

### Pós-condições
- Nenhuma alteração de estado — página é somente leitura.

### Regras de Negócio
- **RN-01:** A rota `/prestador/{id}` é pública — não requer autenticação.
- **RN-02:** Dados exibidos: foto, nome, categorias, cidade/estado, nota geral, notas por dimensão, portfólio, últimas 5 avaliações, badge "Verificado".
- **RN-03:** Dados **não** exibidos: e-mail, telefone, CPF, endereço completo.
- **RN-04:** Prestadores com `ativo = false` retornam 404 (soft delete preservado).
- **RN-05:** A página deve ser indexável por motores de busca (sem `noindex`) para gerar tráfego orgânico por nome + categoria + cidade.

### Critérios de Aceite
- [ ] Página carrega sem autenticação em `/prestador/{id}`.
- [ ] Botão "Solicitar serviço" redireciona para `/register` se não logado, ou para `/cliente/nova-solicitacao` se cliente logado.
- [ ] ID inválido ou inativo retorna página 404 amigável.
- [ ] Dados sensíveis (e-mail, telefone, CPF) não aparecem na página.
- [ ] Botão "Copiar link" copia a URL correta para a área de transferência.

---

## UC-05 — Notificações por E-mail Transacionais

### Descrição
Envia e-mails automáticos em eventos críticos do fluxo do marketplace, garantindo que clientes e prestadores sejam alertados mesmo quando não estão com a plataforma aberta.

### Atores
- **Primário:** Sistema VemResolver (disparador)
- **Beneficiário:** Cliente autenticado / Prestador autenticado
- **Serviço externo:** Provedor de e-mail (SMTP / SendGrid / SES)

### Pré-condições
- Usuário destinatário possui e-mail válido e verificado (`ativo = true`).
- Evento de negócio mapeado ocorre no sistema.

### Fluxo Principal — Gatilhos e destinatários

| Evento | Destinatário | Assunto sugerido |
|--------|-------------|-----------------|
| Prestador envia proposta | Cliente | "Você recebeu uma nova proposta para [título da solicitação]" |
| Cliente aceita proposta | Prestador | "Sua proposta foi aceita! Entre em contato com [nome do cliente]" |
| Cliente recusa proposta | Prestador | "Sua proposta para [título] não foi selecionada desta vez" |
| Nova solicitação aberta compatível com categoria do prestador | Prestador | "Nova oportunidade: [título da solicitação] em [cidade]" |
| Serviço marcado como concluído | Prestador | "Serviço concluído — [nome do cliente] marcou o serviço como finalizado" |
| Avaliação recebida | Prestador ou Cliente | "Você recebeu uma nova avaliação — nota [X]/5" |
| Nova mensagem no chat | Destinatário da mensagem | "Nova mensagem de [nome]: [preview de 80 chars...]" |

### Fluxo Principal — Envio do e-mail

1. Evento de negócio ocorre (ex: proposta criada).
2. Sistema identifica o destinatário e o template do e-mail correspondente.
3. Sistema monta o payload com variáveis do template (nome, título, valor, link direto).
4. Sistema envia para o provedor de e-mail de forma assíncrona (fila ou `@Async`).
5. Sistema registra o envio (timestamp, tipo de evento, destinatário) para auditoria.
6. Provedor entrega o e-mail ao destinatário.

### Fluxo Alternativo — Usuário desativa e-mails

1. Usuário acessa "Configurações" → "Notificações por e-mail".
2. Usuário desativa categorias específicas (ex: "Nova oportunidade") ou todos os e-mails.
3. Sistema persiste a preferência no campo `preferenciasEmail` do usuário.
4. E-mails da categoria desativada não são mais disparados para aquele usuário.

### Fluxo de Exceção — Falha no envio

1. Provedor de e-mail retorna erro (timeout, quota, endereço inválido).
2. Sistema registra a falha com o tipo de erro.
3. Sistema reenvia até 3 vezes com backoff exponencial (1 min, 5 min, 30 min).
4. Após 3 falhas, marca o evento como `FALHOU` no log de auditoria.
5. A operação de negócio original (ex: aceitar proposta) **não é afetada** pela falha de e-mail.

### Pós-condições
- E-mail entregue ao destinatário ou falha registrada no log.
- Operação de negócio não é bloqueada por falha de e-mail.

### Regras de Negócio
- **RN-01:** O envio de e-mail é **sempre assíncrono** — nunca bloqueia a resposta HTTP ao usuário.
- **RN-02:** Cada e-mail contém um link direto para a ação correspondente dentro da plataforma.
- **RN-03:** E-mails de nova oportunidade para prestadores só são enviados se a solicitação for da categoria que o prestador atende E na cidade/estado compatível.
- **RN-04:** Máximo de 1 e-mail de "nova oportunidade" por hora por prestador (debounce) para evitar spam.
- **RN-05:** Preferências de e-mail por categoria são opcionais — padrão é receber todos.
- **RN-06:** E-mails transacionais críticos (proposta aceita, recusada) não podem ser desativados pelo usuário.

### Critérios de Aceite
- [ ] E-mail disparado em até 30 segundos após o evento de negócio.
- [ ] Falha no envio não gera erro 500 para o usuário que realizou a ação.
- [ ] Link no e-mail leva diretamente à tela correspondente (deep link).
- [ ] Preferência de desativação é respeitada no próximo disparo.
- [ ] Log de auditoria registra todos os disparos com status (enviado / falhou).

---

## UC-06 — Badge "Verificado" para Prestadores

### Descrição
Atribui automaticamente um badge visual "Verificado" ao prestador que atingir critérios de qualidade definidos, diferenciando profissionais de alta reputação no marketplace.

### Atores
- **Primário:** Sistema VemResolver (atribui/remove automaticamente)
- **Beneficiário:** Prestador autenticado
- **Secundário:** Cliente (visualiza na busca e no perfil)

### Pré-condições
- Prestador possui perfil ativo com pelo menos 1 contrato concluído.

### Fluxo Principal — Concessão automática do badge

1. Evento de conclusão de contrato ou nova avaliação ocorre.
2. Sistema recalcula os critérios de elegibilidade para o prestador envolvido:
   - Total de contratos concluídos ≥ 5.
   - Média de avaliações ≥ 4,0 (calculada sobre todas as avaliações recebidas).
   - Perfil com completude ≥ 80% (conforme UC-03).
3. Se todos os critérios forem atendidos e o badge não estiver ativo, sistema atribui `badgeVerificado = true` e registra `dataBadge`.
4. Sistema cria notificação in-app: "Parabéns! Você conquistou o badge Verificado."
5. Sistema envia e-mail de parabéns ao prestador.
6. Badge exibido no card de busca, no perfil público e no painel do prestador.

### Fluxo Alternativo — Remoção do badge

1. Prestador recebe avaliação negativa que derruba a média abaixo de 4,0.
2. Sistema recalcula os critérios.
3. Sistema remove o badge (`badgeVerificado = false`) e registra a remoção.
4. Sistema cria notificação: "Seu badge Verificado foi removido. Sua média atual é [X]. Mantenha a qualidade para reconquistá-lo."

### Fluxo Alternativo — Visualização pelo cliente

1. Cliente pesquisa prestadores.
2. Prestadores com badge exibem ícone "✓ Verificado" no card de resultado.
3. Cliente pode filtrar resultados por "Apenas verificados".

### Pós-condições
- Campo `badgeVerificado` e `dataBadge` atualizados no perfil do prestador.
- Badge visível no perfil público e nos cards de busca.

### Regras de Negócio
- **RN-01:** Critérios de concessão: contratos concluídos ≥ 5 **E** média de avaliações ≥ 4,0 **E** completude de perfil ≥ 80%.
- **RN-02:** Recálculo ocorre após cada contrato concluído e após cada avaliação recebida.
- **RN-03:** Badge removido automaticamente se qualquer critério deixar de ser atendido.
- **RN-04:** Não há processo manual de concessão — é 100% automático.
- **RN-05:** Histórico de concessão e remoção é preservado para auditoria.
- **RN-06:** Futura evolução: badge "Top 10%" por categoria (baseado em ranking relativo por categoria).

### Critérios de Aceite
- [ ] Badge concedido automaticamente ao atingir os 3 critérios simultaneamente.
- [ ] Badge removido automaticamente quando qualquer critério cai abaixo do mínimo.
- [ ] Badge visível no card de busca, perfil público e painel do prestador.
- [ ] Filtro "Apenas verificados" funciona na busca de prestadores.
- [ ] Notificação gerada tanto na concessão quanto na remoção.

---

## UC-07 — Avaliação Multidimensional

### Descrição
Expande o sistema de avaliação atual (nota única 1–5) com três dimensões opcionais — pontualidade, qualidade do serviço e comunicação — proporcionando informação mais granular para o cliente escolher o prestador e para o prestador identificar pontos de melhoria.

### Atores
- **Primário:** Cliente autenticado (avalia o prestador)
- **Primário:** Prestador autenticado (avalia o cliente — dimensões não se aplicam)
- **Secundário:** Visitante (visualiza médias no perfil)

### Pré-condições
- Serviço com status `CONCLUIDA`.
- O avaliador ainda não avaliou esse contrato (unicidade garantida).

### Fluxo Principal — Cliente avalia prestador com dimensões

1. Cliente acessa o painel → contrato concluído → botão "Avaliar".
2. Sistema exibe formulário de avaliação:
   - **Nota geral** (obrigatória): estrelas 1–5.
   - **Pontualidade** (opcional): estrelas 1–5 com rótulo "O profissional foi pontual?".
   - **Qualidade do serviço** (opcional): estrelas 1–5 com rótulo "Como foi a qualidade do trabalho?".
   - **Comunicação** (opcional): estrelas 1–5 com rótulo "O profissional se comunicou bem?".
   - **Comentário** (opcional): texto livre até 500 caracteres.
3. Cliente preenche a nota geral e, opcionalmente, as dimensões.
4. Cliente clica em "Enviar avaliação".
5. Sistema persiste: `notaGeral`, `notaPontualidade` (nullable), `notaQualidade` (nullable), `notaComunicacao` (nullable), `comentario`, `dataCriacao`.
6. Sistema recalcula e atualiza as médias do prestador:
   - `avaliacaoMedia` (geral) — baseada em todas as notas gerais.
   - `mediaPontualidade`, `mediaQualidade`, `mediaComunicacao` — calculadas apenas sobre avaliações que preencheram cada dimensão.
7. Sistema exibe confirmação e redireciona ao painel.

### Fluxo Alternativo — Avaliação sem dimensões (retrocompatível)

1. Cliente preenche apenas a nota geral e o comentário.
2. Sistema persiste com dimensões como `null`.
3. Médias dimensionais do prestador não são afetadas.
4. Perfil exibe médias dimensionais somente se houver avaliações suficientes (≥ 3) para cada dimensão.

### Fluxo Alternativo — Visualização das médias no perfil

1. Visitante acessa o perfil público do prestador.
2. Se o prestador tiver ≥ 3 avaliações com dimensão preenchida, sistema exibe:
   ```
   Nota geral:       ⭐ 4,7 (23 avaliações)
   Pontualidade:     ⭐ 4,9 (18 avaliações)
   Qualidade:        ⭐ 4,6 (20 avaliações)
   Comunicação:      ⭐ 4,8 (15 avaliações)
   ```
3. Se houver menos de 3 avaliações para uma dimensão, aquela dimensão não é exibida.

### Pós-condições
- Avaliação persistida com dimensões opcionais.
- Médias do prestador atualizadas em `Prestador` entity.
- Critérios do badge (UC-06) recalculados com base na nova `avaliacaoMedia`.

### Regras de Negócio
- **RN-01:** Nota geral (1–5) é obrigatória; as três dimensões são opcionais.
- **RN-02:** Prestador avalia cliente (avaliação reversa) — somente nota geral + comentário, sem dimensões.
- **RN-03:** Média dimensional só é exibida no perfil quando há ≥ 3 avaliações com aquela dimensão preenchida.
- **RN-04:** `avaliacaoMedia` do prestador continua baseada na nota geral (retrocompatível com avaliações antigas).
- **RN-05:** Cada dimensão é calculada independentemente — apenas sobre avaliações que a preencheram.
- **RN-06:** Migration Flyway necessária para adicionar colunas `nota_pontualidade`, `nota_qualidade`, `nota_comunicacao` (nullable) na tabela `avaliacoes` e campos de média em `prestadores`.

### Critérios de Aceite
- [ ] Formulário de avaliação exibe nota geral obrigatória + 3 dimensões opcionais.
- [ ] Avaliação sem dimensões é aceita normalmente (retrocompatibilidade).
- [ ] Médias dimensionais exibidas no perfil somente quando ≥ 3 avaliações preencheram a dimensão.
- [ ] `avaliacaoMedia` geral não é afetada por avaliações sem dimensões.
- [ ] Badge UC-06 recalculado após nova avaliação.
- [ ] Prestador avaliando cliente não vê as dimensões no formulário.

---

*Documento gerado em 2026-05-15. Atualizar conforme refinamentos durante o sprint de implementação da P4.*

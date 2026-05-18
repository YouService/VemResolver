#!/usr/bin/env python3
"""
Seed de dados para o VemResolver.
Cria 10 clientes, 10 prestadores, fluxo completo de solicitacoes,
propostas, chat, conclusao e avaliacoes variadas.
"""

import base64
import requests
import json
import time
import random
import sys


def b64(text: str) -> str:
    return base64.b64encode(text.encode()).decode()


BASE = "http://localhost:8080"
H_JSON = {"Content-Type": "application/json"}


def post(path, body, token=None):
    headers = {**H_JSON}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    r = requests.post(f"{BASE}{path}", json=body, headers=headers, timeout=10)
    if r.status_code not in (200, 201):
        print(f"  [ERRO {r.status_code}] POST {path}: {r.text[:200]}")
        return None
    return r.json()


def patch(path, body=None, token=None, params=None):
    headers = {**H_JSON}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    r = requests.patch(f"{BASE}{path}", json=body, headers=headers, params=params, timeout=10)
    if r.status_code not in (200, 204):
        print(f"  [ERRO {r.status_code}] PATCH {path}: {r.text[:200]}")
        return None
    return r.json() if r.content else {}


def login(email, senha):
    """Retorna (token, userId) ou (None, None) em caso de erro."""
    r = requests.post(f"{BASE}/api/auth/login",
                      json={"email": email, "senha": senha},
                      headers=H_JSON, timeout=10)
    if r.status_code == 200:
        data = r.json()
        return data["token"], data.get("userId")
    print(f"  [ERRO {r.status_code}] login {email}: {r.text[:200]}")
    return None, None


def register_user(body):
    """Registra o usuario. Nao faz fallback de login (evita consumir rate limit)."""
    r = requests.post(f"{BASE}/api/auth/register", json=body, headers=H_JSON, timeout=10)
    if r.status_code in (200, 201):
        return True
    texto = r.text.lower()
    if r.status_code in (400, 409, 500) and any(k in texto for k in ("duplicate", "unique", "ja existe", "already")):
        return True  # ja existe, ok
    print(f"  [ERRO {r.status_code}] register {body['email']}: {r.text[:200]}")
    return False


def activate_seed_users(admin_tok):
    """Ativa todos os usuarios @seed.com que estiverem inativos."""
    r = requests.get(f"{BASE}/api/admin/usuarios?size=200",
                     headers={"Authorization": f"Bearer {admin_tok}"}, timeout=10)
    if r.status_code != 200:
        print(f"  [AVISO] Nao foi possivel listar usuarios: {r.status_code}")
        return
    users = r.json().get("content", [])
    ativados = 0
    for u in users:
        if "@seed.com" in u.get("email", "") and not u.get("ativo", True):
            patch(f"/api/admin/usuarios/{u['id']}/ativar",
                  params={"ativo": "true"}, token=admin_tok)
            ativados += 1
    print(f"  + {ativados} usuarios ativados (outros ja estavam ativos)")


def decode_user_id(tok: str):
    """Extrai o 'sub' (email/username) do JWT. Nao usado para IDs numericos."""
    payload = tok.split('.')[1]
    payload += '=' * (4 - len(payload) % 4)
    data = json.loads(base64.b64decode(payload))
    return data.get("sub")


# ─── Dados de clientes ────────────────────────────────────────────────────────

CLIENTES = [
    {"nome": "Ana Beatriz Silva",    "email": "ana.beatriz@seed.com",    "cidade": "Sao Paulo",     "estado": "SP", "cpf": "111.222.333-01"},
    {"nome": "Bruno Mendes Costa",   "email": "bruno.mendes@seed.com",   "cidade": "Rio de Janeiro","estado": "RJ", "cpf": "111.222.333-02"},
    {"nome": "Camila Torres Lopes",  "email": "camila.torres@seed.com",  "cidade": "Belo Horizonte","estado": "MG", "cpf": "111.222.333-03"},
    {"nome": "Diego Ferreira Neto",  "email": "diego.ferreira@seed.com", "cidade": "Curitiba",      "estado": "PR", "cpf": "111.222.333-04"},
    {"nome": "Elisa Ramos Duarte",   "email": "elisa.ramos@seed.com",    "cidade": "Salvador",      "estado": "BA", "cpf": "111.222.333-05"},
    {"nome": "Fabio Carvalho Reis",  "email": "fabio.carvalho@seed.com", "cidade": "Fortaleza",     "estado": "CE", "cpf": "111.222.333-06"},
    {"nome": "Gabriela Melo Vieira", "email": "gabriela.melo@seed.com",  "cidade": "Recife",        "estado": "PE", "cpf": "111.222.333-07"},
    {"nome": "Henrique Alves Cruz",  "email": "henrique.alves@seed.com", "cidade": "Porto Alegre",  "estado": "RS", "cpf": "111.222.333-08"},
    {"nome": "Isabela Nunes Pinto",  "email": "isabela.nunes@seed.com",  "cidade": "Manaus",        "estado": "AM", "cpf": "111.222.333-09"},
    {"nome": "Jonas Oliveira Lima",  "email": "jonas.oliveira@seed.com", "cidade": "Brasilia",      "estado": "DF", "cpf": "111.222.333-10"},
]

# ─── Dados de prestadores ─────────────────────────────────────────────────────

PRESTADORES = [
    {"nome": "Roberto Eletrico",    "email": "roberto.eletrico@seed.com",  "cat": [1],  "desc": "Eletricista com 15 anos de experiencia em residencias e comercio.", "cidade": "Sao Paulo",     "estado": "SP", "cpf": "222.333.444-01"},
    {"nome": "Marcos Hidraulica",   "email": "marcos.hidraulica@seed.com", "cat": [2],  "desc": "Encanador especializado em reparos emergenciais e instalacoes.",    "cidade": "Rio de Janeiro","estado": "RJ", "cpf": "222.333.444-02"},
    {"nome": "Lucas Pintura Fina",  "email": "lucas.pintura@seed.com",     "cat": [3],  "desc": "Pintor residencial com acabamento impecavel e tintas premium.",      "cidade": "Sao Paulo",     "estado": "SP", "cpf": "222.333.444-03"},
    {"nome": "Paulo Construcoes",   "email": "paulo.construcoes@seed.com", "cat": [4],  "desc": "Pedreiro experiente em reformas e acabamentos.",                    "cidade": "Belo Horizonte","estado": "MG", "cpf": "222.333.444-04"},
    {"nome": "Jose Marcenaria",     "email": "jose.marcenaria@seed.com",   "cat": [5],  "desc": "Marceneiro artesanal, moveis sob medida e reparos.",                "cidade": "Curitiba",      "estado": "PR", "cpf": "222.333.444-05"},
    {"nome": "Antonio Jardins",     "email": "antonio.jardins@seed.com",   "cat": [6],  "desc": "Jardineiro paisagista, manutencao semanal e projetos verdes.",       "cidade": "Salvador",      "estado": "BA", "cpf": "222.333.444-06"},
    {"nome": "Fernanda Limpeza",    "email": "fernanda.limpeza@seed.com",  "cat": [7],  "desc": "Diarista profissional, limpeza completa e organizacao.",             "cidade": "Fortaleza",     "estado": "CE", "cpf": "222.333.444-07"},
    {"nome": "Prof Carlos Exatas",  "email": "carlos.professor@seed.com",  "cat": [8],  "desc": "Professor de Matematica e Fisica com aprovacao no vestibular.",     "cidade": "Recife",        "estado": "PE", "cpf": "222.333.444-08"},
    {"nome": "Ricardo Mecanica",    "email": "ricardo.mecanica@seed.com",  "cat": [9],  "desc": "Mecanico automotivo geral, preventiva e diagnostico eletronico.",   "cidade": "Porto Alegre",  "estado": "RS", "cpf": "222.333.444-09"},
    {"nome": "Thiago Dev",          "email": "thiago.dev@seed.com",        "cat": [10], "desc": "Desenvolvedor full-stack, sites, apps e automacoes.",               "cidade": "Brasilia",      "estado": "DF", "cpf": "222.333.444-10"},
]

# ─── Fluxos: (cliente_idx, prestador_idx, cat_id, descricao, cidade, estado, valor) ─

FLUXOS = [
    (0, 0,  1, "Troca de disjuntores e revisao do quadro eletrico completo.",         "Sao Paulo",     "SP", 350.00),
    (1, 1,  2, "Vazamento embaixo da pia da cozinha, precisa de reparo urgente.",      "Rio de Janeiro","RJ", 200.00),
    (2, 2,  3, "Pintura de sala e dois quartos, paredes e teto.",                      "Sao Paulo",     "SP", 800.00),
    (3, 3,  4, "Construcao de muro de divisa, 15 metros lineares.",                    "Belo Horizonte","MG", 1500.00),
    (4, 4,  5, "Confeccao de armario embutido para quarto de casal.",                  "Curitiba",      "PR", 2200.00),
    (5, 5,  6, "Poda e manutencao de jardim com 200m2, incluindo irrigacao.",          "Salvador",      "BA", 400.00),
    (6, 6,  7, "Faxina completa em apartamento de 80m2, limpeza pos-reforma.",         "Fortaleza",     "CE", 300.00),
    (7, 7,  8, "Aulas de Matematica para ENEM, 2x por semana, 2 meses.",              "Recife",        "PE", 1200.00),
    (8, 8,  9, "Revisao completa do carro + troca de oleo e filtros.",                 "Porto Alegre",  "RS", 450.00),
    (9, 9, 10, "Criacao de landing page para empresa de consultoria.",                 "Brasilia",      "DF", 3000.00),
    (0, 2,  3, "Pintura da fachada do apartamento.",                                   "Sao Paulo",     "SP", 1200.00),
    (1, 0,  1, "Instalacao de tomadas e pontos de luz em escritorio.",                 "Rio de Janeiro","RJ", 500.00),
    (2, 5,  6, "Manutencao quinzenal do jardim do condominio.",                        "Belo Horizonte","MG", 350.00),
    (3, 6,  7, "Limpeza semanal de casa duplex, 4 comodos.",                           "Curitiba",      "PR", 280.00),
    (4, 9, 10, "Desenvolvimento de sistema de agendamento para clinica.",              "Salvador",      "BA", 5000.00),
]

NOTAS = [
    (5, 5), (4, 5), (5, 4), (3, 4), (5, 5),
    (4, 3), (5, 5), (2, 4), (4, 4), (5, 5),
    (5, 4), (4, 5), (3, 3), (5, 5), (4, 4),
]

COMENTARIOS_CLIENTE = [
    "Excelente profissional! Trabalho impecavel e dentro do prazo.",
    "Muito bom, cumpriu o combinado. Recomendo.",
    "Trabalho de otima qualidade, ficou perfeito.",
    "Resultado ok, mas demorou um pouco mais do que o esperado.",
    "Superou minhas expectativas! Voltarei a contratar.",
    "Bom servico, preco justo. Atencioso e pontual.",
    "Otimo! Deixou tudo limpo depois do servico.",
    "Servico razoavel, algumas coisas poderiam ser melhores.",
    "Profissional competente, servico bem feito.",
    "Perfeito! Muito dedicado e detalhista.",
    "Adorei o resultado final, ficou lindo!",
    "Pontual, educado e trabalho de qualidade.",
    "Esperava mais, mas o resultado foi satisfatorio.",
    "Muito bom! Faria de novo sem hesitar.",
    "Bom profissional, cumpriu o que prometeu.",
]

COMENTARIOS_PRESTADOR = [
    "Otimo cliente! Ambiente organizado e pagamento pontual.",
    "Muito prestativo, facilitou bastante o trabalho.",
    "Cliente comunicativo e objetivo. Boa experiencia.",
    "Pagamento no prazo, espaco bem preparado.",
    "Excelente! Um dos melhores clientes. Muito respeitoso.",
    "Bom cliente, fez perguntas pertinentes sobre o servico.",
    "Cliente tranquilo, deixou trabalhar sem interrupcoes.",
    "Poderia ter informado melhor os detalhes antes, mas ok.",
    "Cliente parceiro, boa comunicacao durante o projeto.",
    "Pagamento imediato e feedback construtivo. Otimo!",
    "Muito grato pela confianca. Voltaremos a trabalhar.",
    "Ambiente bem preparado, facil de trabalhar.",
    "Cliente exigente mas justo. Aprendi bastante.",
    "Perfeito! Muito organizado e cordial.",
    "Bom relacionamento profissional.",
]

MSGS_PRESTADOR = [
    "Ola! Vi sua solicitacao e posso realizar o servico. Quando seria conveniente comecar?",
    "Bom dia! Tenho disponibilidade para esta semana. Podemos agendar uma visita?",
    "Ola! Tenho experiencia com esse tipo de servico. Quando fica melhor para voce?",
]

MSGS_CLIENTE = [
    "Otimo! Prefiro no final de semana, sabado de manha.",
    "Perfeito! Pode ser na quinta-feira a tarde?",
    "Que bom! Tenho disponibilidade qualquer dia util.",
]

MSGS_COMBO = [
    "Fechado! Vejo voce no dia combinado.",
    "Combinado! Estarei la com todos os materiais necessarios.",
    "Perfeito! Qualquer duvida, manda mensagem aqui.",
]


def aguardar_rate_limit(motivo=""):
    msg = f"  Aguardando 65s para reset do rate limit{' (' + motivo + ')' if motivo else ''}..."
    print(msg)
    for i in range(65, 0, -5):
        print(f"    {i}s...", end="\r", flush=True)
        time.sleep(5)
    print("    Pronto!                 ")


def main():
    print("=" * 60)
    print("  VemResolver - Seed de Dados")
    print("=" * 60)

    senha_padrao = "123456"
    tokens_clientes = []
    tokens_prestadores = []
    ids_prestadores_perfil = []

    # ── 0. Login admin ────────────────────────────────────────────────────────
    print("\n[0] Login admin...")
    admin_tok, _ = login("admin@vemresolver.com", "Admin@2025")
    if not admin_tok:
        print("  Falha no login admin!")
        sys.exit(1)
    print("  + Admin logado")

    # ── 1. Registrar todos os clientes (sem login) ────────────────────────────
    print("\n[1] Registrando 10 clientes (sem login)...")
    for c in CLIENTES:
        body = {
            "nome": c["nome"], "email": c["email"], "senha": b64(senha_padrao),
            "tipo": "CLIENTE", "cidade": c["cidade"], "estado": c["estado"],
            "cpf": c["cpf"],
            "telefone": f"(11) 9{random.randint(1000,9999)}-{random.randint(1000,9999)}"
        }
        ok = register_user(body)
        status = "+" if ok else "!"
        print(f"  {status} {c['nome']}")
        time.sleep(0.1)

    # ── 2. Registrar todos os prestadores (sem login) ─────────────────────────
    print("\n[2] Registrando 10 prestadores (sem login)...")
    for p in PRESTADORES:
        body = {
            "nome": p["nome"], "email": p["email"], "senha": b64(senha_padrao),
            "tipo": "PRESTADOR", "cidade": p["cidade"], "estado": p["estado"],
            "cpf": p["cpf"],
            "telefone": f"(11) 9{random.randint(1000,9999)}-{random.randint(1000,9999)}"
        }
        ok = register_user(body)
        status = "+" if ok else "!"
        print(f"  {status} {p['nome']}")
        time.sleep(0.1)

    # ── 3. Ativar todos os usuarios seed via admin ────────────────────────────
    print("\n[3] Ativando todos os usuarios @seed.com...")
    activate_seed_users(admin_tok)

    # ── Pausa para reset do rate limit ────────────────────────────────────────
    # Admin fez 1 login no inicio. Precisamos esperar a janela de 60s resetar
    # antes de fazer os 10 logins de clientes (para nao bater o limite de 10/min).
    aguardar_rate_limit("login admin na janela anterior")

    # ── 4. Login dos clientes (10 logins em janela limpa) ─────────────────────
    print("[4] Fazendo login dos 10 clientes...")
    for c in CLIENTES:
        tok, _ = login(c["email"], senha_padrao)
        if not tok:
            print(f"  Falha no login de {c['nome']}!")
            sys.exit(1)
        tokens_clientes.append(tok)
        print(f"  + {c['nome']}")
        time.sleep(0.1)
    print(f"  Total: {len(tokens_clientes)} clientes logados")

    # ── Pausa para reset do rate limit ────────────────────────────────────────
    aguardar_rate_limit("10 logins de clientes na janela anterior")

    # ── 5. Login e configuracao dos prestadores (10 logins em janela limpa) ───
    print("[5] Fazendo login e configurando perfis dos 10 prestadores...")
    for i, p in enumerate(PRESTADORES):
        tok, user_id = login(p["email"], senha_padrao)
        if not tok:
            print(f"  Falha no login de {p['nome']}!")
            sys.exit(1)
        tokens_prestadores.append(tok)

        perfil = post("/api/prestadores", {
            "usuarioId": user_id,
            "categoriasIds": p["cat"],
            "descricaoPerfil": p["desc"],
            "cidade": p["cidade"],
            "estado": p["estado"],
            "latitude": round(random.uniform(-23.6, -23.4), 6),
            "longitude": round(random.uniform(-46.8, -46.5), 6),
        }, token=tok)

        if not perfil:
            r2 = requests.get(f"{BASE}/api/prestadores/me",
                              headers={"Authorization": f"Bearer {tok}"}, timeout=10)
            if r2.status_code == 200:
                perfil = r2.json()
                print(f"  + {p['nome']} (perfil ja existia, prestadorId={perfil['id']})")
            else:
                print(f"  Falha ao criar/buscar perfil de {p['nome']}!")
                sys.exit(1)
        else:
            print(f"  + {p['nome']} (prestadorId={perfil['id']})")

        ids_prestadores_perfil.append(perfil["id"])
        time.sleep(0.1)
    print(f"  Total: {len(tokens_prestadores)} prestadores configurados")

    # ── 6. Executar os fluxos completos ──────────────────────────────────────
    print(f"\n[6] Executando {len(FLUXOS)} fluxos completos...")
    print("    solicitacao -> proposta -> chat -> aceitar -> concluir -> avaliar\n")

    fluxos_ok = 0
    for idx, (ci, pi, cat_id, desc, cidade, estado, valor) in enumerate(FLUXOS):
        cli_tok = tokens_clientes[ci % len(tokens_clientes)]
        pre_tok = tokens_prestadores[pi % len(tokens_prestadores)]
        pre_perfil_id = ids_prestadores_perfil[pi % len(ids_prestadores_perfil)]
        nota_cli, nota_pre = NOTAS[idx % len(NOTAS)]
        nome_cli = CLIENTES[ci % len(CLIENTES)]['nome']
        nome_pre = PRESTADORES[pi % len(PRESTADORES)]['nome']

        print(f"  Fluxo {idx+1:02d}/{len(FLUXOS)}: {nome_cli} -> {nome_pre}")

        # 6a. Criar solicitacao
        sol = post("/api/solicitacoes", {
            "categoriaId": cat_id,
            "descricao": desc,
            "cidade": cidade,
            "estado": estado
        }, token=cli_tok)
        if not sol:
            print(f"    [SKIP] Falha ao criar solicitacao")
            continue
        sol_id = sol["id"]

        # 6b. Enviar proposta
        prop = post("/api/propostas", {
            "solicitacaoId": sol_id,
            "valor": valor,
            "mensagem": f"Ola! Posso realizar este servico por R$ {valor:.2f}. Tenho experiencia e posso comecar em breve."
        }, token=pre_tok)
        if not prop:
            print(f"    [SKIP] Falha ao enviar proposta")
            continue
        prop_id = prop["id"]

        # 6c. Chat (3 mensagens)
        post(f"/api/chat/proposta/{prop_id}",
             {"texto": MSGS_PRESTADOR[idx % len(MSGS_PRESTADOR)], "propostaId": prop_id},
             token=pre_tok)
        post(f"/api/chat/proposta/{prop_id}",
             {"texto": MSGS_CLIENTE[idx % len(MSGS_CLIENTE)], "propostaId": prop_id},
             token=cli_tok)
        post(f"/api/chat/proposta/{prop_id}",
             {"texto": MSGS_COMBO[idx % len(MSGS_COMBO)], "propostaId": prop_id},
             token=pre_tok)

        # 6d. Cliente aceita proposta
        aceite = patch(f"/api/propostas/{prop_id}/aceitar", token=cli_tok)
        if aceite is None:
            print(f"    [SKIP] Falha ao aceitar proposta")
            continue

        # 6e. Mensagem pos-aceite
        post(f"/api/chat/proposta/{prop_id}", {
            "texto": "Proposta aceita! Aguardando voce no dia combinado.",
            "propostaId": prop_id
        }, token=cli_tok)

        # 6f. Concluir servico
        concl = patch(f"/api/solicitacoes/{sol_id}/concluir", token=cli_tok)
        if concl is None:
            print(f"    [SKIP] Falha ao concluir solicitacao")
            continue

        # 6g. Avaliacao do cliente para o prestador
        av_cli = post("/api/avaliacoes", {
            "solicitacaoId": sol_id,
            "prestadorId": pre_perfil_id,
            "nota": nota_cli,
            "comentario": COMENTARIOS_CLIENTE[idx % len(COMENTARIOS_CLIENTE)]
        }, token=cli_tok)

        # 6h. Avaliacao do prestador para o cliente
        av_pre = post("/api/avaliacoes/para-cliente", {
            "solicitacaoId": sol_id,
            "prestadorId": pre_perfil_id,
            "nota": nota_pre,
            "comentario": COMENTARIOS_PRESTADOR[idx % len(COMENTARIOS_PRESTADOR)]
        }, token=pre_tok)

        estrelas_cli = f"{nota_cli}* -> prestador" if av_cli else "falha avaliacao prestador"
        estrelas_pre = f"{nota_pre}* -> cliente" if av_pre else "falha avaliacao cliente"
        print(f"    OK | sol #{sol_id} | prop #{prop_id} | {estrelas_cli} | {estrelas_pre}")
        fluxos_ok += 1
        time.sleep(0.15)

    # ── 7. Sumario ────────────────────────────────────────────────────────────
    print()
    print("=" * 60)
    print("  CONCLUIDO!")
    print("=" * 60)
    print(f"  Clientes registrados:   {len(CLIENTES)}")
    print(f"  Prestadores registrados:{len(PRESTADORES)}")
    print(f"  Fluxos completos:       {fluxos_ok}/{len(FLUXOS)}")
    print(f"  Senha padrao:           {senha_padrao}")
    print()
    print("  Admin dashboard: http://localhost:4200")
    print("  Login admin: admin@vemresolver.com / Admin@2025")
    print("=" * 60)


if __name__ == "__main__":
    main()

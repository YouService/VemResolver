export enum TipoUsuario {
  CLIENTE = 'CLIENTE',
  PRESTADOR = 'PRESTADOR'
}

export enum StatusSolicitacao {
  ABERTA = 'ABERTA',
  EM_ANDAMENTO = 'EM_ANDAMENTO',
  CONCLUIDA = 'CONCLUIDA',
  CANCELADA = 'CANCELADA'
}

export enum StatusProposta {
  ENVIADA = 'ENVIADA',
  ACEITA = 'ACEITA',
  RECUSADA = 'RECUSADA'
}

export interface User {
  id: number;
  nome: string;
  email: string;
  tipo: TipoUsuario;
  telefone?: string;
  cpf?: string;
  rg?: string;
  orgaoExpedidor?: string;
  dataExpedicaoRg?: string;
  cep?: string;
  endereco?: string;
  cidade?: string;
  estado?: string;
  numero?: string;
  complemento?: string;
  dataCadastro: Date;
  ativo: boolean;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  userId: number;
  nome: string;
  email: string;
  tipoUsuario: TipoUsuario;
}

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface RegisterRequest {
  nome: string;
  email: string;
  senha: string;
  tipo: TipoUsuario;
  telefone?: string;
  cpf?: string;
  rg?: string;
  orgaoExpedidor?: string;
  dataExpedicaoRg?: string;
  cep?: string;
  endereco?: string;
  cidade?: string;
  estado?: string;
  numero?: string;
  complemento?: string;
  dataCadastro: Date;
}

export interface UpdatePerfilRequest {
  nome: string;
  email: string;
  telefone?: string;
  cep?: string;
  endereco?: string;
  cidade?: string;
  estado?: string;
  rg?: string;
  orgaoExpedidor?: string;
  dataExpedicaoRg?: string;
}

export interface AlterarSenhaRequest {
  senhaAtual: string;
  novaSenha: string;
  confirmarSenha: string;
}

export interface Categoria {
  id: number;
  nome: string;
  descricao?: string;
}

export interface Prestador {
  id: number;
  usuarioId: number;
  categoriasIds: number[];
  descricaoPerfil?: string;
  cidade: string;
  estado: string;
  avaliacaoMedia?: number;
  totalAvaliacoes?: number;
}

export interface SolicitacaoServico {
  id?: number;
  clienteId?: number;
  clienteNome?: string;
  categoriaId: number;
  categoriaNome?: string;
  descricao: string;
  cidade: string;
  estado: string;
  status?: StatusSolicitacao;
  dataCriacao?: Date;
}

export interface Proposta {
  id?: number;
  solicitacaoId: number;
  prestadorId?: number;
  prestadorNome?: string;
  valor: number;
  mensagem?: string;
  status?: StatusProposta;
  dataEnvio?: Date;
  solicitacaoDescricao?: string;
  solicitacaoStatus?: StatusSolicitacao;
  clienteNome?: string;
  clienteId?: number;
  cidade?: string;
  estado?: string;
  categoriaNome?: string;
}

export interface Avaliacao {
  id?: number;
  solicitacaoId: number;
  prestadorId: number;
  clienteId?: number;
  clienteNome?: string;
  nota: number;
  comentario?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

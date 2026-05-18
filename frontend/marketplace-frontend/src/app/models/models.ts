export enum TipoUsuario {
  CLIENTE = 'CLIENTE',
  PRESTADOR = 'PRESTADOR',
  ADMIN = 'ADMIN'
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

export enum TipoAvaliacao {
  CLIENTE_AVALIA_PRESTADOR = 'CLIENTE_AVALIA_PRESTADOR',
  PRESTADOR_AVALIA_CLIENTE = 'PRESTADOR_AVALIA_CLIENTE'
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
  avaliacaoMedia?: number;
  totalAvaliacoes?: number;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  userId: number;
  nome: string;
  email: string;
  tipoUsuario: TipoUsuario;
  refreshToken?: string;
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
  latitude?: number;
  longitude?: number;
  fotoPerfilUrl?: string;
  percentualCompletude?: number;
}

export interface ItemChecklist {
  id: string;
  descricao: string;
  concluido: boolean;
  peso: number;
  linkDestino: string;
}

export interface CompletudePrestador {
  percentual: number;
  itens: ItemChecklist[];
}

export interface NotificacaoDTO {
  id: number;
  mensagem: string;
  tipo: string;
  lida: boolean;
  linkDestino: string | null;
  dataCriacao: string;
}

export interface UserResumoDTO {
  id: number;
  nome: string;
  email: string;
  tipo: string;
  ativo: boolean;
  dataCadastro: string;
  avaliacaoMedia: number;
  totalAvaliacoes: number;
  ultimoAcesso?: string;
  online?: boolean;
}

export interface AdminStats {
  totalUsuarios: number;
  totalPrestadores: number;
  totalSolicitacoes: number;
  totalPropostas: number;
  usuariosOnline: number;
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
  foiAvaliadoPeloCliente?: boolean;
  foiAvaliadoPeloPrestador?: boolean;
  propostaAceitaId?: number;
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
  foiAvaliadoPeloPrestador?: boolean;
}

export interface Avaliacao {
  id?: number;
  solicitacaoId: number;
  prestadorId?: number;
  clienteId?: number;
  clienteNome?: string;
  tipo?: TipoAvaliacao;
  nomeAvaliador?: string;
  nomeAvaliado?: string;
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

export interface FotoPrestador {
  id: number;
  url: string;
  dataUpload: string;
}

export interface AvaliacaoResumo {
  nomeAvaliador: string;
  nota: number;
  comentario?: string;
  dataCriacao?: string;
}

export interface PerfilPublico {
  id: number;
  nome: string;
  fotoPerfilUrl?: string;
  categorias: string[];
  cidade: string;
  estado: string;
  avaliacaoMedia: number;
  totalAvaliacoes: number;
  verificado: boolean;
  portafolioUrls: string[];
  avaliacoes: AvaliacaoResumo[];
}

export interface MensagemChat {
  id?: number;
  propostaId: number;
  remetenteId?: number;
  remetenteNome?: string;
  texto: string;
  dataEnvio?: string;
  lida?: boolean;
}

export interface SolicitacaoAdminDTO {
  id: number;
  clienteNome: string;
  clienteEmail: string;
  categoriaNome: string;
  descricao: string;
  cidade: string;
  estado: string;
  status: string;
  dataCriacao: string;
  totalPropostas: number;
}

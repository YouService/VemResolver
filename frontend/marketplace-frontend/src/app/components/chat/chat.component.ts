import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ActivatedRoute, Router } from '@angular/router';
import { ChatService } from '../../services/chat.service';
import { PropostaService } from '../../services/proposta.service';
import { AuthService } from '../../services/auth.service';
import { MensagemChat, StatusProposta, StatusSolicitacao } from '../../models/models';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './chat.component.html'
})
export class ChatComponent implements OnInit, OnDestroy {
  propostaId!: number;
  currentUserId!: number;

  mensagens: MensagemChat[] = [];
  novasMensagens: number = 0;
  textoMensagem = '';
  enviando = false;
  uploadandoImagem = false;
  carregando = false;
  carregandoAnteriores = false;
  erro = '';
  contratoEncerrado = false;
  contratoDescricao = '';
  clienteNome = '';
  prestadorNome = '';

  backendUrl = environment.apiUrl.replace('/api', '');

  paginaAtual = 0;
  totalPaginas = 1;
  readonly tamanhoPagina = 20;

  private intervalo?: ReturnType<typeof setInterval>;

  constructor(
    private route: ActivatedRoute,
    public router: Router,
    private chatService: ChatService,
    private propostaService: PropostaService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.propostaId = Number(idParam);
    this.currentUserId = this.authService.getCurrentUser()?.userId ?? 0;

    this.propostaService.buscarPorId(this.propostaId).subscribe({
      next: (proposta) => {
        this.contratoDescricao = proposta.solicitacaoDescricao ?? '';
        this.clienteNome = proposta.clienteNome ?? '';
        this.prestadorNome = proposta.prestadorNome ?? '';
        this.contratoEncerrado =
          proposta.status === StatusProposta.RECUSADA ||
          proposta.status === StatusProposta.CANCELADA_PELO_PRESTADOR ||
          proposta.solicitacaoStatus === StatusSolicitacao.CONCLUIDA ||
          proposta.solicitacaoStatus === StatusSolicitacao.CANCELADA;
      },
      error: () => {
        this.erro = 'Não foi possível carregar os dados da proposta.';
      }
    });

    this.carregarPaginaInicial();
    this.intervalo = setInterval(() => this.carregarNovasMensagens(), 3000);
  }

  ngOnDestroy(): void {
    if (this.intervalo) {
      clearInterval(this.intervalo);
    }
  }

  carregarPaginaInicial(): void {
    this.carregando = true;
    this.chatService.listarPaginado(this.propostaId, 0, this.tamanhoPagina).subscribe({
      next: (page) => {
        this.totalPaginas = page.totalPages;
        this.paginaAtual = 0;
        // Backend retorna DESC (mais recente primeiro) — invertemos para exibir
        this.mensagens = [...page.content].reverse();
        this.carregando = false;
        this.marcarLidasSeNecessario();
        setTimeout(() => this.scrollParaFim(), 50);
      },
      error: () => {
        this.carregando = false;
      }
    });
  }

  carregarMensagensAnteriores(): void {
    if (this.paginaAtual + 1 >= this.totalPaginas || this.carregandoAnteriores) return;
    this.carregandoAnteriores = true;
    const proximaPagina = this.paginaAtual + 1;

    this.chatService.listarPaginado(this.propostaId, proximaPagina, this.tamanhoPagina).subscribe({
      next: (page) => {
        const container = document.getElementById('chat-messages');
        const alturaAntes = container?.scrollHeight ?? 0;

        const antigas = [...page.content].reverse();
        this.mensagens = [...antigas, ...this.mensagens];
        this.paginaAtual = proximaPagina;
        this.carregandoAnteriores = false;

        // Mantém posição do scroll após prepend
        setTimeout(() => {
          if (container) {
            container.scrollTop = container.scrollHeight - alturaAntes;
          }
        }, 0);
      },
      error: () => { this.carregandoAnteriores = false; }
    });
  }

  carregarNovasMensagens(): void {
    if (!this.propostaId) return;
    this.chatService.listar(this.propostaId).subscribe({
      next: (todas) => {
        const ids = new Set(this.mensagens.map(m => m.id));
        const novas = todas.filter(m => m.id && !ids.has(m.id));
        if (novas.length > 0) {
          this.mensagens = [...this.mensagens, ...novas];
          const naoLidas = novas.filter(m => !m.lida && m.remetenteId !== this.currentUserId).length;
          this.novasMensagens += naoLidas;
          if (naoLidas > 0) this.chatService.marcarComoLidas(this.propostaId).subscribe();
          setTimeout(() => this.scrollParaFim(), 50);
        }
      },
      error: () => {}
    });
  }

  private marcarLidasSeNecessario(): void {
    const naoLidas = this.mensagens.filter(m => !m.lida && m.remetenteId !== this.currentUserId).length;
    this.novasMensagens = naoLidas;
    if (naoLidas > 0) this.chatService.marcarComoLidas(this.propostaId).subscribe();
  }

  get temMensagensAnteriores(): boolean {
    return this.paginaAtual + 1 < this.totalPaginas;
  }

  enviarMensagem(): void {
    const texto = this.textoMensagem.trim();
    if (texto === '' || this.textoMensagem.length > 2000 || this.contratoEncerrado) {
      return;
    }

    this.enviando = true;
    this.erro = '';

    const msgOtimista: MensagemChat = {
      propostaId: this.propostaId,
      texto,
      remetenteId: this.currentUserId,
      remetenteNome: 'Você',
      dataEnvio: new Date().toISOString(),
      lida: false
    };

    this.mensagens = [...this.mensagens, msgOtimista];
    this.textoMensagem = '';
    setTimeout(() => this.scrollParaFim(), 50);

    this.chatService.enviar(this.propostaId, texto).subscribe({
      next: (msgReal) => {
        this.mensagens = [...this.mensagens.slice(0, -1), msgReal];
        this.enviando = false;
      },
      error: () => {
        this.mensagens = this.mensagens.slice(0, -1);
        this.textoMensagem = texto;
        this.erro = 'Não foi possível enviar a mensagem. Tente novamente.';
        this.enviando = false;
      }
    });
  }

  scrollParaFim(): void {
    const container = document.getElementById('chat-messages');
    if (container) container.scrollTop = container.scrollHeight;
  }

  get caractereRestantes(): number {
    return 2000 - this.textoMensagem.length;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || !input.files[0]) return;
    const file = input.files[0];
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
      this.erro = 'Formato inválido. Use JPEG, PNG ou WEBP.';
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.erro = 'A imagem deve ter no máximo 5 MB.';
      return;
    }
    this.uploadandoImagem = true;
    this.chatService.uploadImagem(this.propostaId, file).subscribe({
      next: (msg) => {
        this.mensagens = [...this.mensagens, msg];
        this.uploadandoImagem = false;
        setTimeout(() => this.scrollParaFim(), 50);
      },
      error: () => {
        this.erro = 'Não foi possível enviar a imagem. Tente novamente.';
        this.uploadandoImagem = false;
      }
    });
    input.value = '';
  }

  abrirImagem(url: string): void {
    window.open(this.backendUrl + url, '_blank');
  }

  onKeydownEnter(event: Event): void {
    const ke = event as KeyboardEvent;
    if (!ke.shiftKey) {
      event.preventDefault();
      this.enviarMensagem();
    }
  }

  formatarHora(dataEnvio: string): string {
    const d = new Date(dataEnvio);
    const hh = d.getHours().toString().padStart(2, '0');
    const mm = d.getMinutes().toString().padStart(2, '0');
    return `${hh}:${mm}`;
  }

  isMinhaMsg(msg: MensagemChat): boolean {
    return msg.remetenteId === this.currentUserId;
  }

  goBack(): void {
    const tipo = this.authService.getCurrentUser()?.tipoUsuario;
    if (tipo === 'CLIENTE') this.router.navigate(['/cliente/dashboard']);
    else if (tipo === 'PRESTADOR') this.router.navigate(['/prestador/dashboard']);
    else this.router.navigate(['/']);
  }
}

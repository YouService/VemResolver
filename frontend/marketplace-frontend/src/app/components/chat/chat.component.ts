import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ActivatedRoute, Router } from '@angular/router';
import { ChatService } from '../../services/chat.service';
import { PropostaService } from '../../services/proposta.service';
import { AuthService } from '../../services/auth.service';
import { MensagemChat, StatusProposta, StatusSolicitacao } from '../../models/models';

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
  carregando = false;
  erro = '';
  contratoEncerrado = false;
  contratoDescricao = '';
  clienteNome = '';
  prestadorNome = '';

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
          proposta.solicitacaoStatus === StatusSolicitacao.CONCLUIDA ||
          proposta.solicitacaoStatus === StatusSolicitacao.CANCELADA;
      },
      error: () => {
        this.erro = 'Não foi possível carregar os dados da proposta.';
      }
    });

    this.carregarMensagens();
    this.intervalo = setInterval(() => this.carregarMensagens(), 3000);
  }

  ngOnDestroy(): void {
    if (this.intervalo) {
      clearInterval(this.intervalo);
    }
  }

  carregarMensagens(): void {
    if (!this.propostaId) return;
    this.carregando = true;
    this.chatService.listar(this.propostaId).subscribe({
      next: (msgs) => {
        this.mensagens = msgs;
        this.carregando = false;
        const naoLidas = msgs.filter(
          (m) => !m.lida && m.remetenteId !== this.currentUserId
        ).length;
        this.novasMensagens = naoLidas;
        if (naoLidas > 0) {
          this.chatService.marcarComoLidas(this.propostaId).subscribe();
        }
        setTimeout(() => this.scrollParaFim(), 50);
      },
      error: () => {
        this.carregando = false;
      }
    });
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
        this.carregarMensagens();
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

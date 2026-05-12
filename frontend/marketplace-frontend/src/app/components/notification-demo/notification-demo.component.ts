import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-notification-demo',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule, MatIconModule],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 p-8">
      <div class="max-w-4xl mx-auto">
        <!-- Header -->
        <div class="mb-12">
          <h1 class="text-4xl font-bold text-gray-800 mb-2">Sistema de Notificações</h1>
          <p class="text-gray-600">Clique nos botões abaixo para testar diferentes tipos de notificações</p>
        </div>

        <!-- Notificações Básicas -->
        <div class="bg-white rounded-lg shadow-lg p-8 mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-6">
            <mat-icon class="inline mr-2">notifications_active</mat-icon>
            Notificações Básicas
          </h2>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <!-- Success -->
            <button
              (click)="testarSucesso()"
              class="bg-emerald-600 hover:bg-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition transform hover:scale-105 shadow-md flex items-center justify-center gap-2"
            >
              <mat-icon>check_circle</mat-icon>
              Sucesso
            </button>

            <!-- Error -->
            <button
              (click)="testarErro()"
              class="bg-red-600 hover:bg-red-700 text-white font-semibold py-3 px-6 rounded-lg transition transform hover:scale-105 shadow-md flex items-center justify-center gap-2"
            >
              <mat-icon>error</mat-icon>
              Erro
            </button>

            <!-- Warning -->
            <button
              (click)="testarAviso()"
              class="bg-amber-600 hover:bg-amber-700 text-white font-semibold py-3 px-6 rounded-lg transition transform hover:scale-105 shadow-md flex items-center justify-center gap-2"
            >
              <mat-icon>warning</mat-icon>
              Aviso
            </button>

            <!-- Info -->
            <button
              (click)="testarInfo()"
              class="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-6 rounded-lg transition transform hover:scale-105 shadow-md flex items-center justify-center gap-2"
            >
              <mat-icon>info</mat-icon>
              Informação
            </button>
          </div>
        </div>

        <!-- Notificações com Título -->
        <div class="bg-white rounded-lg shadow-lg p-8 mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-6">
            <mat-icon class="inline mr-2">label</mat-icon>
            Com Título
          </h2>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <button
              (click)="testarComTitulo()"
              class="bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-3 px-6 rounded-lg transition transform hover:scale-105 shadow-md flex items-center justify-center gap-2"
            >
              <mat-icon>edit</mat-icon>
              Sucesso com Título
            </button>

            <button
              (click)="testarErroComTitulo()"
              class="bg-pink-600 hover:bg-pink-700 text-white font-semibold py-3 px-6 rounded-lg transition transform hover:scale-105 shadow-md flex items-center justify-center gap-2"
            >
              <mat-icon>delete</mat-icon>
              Erro com Título
            </button>
          </div>
        </div>

        <!-- Notificações com Ação -->
        <div class="bg-white rounded-lg shadow-lg p-8 mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-6">
            <mat-icon class="inline mr-2">touch_app</mat-icon>
            Com Botão de Ação
          </h2>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <button
              (click)="testarComAcao()"
              class="bg-cyan-600 hover:bg-cyan-700 text-white font-semibold py-3 px-6 rounded-lg transition transform hover:scale-105 shadow-md flex items-center justify-center gap-2"
            >
              <mat-icon>done_all</mat-icon>
              Confirmação
            </button>

            <button
              (click)="testarAbrirArquivo()"
              class="bg-teal-600 hover:bg-teal-700 text-white font-semibold py-3 px-6 rounded-lg transition transform hover:scale-105 shadow-md flex items-center justify-center gap-2"
            >
              <mat-icon>download</mat-icon>
              Download Completo
            </button>

            <button
              (click)="testarDeletar()"
              class="bg-red-700 hover:bg-red-800 text-white font-semibold py-3 px-6 rounded-lg transition transform hover:scale-105 shadow-md flex items-center justify-center gap-2"
            >
              <mat-icon>delete_forever</mat-icon>
              Deletar Item
            </button>

            <button
              (click)="testarRetentativa()"
              class="bg-orange-600 hover:bg-orange-700 text-white font-semibold py-3 px-6 rounded-lg transition transform hover:scale-105 shadow-md flex items-center justify-center gap-2"
            >
              <mat-icon>refresh</mat-icon>
              Tentar Novamente
            </button>
          </div>
        </div>

        <!-- Notificações Permanentes -->
        <div class="bg-white rounded-lg shadow-lg p-8 mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-6">
            <mat-icon class="inline mr-2">schedule</mat-icon>
            Duração Customizada
          </h2>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <button
              (click)="testarCurta()"
              class="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-6 rounded-lg transition transform hover:scale-105 shadow-md flex items-center justify-center gap-2"
            >
              <mat-icon>timer</mat-icon>
              2 Segundos
            </button>

            <button
              (click)="testarLonga()"
              class="bg-blue-500 hover:bg-blue-600 text-white font-semibold py-3 px-6 rounded-lg transition transform hover:scale-105 shadow-md flex items-center justify-center gap-2"
            >
              <mat-icon>hourglass_full</mat-icon>
              10 Segundos
            </button>

            <button
              (click)="testarPermanente()"
              class="bg-blue-700 hover:bg-blue-800 text-white font-semibold py-3 px-6 rounded-lg transition transform hover:scale-105 shadow-md flex items-center justify-center gap-2"
            >
              <mat-icon>lock_clock</mat-icon>
              Permanente (0s)
            </button>

            <button
              (click)="removerTodas()"
              class="bg-gray-600 hover:bg-gray-700 text-white font-semibold py-3 px-6 rounded-lg transition transform hover:scale-105 shadow-md flex items-center justify-center gap-2"
            >
              <mat-icon>clear_all</mat-icon>
              Remover Todas
            </button>
          </div>
        </div>

        <!-- Info Box -->
        <div class="bg-blue-50 border-l-4 border-blue-500 p-6 rounded-lg">
          <div class="flex items-start gap-3">
            <mat-icon class="text-blue-600 text-2xl mt-1">lightbulb</mat-icon>
            <div>
              <h3 class="font-semibold text-blue-900 mb-2">Dica:</h3>
              <p class="text-blue-800 text-sm">
                Este é apenas um componente de demonstração. Para usar em seus componentes, injete o
                <code class="bg-blue-100 px-2 py-1 rounded font-mono">NotificationService</code>
                e chame os métodos como:
                <code class="bg-blue-100 px-2 py-1 rounded font-mono">this.notificationService.success('Mensagem')</code>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
    }
  `]
})
export class NotificationDemoComponent {
  constructor(private notifService: NotificationService) {}

  // Notificações Básicas
  testarSucesso(): void {
    this.notifService.success(
      'Operação concluída com êxito! Todas as mudanças foram salvas.',
      'Sucesso!'
    );
  }

  testarErro(): void {
    this.notifService.error(
      'Ocorreu um erro ao processar sua solicitação. Por favor, tente novamente.',
      'Erro!'
    );
  }

  testarAviso(): void {
    this.notifService.warning(
      'Esta ação pode ter consequências. Verifique seus dados antes de continuar.',
      'Atenção!'
    );
  }

  testarInfo(): void {
    this.notifService.info(
      'Suas alterações foram salvas automaticamente. Não há perda de dados.',
      'Informação'
    );
  }

  // Com Título
  testarComTitulo(): void {
    this.notifService.success(
      'Seu perfil foi atualizado com sucesso. Todas as alterações foram aplicadas.',
      'Perfil Atualizado',
      5000
    );
  }

  testarErroComTitulo(): void {
    this.notifService.error(
      'Não foi possível deletar este item porque ele está sendo usado por outro recurso.',
      'Não é Possível Deletar',
      5000
    );
  }

  // Com Ação
  testarComAcao(): void {
    this.notifService.warning(
      'Você está prestes a deletar este registro permanentemente.',
      'Confirmação Necessária',
      0, // não auto-dismiss
      'Confirmar Exclusão',
      () => {
        this.notifService.success('Item deletado com sucesso!', 'Deletado');
      }
    );
  }

  testarAbrirArquivo(): void {
    this.notifService.success(
      'Seu arquivo foi baixado com sucesso.',
      'Download Completo',
      0,
      'Abrir Arquivo',
      () => {
        this.notifService.info('Abrindo arquivo...', 'Processando', 3000);
      }
    );
  }

  testarDeletar(): void {
    this.notifService.error(
      'Tem certeza que deseja deletar permanentemente?',
      'Deletar Item',
      0,
      'Deletar',
      () => {
        this.notifService.error('Item deletado permanentemente!', 'Deletado', 5000);
      }
    );
  }

  testarRetentativa(): void {
    this.notifService.error(
      'Falha na conexão com o servidor.',
      'Erro de Conexão',
      0,
      'Tentar Novamente',
      () => {
        this.notifService.info('Tentando novamente...', 'Processando', 2000);
        setTimeout(() => {
          this.notifService.success('Conectado com sucesso!', 'Conectado');
        }, 2000);
      }
    );
  }

  // Duração customizada
  testarCurta(): void {
    this.notifService.success(
      'Esta notificação desaparecerá em 2 segundos.',
      'Rápida',
      2000
    );
  }

  testarLonga(): void {
    this.notifService.info(
      'Esta notificação permanecerá visível por 10 segundos.',
      'Longa',
      10000
    );
  }

  testarPermanente(): void {
    this.notifService.warning(
      'Esta notificação permanecerá até você fechá-la manualmente. Clique no X para fechar.',
      'Mensagem Importante',
      0 // 0 significa infinito
    );
  }

  removerTodas(): void {
    this.notifService.removeAll();
    this.notifService.info('Todas as notificações foram removidas.', 'Limpeza Completa', 3000);
  }
}

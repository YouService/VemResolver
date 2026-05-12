import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-seguranca',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  template: `
    <div class="min-h-screen flex flex-col bg-white font-sans">
      
      <!-- Faixa de Título (Padrão Azul) -->
      <div class="bg-blue-600 py-20">
        <div class="container max-w-6xl mx-auto px-4">
          <h1 class="text-4xl md:text-5xl font-bold text-white mb-4">Dicas de Segurança</h1>
          <p class="text-blue-100 text-lg">Saiba como negociar e contratar serviços com tranquilidade</p>
        </div>
      </div>

      <!-- Conteúdo -->
      <main class="flex-grow container max-w-6xl mx-auto px-4 py-16">
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-12">
          
          <!-- Coluna Clientes -->
          <div>
            <div class="flex items-center gap-3 mb-6">
              <div class="bg-blue-100 p-3 rounded-full text-blue-600">
                <mat-icon class="text-3xl w-8 h-8">person</mat-icon>
              </div>
              <h2 class="text-2xl font-bold text-gray-900">Para Clientes</h2>
            </div>
            
            <ul class="space-y-8">
              <li class="flex gap-4">
                <mat-icon class="text-green-500 mt-1">check_circle</mat-icon>
                <div>
                  <h3 class="font-bold text-gray-800 text-lg">Verifique as Avaliações</h3>
                  <p class="text-gray-600">Sempre confira a nota e os comentários deixados por outros clientes no perfil do profissional. Isso ajuda a entender a qualidade do serviço.</p>
                </div>
              </li>
              <li class="flex gap-4">
                <mat-icon class="text-green-500 mt-1">check_circle</mat-icon>
                <div>
                  <h3 class="font-bold text-gray-800 text-lg">Compare Orçamentos</h3>
                  <p class="text-gray-600">Não feche negócio com o primeiro que aparecer. Solicite ao menos 3 orçamentos para ter uma base de preço justa e evitar surpresas.</p>
                </div>
              </li>
              <li class="flex gap-4">
                <mat-icon class="text-green-500 mt-1">check_circle</mat-icon>
                <div>
                  <h3 class="font-bold text-gray-800 text-lg">Pagamento Seguro</h3>
                  <p class="text-gray-600">Evite pagamentos antecipados integrais, principalmente fora da plataforma. Se possível, combine o pagamento após a conclusão ou por etapas.</p>
                </div>
              </li>
              <li class="flex gap-4">
                <mat-icon class="text-green-500 mt-1">check_circle</mat-icon>
                <div>
                  <h3 class="font-bold text-gray-800 text-lg">Local Seguro</h3>
                  <p class="text-gray-600">Se o serviço for na sua casa, certifique-se de que haverá alguém acompanhando. Guarde objetos de valor antes da chegada do profissional.</p>
                </div>
              </li>
            </ul>
          </div>

          <!-- Coluna Profissionais -->
          <div>
            <div class="flex items-center gap-3 mb-6">
              <div class="bg-orange-100 p-3 rounded-full text-orange-600">
                <mat-icon class="text-3xl w-8 h-8">handyman</mat-icon>
              </div>
              <h2 class="text-2xl font-bold text-gray-900">Para Profissionais</h2>
            </div>

            <ul class="space-y-8">
              <li class="flex gap-4">
                <mat-icon class="text-blue-500 mt-1">verified</mat-icon>
                <div>
                  <h3 class="font-bold text-gray-800 text-lg">Mantenha Dados Atualizados</h3>
                  <p class="text-gray-600">Um perfil completo com foto real e documentos verificados transmite muito mais confiança para quem vai te contratar.</p>
                </div>
              </li>
              <li class="flex gap-4">
                <mat-icon class="text-blue-500 mt-1">verified</mat-icon>
                <div>
                  <h3 class="font-bold text-gray-800 text-lg">Formalize o Combinado</h3>
                  <p class="text-gray-600">Sempre deixe registrado (por mensagem ou contrato) o que será feito, o prazo e o valor. Isso protege você e o cliente.</p>
                </div>
              </li>
              <li class="flex gap-4">
                <mat-icon class="text-blue-500 mt-1">verified</mat-icon>
                <div>
                  <h3 class="font-bold text-gray-800 text-lg">Seja Profissional</h3>
                  <p class="text-gray-600">Pontualidade e educação são essenciais. Se não puder comparecer, avise com antecedência.</p>
                </div>
              </li>
              <li class="flex gap-4">
                <mat-icon class="text-blue-500 mt-1">verified</mat-icon>
                <div>
                  <h3 class="font-bold text-gray-800 text-lg">Cuidado com Dados</h3>
                  <p class="text-gray-600">Nunca compartilhe seus dados de acesso (login/senha) com terceiros. O VemResolver nunca pede sua senha por telefone.</p>
                </div>
              </li>
            </ul>
          </div>

        </div>

        <!-- Box de Alerta -->
        <div class="mt-16 bg-yellow-50 border-l-4 border-yellow-400 p-6 rounded-r-lg">
          <div class="flex items-start">
            <mat-icon class="text-yellow-600 mr-4">warning</mat-icon>
            <div>
              <h3 class="text-lg font-bold text-yellow-800 mb-2">Importante: O que o VemResolver NÃO faz</h3>
              <p class="text-yellow-700">
                Nós conectamos pessoas, mas não participamos da negociação final nem da execução do serviço. 
                Qualquer problema decorrente da contratação deve ser resolvido diretamente entre as partes, embora possamos auxiliar com informações em casos específicos.
              </p>
            </div>
          </div>
        </div>

      </main>

      <!-- Footer -->
      <footer class="bg-white py-10 border-t border-gray-100">
        <div class="container max-w-6xl mx-auto px-4 flex flex-col md:flex-row items-center justify-between gap-6">
          <div class="flex items-center gap-2">
            <img src="assets/logo.png" alt="VemResolver" class="h-8 w-auto grayscale opacity-50 hover:grayscale-0 hover:opacity-100 transition">
          </div>
          <div class="flex flex-col md:flex-row gap-6 items-center">
            <div class="flex gap-4 text-sm font-medium text-gray-500">
              <a routerLink="/sobre-nos" class="hover:text-blue-600 transition cursor-pointer">Sobre nós</a>
              <a routerLink="/termos-uso" class="hover:text-blue-600 transition cursor-pointer">Termos de uso</a>
              <a routerLink="/politica-privacidade" class="hover:text-blue-600 transition cursor-pointer">Política de Privacidade</a>
            </div>
            <div class="text-sm text-gray-500 text-center md:text-right">
              &copy; 2026 VemResolver LTDA. Todos os direitos reservados.
            </div>
          </div>
        </div>
      </footer>
    </div>
  `
})
export class SegurancaComponent {}
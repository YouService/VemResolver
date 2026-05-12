import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-sobre-nos',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="min-h-screen flex flex-col bg-white font-sans">


      <!-- Banner -->
      <div class="bg-blue-600 py-20">
        <div class="container max-w-6xl mx-auto px-4">
          <h1 class="text-4xl md:text-5xl font-bold text-white mb-4">Sobre Nós</h1>
          <p class="text-blue-100 text-lg">Conectando quem precisa com quem sabe fazer.</p>
        </div>
      </div>

      <!-- Conteúdo -->
      <main class="flex-grow container max-w-6xl mx-auto px-4 py-16">
        <div class="prose prose-lg max-w-none text-gray-700 text-justify leading-relaxed">
          <p>
            O <strong>VemResolver</strong> nasceu com uma missão simples, mas poderosa: facilitar a vida das pessoas conectando clientes que precisam de serviços a profissionais qualificados prontos para ajudar.
          </p>
          <p>
            Sabemos como pode ser difícil encontrar um encanador de confiança, um professor particular dedicado ou um eletricista disponível rapidamente. Ao mesmo tempo, sabemos que existem milhares de profissionais talentosos procurando por novas oportunidades de trabalho.
          </p>
          <p>
            Nossa plataforma serve como essa ponte segura e eficiente. Utilizamos tecnologia de ponta para garantir que você encontre a ajuda certa, no momento certo, com total transparência e segurança.
          </p>
          
          <h2 class="text-2xl font-bold text-gray-900 mt-8">Nossa Missão</h2>
          <p>
            Empoderar profissionais autônomos e pequenas empresas, oferecendo ferramentas para que cresçam seus negócios, enquanto proporcionamos aos clientes uma experiência de contratação de serviços sem estresse.
          </p>

          <h2 class="text-2xl font-bold text-gray-900 mt-8">Nossos Valores</h2>
          <ul class="list-disc pl-5 space-y-2">
            <li><strong>Confiança:</strong> Construímos relações baseadas na transparência.</li>
            <li><strong>Qualidade:</strong> Valorizamos o trabalho bem feito.</li>
            <li><strong>Inovação:</strong> Buscamos sempre a melhor solução tecnológica.</li>
            <li><strong>Segurança:</strong> A proteção dos nossos usuários é prioridade.</li>
          </ul>
        </div>
      </main>

      <!-- Footer Minimalista -->
      <footer class="bg-white py-10 border-t border-gray-100">
        <div class="container max-w-6xl mx-auto px-4 flex flex-col md:flex-row items-center justify-between gap-6">
          <div class="flex items-center gap-2">
             <img src="assets/logo.png" alt="VemResolver" class="h-8 w-auto grayscale opacity-50 hover:grayscale-0 hover:opacity-100 transition">
          </div>
          <div class="text-sm text-gray-500 text-center md:text-right max-w-xl">
            &copy; 2026 VemResolver LTDA. Todos os direitos reservados.
          </div>
        </div>
      </footer>
    </div>
  `
})
export class SobreNosComponent {}
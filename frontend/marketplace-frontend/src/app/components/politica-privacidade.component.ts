import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-politica-privacidade',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="min-h-screen flex flex-col bg-white font-sans">
      <!-- Header com Logo -->
      <header class="bg-white py-6 border-b border-gray-100">
        <div class="container mx-auto px-4">
          <a routerLink="/" class="text-3xl font-extrabold text-blue-600 tracking-tight cursor-pointer no-underline flex items-center gap-2">
            <span class="text-3xl">🛠️</span> VemResolver
          </a>
        </div>
      </header>

      <!-- Faixa de Título (Padrão Azul) -->
      <div class="bg-blue-600 py-20">
        <div class="container mx-auto px-4">
          <h1 class="text-4xl md:text-5xl font-bold text-white mb-4">Política de Privacidade</h1>
          <p class="text-blue-100 text-lg">Entenda como cuidamos dos seus dados no VemResolver</p>
        </div>
      </div>

      <!-- Conteúdo do Texto -->
      <main class="flex-grow container mx-auto px-4 py-16 max-w-5xl">
        <div class="prose prose-lg max-w-none text-gray-700 text-justify leading-relaxed">
          <p>
            <strong>VEMRESOLVER LTDA (“VemResolver”)</strong>, sociedade limitada, inscrita no CNPJ/MF sob o nº [AGUARDANDO REGISTRO], é uma pessoa jurídica de direito privado prestadora de serviços de anúncios online, realizados entre promitentes contratantes (“Contratante”) e promitentes Anunciantes de serviço (“Anunciantes”), por meio do aplicativo e do site VemResolver (“Plataforma”).
          </p>
          <p>
            Esta Política de Privacidade esclarece aos Usuários as práticas no que diz respeito à privacidade das informações prestadas, demonstrando nosso comprometimento em resguardar sua privacidade e proteger seus dados pessoais conforme a Lei Geral de Proteção de Dados (LGPD).
          </p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">1. QUAIS INFORMAÇÕES COLETAMOS?</h3>
          <p>Todas as informações ou os dados pessoais prestados pelo usuário de VemResolver são armazenados em servidores de alta segurança e criptografados.</p>
          
          <div class="overflow-x-auto mt-6 mb-8">
            <table class="min-w-full bg-white border border-gray-200 rounded-lg">
              <thead class="bg-gray-50">
                <tr>
                  <th class="py-3 px-4 text-left font-bold text-gray-900 border-b">Finalidade</th>
                  <th class="py-3 px-4 text-left font-bold text-gray-900 border-b">Dados Pessoais</th>
                  <th class="py-3 px-4 text-left font-bold text-gray-900 border-b">Base Legal</th>
                </tr>
              </thead>
              <tbody>
                <tr class="border-b hover:bg-gray-50">
                  <td class="py-3 px-4">Cadastro: Contratante</td>
                  <td class="py-3 px-4">Nome completo, celular, e-mail, CEP, Geolocalização e IP</td>
                  <td class="py-3 px-4"><span class="bg-blue-100 text-blue-800 text-xs font-semibold px-2.5 py-0.5 rounded">Consentimento</span></td>
                </tr>
                <tr class="border-b hover:bg-gray-50">
                  <td class="py-3 px-4">Cadastro: Anunciante</td>
                  <td class="py-3 px-4">Nome completo, CPF/CNPJ, celular, e-mail, CEP, Endereço e Foto</td>
                  <td class="py-3 px-4"><span class="bg-blue-100 text-blue-800 text-xs font-semibold px-2.5 py-0.5 rounded">Consentimento</span></td>
                </tr>
                <tr class="border-b hover:bg-gray-50">
                  <td class="py-3 px-4">Uso da Plataforma</td>
                  <td class="py-3 px-4">Características do dispositivo, navegador e logs de acesso (data/hora)</td>
                  <td class="py-3 px-4"><span class="bg-gray-100 text-gray-800 text-xs font-semibold px-2.5 py-0.5 rounded">Legítimo Interesse</span></td>
                </tr>
                <tr class="hover:bg-gray-50">
                  <td class="py-3 px-4">Comunicação</td>
                  <td class="py-3 px-4">E-mail e telefone para avisos importantes e suporte</td>
                  <td class="py-3 px-4"><span class="bg-blue-100 text-blue-800 text-xs font-semibold px-2.5 py-0.5 rounded">Consentimento</span></td>
                </tr>
              </tbody>
            </table>
          </div>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">2. PARA QUE USAMOS AS INFORMAÇÕES?</h3>
          <ul class="list-none space-y-2 pl-0">
            <li>(i) Identificação e manutenção cadastral;</li>
            <li>(ii) Viabilizar o contato direto entre Contratantes e Anunciantes;</li>
            <li>(iii) Localização de profissionais próximos ao Contratante através de geolocalização;</li>
            <li>(iv) Prevenção de fraudes e implementação de medidas de segurança (como validação de JWT);</li>
            <li>(v) Prestação de suporte e atendimento;</li>
            <li>(vi) Cumprimento de obrigações legais e ordens judiciais.</li>
          </ul>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">3. COMPARTILHAMENTO DE INFORMAÇÕES</h3>
          <p>O VemResolver não comercializa seus dados pessoais. O compartilhamento ocorre apenas:</p>
          <ul class="list-disc pl-5 mt-2 space-y-2">
            <li><strong>Entre Usuários:</strong> Para viabilizar a prestação do serviço (após o uso de Moedas pelo Anunciante).</li>
            <li><strong>Provedores de Tecnologia:</strong> Empresas de hospedagem em nuvem, serviços de e-mail e gateways de pagamento.</li>
            <li><strong>Parceiros Estratégicos:</strong> Para oferta de benefícios exclusivos (conforme atualizado periodicamente na plataforma).</li>
          </ul>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">4. DIREITOS DO USUÁRIO</h3>
          <p>Em conformidade com a LGPD, garantimos a você:</p>
          <ul class="list-disc pl-5 mt-2 space-y-2">
            <li><strong>Acesso e Retificação:</strong> Solicitar acesso aos seus dados ou correção de informações incompletas.</li>
            <li><strong>Exclusão:</strong> Solicitar a eliminação de dados desnecessários ou excessivos.</li>
            <li><strong>Portabilidade:</strong> Direito de transferir seus dados para outro fornecedor de serviço.</li>
            <li><strong>Revogação de Consentimento:</strong> Direito de retirar a autorização para tratamento de dados a qualquer momento.</li>
          </ul>
          <p class="mt-4 bg-gray-50 p-4 rounded border-l-4 border-blue-500">
            Para exercer seus direitos, entre em contato através do e-mail: <strong>contato&#64;vemresolver.com.br</strong>.
          </p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">5. SEGURANÇA DAS INFORMAÇÕES</h3>
          <p>
            Cuidamos das suas informações com padrões rígidos de segurança. Utilizamos criptografia (BCrypt) para senhas e tokens JWT para garantir que apenas usuários autenticados acessem suas contas. Embora adotemos as melhores práticas, o usuário reconhece que nenhum sistema é 100% imune a invasões de terceiros agindo de má-fé.
          </p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">6. COOKIES</h3>
          <p>
            Usamos cookies no Angular para melhorar sua experiência, lembrar preferências e garantir a segurança da sessão. Você pode desativar os cookies nas configurações do seu navegador, ciente de que isso pode limitar algumas funcionalidades da Plataforma.
          </p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">7. ATUALIZAÇÕES</h3>
          <p>
            Esta Política pode ser atualizada a qualquer momento para refletir melhorias técnicas ou mudanças legais. A data da última atualização será sempre exibida no topo do documento.
          </p>
        </div>
      </main>

      <!-- Footer Minimalista -->
      <footer class="bg-white py-10 border-t border-gray-100">
        <div class="container mx-auto px-4 flex flex-col md:flex-row items-center justify-between gap-6">
          <div class="text-3xl font-extrabold text-gray-900 tracking-tight flex items-center gap-2">
            VemResolver
          </div>
          <div class="text-sm text-gray-500 text-center md:text-right max-w-xl">
            &copy; 2026 VemResolver LTDA. Todos os direitos reservados.
          </div>
        </div>
      </footer>
    </div>
  `
})
export class PoliticaPrivacidadeComponent {}
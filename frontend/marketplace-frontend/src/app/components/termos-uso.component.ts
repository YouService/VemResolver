import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-termos-uso',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="min-h-screen flex flex-col bg-white font-sans">
      
      <!-- Faixa de Título (Padrão Azul) -->
      <div class="bg-blue-600 py-20">
        <div class="container max-w-6xl mx-auto px-4">
          <h1 class="text-4xl md:text-5xl font-bold text-white mb-4">Termos e condições gerais de uso do site</h1>
          <p class="text-blue-100 text-lg">Verifique aqui os termos e condições gerais de uso do VemResolver</p>
        </div>
      </div>

      <!-- Conteúdo do Texto -->
      <main class="flex-grow container max-w-6xl mx-auto px-4 py-16">
        <div class="prose prose-lg max-w-none text-gray-700 text-justify leading-relaxed">
          <p>
            <strong>VEMRESOLVER LTDA (“VemResolver”)</strong>, sociedade limitada, inscrita no CNPJ/MF sob o nº [AGUARDANDO REGISTRO], é uma pessoa jurídica de direito privado prestadora de serviços de anúncios online, realizados entre promitentes contratantes (“Contratante”) e promitentes Anunciantes de serviço (“Anunciantes”), por meio do aplicativo e do site VemResolver (“Plataforma”).
          </p>
          <p>
            Por intermédio destes Termos e Condições Gerais de Uso (“Termos”), o VemResolver apresenta aos usuários em geral, Contratantes e Anunciantes (em conjunto denominados “Usuários”) as condições essenciais para o uso dos serviços oferecidos na Plataforma.
          </p>
          <p>
            Ao utilizar a Plataforma ou utilizar os serviços ofertados pelo VemResolver, os Usuários aceitam e se submetem às condições destes Termos e às Políticas de Privacidade, bem como a todos os documentos anexos a estes.
          </p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">1. OBJETO</h3>
          <ul class="list-none space-y-4 pl-0">
            <li><strong>a.</strong> Os serviços objeto dos presentes Termos consistem em:
              <ul class="list-none pl-6 mt-2 space-y-2">
                <li><strong>i.</strong> Permitir aos Contratantes que utilizem a Plataforma para livremente e sem direcionamento ou interferência busquem orçamentos de Anunciantes;</li>
                <li><strong>ii.</strong> O Modelo de Utilização de Moedas apresenta os orçamentos requeridos pelos Contratantes aos Anunciantes (“Orçamentos”), que poderão oferecer ou não os seus Serviços (“Serviços”) ao Contratante.</li>
                <li><strong>iii.</strong> Viabilizar o contato direto entre Anunciantes e Contratantes interessados em adquirir os Serviços, por meio da divulgação das informações de contato de uma parte à outra.</li>
              </ul>
            </li>
            <li><strong>b.</strong> O VemResolver, portanto, possibilita que os Usuários se contatem e negociem entre si diretamente, sem intervir no contato, na negociação ou na efetivação dos negócios, não sendo, nesta qualidade, fornecedor de quaisquer Serviços anunciados por seus Usuários na Plataforma.</li>
            <li><strong>c.</strong> Na qualidade de classificado de Serviços, o VemResolver não impõe ou interfere em qualquer negociação sobre condição, valor, qualidade, forma ou prazo da contratação entre os Contratantes e Anunciantes, tampouco garante a qualidade ou entrega dos Serviços contratados entre os Usuários.</li>
          </ul>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">2. CAPACIDADE PARA CADASTRAR-SE</h3>
          <ul class="list-none space-y-2 pl-0">
            <li><strong>a.</strong> Os serviços do VemResolver estão disponíveis para pessoas físicas e pessoas jurídicas regularmente inscritas nos cadastros de contribuintes federal e estaduais que tenham capacidade legal para contratá-los. Menores de idade ou pessoas inabilitadas pelo VemResolver não podem utilizar os serviços.</li>
            <li><strong>b.</strong> É vedada a criação de mais de um cadastro por Usuário. O VemResolver reserva-se o direito de inabilitar cadastros duplicados sem aviso prévio.</li>
          </ul>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">3. CADASTRO</h3>
          <ul class="list-none space-y-2 pl-0">
            <li><strong>a.</strong> É necessário o preenchimento completo de todos os dados pessoais exigidos. A veracidade dos dados é de exclusiva responsabilidade do Usuário.</li>
            <li><strong>b.</strong> O VemResolver reserva-se o direito de suspender cadastros suspeitos de conter dados errôneos ou inverídicos, sem direito a indenização.</li>
          </ul>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">4. MODIFICAÇÕES DOS TERMOS E CONDIÇÕES GERAIS</h3>
          <p><strong>a.</strong> O VemResolver poderá alterar estes Termos a qualquer tempo. As alterações entrarão em vigor 10 (dez) dias após a publicação.</p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">5. ACEITE DE ORÇAMENTOS</h3>
          <p><strong>a.</strong> Os dados de contato do Contratante serão disponibilizados ao Anunciante após o desconto de Moedas Virtuais ("Moedas"). O Anunciante reconhece que paga pelo acesso ao contato, não havendo garantia de sucesso na negociação final.</p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">6. PRIVACIDADE DA INFORMAÇÃO</h3>
          <p><strong>a.</strong> Os dados pessoais são protegidos conforme a Política de Privacidade da Plataforma.</p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">7. TARIFAS</h3>
          <p><strong>a.</strong> As Moedas são a unidade de troca para funcionalidades da plataforma. O preço e disponibilidade podem variar conforme região e demanda.</p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">8. REGIME ESPECIAL</h3>
          <p><strong>a.</strong> O VemResolver segue normas de emissão de Notas Fiscais e Recibos conforme as autorizações municipais vigentes e regimes especiais de tributação aplicáveis.</p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">9. POLÍTICA DE CANCELAMENTO E VALIDADE</h3>
          <ul class="list-none space-y-2 pl-0">
            <li><strong>a.</strong> As Moedas adquiridas pelo Anunciante não serão devolvidas ou convertidas em moeda corrente em caso de desistência do uso da Plataforma.</li>
            <li><strong>b.</strong> Validade das Moedas: As Moedas adquiridas terão validade de 6 (seis) meses a partir do momento da aquisição. Após esse período, Moedas vencidas serão expiradas. O VemResolver notificará o usuário via e-mail ou push antes do vencimento.</li>
            <li><strong>c.</strong> O Anunciante poderá exercer o direito de arrependimento em até 7 (sete) dias após a compra, desde que não tenha utilizado as Moedas adquiridas. Em caso de uso parcial, o reembolso não será permitido.</li>
          </ul>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">10. SERVIÇOS E ANÚNCIOS PROIBIDOS</h3>
          <ul class="list-none space-y-2 pl-0">
            <li><strong>a.</strong> É proibida a veiculação de anúncios de venda/troca de bens móveis ou imóveis, bem como serviços ilegais ou ofensivos.</li>
            <li><strong>b.</strong> O VemResolver não realiza curadoria prévia, mas removerá conteúdos que violem a lei mediante denúncia.</li>
          </ul>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">11. SISTEMA DE QUALIFICAÇÃO E MODERAÇÃO</h3>
          <ul class="list-none space-y-2 pl-0">
            <li><strong>a.</strong> Os Anunciantes aceitam o sistema de qualificação da Plataforma.</li>
            <li><strong>b.</strong> Moderação de Conteúdo: Embora os comentários sejam opiniões pessoais dos Usuários, o VemResolver reserva-se o direito de remover, unilateralmente e a seu exclusivo critério, comentários que contenham calúnia, injúria, difamação, racismo, pornografia ou violação manifesta de direitos autorais e de imagem.</li>
            <li><strong>c.</strong> O Anunciante isenta o VemResolver de responsabilidade por opiniões publicadas, exceto nos casos em que a Plataforma, após notificada sobre conteúdo ilícito, não tome as devidas providências.</li>
          </ul>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">12. OBRIGAÇÕES DOS USUÁRIOS</h3>
          <p><strong>a.</strong> A responsabilidade por obrigações fiscais, trabalhistas e consumeristas decorrentes da prestação do serviço é exclusivamente dos Usuários (Anunciante e Contratante).</p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">13. PRÁTICAS VEDADAS</h3>
          <p><strong>a.</strong> É proibido manipular preços, usar robôs para baixar dados da plataforma ou incluir contatos pessoais em descrições públicas de ofertas antes da liberação via Moedas.</p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">14. VIOLAÇÃO NO SISTEMA</h3>
          <p><strong>a.</strong> Atividades que interfiram na operação da plataforma sujeitarão o responsável a sanções legais e indenização por danos causados.</p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">15. SANÇÕES</h3>
          <p><strong>a.</strong> O VemResolver poderá advertir, suspender ou cancelar cadastros que descumpram estes Termos. Em casos de fraude comprovada, o saldo de Moedas não será devolvido.</p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">16. RESPONSABILIDADES E LIMITAÇÃO</h3>
          <ul class="list-none space-y-2 pl-0">
            <li><strong>a.</strong> O VemResolver é um classificado online e não garante a qualidade ou entrega dos serviços prestados pelos Anunciantes.</li>
            <li><strong>b.</strong> Limitação de Responsabilidade: Em nenhuma hipótese a responsabilidade total da VemResolver perante o usuário excederá o valor total pago por este à plataforma nos últimos 12 (doze) meses.</li>
          </ul>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">17. ALCANCE DOS SERVIÇOS</h3>
          <p><strong>a.</strong> Estes Termos não geram contrato de sociedade, franquia ou relação de trabalho entre a VemResolver e o Usuário.</p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">18. PROBLEMAS DECORRENTES DO USO DO SISTEMA</h3>
          <p><strong>a.</strong> A Plataforma não se responsabiliza por falhas de conexão, vírus ou malwares no equipamento do Usuário.</p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">19. PROPRIEDADE INTELECTUAL</h3>
          <p><strong>a.</strong> A marca "VemResolver", logos e códigos-fonte são propriedade exclusiva da empresa.</p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">20. INDENIZAÇÃO</h3>
          <p><strong>a.</strong> O Usuário indenizará a VemResolver por demandas decorrentes de suas atividades ilícitas ou descumprimento destes Termos.</p>

          <h3 class="text-2xl font-bold text-gray-900 mt-10 mb-4">21. LEGISLAÇÃO APLICÁVEL E FORO</h3>
          <p><strong>a.</strong> Este Termo é regido pelas leis da República Federativa do Brasil. Fica eleito o Foro da Comarca de Recife para dirimir quaisquer controvérsias.</p>
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
export class TermosUsoComponent {}
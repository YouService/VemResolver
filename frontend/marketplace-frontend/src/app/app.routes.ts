import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/login.component';
import { ClienteDashboardComponent } from './components/cliente-dashboard.component';
import { ClienteEditarSolicitacaoComponent } from './components/cliente-editar-solicitacao.component';
import { ClienteNovaSolicitacaoComponent } from './components/cliente-nova-solicitacao.component';
import { ClientePerfilComponent } from './components/cliente-perfil.component';
import { NotificationDemoComponent } from './components/notification-demo/notification-demo.component';
import { PoliticaPrivacidadeComponent } from './components/politica-privacidade.component';
import { PrestadorDashboardComponent } from './components/prestador-dashboard.component';
import { PrestadorEnviarPropostaComponent } from './components/prestador-enviar-proposta.component';
import { PrestadorPerfilComponent } from './components/prestador-perfil/prestador-perfil.component';
import { ClienteAvaliacaoComponent } from './components/cliente-avaliacao.component';
import { ClientePropostasRecebidasComponent } from './components/cliente-propostas-recebidas.component';
import { RegisterComponent } from './components/register/register.component';
import { SegurancaComponent } from './components/seguranca.component';
import { SejaProfissionalComponent } from './components/seja-profissional.component';
import { SobreNosComponent } from './components/sobre-nos.component';
import { TermosUsoComponent } from './components/termos-uso.component';
import { VerifyAccountComponent } from './components/verify-account.component';
import { clienteGuard, prestadorGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'verificar-conta', component: VerifyAccountComponent },
  { path: 'termos-uso', component: TermosUsoComponent },
  { path: 'politica-privacidade', component: PoliticaPrivacidadeComponent },
  { path: 'seja-um-profissional', component: SejaProfissionalComponent },
  { path: 'seguranca', component: SegurancaComponent },
  { path: 'sobre-nos', component: SobreNosComponent },
  {
    path: 'cliente/dashboard',
    component: ClienteDashboardComponent,
    canActivate: [clienteGuard]
  },
  {
    path: 'cliente/nova-solicitacao',
    component: ClienteNovaSolicitacaoComponent,
    canActivate: [clienteGuard]
  },
  {
    path: 'cliente/editar-solicitacao/:id',
    component: ClienteEditarSolicitacaoComponent,
    canActivate: [clienteGuard]
  },
  {
    path: 'cliente/perfil',
    component: ClientePerfilComponent,
    canActivate: [clienteGuard]
  },
  {
    path: 'prestador/dashboard',
    component: PrestadorDashboardComponent,
    canActivate: [prestadorGuard]
  },
  {
    path: 'prestador/perfil',
    component: PrestadorPerfilComponent,
    canActivate: [prestadorGuard]
  },
  {
    path: 'prestador/solicitacao/:id',
    component: PrestadorEnviarPropostaComponent,
    canActivate: [prestadorGuard]
  },
  {
    path: 'cliente/solicitacao/:id/propostas',
    component: ClientePropostasRecebidasComponent,
    canActivate: [clienteGuard]
  },
  {
    path: 'cliente/solicitacao/:id/avaliacao',
    component: ClienteAvaliacaoComponent,
    canActivate: [clienteGuard]
  },
  { path: 'notification-demo', component: NotificationDemoComponent },
  { path: '**', redirectTo: '' }
];

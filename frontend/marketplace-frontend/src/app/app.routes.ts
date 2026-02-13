import { Routes } from '@angular/router';
import { HomeComponent } from './components/home.component';
import { LoginComponent } from './components/login.component';
import { RegisterComponent } from './components/register.component';
import { ClienteDashboardComponent } from './components/cliente-dashboard.component';
import { ClienteNovaSolicitacaoComponent } from './components/cliente-nova-solicitacao.component';
import { ClienteEditarSolicitacaoComponent } from './components/cliente-editar-solicitacao.component';
import { ClientePerfilComponent } from './components/cliente-perfil.component';
import { PrestadorDashboardComponent } from './components/prestador-dashboard.component';
import { PrestadorPerfilComponent } from './components/prestador-perfil/prestador-perfil.component';
import { VerifyAccountComponent } from './components/verify-account.component';
import { clienteGuard, prestadorGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'verificar-conta', component: VerifyAccountComponent },
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
  { path: '**', redirectTo: '' }
];

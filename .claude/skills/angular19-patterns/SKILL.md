---
name: angular18-patterns
description: Ativar quando trabalhar com código Angular no VemResolver — componentes, serviços, rotas, formulários, estado reativo com RxJS, Angular Material ou qualquer arquivo .ts/.html do diretório frontend.
---

# Angular 18 — Padrões VemResolver

## Stack real
- Angular 18.2 + Angular Material 18.2
- RxJS 7.8, Zone.js 0.14, TypeScript 5.5
- SSR habilitado (@angular/ssr)
- Sem Tailwind — usar Angular Material para todos os componentes de UI

## Estrutura de diretórios

```
frontend/src/app/
├── core/
│   ├── auth/           # guards, interceptors, serviço de auth
│   ├── models/         # interfaces TypeScript das entidades
│   └── services/       # serviços HTTP globais
├── features/
│   ├── auth/           # login, cadastro
│   ├── cliente/        # área do cliente
│   ├── profissional/   # área do profissional
│   ├── servicos/       # listagem e criação de serviços
│   └── contratos/      # gestão de contratos
├── shared/
│   ├── components/     # componentes reutilizáveis
│   ├── pipes/          # pipes customizados
│   └── directives/     # diretivas customizadas
└── app.routes.ts       # rotas com lazy loading
```

## Componente standalone — padrão correto

```typescript
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Observable, catchError, of } from 'rxjs';
import { AsyncPipe } from '@angular/common';
import { ServicoService } from '../../core/services/servico.service';
import { Servico } from '../../core/models/servico.model';

@Component({
  selector: 'app-lista-servicos',
  standalone: true,
  imports: [
    CommonModule,
    AsyncPipe,
    MatCardModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div *ngIf="carregando; else conteudo" class="spinner-container">
      <mat-spinner></mat-spinner>
    </div>
    <ng-template #conteudo>
      <div *ngIf="erro" class="erro">{{ erro }}</div>
      <mat-card *ngFor="let servico of servicos$ | async">
        <mat-card-title>{{ servico.titulo }}</mat-card-title>
        <mat-card-content>{{ servico.descricao }}</mat-card-content>
      </mat-card>
    </ng-template>
  `
})
export class ListaServicosComponent implements OnInit {
  private servicoService = inject(ServicoService);

  servicos$!: Observable<Servico[]>;
  carregando = false;
  erro: string | null = null;

  ngOnInit() {
    this.carregando = true;
    this.servicos$ = this.servicoService.listar().pipe(
      catchError(err => {
        this.erro = 'Erro ao carregar serviços.';
        return of([]);
      })
    );
    this.carregando = false;
  }
}
```

## Serviço HTTP — padrão correto

```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Servico } from '../models/servico.model';

@Injectable({ providedIn: 'root' })
export class ServicoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/servicos`;

  listar(): Observable<Servico[]> {
    return this.http.get<Servico[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<Servico> {
    return this.http.get<Servico>(`${this.apiUrl}/${id}`);
  }

  criar(servico: Partial<Servico>): Observable<Servico> {
    return this.http.post<Servico>(this.apiUrl, servico);
  }
}
```

## Rotas com lazy loading

```typescript
// app.routes.ts
import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/servicos', pathMatch: 'full' },
  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: 'servicos',
    loadChildren: () =>
      import('./features/servicos/servicos.routes').then(m => m.SERVICOS_ROUTES)
  },
  {
    path: 'contratos',
    loadChildren: () =>
      import('./features/contratos/contratos.routes').then(m => m.CONTRATOS_ROUTES)
  }
];
```

## Modelos TypeScript

```typescript
export interface Servico {
  id: number;
  titulo: string;
  descricao: string;
  categoria: Categoria;
  cidade: string;
  estado: string;
  status: 'ABERTO' | 'EM_ANDAMENTO' | 'CONCLUIDO' | 'CANCELADO';
  criadoEm: string;
}

export interface Proposta {
  id: number;
  servico: Servico;
  profissional: Profissional;
  valorProposto: number;
  prazoEmDias: number;
  mensagem: string;
  status: 'PENDENTE' | 'ACEITA' | 'RECUSADA' | 'CANCELADA';
}
```

## O que NUNCA fazer

```typescript
// ❌ ERRADO — tipo any
const dados: any = response;

// ❌ ERRADO — NgModule novo
@NgModule({ declarations: [MeuComponent] })
export class MeuModule {}

// ❌ ERRADO — componentes de UI sem Angular Material
<button style="background: blue">Salvar</button>

// ✅ CERTO — Angular Material
<button mat-raised-button color="primary">Salvar</button>

// ✅ CERTO — standalone
@Component({ standalone: true, imports: [MatButtonModule] })
```

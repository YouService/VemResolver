# 📚 Exemplos Práticos de Integração

Aqui estão exemplos de como integrar o NotificationService em seus componentes reais.

---

## 1️⃣ Em um Formulário de Login

```typescript
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NotificationService } from './services/notification.service';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent {
  form: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private notifService: NotificationService,
    private authService: AuthService
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  login() {
    if (this.form.invalid) {
      this.notifService.warning('Por favor, preencha todos os campos corretamente');
      return;
    }

    this.loading = true;

    this.authService.login(this.form.value).subscribe({
      next: (response) => {
        this.notifService.success('Bem-vindo de volta!', 'Login bem-sucedido');
        // Navegar após sucesso
        setTimeout(() => this.router.navigate(['/dashboard']), 1500);
      },
      error: (error) => {
        const msg = error.error?.message || 'Email ou senha incorretos';
        this.notifService.error(msg, 'Falha no Login');
        this.loading = false;
      }
    });
  }
}
```

---

## 2️⃣ Em um CRUD com Confirmação de Delete

```typescript
import { Component, OnInit } from '@angular/core';
import { NotificationService } from './services/notification.service';
import { ItemService } from './services/item.service';

@Component({
  selector: 'app-item-list',
  templateUrl: './item-list.component.html'
})
export class ItemListComponent implements OnInit {
  itens: any[] = [];

  constructor(
    private notifService: NotificationService,
    private itemService: ItemService
  ) {}

  ngOnInit() {
    this.carregarItens();
  }

  carregarItens() {
    this.itemService.listar().subscribe({
      next: (itens) => {
        this.itens = itens;
      },
      error: (error) => {
        this.notifService.error('Erro ao carregar itens', 'Erro');
      }
    });
  }

  deletarItem(id: number, nome: string) {
    // Notificação com confirmação
    this.notifService.error(
      `Tem certeza que deseja deletar "${nome}"? Esta ação não pode ser desfeita.`,
      'Confirmar Exclusão',
      0, // permanente
      'Deletar',
      () => {
        this.itemService.deletar(id).subscribe({
          next: () => {
            this.notifService.success('Item deletado com sucesso!');
            this.carregarItens();
          },
          error: () => {
            this.notifService.error('Erro ao deletar item');
          }
        });
      }
    );
  }

  editarItem(id: number) {
    // Notificação de informação enquanto carrega
    this.notifService.info('Carregando dados do item...', 'Por favor aguarde', 0);

    this.itemService.obter(id).subscribe({
      next: (item) => {
        this.notifService.removeAll();
        // Abrir modal ou navegar para edição
        this.abrirEdicao(item);
      },
      error: () => {
        this.notifService.error('Erro ao carregar item para edição');
      }
    });
  }

  private abrirEdicao(item: any) {
    // Implementar lógica de edição
  }
}
```

---

## 3️⃣ Em um Upload de Arquivo

```typescript
import { Component } from '@angular/core';
import { NotificationService } from './services/notification.service';
import { FileService } from './services/file.service';

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html'
})
export class FileUploadComponent {
  uploading = false;

  constructor(
    private notifService: NotificationService,
    private fileService: FileService
  ) {}

  onFileSelected(event: any) {
    const file = event.target.files[0];

    if (!file) return;

    // Validar tamanho
    if (file.size > 5 * 1024 * 1024) { // 5MB
      this.notifService.warning(
        'Arquivo muito grande. Máximo de 5MB permitido.',
        'Arquivo Grande'
      );
      return;
    }

    // Validar tipo
    const tiposPermitidos = ['image/jpeg', 'image/png', 'application/pdf'];
    if (!tiposPermitidos.includes(file.type)) {
      this.notifService.warning(
        'Tipo de arquivo não permitido. Use JPG, PNG ou PDF.',
        'Tipo Inválido'
      );
      return;
    }

    this.uploading = true;
    
    // Notificação de progresso
    this.notifService.info(
      'Enviando arquivo...',
      'Upload em Progresso',
      0 // não desaparece até completar
    );

    this.fileService.upload(file).subscribe({
      next: (response) => {
        this.notifService.removeAll();
        this.notifService.success(
          'Arquivo enviado com sucesso!',
          'Upload Completo',
          5000,
          'Visualizar',
          () => {
            window.open(response.url);
          }
        );
        this.uploading = false;
      },
      error: (error) => {
        this.notifService.error(
          'Erro ao enviar arquivo. Tente novamente.',
          'Erro no Upload'
        );
        this.uploading = false;
      }
    });
  }
}
```

---

## 4️⃣ Em um Formulário com Validações Complexas

```typescript
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, AbstractControl, ValidationErrors } from '@angular/forms';
import { NotificationService } from './services/notification.service';
import { UserService } from './services/user.service';

@Component({
  selector: 'app-user-form',
  templateUrl: './user-form.component.html'
})
export class UserFormComponent {
  form: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private notifService: NotificationService,
    private userService: UserService
  ) {
    this.form = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmarPassword: ['', Validators.required],
      termos: [false, Validators.requiredTrue]
    }, {
      validators: this.passwordsMatch('password', 'confirmarPassword')
    });
  }

  private passwordsMatch(passwordField: string, confirmField: string) {
    return (control: AbstractControl): ValidationErrors | null => {
      const pwd = control.get(passwordField)?.value;
      const confirm = control.get(confirmField)?.value;
      return pwd === confirm ? null : { passwordMismatch: true };
    };
  }

  validarCampo(fieldName: string) {
    const field = this.form.get(fieldName);

    if (!field) return;

    if (field.invalid && field.touched) {
      // Diferentes mensagens por tipo de erro
      if (fieldName === 'nome' && field.hasError('minlength')) {
        this.notifService.warning('Nome deve ter pelo menos 3 caracteres', 'Campo Inválido');
      } else if (fieldName === 'email' && field.hasError('email')) {
        this.notifService.warning('Email inválido. Use um email válido', 'Email Inválido');
      } else if (fieldName === 'password' && field.hasError('minlength')) {
        this.notifService.warning('Senha deve ter pelo menos 8 caracteres', 'Senha Fraca');
      }
    }
  }

  submit() {
    if (this.form.invalid) {
      this.notifService.warning(
        'Por favor, revise os erros no formulário',
        'Formulário Inválido'
      );
      return;
    }

    if (!this.form.get('termos')?.value) {
      this.notifService.warning(
        'Você precisa aceitar os termos de uso',
        'Termos Não Aceitos'
      );
      return;
    }

    this.loading = true;

    this.userService.criar(this.form.value).subscribe({
      next: () => {
        this.notifService.success(
          'Conta criada com sucesso! Verifique seu email para ativar.',
          'Conta Criada'
        );
        this.form.reset();
        // Redirecionar após sucesso
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (error) => {
        const msg = error.error?.message || 'Erro ao criar conta';
        this.notifService.error(msg, 'Erro');
        this.loading = false;
      }
    });
  }
}
```

---

## 5️⃣ Com HttpClient e Interceptor de Erro

```typescript
import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { NotificationService } from './notification.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private notifService: NotificationService) {}

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        let mensagem = 'Erro desconhecido';

        switch (error.status) {
          case 400:
            mensagem = error.error?.message || 'Dados inválidos';
            this.notifService.warning(mensagem, 'Validação');
            break;
          case 401:
            mensagem = 'Sua sessão expirou. Faça login novamente';
            this.notifService.error(mensagem, 'Não Autenticado');
            // Redirecionar para login
            break;
          case 403:
            mensagem = 'Você não tem permissão para esta ação';
            this.notifService.error(mensagem, 'Acesso Negado');
            break;
          case 404:
            mensagem = 'Recurso não encontrado';
            this.notifService.info(mensagem, 'Não Encontrado');
            break;
          case 409:
            mensagem = error.error?.message || 'Conflito nos dados';
            this.notifService.warning(mensagem, 'Conflito');
            break;
          case 500:
            mensagem = 'Erro no servidor. Tente novamente mais tarde';
            this.notifService.error(mensagem, 'Erro do Servidor');
            break;
          case 503:
            mensagem = 'Serviço indisponível. Tente novamente em breve';
            this.notifService.error(mensagem, 'Serviço Indisponível');
            break;
          default:
            if (error.status >= 500) {
              mensagem = 'Erro no servidor';
              this.notifService.error(mensagem, 'Erro');
            } else if (error.status >= 400) {
              mensagem = 'Erro na requisição';
              this.notifService.warning(mensagem, 'Erro');
            } else if (error.status === 0) {
              mensagem = 'Erro de conexão. Verifique sua internet';
              this.notifService.error(mensagem, 'Sem Conexão');
            }
        }

        return throwError(() => error);
      })
    );
  }
}
```

---

## 6️⃣ Notificações com Retry (Tentar Novamente)

```typescript
import { Component } from '@angular/core';
import { NotificationService } from './services/notification.service';
import { DataService } from './services/data.service';

@Component({
  selector: 'app-data-loader',
  templateUrl: './data-loader.component.html'
})
export class DataLoaderComponent {
  dados: any[] = [];
  loading = false;
  tentativas = 0;

  constructor(
    private notifService: NotificationService,
    private dataService: DataService
  ) {}

  carregarDados() {
    this.loading = true;
    this.tentativas = 0;
    this.tentar();
  }

  private tentar() {
    this.dataService.obter().subscribe({
      next: (dados) => {
        this.dados = dados;
        this.loading = false;
        this.notifService.success('Dados carregados com sucesso!');
      },
      error: (error) => {
        this.tentativas++;

        if (this.tentativas < 3) {
          this.notifService.error(
            `Erro na tentativa ${this.tentativas}. Tentando novamente...`,
            'Erro de Conexão',
            0,
            'Tentar Agora',
            () => this.tentar()
          );

          // Esperar 2 segundos e tentar novamente
          setTimeout(() => this.tentar(), 2000);
        } else {
          this.notifService.error(
            'Não foi possível carregar os dados após 3 tentativas. Verifique sua conexão.',
            'Falha de Carregamento',
            0,
            'Tentar Novamente',
            () => this.carregarDados()
          );
          this.loading = false;
        }
      }
    });
  }
}
```

---

## 7️⃣ Notificações em Tempo Real

```typescript
import { Component, OnInit } from '@angular/core';
import { NotificationService } from './services/notification.service';
import { WebSocketService } from './services/websocket.service';

@Component({
  selector: 'app-notifications-real-time',
  templateUrl: './notifications-real-time.component.html'
})
export class NotificationsRealTimeComponent implements OnInit {
  constructor(
    private notifService: NotificationService,
    private wsService: WebSocketService
  ) {}

  ngOnInit() {
    this.setupRealtimeNotifications();
  }

  private setupRealtimeNotifications() {
    this.wsService.mensagens$.subscribe((mensagem) => {
      switch (mensagem.tipo) {
        case 'novo_pedido':
          this.notifService.success(
            `Novo pedido de ${mensagem.cliente}`,
            'Novo Pedido',
            0,
            'Visualizar',
            () => this.abrirPedido(mensagem.pedidoId)
          );
          break;

        case 'pedido_aceito':
          this.notifService.info(
            `Seu pedido foi aceito por ${mensagem.prestador}`,
            'Pedido Aceito',
            5000
          );
          break;

        case 'aviso_importante':
          this.notifService.warning(
            mensagem.conteudo,
            'Aviso',
            0
          );
          break;

        case 'erro_sistema':
          this.notifService.error(
            mensagem.conteudo,
            'Erro do Sistema',
            0,
            'Reportar',
            () => this.reportarErro(mensagem.erroId)
          );
          break;
      }
    });
  }

  private abrirPedido(pedidoId: string) {
    // Implementar
  }

  private reportarErro(erroId: string) {
    // Implementar
  }
}
```

---

## 💡 Dicas Importantes

1. **Use `0` para tempo infinito** quando precisar que o usuário leia a mensagem
2. **Adicione botões de ação** para notificações importantes
3. **Use `removeAll()` antes de carregar** para limpar notificações antigas
4. **Combine com loading spinners** para melhor UX
5. **Personalize mensagens de erro** do backend

---

## 🎯 Padrões Recomendados

| Cenário | Tipo | Duração | Botão |
|---------|------|---------|-------|
| Login bem-sucedido | Success | 3000ms | Não |
| Erro de validação | Warning | 5000ms | Não |
| Confirmação crítica | Error | 0 | Sim |
| Carregamento | Info | 0 | Não |
| Sucesso genérico | Success | 4000ms | Não |
| Erro crítico | Error | 0 | Sim (Retry) |

Pronto! Use estes exemplos como referência para integrar em seus componentes! 🚀

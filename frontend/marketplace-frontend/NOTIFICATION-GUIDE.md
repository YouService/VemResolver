# Sistema de Notificações Personalizado

Bem-vindo ao novo sistema de notificações Angular! Um componente moderno, profissional e totalmente customizável usando TailwindCSS.

## 📋 Características

✅ **4 Tipos de Notificações**: Success (verde), Error (vermelho), Warning (amarelo), Info (azul)
✅ **Animações Suaves**: Slide-in/out com efeito visual
✅ **Auto-dismissal**: Fechamento automático após tempo configurável
✅ **Ación Personalizadas**: Botões de ação com callbacks opcionais
✅ **Barra de Progresso**: Indicação visual do tempo de auto-dismiss
✅ **Responsive**: Adapta-se a diferentes tamanhos de tela
✅ **TailwindCSS**: Estilizado completamente com Tailwind
✅ **Material Icons**: Usa ícones do Material Design

## 🚀 Como Usar

### 1. Injetar o NotificationService

```typescript
import { NotificationService } from './services/notification.service';

export class MinhaComponent {
  constructor(private notificationService: NotificationService) {}
}
```

### 2. Usar os Tipos de Notificação

#### Sucesso (Verde)
```typescript
this.notificationService.success(
  'Cadastro realizado com sucesso!',
  'Sucesso', // título (opcional)
  5000 // duração em ms (opcional)
);
```

#### Erro (Vermelho)
```typescript
this.notificationService.error(
  'Erro ao salvar os dados',
  'Erro!',
  5000
);
```

#### Aviso (Amarelo)
```typescript
this.notificationService.warning(
  'Esta ação não pode ser desfeita',
  'Atenção!'
);
```

#### Informação (Azul)
```typescript
this.notificationService.info(
  'Suas alterações foram salvas automaticamente',
  'Informação'
);
```

### 3. Notificações com Botão de Ação

```typescript
this.notificationService.success(
  'Arquivo carregado com sucesso!',
  'Upload',
  0, // 0 = não desaparece automaticamente
  'Visualizar', // texto do botão
  () => {
    // Código executado quando clicar no botão
    console.log('Botão clicado!');
    this.abrirArquivo();
  }
);
```

### 4. Remover Notificações

```typescript
// Remover uma notificação específica
const id = this.notificationService.success('Mensagem');
this.notificationService.remove(id);

// Remover todas as notificações
this.notificationService.removeAll();

// Remover por tipo
this.notificationService.removeByType('error');
```

### 5. Usar o Método Genérico

```typescript
this.notificationService.show(
  'Mensagem personalizada',
  'success', // tipo: 'success' | 'error' | 'warning' | 'info'
  5000, // duração
  'Título', // título opcional
  'Ação', // botão opcional
  () => { /* callback */ } // ação ao clicar
);
```

## 📍 Exemplo Completo

```typescript
import { Component } from '@angular/core';
import { NotificationService } from './services/notification.service';

@Component({
  selector: 'app-exemplo',
  template: `
    <div class="p-8 space-y-4">
      <button 
        (click)="testarSucesso()"
        class="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 transition"
      >
        Sucesso
      </button>

      <button 
        (click)="testarErro()"
        class="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700 transition"
      >
        Erro
      </button>

      <button 
        (click)="testarComAcao()"
        class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition"
      >
        Com Ação
      </button>

      <button 
        (click)="testarPermanente()"
        class="bg-amber-600 text-white px-4 py-2 rounded hover:bg-amber-700 transition"
      >
        Permanente (0 segundos)
      </button>
    </div>
  `,
  standalone: true
})
export class ExemploComponent {
  constructor(private notificationService: NotificationService) {}

  testarSucesso(): void {
    this.notificationService.success(
      'Operação concluída com êxito!',
      'Sucesso'
    );
  }

  testarErro(): void {
    this.notificationService.error(
      'Ocorreu um problema ao processar a solicitação',
      'Erro'
    );
  }

  testarComAcao(): void {
    this.notificationService.warning(
      'Você deseja realmente deletar este item?',
      'Confirmação',
      0, // não desaparece automaticamente
      'Deletar',
      () => {
        console.log('Item deletado!');
        this.notificationService.success('Item deletado com sucesso!');
      }
    );
  }

  testarPermanente(): void {
    this.notificationService.info(
      'Esta mensagem permanecerá até você fechar',
      'Informação Importante',
      0 // 0 = infinito
    );
  }
}
```

## 🎨 Customização de Cores

Todas as cores são definidas usando as paletas de cores TailwindCSS:

- **Success**: `emerald-*` (verde)
- **Error**: `red-*` (vermelho)
- **Warning**: `amber-*` (amarelo/âmbar)
- **Info**: `blue-*` (azul)

Para modificar as cores, edite o arquivo `notification.component.ts` nos métodos que retornam as classes CSS (getNotificationClasses, etc).

## 📦 Estrutura de Arquivos

```
src/app/
├── models/
│   └── notification.model.ts (interfaces)
├── services/
│   └── notification.service.ts (lógica principal)
└── components/
    ├── notification/
    │   └── notification.component.ts (componente individual)
    └── notification-container/
        └── notification-container.component.ts (container)
```

## ⚙️ Configuração

O NotificationContainerComponent já está integrado no `app.component.ts`, então basta usar o serviço em qualquer componente.

Se precisar de múltiplos containers em diferentes posições, você pode:

1. Adicionar múltiplos elementos `<app-notification-container>` em diferentes UIs
2. Modificar o CSS para posicionar em: `top-left`, `bottom-right`, etc

## 🔄 Migração do SnackService

Se havia código usando `SnackService`, atualize assim:

**Antes:**
```typescript
this.snackService.open('Mensagem de sucesso', 'Título', 'success', 5000);
```

**Depois:**
```typescript
this.notificationService.success('Mensagem de sucesso', 'Título', 5000);
```

## 📝 Notas Importantes

- O container está fixado no `top-right` (canto superior direito)
- As notificações aparecem com animação suave
- Múltiplas notificações se empilham verticalmente
- O z-index é 9999 para garantir que fique acima de outros elementos
- Cada notificação tem um ID único para controle individual

Aproveite seu novo sistema de notificações! 🎉

# 🚀 Guia Rápido - Sistema de Notificações

## ⚡ 30 Segundos para Começar

### 1. Injete o serviço
```typescript
constructor(private notifService: NotificationService) {}
```

### 2. Use em seu código
```typescript
// Sucesso
this.notifService.success('Salvo com sucesso!');

// Erro
this.notifService.error('Algo deu errado');

// Aviso
this.notifService.warning('Cuidado!');

// Info
this.notifService.info('Para sua informação...');
```

**Pronto!** ✅ Suas notificações estarão funcionando.

---

## 📋 Referência Rápida

### Métodos Básicos

```typescript
// syntax: notifService.TYPE(message, title?, duration?, actionText?, actionCallback?)

success(msg)                    // Verde, 5s
error(msg)                      // Vermelho, 5s
warning(msg)                    // Amarelo, 5s
info(msg)                       // Azul, 5s

success(msg, 'Título')          // Com título
success(msg, 'Título', 3000)    // Duração custom (ms)
success(msg, 'Título', 0)       // Permanente (clique para fechar)
```

### Com Botão de Ação

```typescript
notifService.success(
  'Arquivo pronto',
  'Download',
  0,                                  // Permanente
  'Baixar',                            // Texto do botão
  () => console.log('Clicou!')       // Callback
);
```

### Remover Notificações

```typescript
const id = notifService.success('Msg');
notifService.remove(id);              // Remove uma específica
notifService.removeAll();             // Remove todas
notifService.removeByType('error');   // Remove por tipo
```

---

## 🎨 Tipos e Cores

| Tipo | Usando | Cor |
|------|--------|-----|
| **Success** | `notifService.success()` | 🟢 Verde |
| **Error** | `notifService.error()` | 🔴 Vermelho |
| **Warning** | `notifService.warning()` | 🟡 Amarelo |
| **Info** | `notifService.info()` | 🔵 Azul |

---

## 🧪 Testar Agora

1. Vá para: `http://localhost:4200/notification-demo`
2. Clique nos botões para ver exemplos
3. Experimente diferentes tipos

---

## 📍 Exemplos em Contexto

### Após Salvamento
```typescript
save() {
  this.service.save(this.data).subscribe({
    next: () => this.notifService.success('Salvo!'),
    error: () => this.notifService.error('Erro ao salvar')
  });
}
```

### Confirmação de Delete
```typescript
delete(id: number) {
  this.notifService.error(
    'Deseja realmente deletar?',
    'Confirmação',
    0,
    'Deletar',
    () => this.service.delete(id).subscribe({
      next: () => this.notifService.success('Deletado!')
    })
  );
}
```

### Com Loading
```typescript
load() {
  this.notifService.info('Carregando...', 'Por favor aguarde', 0);
  
  this.service.fetch().subscribe({
    next: (data) => {
      this.notifService.removeAll();
      this.data = data;
    },
    error: () => this.notifService.error('Erro ao carregar')
  });
}
```

### Upload com Retry
```typescript
upload(file: File) {
  this.notifService.info('Enviando...', 'Upload', 0);
  
  this.service.upload(file).subscribe({
    next: () => this.notifService.success('Upload completo!'),
    error: () => this.notifService.error(
      'Erro no upload',
      'Falha',
      0,
      'Tentar Novamente',
      () => this.upload(file)
    )
  });
}
```

---

## ⚙️ Onde Está o Sistema?

```
src/app/
├── models/notification.model.ts          ← Tipos
├── services/notification.service.ts      ← Serviço Principal
└── components/
    ├── notification/                     ← Componente Individual
    ├── notification-container/           ← Gerenciador
    └── notification-demo/                ← Exemplos
```

---

## 🔧 Integração no seu Projeto

✅ **Já Integrado:**
- App component (vai aparecer no canto superior direito)
- Register component (já usando)
- Rotas carregadas

✅ **Para Usar em seu Componente:**
```typescript
import { NotificationService } from '@app/services/notification.service';

constructor(private notifService: NotificationService) {}

// ... use conforme acima
```

---

## 🎯 Durações Comuns

| Caso | Millisegundos | Código |
|------|---------------|--------|
| Rápida | 2000 | `notifService.success(msg, undefined, 2000)` |
| Normal | 5000 | `notifService.success(msg)` ← Padrão |
| Longa | 10000 | `notifService.success(msg, undefined, 10000)` |
| Permanente | 0 | `notifService.success(msg, undefined, 0)` |

---

## ❓ FAQ Rápido

**P: Como mudo de cor?**  
A: Não precisa! As cores são automáticas por tipo (success=verde, error=vermelho, etc)

**P: Posso ter vários alerts ao mesmo tempo?**  
A: Sim! Eles se empilham automaticamente

**P: Como faço para posicionar em outro lugar?**  
A: Edite o CSS da `notification-container.component.ts` (está em `top-right`)

**P: Preciso remover SnackBar?**  
A: Sim, delete a pasta `global-snack-bar` e `snack.service.ts` quando terminar a migração

**P: Funciona mobile?**  
A: Sim! É totalmente responsivo

---

## 🚨 Checklist de Integração

- [ ] Testei a página de demo (`/notification-demo`)
- [ ] Injetet o serviço em meu componente
- [ ] Chamei um método de sucesso/erro
- [ ] Vi a notificação aparecer no canto superior direito
- [ ] Testei remover a notificação
- [ ] Adicionei um botão de ação (opcional)

---

## 📚 Leia Também

- [NOTIFICATION-GUIDE.md](./NOTIFICATION-GUIDE.md) - Documentação completa
- [NOTIFICATION-EXEMPLOS.md](./NOTIFICATION-EXEMPLOS.md) - Exemplos de integração
- [NOTIFICATION-SETUP-COMPLETE.md](./NOTIFICATION-SETUP-COMPLETE.md) - Detalhes técnicos

---

## 💬 Sintaxe Mais Comum

```typescript
// ✅ Você vai usar MUITO isso:
this.notifService.success('Salvo!');
this.notifService.error('Erro ao salvar');
this.notifService.warning('Confirma exclusão?', 'Atenção', 0, 'Sim', () => deletar());

// ✅ Algumas vezes:
this.notifService.info('Carregando...', 'Aguarde', 0);

// ✅ Raramente:
const id = this.notifService.show(msg, 'error', 5000);
this.notifService.removeByType('error');
```

---

🎉 **Você está pronto para usar o novo sistema!**

Qualquer dúvida, consulte os arquivos de documentação ou veja a demo em `/notification-demo`

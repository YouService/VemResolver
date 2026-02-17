import { Directive, HostListener, ElementRef, Input } from '@angular/core';

@Directive({
  selector: '[appMask]',
  standalone: true
})
export class MaskDirective {
  @Input('appMask') maskPattern: string = '';

  constructor(private el: ElementRef) {}

  @HostListener('input', ['$event'])
  onInput(event: InputEvent): void {
    const input = this.el.nativeElement as HTMLInputElement;
    const originalValue = input.value;
    
    // 1. Remove tudo que não é dígito
    let value = originalValue.replace(/\D/g, '');

    // 2. Lógica da Máscara
    let formatted = '';
    let index = 0;

    for (let i = 0; i < this.maskPattern.length && index < value.length; i++) {
      if (this.maskPattern[i] === '0') {
        formatted += value[index];
        index++;
      } else {
        formatted += this.maskPattern[i];
      }
    }

    // 3. A TRAVA (Evita Recursão): 
    // Só atualizamos e disparamos o evento se o valor formatado for DIFERENTE 
    // do que já está no input.
    if (originalValue !== formatted) {
      input.value = formatted;
      
      // Dispara o evento apenas para notificar o Angular (ngModel/FormControl)
      // Como o valor agora é igual ao 'formatted', na próxima execução 
      // ele cairá no 'else' e não disparará novamente.
      input.dispatchEvent(new Event('input', { bubbles: true }));
    }
  }
}
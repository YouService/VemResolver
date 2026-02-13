import { Directive, HostListener, ElementRef, Input } from '@angular/core';

@Directive({
  selector: '[appMask]',
  standalone: true
})
export class MaskDirective {
  @Input('appMask') maskPattern!: string;

  constructor(private el: ElementRef) {}

  @HostListener('input', ['$event'])
  onInput(event: Event): void {
    const input = this.el.nativeElement as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');

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

    input.value = formatted;
    input.dispatchEvent(new Event('input'));
  }
}

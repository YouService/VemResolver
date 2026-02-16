import { Directive, HostListener } from '@angular/core';
import { NgControl } from '@angular/forms';

@Directive({
  selector: '[appUppercase]',
  standalone: true
})
export class UppercaseDirective {
  constructor(private ngControl: NgControl) {}

  @HostListener('input', ['$event'])
  onInput(event: any): void {
    const input = event.target as HTMLInputElement;
    if (input && this.ngControl.control) {
      const upperValue = input.value.toUpperCase();
      this.ngControl.control.setValue(upperValue, { emitEvent: false });
      input.value = upperValue;
    }
  }
}

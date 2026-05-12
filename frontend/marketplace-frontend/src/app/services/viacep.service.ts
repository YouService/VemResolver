import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface EnderecoViaCEP {
  cep?: string;
  logradouro?: string;
  complemento?: string;
  bairro?: string;
  localidade?: string;
  uf?: string;
  ibge?: string;
  gia?: string;
  ddd?: string;
  siafi?: string;
  erro?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ViaCEPService {
  private apiUrl = 'https://viacep.com.br/ws';

  constructor(private http: HttpClient) {}

  buscarCEP(cep: string): Observable<EnderecoViaCEP> {
    const cepLimpo = cep.replace(/\D/g, '');
    return this.http.get<EnderecoViaCEP>(`${this.apiUrl}/${cepLimpo}/json/`);
  }
}

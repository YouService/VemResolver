import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MensagemChat } from '../models/models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private apiUrl = `${environment.apiUrl}/chat`;

  constructor(private http: HttpClient) {}

  listar(propostaId: number): Observable<MensagemChat[]> {
    return this.http.get<MensagemChat[]>(`${this.apiUrl}/proposta/${propostaId}`);
  }

  enviar(propostaId: number, texto: string): Observable<MensagemChat> {
    return this.http.post<MensagemChat>(`${this.apiUrl}/proposta/${propostaId}`, { texto });
  }

  marcarComoLidas(propostaId: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/proposta/${propostaId}/ler`, null);
  }
}

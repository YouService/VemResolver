import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MensagemChat, PageResponse } from '../models/models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private apiUrl = `${environment.apiUrl}/chat`;

  constructor(private http: HttpClient) {}

  listar(propostaId: number): Observable<MensagemChat[]> {
    return this.http.get<MensagemChat[]>(`${this.apiUrl}/proposta/${propostaId}`);
  }

  listarPaginado(propostaId: number, page: number = 0, size: number = 20): Observable<PageResponse<MensagemChat>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<MensagemChat>>(`${this.apiUrl}/proposta/${propostaId}/paginado`, { params });
  }

  enviar(propostaId: number, texto: string): Observable<MensagemChat> {
    return this.http.post<MensagemChat>(`${this.apiUrl}/proposta/${propostaId}`, { texto });
  }

  marcarComoLidas(propostaId: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/proposta/${propostaId}/ler`, null);
  }
}

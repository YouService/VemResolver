import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Avaliacao, PageResponse } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class AvaliacaoService {
  private apiUrl = 'http://localhost:8080/api/avaliacoes';

  constructor(private http: HttpClient) {}

  criar(avaliacao: Avaliacao): Observable<Avaliacao> {
    return this.http.post<Avaliacao>(this.apiUrl, avaliacao);
  }

  listarPorPrestador(prestadorId: number, page: number = 0, size: number = 10): Observable<PageResponse<Avaliacao>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<Avaliacao>>(`${this.apiUrl}/prestador/${prestadorId}`, { params });
  }
}

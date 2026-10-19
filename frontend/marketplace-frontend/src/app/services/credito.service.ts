import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CompraCredito, PacoteCredito, ResumoCreditos } from '../models/models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CreditoService {
  private apiUrl = `${environment.apiUrl}/prestadores/me/creditos`;

  constructor(private http: HttpClient) {}

  resumo(page = 0, size = 10): Observable<ResumoCreditos> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<ResumoCreditos>(this.apiUrl, { params });
  }

  pacotes(): Observable<PacoteCredito[]> {
    return this.http.get<PacoteCredito[]>(`${this.apiUrl}/pacotes`);
  }

  comprar(pacoteId: number): Observable<CompraCredito> {
    return this.http.post<CompraCredito>(`${this.apiUrl}/comprar`, { pacoteId });
  }
}

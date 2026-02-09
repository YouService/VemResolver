import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SolicitacaoServico, PageResponse, StatusSolicitacao } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class SolicitacaoService {
  private apiUrl = 'http://localhost:8080/api/solicitacoes';

  constructor(private http: HttpClient) {}

  criar(solicitacao: SolicitacaoServico): Observable<SolicitacaoServico> {
    return this.http.post<SolicitacaoServico>(this.apiUrl, solicitacao);
  }

  buscarPorId(id: number): Observable<SolicitacaoServico> {
    return this.http.get<SolicitacaoServico>(`${this.apiUrl}/${id}`);
  }

  listarMinhas(page: number = 0, size: number = 10): Observable<PageResponse<SolicitacaoServico>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<SolicitacaoServico>>(`${this.apiUrl}/minhas`, { params });
  }

  buscarAbertas(
    categoriaId?: number,
    cidade?: string,
    estado?: string,
    page: number = 0,
    size: number = 10
  ): Observable<PageResponse<SolicitacaoServico>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (categoriaId) params = params.set('categoriaId', categoriaId.toString());
    if (cidade) params = params.set('cidade', cidade);
    if (estado) params = params.set('estado', estado);

    return this.http.get<PageResponse<SolicitacaoServico>>(`${this.apiUrl}/abertas`, { params });
  }

  atualizarStatus(id: number, status: StatusSolicitacao): Observable<SolicitacaoServico> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<SolicitacaoServico>(`${this.apiUrl}/${id}/status`, null, { params });
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

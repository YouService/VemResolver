import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse, StatusVerificacao, VerificacaoDocumentoDTO } from '../models/models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class VerificacaoService {
  private readonly prestadorUrl = `${environment.apiUrl}/prestadores/me/verificacao`;
  private readonly adminUrl = `${environment.apiUrl}/admin/verificacoes`;

  constructor(private http: HttpClient) {}

  consultarStatus(): Observable<VerificacaoDocumentoDTO> {
    return this.http.get<VerificacaoDocumentoDTO>(this.prestadorUrl);
  }

  enviarDocumentos(documento: File, selfie: File): Observable<VerificacaoDocumentoDTO> {
    const formData = new FormData();
    formData.append('documento', documento);
    formData.append('selfie', selfie);
    return this.http.post<VerificacaoDocumentoDTO>(this.prestadorUrl, formData);
  }

  listar(status?: StatusVerificacao, page = 0, size = 15): Observable<PageResponse<VerificacaoDocumentoDTO>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http.get<PageResponse<VerificacaoDocumentoDTO>>(this.adminUrl, { params });
  }

  aprovar(id: number, observacao?: string): Observable<VerificacaoDocumentoDTO> {
    return this.http.patch<VerificacaoDocumentoDTO>(`${this.adminUrl}/${id}/aprovar`, { observacao });
  }

  recusar(id: number, observacao?: string): Observable<VerificacaoDocumentoDTO> {
    return this.http.patch<VerificacaoDocumentoDTO>(`${this.adminUrl}/${id}/recusar`, { observacao });
  }

  urlDocumento(id: number): string {
    return `${environment.apiUrl}/admin/verificacoes/${id}/documento`;
  }

  urlSelfie(id: number): string {
    return `${environment.apiUrl}/admin/verificacoes/${id}/selfie`;
  }
}

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CriarDenunciaRequest, DenunciaDTO, PageResponse, StatusDenuncia } from '../models/models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class DenunciaService {
  private readonly url = `${environment.apiUrl}/denuncias`;
  private readonly adminUrl = `${environment.apiUrl}/admin/denuncias`;

  constructor(private http: HttpClient) {}

  criar(dto: CriarDenunciaRequest): Observable<DenunciaDTO> {
    return this.http.post<DenunciaDTO>(this.url, dto);
  }

  listar(status?: StatusDenuncia, page = 0, size = 15): Observable<PageResponse<DenunciaDTO>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http.get<PageResponse<DenunciaDTO>>(this.adminUrl, { params });
  }

  atualizarStatus(id: number, status: StatusDenuncia, observacaoAdmin?: string): Observable<DenunciaDTO> {
    return this.http.patch<DenunciaDTO>(`${this.adminUrl}/${id}/status`, { status, observacaoAdmin });
  }
}

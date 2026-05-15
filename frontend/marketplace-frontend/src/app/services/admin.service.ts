import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AdminStats, UserResumoDTO, PageResponse } from '../models/models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  buscarStats(): Observable<AdminStats> {
    return this.http.get<AdminStats>(`${this.apiUrl}/stats`);
  }

  listarUsuarios(page = 0, size = 20): Observable<PageResponse<UserResumoDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<UserResumoDTO>>(`${this.apiUrl}/usuarios`, { params });
  }

  alterarAtivacao(id: number, ativo: boolean): Observable<void> {
    const params = new HttpParams().set('ativo', ativo.toString());
    return this.http.patch<void>(`${this.apiUrl}/usuarios/${id}/ativar`, null, { params });
  }
}

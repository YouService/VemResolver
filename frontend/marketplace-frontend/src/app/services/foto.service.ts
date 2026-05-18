import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FotoPrestador } from '../models/models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class FotoService {
  private apiUrl = `${environment.apiUrl}/prestadores`;

  constructor(private http: HttpClient) {}

  listar(prestadorId: number): Observable<FotoPrestador[]> {
    return this.http.get<FotoPrestador[]>(`${this.apiUrl}/${prestadorId}/fotos`);
  }

  upload(file: File): Observable<FotoPrestador> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<FotoPrestador>(`${this.apiUrl}/me/fotos`, formData);
  }

  deletar(fotoId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/me/fotos/${fotoId}`);
  }

  urlCompleta(urlRelativa: string): string {
    return `${environment.baseUrl}${urlRelativa}`;
  }
}

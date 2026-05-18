import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { PrestadorService } from '../../services/prestador.service';
import { CategoriaService } from '../../services/categoria.service';
import { AuthService } from '../../services/auth.service';
import { FotoService } from '../../services/foto.service';
import { Prestador, Categoria, User } from '../../models/models';
import { FotosPrestadorComponent } from '../fotos-prestador/fotos-prestador.component';

@Component({
  selector: 'app-prestador-perfil',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, FotosPrestadorComponent],
  templateUrl: './prestador-perfil.component.html',
  styleUrls: ['./prestador-perfil.component.css']
})
export class PrestadorPerfilComponent implements OnInit {
  perfilForm: FormGroup;
  categorias: Categoria[] = [];
  categoriasSelecionadas: number[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';
  categoriasInvalidas = false;
  prestadorExistente?: Prestador;
  fotoCount = 0;
  usandoLocalizacao = false;
  erroLocalizacao = '';

  currentUser?: User;
  telefone = '';

  fotoPerfilUrl?: string;
  uploadingAvatar = false;
  erroAvatar = '';

  constructor(
    private fb: FormBuilder,
    private prestadorService: PrestadorService,
    private categoriaService: CategoriaService,
    private authService: AuthService,
    private fotoService: FotoService,
    private router: Router
  ) {
    this.perfilForm = this.fb.group({
      descricaoPerfil: ['', [Validators.required, Validators.minLength(50)]],
      cidade: ['', Validators.required],
      estado: ['', Validators.required],
      latitude: [null],
      longitude: [null]
    });
  }

  ngOnInit() {
    this.carregarCategorias();
    this.carregarPerfilExistente();
    this.carregarDadosUsuario();
  }

  carregarCategorias() {
    this.categoriaService.listarTodas(0, 100).subscribe({
      next: (response) => { this.categorias = response.content; },
      error: () => { this.errorMessage = 'Erro ao carregar categorias'; }
    });
  }

  carregarDadosUsuario() {
    this.authService.buscarPerfil().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.telefone = user.telefone || '';
      },
      error: () => {}
    });
  }

  carregarPerfilExistente() {
    this.prestadorService.buscarMeuPerfil().subscribe({
      next: (prestador: Prestador) => {
        this.prestadorExistente = prestador;
        this.fotoPerfilUrl = prestador.fotoPerfilUrl;
        this.perfilForm.patchValue({
          descricaoPerfil: prestador.descricaoPerfil || '',
          cidade: prestador.cidade || '',
          estado: prestador.estado || '',
          latitude: prestador.latitude ?? null,
          longitude: prestador.longitude ?? null
        });
        this.categoriasSelecionadas = prestador.categoriasIds || [];
      },
      error: () => {
        // 404 esperado para prestador sem perfil ainda
      }
    });
  }

  isCategoriaSelected(categoriaId: number): boolean {
    return this.categoriasSelecionadas.includes(categoriaId);
  }

  onCategoriaChange(event: any, categoriaId: number) {
    if (event.target.checked) {
      this.categoriasSelecionadas.push(categoriaId);
    } else {
      const index = this.categoriasSelecionadas.indexOf(categoriaId);
      if (index > -1) this.categoriasSelecionadas.splice(index, 1);
    }
    this.categoriasInvalidas = false;
  }

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    const file = input.files[0];
    input.value = '';

    const tiposAceitos = ['image/jpeg', 'image/png', 'image/webp'];
    if (!tiposAceitos.includes(file.type)) {
      this.erroAvatar = 'Tipo não suportado. Use JPEG, PNG ou WEBP.';
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.erroAvatar = 'Arquivo muito grande. Máximo 5 MB.';
      return;
    }

    this.erroAvatar = '';
    this.uploadingAvatar = true;
    this.fotoService.uploadAvatar(file).subscribe({
      next: (result) => {
        this.fotoPerfilUrl = result.url;
        this.uploadingAvatar = false;
        this.successMessage = 'Foto de perfil atualizada!';
        setTimeout(() => { this.successMessage = ''; }, 3000);
      },
      error: (err: any) => {
        this.erroAvatar = err?.error?.message || 'Erro ao enviar foto. Tente novamente.';
        this.uploadingAvatar = false;
      }
    });
  }

  removerAvatar(): void {
    this.fotoService.deletarAvatar().subscribe({
      next: () => { this.fotoPerfilUrl = undefined; },
      error: () => { this.erroAvatar = 'Erro ao remover foto de perfil.'; }
    });
  }

  avatarUrl(): string {
    return this.fotoPerfilUrl ? this.fotoService.urlCompleta(this.fotoPerfilUrl) : '';
  }

  get iniciaisNome(): string {
    const nome = this.currentUser?.nome || '';
    return nome.split(' ').map(p => p[0]).slice(0, 2).join('').toUpperCase();
  }

  salvarPerfil() {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.perfilForm.invalid) {
      Object.keys(this.perfilForm.controls).forEach(key => {
        this.perfilForm.get(key)?.markAsTouched();
      });
      this.errorMessage = 'Por favor, preencha todos os campos obrigatórios';
      return;
    }

    if (this.categoriasSelecionadas.length === 0) {
      this.categoriasInvalidas = true;
      this.errorMessage = 'Selecione pelo menos uma categoria de serviço';
      return;
    }

    this.loading = true;
    const userId = this.authService.getCurrentUserId();

    const prestadorData: Prestador = {
      id: this.prestadorExistente?.id || 0,
      usuarioId: userId!,
      categoriasIds: this.categoriasSelecionadas,
      descricaoPerfil: this.perfilForm.value.descricaoPerfil,
      cidade: this.perfilForm.value.cidade,
      estado: this.perfilForm.value.estado,
      latitude: this.perfilForm.value.latitude ?? undefined,
      longitude: this.perfilForm.value.longitude ?? undefined
    };

    const savePrestador$ = this.prestadorService.criarOuAtualizar(prestadorData);

    const saveUser$ = this.currentUser
      ? this.authService.atualizarPerfil({
          nome: this.currentUser.nome,
          email: this.currentUser.email,
          telefone: this.telefone,
          cep: this.currentUser.cep,
          endereco: this.currentUser.endereco,
          cidade: this.currentUser.cidade,
          estado: this.currentUser.estado,
          rg: this.currentUser.rg,
          orgaoExpedidor: this.currentUser.orgaoExpedidor,
          dataExpedicaoRg: this.currentUser.dataExpedicaoRg
        })
      : of(null);

    forkJoin([savePrestador$, saveUser$]).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Perfil salvo com sucesso!';
        setTimeout(() => {
          this.router.navigate(['/prestador/dashboard']);
        }, 2000);
      },
      error: (error: any) => {
        this.loading = false;
        this.errorMessage = error?.error?.message || 'Erro ao salvar perfil. Tente novamente.';
      }
    });
  }

  obterLocalizacao(): void {
    if (!navigator.geolocation) {
      this.erroLocalizacao = 'Geolocalização não suportada pelo navegador.';
      return;
    }
    this.usandoLocalizacao = true;
    this.erroLocalizacao = '';
    navigator.geolocation.getCurrentPosition(
      pos => {
        this.perfilForm.patchValue({
          latitude: pos.coords.latitude,
          longitude: pos.coords.longitude
        });
        this.usandoLocalizacao = false;
      },
      () => {
        this.erroLocalizacao = 'Não foi possível obter localização. Verifique as permissões do navegador.';
        this.usandoLocalizacao = false;
      }
    );
  }

  voltarDashboard(): void {
    this.router.navigate(['/prestador/dashboard']);
  }
}

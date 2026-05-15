import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { SolicitacaoService } from '../services/solicitacao.service';
import { CategoriaService } from '../services/categoria.service';
import { Categoria, SolicitacaoServico } from '../models/models';

@Component({
  selector: 'app-cliente-editar-solicitacao',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './cliente-editar-solicitacao.component.html'
})
export class ClienteEditarSolicitacaoComponent implements OnInit {
  solicitacaoForm: FormGroup;
  categorias: Categoria[] = [];
  loading = true;
  salvando = false;
  errorMessage = '';
  successMessage = '';
  solicitacaoId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private solicitacaoService: SolicitacaoService,
    private categoriaService: CategoriaService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.solicitacaoForm = this.fb.group({
      categoriaId: ['', Validators.required],
      descricao: ['', [Validators.required, Validators.minLength(20)]],
      cidade: ['', Validators.required],
      estado: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.carregarCategorias();
    this.solicitacaoId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.solicitacaoId) {
      this.carregarSolicitacao(this.solicitacaoId);
    }
  }

  carregarCategorias() {
    this.categoriaService.listarTodas(0, 100).subscribe({
      next: (response) => {
        this.categorias = response.content;
      },
      error: (error: any) => {
        console.error('Erro ao carregar categorias:', error);
        this.errorMessage = 'Erro ao carregar categorias';
      }
    });
  }

  carregarSolicitacao(id: number) {
    this.solicitacaoService.buscarPorId(id).subscribe({
      next: (solicitacao) => {
        this.solicitacaoForm.patchValue({
          categoriaId: solicitacao.categoriaId,
          descricao: solicitacao.descricao,
          cidade: solicitacao.cidade,
          estado: solicitacao.estado
        });
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Erro ao carregar solicitação:', error);
        this.errorMessage = 'Erro ao carregar solicitação';
        this.loading = false;
      }
    });
  }

  atualizarSolicitacao() {
    if (this.solicitacaoForm.invalid) {
      this.solicitacaoForm.markAllAsTouched();
      return;
    }

    if (!this.solicitacaoId) {
      this.errorMessage = 'ID da solicitação não encontrado';
      return;
    }

    this.salvando = true;
    this.successMessage = '';
    this.errorMessage = '';

    const dados: SolicitacaoServico = this.solicitacaoForm.value;

    this.solicitacaoService.atualizar(this.solicitacaoId, dados).subscribe({
      next: () => {
        this.successMessage = 'Solicitação atualizada com sucesso!';
        this.salvando = false;
        setTimeout(() => {
          this.successMessage = '';
          this.router.navigate(['/cliente/dashboard']);
        }, 1500);
      },
      error: (error: any) => {
        console.error('Erro ao atualizar solicitação:', error);
        this.errorMessage = error.error?.message || 'Erro ao atualizar solicitação';
        this.salvando = false;
      }
    });
  }

  voltar() {
    this.router.navigate(['/cliente/dashboard']);
  }
}

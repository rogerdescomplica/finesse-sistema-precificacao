import type { Usuario, UsuarioInput, Perfil } from '$lib/services/usuario.service';
import { usuarioService } from '$lib/services/usuario.service';
import { handleAuthFailure } from '$lib/security/sessionGuard.svelte.ts';
import { SvelteURLSearchParams } from 'svelte/reactivity';

export type SortBy = 'id' | 'nome' | 'email' | 'ativo';
type StatusFilter = '' | 'ativo' | 'inativo';
type PerfilFilter = '' | Perfil;
type SearchField = 'nome' | 'email';

export class UsuarioState {
  items = $state<Usuario[]>([]);
  error = $state<string>('');
  loadingList = $state(false);
  mutating = $state(false);

  search = $state('');
  searchField = $state<SearchField>('nome');
  searchStatus = $state<StatusFilter>('');
  searchPerfil = $state<PerfilFilter>('');

  page = $state(1);
  perPage = $state(10);
  totalPages = $state(1);
  totalItems = $state(0);

  sortBy = $state<SortBy>('id');
  sortAsc = $state(true);

  private currentLoadController: AbortController | null = null;
  private loadDebounceTimer: ReturnType<typeof setTimeout> | null = null;

  resetPage() { this.page = 1; }
  setPage(page: number) {
    const p = Math.max(1, page);
    if (p === this.page) return;
    this.page = p;
    this.requestLoad(0);
  }
  setSortBy(column: SortBy) {
    if (this.loadingList) return;
    if (this.sortBy === column) this.sortAsc = !this.sortAsc;
    else { this.sortBy = column; this.sortAsc = true; }
    this.requestLoad();
  }
  requestLoad(delayMs = 250) {
    if (this.loadDebounceTimer) { clearTimeout(this.loadDebounceTimer); this.loadDebounceTimer = null; }
    this.loadDebounceTimer = setTimeout(() => { this.loadDebounceTimer = null; void this.load(); }, delayMs);
  }

  private buildQueryParams() {
    const params = new SvelteURLSearchParams();
    params.set('page', String(Math.max(0, this.page - 1)));
    params.set('size', String(this.perPage));
    params.set('sort', `${this.sortBy},${this.sortAsc ? 'asc' : 'desc'}`);
    const q = this.search.trim();
    if (q) {
      if (this.searchField === 'email') params.set('email', q);
      else params.set('nome', q);
    }
    if (this.searchStatus === 'ativo') params.set('ativo', 'true');
    if (this.searchStatus === 'inativo') params.set('ativo', 'false');
    if (this.searchPerfil) params.set('perfil', String(this.searchPerfil));
    return params;
  }

  async load(opts?: { silent?: boolean }) {
    this.currentLoadController?.abort();
    const controller = new AbortController();
    this.currentLoadController = controller;

    if (!opts?.silent) this.loadingList = true;
    if (!opts?.silent) this.error = '';

    try {
      const params = this.buildQueryParams();
      const page = await usuarioService.list(params, controller.signal);
      if (this.currentLoadController !== controller) return;

      const items = Array.isArray((page as any).content) ? (page as any).content : (page as any);
      let shown = items as Usuario[];
      const q = this.search.trim().toLowerCase();
      if (q) {
        if (this.searchField === 'email') shown = shown.filter(u => u.email?.toLowerCase().includes(q));
        else shown = shown.filter(u => u.nome?.toLowerCase().includes(q));
      }
      if (this.searchStatus === 'ativo') shown = shown.filter(u => u.ativo);
      if (this.searchStatus === 'inativo') shown = shown.filter(u => !u.ativo);
      this.items = shown;
      this.totalPages = Number.isFinite((page as any).totalPages) && (page as any).totalPages > 0 ? (page as any).totalPages : 1;
      this.totalItems = Number.isFinite((page as any).totalElements) ? (page as any).totalElements : shown.length ?? 0;
    } catch (err) {
      if (err instanceof DOMException && err.name === 'AbortError') return;
      this.error = err instanceof Error ? err.message : 'Erro inesperado';
      await handleAuthFailure(this.error);
    } finally {
      if (this.currentLoadController === controller && !opts?.silent) {
        this.loadingList = false;
      }
    }

}

  async create(data: UsuarioInput) {
  this.mutating = true; this.error = '';
  try {
    await usuarioService.create(data);
    await this.load({ silent: true });
  } catch (err) {
    this.error = err instanceof Error ? err.message : 'Erro ao cadastrar usuário';
    await handleAuthFailure(this.error);
    throw err;
  } finally {
    this.mutating = false;
  }
}

async update(id: number, data: UsuarioInput) {
  this.mutating = true
  try {
    await usuarioService.update(id, data)
    await this.load()
  } finally {
    this.mutating = false
  }
}

async delete(id: number) {
  this.mutating = true
  try {
    await usuarioService.remove(id)
    await this.load()
  } finally {
    this.mutating = false
  }
}

  async toggleStatus(id: number, ativo: boolean) {
      this.mutating = true; this.error = '';
      try {
        await usuarioService.toggleStatus(id, ativo);
        await this.load({ silent: true });
      } catch (err) {
        this.error = err instanceof Error ? err.message : 'Erro inesperado';
        await handleAuthFailure(this.error);
        throw err;
      } finally { this.mutating = false; }
    }


}

export const usuarioState = new UsuarioState();

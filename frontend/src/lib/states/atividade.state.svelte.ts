import type { Atividade, AtividadeInput } from '$lib/services/atividade.service';
import { atividadeService } from '$lib/services/atividade.service';
import { SvelteURLSearchParams } from 'svelte/reactivity';
import { handleAuthFailure } from '$lib/security/sessionGuard.svelte.ts';

export type SortBy = 'id' | 'nome' | 'cnae' | 'aliquotaTotalPct' | 'issPct' | 'ativo';
type StatusFilter = '' | 'ativo' | 'inativo';

export class AtividadeState {
	items = $state<Atividade[]>([]);
	error = $state<string>('');

	loadingList = $state(false);
	mutating = $state(false);

	search = $state('');
	searchStatus = $state<StatusFilter>('');

	page = $state(1);
	perPage = $state(10);
	totalPages = $state(1);
	totalItems = $state(0);

	sortBy = $state<SortBy>('id');
	sortAsc = $state(true);

	private currentLoadController: AbortController | null = null;
	private loadDebounceTimer: ReturnType<typeof setTimeout> | null = null;

	resetPage() {
		this.page = 1;
	}

	setPage(page: number) {
		const p = Math.max(1, page);
		if (p === this.page) return;
		this.page = p;
		this.requestLoad(0);
	}

	setSortBy(column: SortBy) {
		if (this.loadingList) return;
		if (this.sortBy === column) {
			this.sortAsc = !this.sortAsc;
		} else {
			this.sortBy = column;
			this.sortAsc = true;
		}
		this.requestLoad();
	}

	requestLoad(delayMs = 250) {
		if (this.loadDebounceTimer !== null) {
			clearTimeout(this.loadDebounceTimer);
			this.loadDebounceTimer = null;
		}
		this.loadDebounceTimer = setTimeout(() => {
			this.loadDebounceTimer = null;
			void this.load();
		}, delayMs);
	}

	private buildQueryParams() {
		const params = new SvelteURLSearchParams();
		params.set('page', String(Math.max(0, this.page - 1)));
		params.set('size', String(this.perPage));
		params.set('sort', `${this.sortBy},${this.sortAsc ? 'asc' : 'desc'}`);
		const q = this.search.trim();
		if (q) params.set('titulo', q);
		if (this.searchStatus === 'ativo') params.set('ativo', 'true');
		if (this.searchStatus === 'inativo') params.set('ativo', 'false');
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
			const page = await atividadeService.list(params, controller.signal);
			this.items = Array.isArray(page.content) ? page.content : [];
			this.totalPages = Number.isFinite(page.totalPages) && page.totalPages > 0 ? page.totalPages : 1;
			this.totalItems = Number.isFinite(page.totalElements) ? page.totalElements : 0;
		} catch (err) {
			if (err instanceof DOMException && err.name === 'AbortError') {
				return;
			}
			this.error = err instanceof Error ? err.message : 'Erro inesperado';
			await handleAuthFailure(this.error);
		} finally {
			if (this.currentLoadController === controller && !opts?.silent) {
				this.loadingList = false;
			}
		}
	}

	async create(data: AtividadeInput) {
		this.mutating = true;
		this.error = '';
		try {
			await atividadeService.create(data);
			await this.load({ silent: true });
		} catch (err) {
			this.error = err instanceof Error ? err.message : 'Erro inesperado';
			await handleAuthFailure(this.error);
			throw err;
		} finally {
			this.mutating = false;
		}
	}

	async update(id: number, data: AtividadeInput) {
		this.mutating = true;
		this.error = '';
		try {
			await atividadeService.update(id, data);
			await this.load({ silent: true });
		} catch (err) {
			this.error = err instanceof Error ? err.message : 'Erro inesperado';
			await handleAuthFailure(this.error);
			throw err;
		} finally {
			this.mutating = false;
		}
	}

	async toggleStatus(id: number, ativo: boolean) {
		this.mutating = true;
		this.error = '';
		try {
			await atividadeService.toggleStatus(id, ativo);
			await this.load({ silent: true });
		} catch (err) {
			this.error = err instanceof Error ? err.message : 'Erro inesperado';
			await handleAuthFailure(this.error);
			throw err;
		} finally {
			this.mutating = false;
		}
	}

	async delete(id: number) {
		this.mutating = true;
		this.error = '';
		try {
			await atividadeService.remove(id);
			if (this.items.length === 1 && this.page > 1) this.page -= 1;
			await this.load({ silent: true });
		} catch (err) {
			this.error = err instanceof Error ? err.message : 'Erro inesperado';
			throw err;
		} finally {
			this.mutating = false;
		}
	}
}

export const atividadeState = new AtividadeState();

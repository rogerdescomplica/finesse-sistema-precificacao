import type { Servico, ServicoInput } from '$lib/services/servico.service';
import { servicoService } from '$lib/services/servico.service';
import { precoPraticadoService } from '$lib/services/preco-praticado.service';
import { handleAuthFailure } from '$lib/security/sessionGuard.svelte.ts';
import { SvelteURLSearchParams } from 'svelte/reactivity';

export type SortBy = 'id' | 'nome' | 'grupo' | 'duracaoMinutos' | 'ativo';
type StatusFilter = '' | 'ativo' | 'inativo';
type SearchField = 'nome' | 'grupo';

export class ServicoState {
	items = $state<Servico[]>([]);
	error = $state<string>('');

	loadingList = $state(false);
	mutating = $state(false);

	search = $state('');
	searchField = $state<SearchField>('nome');
	searchStatus = $state<StatusFilter>('');

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
		if (q) params.set(this.searchField, q);
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
			const page = await servicoService.list(params, controller.signal);
			const baseItems = Array.isArray(page.content) ? page.content : [];
			const precos = await precoPraticadoService.listarPrecosAtuais();
			const precoMap = new Map<number, number | null>(precos.map(p => [p.servicoId, p.precoAtual]));
			const itemsWithPrice = baseItems.map(s => ({ ...s, precoVigente: precoMap.get(s.id) ?? null }));
			if (this.currentLoadController !== controller) return;
			this.items = itemsWithPrice;
			this.totalPages = Number.isFinite(page.totalPages) && page.totalPages > 0 ? page.totalPages : 1;
			this.totalItems = Number.isFinite(page.totalElements) ? page.totalElements : 0;
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

	async create(data: ServicoInput) {
		this.mutating = true; this.error = '';
		try {
			await servicoService.create(data);
			await this.load({ silent: true });
		} catch (err) {
			this.error = err instanceof Error ? err.message : 'Erro inesperado';
			await handleAuthFailure(this.error);
			throw err;
		} finally { this.mutating = false; }
	}

	async update(id: number, data: ServicoInput) {
		this.mutating = true; this.error = '';
		try {
			await servicoService.update(id, data);
			await this.load({ silent: true });
		} catch (err) {
			this.error = err instanceof Error ? err.message : 'Erro inesperado';
			await handleAuthFailure(this.error);
			throw err;
		} finally { this.mutating = false; }
	}

	async toggleStatus(id: number, ativo: boolean) {
		this.mutating = true; this.error = '';
		try {
			await servicoService.toggleStatus(id, ativo);
			await this.load({ silent: true });
		} catch (err) {
			this.error = err instanceof Error ? err.message : 'Erro inesperado';
			await handleAuthFailure(this.error);
			throw err;
		} finally { this.mutating = false; }
	}

	async delete(id: number) {
		this.mutating = true; this.error = '';
		try {
			await servicoService.remove(id);
			if (this.items.length === 1 && this.page > 1) this.page -= 1;
			await this.load({ silent: true });
		} catch (err) {
			this.error = err instanceof Error ? err.message : 'Erro inesperado';
			await handleAuthFailure(this.error);
			throw err;
		} finally { this.mutating = false; }
	}
}

export const servicoState = new ServicoState();

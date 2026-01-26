<script lang="ts">
	import { Button } from '$lib/components/ui/button/index.js';
	import * as Card from '$lib/components/ui/card/index.js';
	import { Input } from '$lib/components/ui/input/index.js';
	import { Plus, Search } from '@lucide/svelte';
	import ConfigTable from '$lib/components/configuracoes/ConfigTable.svelte';
	import ConfigForm from '$lib/components/configuracoes/ConfigForm.svelte';
	import DeleteConfirmDialog from '$lib/components/configuracoes/DeleteConfirmDialog.svelte';
	import TablePagination from '$lib/components/materiais/TablePagination.svelte';
	import { auth } from '$lib/states/auth.state.svelte';
	import { configState } from '$lib/states/config.state.svelte.ts';

	let formOpen = $state(false);
	let editingConfig = $state<import('$lib/services/config.service').Configuracao | null>(null);
	let confirmDeleteConfig = $state<import('$lib/services/config.service').Configuracao | null>(null);

	$effect(() => {
		configState.page; configState.perPage; configState.sortBy; configState.sortAsc;
		configState.search; configState.searchStatus;
		void configState.load();
	});
	$effect(() => { configState.search; configState.searchStatus; configState.resetPage(); });

	function openCreateForm() { editingConfig = null; formOpen = true; }
	function openEditForm(c: import('$lib/services/config.service').Configuracao) { editingConfig = c; formOpen = true; }
	function closeForm() { formOpen = false; editingConfig = null; }
	async function handleFormSubmit(data: {
		pretensaoSalarialMensal: number; horasSemanais: number; semanasMediaMes: number;
		custoFixoPct: number; margemLucroPadraoPct: number; ativo: boolean;
	}) {
		try {
			if (editingConfig) await configState.update(editingConfig.id, data);
			else await configState.create(data);
			closeForm();
		} catch (e) { console.error('Erro ao salvar configuração:', e); }
	}
	async function handleToggleStatus(c: import('$lib/services/config.service').Configuracao) {
		await configState.toggleStatus(c.id, !c.ativo);
	}
	function openDeleteDialog(c: import('$lib/services/config.service').Configuracao) { confirmDeleteConfig = c; }
	async function handleConfirmDelete(c: import('$lib/services/config.service').Configuracao) {
		await configState.delete(c.id); confirmDeleteConfig = null;
	}
	function handleCancelDelete() { confirmDeleteConfig = null; }
</script>

<svelte:head><title>Geral - Configurações</title></svelte:head>

<div class="flex items-center justify-between px-2 pb-5">
	<div>
		<Card.Title class="text-xl">Configurações</Card.Title>
		<Card.Description>Gerais do sistema</Card.Description>
	</div>
	<Button class="inline-flex items-center gap-2" onclick={openCreateForm} disabled={!auth.isAuthenticated}>
		<Plus size={16} /> Nova Configuração
	</Button>
</div>

<Card.Root class="w-full">
	<Card.Content>
		<div class="mb-2 px-2 py-2">
			<div class="flex items-center gap-4">
				<div class="relative flex-1">
					<Search size={16} class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
					<Input placeholder="Buscar..." bind:value={configState.search} class="h-10 w-1/2 rounded-xl pl-9" />
				</div>
				<div class="flex items-center gap-3">
					<div class="flex items-center gap-1 rounded-xl bg-gray-50 ring-1 ring-pink-100 p-1">
						<button type="button" class="rounded-lg px-3 py-1.5 text-sm font-semibold transition-colors cursor-pointer"
							class:bg-white={configState.searchStatus === ''} class:shadow-sm={configState.searchStatus === ''}
							class:text-gray-800={configState.searchStatus === ''} class:text-gray-400={configState.searchStatus !== ''}
							aria-pressed={configState.searchStatus === ''} onclick={() => (configState.searchStatus = '')}>Todos</button>
						<button type="button" class="rounded-lg px-3 py-1.5 text-sm font-semibold transition-colors cursor-pointer"
							class:bg-white={configState.searchStatus === 'ativo'} class:shadow-sm={configState.searchStatus === 'ativo'}
							class:text-green-600={configState.searchStatus === 'ativo'} class:text-gray-400={configState.searchStatus !== 'ativo'}
							aria-pressed={configState.searchStatus === 'ativo'} onclick={() => (configState.searchStatus = 'ativo')}>Ativos</button>
						<button type="button" class="rounded-lg px-3 py-1.5 text-sm font-semibold transition-colors cursor-pointer"
							class:bg-white={configState.searchStatus === 'inativo'} class:shadow-sm={configState.searchStatus === 'inativo'}
							class:text-red-600={configState.searchStatus === 'inativo'} class:text-gray-400={configState.searchStatus !== 'inativo'}
							aria-pressed={configState.searchStatus === 'inativo'} onclick={() => (configState.searchStatus = 'inativo')}>Inativos</button>
					</div>
					<span class="mx-1 h-6 w-px bg-gray-200" aria-hidden="true"> </span>
					<label for="per-page-select" class="text-sm text-gray-600">Exibir</label>
					<select id="per-page-select" class="h-9 rounded-xl border border-pink-200 bg-white px-3" bind:value={configState.perPage}>
						<option value={10}>10</option><option value={25}>25</option><option value={50}>50</option><option value={100}>100</option>
					</select>
				</div>
			</div>
		</div>
		<hr class="divide-y divide-gray-100">
		{#if configState.error}
			<div class="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-800">{configState.error}</div>
		{/if}
		{#if configState.loading && configState.items.length === 0}
			<div class="py-10 text-center text-sm text-gray-600">Carregando...</div>
		{:else}
			<ConfigTable
				configs={configState.items}
				sortBy={configState.sortBy}
				sortAsc={configState.sortAsc}
				onSort={(column) => configState.setSortBy(column)}
				onEdit={openEditForm}
				onToggleStatus={handleToggleStatus}
				onDelete={openDeleteDialog}
			/>
			<div class="mt-4">
				<TablePagination
					currentPage={configState.page}
					totalPages={configState.totalPages}
					perPage={configState.perPage}
					totalItems={configState.totalItems}
					onPageChange={(page) => (configState.page = page)}
				/>
			</div>
		{/if}
	</Card.Content>
</Card.Root>

<ConfigForm bind:open={formOpen} editingConfig={editingConfig} loading={configState.loading} error={configState.error} onClose={closeForm} onSubmit={handleFormSubmit} />
<DeleteConfirmDialog config={confirmDeleteConfig} loading={configState.loading} onConfirm={handleConfirmDelete} onCancel={handleCancelDelete} />

<script lang="ts">
	import { Button } from '$lib/components/ui/button/index.js';
	import * as Card from '$lib/components/ui/card/index.js';
	import { Plus } from '@lucide/svelte';
	import type { Configuracao } from '$lib/services/configuracoes.service';

	import ConfigTable from '$lib/components/configuracoes/ConfigTable.svelte';
	import ConfigForm from '$lib/components/configuracoes/ConfigForm.svelte';
	import DeleteConfirmDialog from '$lib/components/configuracoes/DeleteConfirmDialog.svelte';
	import TablePagination from '$lib/components/utils/TablePagination.svelte';
	import ListFilterBar from '$lib/components/utils/ListFilterBar.svelte';



	import { auth } from '$lib/states/auth.state.svelte';
	import { configState } from '$lib/states/configuracoes.state.svelte';
	import { useCrudPage } from '$lib/composables/crudPage.svelte.ts';
	import type { ConfigInput } from '$lib/services/configuracoes.service';


	const crud = useCrudPage<Configuracao, any, keyof Configuracao>(configState, { isAuthenticated: () => auth.isAuthenticated && auth.checked });
	const formOpen = $derived(crud.formOpen);
	const editingConfig = $derived(crud.editingItem);
	const confirmDeleteConfig = $derived(crud.confirmDeleteItem);

	// variável local mutável para binding do Sheet
	let sheetOpen = $state(false);
	let successMessage = $state('');

	const SUCCESS_DISPLAY_TIME = 1800;
	let successTimer: ReturnType<typeof setTimeout>;

	async function handleSubmitConfig(data: ConfigInput) {
		try {
			await crud.submit(data);
			successMessage = 'Configuração salva com sucesso';
			sheetOpen = false;
			
			clearTimeout(successTimer);
			successTimer = setTimeout(() => { 
				successMessage = ''; 
			}, SUCCESS_DISPLAY_TIME);
		} catch (error) {
			console.error('Erro ao salvar configuração:', error);
			// Tratamento adicional se necessário
		}
	}
</script>

<svelte:head>
	<title>Geral - Configurações</title>
</svelte:head>

<div class="flex items-center justify-between px-2 pb-5">
	<div>
		<Card.Title class="text-xl">Configurações</Card.Title>
		<Card.Description>Gerais do sistema</Card.Description>
	</div>

	<Button
		class="inline-flex items-center gap-2"
		onclick={() => { crud.openCreateForm(); sheetOpen = true; }}
		disabled={!auth.isAuthenticated}
	>
		<Plus size={16} /> Nova Configuração
	</Button>
</div>

<Card.Root class="w-full">
	<Card.Content>
		<ListFilterBar
			placeholder="Buscar por configuração..."
			bind:search={configState.search}
			bind:searchStatus={configState.searchStatus}
			bind:perPage={configState.perPage}
		/>

		<hr class="divide-y divide-gray-100" />

		{#if successMessage}
			<div class="mb-4 rounded-md bg-green-50 p-3 text-sm text-green-800">
				{successMessage}
			</div>
		{/if}

		{#if configState.error}
			<div class="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-800">
				{configState.error}
			</div>
		{/if}

		{#if configState.loading && configState.items.length === 0}
			<div class="py-10 text-center text-sm text-gray-600">Carregando...</div>
		{:else}
			<ConfigTable
				configs={configState.items}
				sortBy={configState.sortBy}
				sortAsc={configState.sortAsc}
				onSort={(column) => configState.setSortBy(column)}
				onEdit={(cfg) => { sheetOpen = true; crud.openEditForm(cfg); }}
				onToggleStatus={crud.toggleStatus}
				onDelete={crud.openDeleteDialog}
			/>

			<div class="mt-4">
				<TablePagination
					currentPage={configState.page}
					totalPages={configState.totalPages}
					perPage={configState.perPage}
					totalItems={configState.totalItems}
					onPageChange={crud.onPageChange}
				/>
			</div>
		{/if}
	</Card.Content>
</Card.Root>

{#key editingConfig ? editingConfig.id : 'new'}
	<ConfigForm
		bind:open={sheetOpen}
		editingConfig={editingConfig}
		loading={configState.loading}
		error={configState.error}
		onClose={() => { sheetOpen = false; crud.closeForm(); }}
		onSubmit={handleSubmitConfig}
	/>
{/key}

<DeleteConfirmDialog
	config={confirmDeleteConfig}
	loading={configState.loading}
	onConfirm={crud.confirmDelete}
	onCancel={crud.cancelDelete}
/>

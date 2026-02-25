<script lang="ts">
	import { Button } from '$lib/components/ui/button/index.js';
	import * as Card from '$lib/components/ui/card/index.js';
	import { Plus } from '@lucide/svelte';
	import DeleteConfirmDialog from '$lib/components/atividades/DeleteConfirmDialog.svelte';
	import AtividadeForm from '$lib/components/atividades/AtividadeForm.svelte';
	import AtividadeTable from '$lib/components/atividades/AtividadeTable.svelte';
	import TablePagination from '$lib/components/utils/TablePagination.svelte';
	import ListFilterBar from '$lib/components/utils/ListFilterBar.svelte';

	import { auth } from '$lib/states/auth.state.svelte';
	import { atividadeState, type SortBy } from '$lib/states/atividade.state.svelte.ts';
	import { useCrudPage } from '$lib/composables/crudPage.svelte.ts';
	import type { AtividadeInput, Atividade } from '$lib/services/atividade.service';

	const crud = useCrudPage<Atividade, AtividadeInput, SortBy>(atividadeState, { isAuthenticated: () => auth.isAuthenticated && auth.checked });
	const formOpen = $derived(crud.formOpen);
	const editingAtividade = $derived(crud.editingItem);
	const confirmDeleteAtividade = $derived(crud.confirmDeleteItem);

	let sheetOpen = $state(false);
	let successMessage = $state('');
	const SUCCESS_DISPLAY_TIME = 1800;
	let successTimer: ReturnType<typeof setTimeout>;

	async function handleSubmitAtividade(data: AtividadeInput) {
		try {
			await crud.submit(data);
			successMessage = 'Atividade salva com sucesso';
			sheetOpen = false;
			clearTimeout(successTimer);
			successTimer = setTimeout(() => { successMessage = '' }, SUCCESS_DISPLAY_TIME);
		} catch (error) {
			console.error('Erro ao salvar atividade:', error);
		}
	}
</script>

<svelte:head>
	<title>Atividades</title>
</svelte:head>

<div class="flex items-center justify-between px-2 pb-5">
	<div>
		<Card.Title class="text-xl">Atividades</Card.Title>
		<Card.Description>Listagem e gerenciamento</Card.Description>
	</div>
	<Button
		class="inline-flex items-center gap-2 cursor-pointer"
		onclick={() => { sheetOpen = true; crud.openCreateForm(); }}
		disabled={!auth.isAuthenticated}
	>
		<Plus size={16} />
		Cadastrar Nova Atividade
	</Button>
</div>

<Card.Root class="w-full">
	<Card.Content>
		<ListFilterBar
			placeholder="Buscar por atividades..."
			bind:search={atividadeState.search}
			bind:searchStatus={atividadeState.searchStatus}
			bind:perPage={atividadeState.perPage}
		/>

		<hr class="divide-y divide-gray-100" />

		{#if successMessage}
			<div class="mb-4 rounded-md bg-green-50 p-3 text-sm text-green-800">
				{successMessage}
			</div>
		{/if}

		{#if atividadeState.error}
			<div class="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-800">
				{atividadeState.error}
			</div>
		{/if}

		{#if atividadeState.loadingList && atividadeState.items.length === 0}
			<div class="py-10 text-center text-sm text-gray-600">Carregando...</div>
		{:else}
			<AtividadeTable
				atividades={atividadeState.items}
				sortBy={atividadeState.sortBy}
				sortAsc={atividadeState.sortAsc}
				loading={atividadeState.loadingList}
				onSort={crud.onSort}
				onEdit={(a) => { sheetOpen = true; crud.openEditForm(a); }}
				onToggleStatus={crud.toggleStatus}
				onDelete={crud.openDeleteDialog}
			/>

			<div class="mt-4">
				<TablePagination
					currentPage={atividadeState.page}
					totalPages={atividadeState.totalPages}
					perPage={atividadeState.perPage}
					totalItems={atividadeState.totalItems}
					onPageChange={crud.onPageChange}
				/>
			</div>
		{/if}
	</Card.Content>
</Card.Root>

{#key editingAtividade ? editingAtividade.id : 'new'}
	<AtividadeForm
		bind:open={sheetOpen}
		editingAtividade={editingAtividade}
		loading={atividadeState.mutating}
		error={atividadeState.error}
		onClose={() => { sheetOpen = false; crud.closeForm(); }}
		onSubmit={handleSubmitAtividade}
	/>
{/key}

<DeleteConfirmDialog
	atividade={confirmDeleteAtividade}
	loading={atividadeState.mutating}
	onConfirm={async (a) => {
		try {
			await crud.confirmDelete(a);
			successMessage = 'Atividade excluída com sucesso';
			clearTimeout(successTimer);
			successTimer = setTimeout(() => { successMessage = ''; }, SUCCESS_DISPLAY_TIME);
		} catch (error) {
			console.error('Erro ao excluir atividade:', error);
		}
	}}
	onCancel={crud.cancelDelete}
/>

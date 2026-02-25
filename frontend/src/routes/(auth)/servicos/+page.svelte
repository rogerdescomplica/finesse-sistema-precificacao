<script lang="ts">
	import { Button } from '$lib/components/ui/button/index.js';
	import * as Card from '$lib/components/ui/card/index.js';
	import ListFilterBar from '$lib/components/utils/ListFilterBar.svelte';
	import TablePagination from '$lib/components/utils/TablePagination.svelte';
	import { Plus } from '@lucide/svelte';
	import { auth } from '$lib/states/auth.state.svelte';
	import { useCrudPage } from '$lib/composables/crudPage.svelte.ts';
	import ServicoForm from '$lib/components/servicos/ServicoForm.svelte';
	import ServicoTable from '$lib/components/servicos/ServicoTable.svelte';
	import DeleteConfirmDialog from '$lib/components/servicos/DeleteConfirmDialog.svelte';
	import { servicoState, type SortBy } from '$lib/states/servico.state.svelte.ts';
	import type { Servico, ServicoInput } from '$lib/services/servico.service';

	const crud = useCrudPage<Servico, ServicoInput, SortBy>(servicoState, { isAuthenticated: () => auth.isAuthenticated && auth.checked });
	const editingServico = $derived(crud.editingItem);
	const confirmDeleteServico = $derived(crud.confirmDeleteItem);

	let sheetOpen = $state(false);
	let successMessage = $state('');
	const SUCCESS_DISPLAY_TIME = 1800;
	let successTimer: ReturnType<typeof setTimeout>;

	async function handleSubmitServico(data: ServicoInput) {
		try {
			await crud.submit(data);
			successMessage = 'Serviço salvo com sucesso';
			sheetOpen = false;
			clearTimeout(successTimer);
			successTimer = setTimeout(() => { successMessage = ''; }, SUCCESS_DISPLAY_TIME);
		} catch (error) {
			console.error('Erro ao salvar serviço:', error);
		}
	}
</script>

<svelte:head>
	<title>Serviços</title>
</svelte:head>

<div class="flex items-center justify-between px-2 pb-5">
	<div>
		<Card.Title class="text-xl">Serviços</Card.Title>
		<Card.Description>Listagem e gerenciamento</Card.Description>
	</div>

	<Button
		class="inline-flex items-center gap-2"
		onclick={() => { crud.openCreateForm(); sheetOpen = true; }}
		disabled={!auth.isAuthenticated}
	>
		<Plus size={16} />
		Cadastrar Novo Serviço
	</Button>
	</div>

<Card.Root class="w-full">
	<Card.Content>
		<ListFilterBar
			placeholder="Buscar por nome..."
			bind:search={servicoState.search}
			bind:searchStatus={servicoState.searchStatus}
			bind:perPage={servicoState.perPage}
		>
			<label class="ml-1 text-sm text-gray-600">Buscar em:</label>
			<select class="h-9 rounded-xl border border-pink-200 bg-white px-3 text-sm" bind:value={servicoState.searchField}>
				<option value="nome">Nome</option>
				<option value="grupo">Categoria (Grupo)</option>
			</select>
		</ListFilterBar>

		<hr class="divide-y divide-gray-100" />

		{#if successMessage}
			<div class="mb-4 rounded-md bg-green-50 p-3 text-sm text-green-800">
				{successMessage}
			</div>
		{/if}

		{#if servicoState.error}
			<div class="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-800">
				{servicoState.error}
			</div>
		{/if}

		{#if servicoState.loadingList && servicoState.items.length === 0}
			<div class="py-10 text-center text-sm text-gray-600">Carregando...</div>
		{:else}
			<ServicoTable
				servicos={servicoState.items}
				sortBy={servicoState.sortBy}
				sortAsc={servicoState.sortAsc}
				loading={servicoState.loadingList}
				onSort={crud.onSort}
				onEdit={(s) => { sheetOpen = true; crud.openEditForm(s); }}
				onToggleStatus={crud.toggleStatus}
				onDelete={crud.openDeleteDialog}
			/>

			<div class="mt-4">
				<TablePagination
					currentPage={servicoState.page}
					totalPages={servicoState.totalPages}
					perPage={servicoState.perPage}
					totalItems={servicoState.totalItems}
					onPageChange={crud.onPageChange}
				/>
			</div>
		{/if}
	</Card.Content>
</Card.Root>

{#key editingServico ? editingServico.id : 'new'}
	<ServicoForm
		bind:open={sheetOpen}
		editingServico={editingServico}
		loading={servicoState.mutating}
		error={servicoState.error}
		onClose={() => { sheetOpen = false; crud.closeForm(); }}
		onSubmit={handleSubmitServico}
	/>
{/key}

<DeleteConfirmDialog
	servico={confirmDeleteServico}
	loading={servicoState.mutating}
	onConfirm={async (s) => {
		try {
			await crud.confirmDelete(s);
			successMessage = 'Serviço excluído com sucesso';
			clearTimeout(successTimer);
			successTimer = setTimeout(() => { successMessage = ''; }, SUCCESS_DISPLAY_TIME);
		} catch (error) {
			console.error('Erro ao excluir serviço:', error);
		}
	}}
	onCancel={crud.cancelDelete}
/>

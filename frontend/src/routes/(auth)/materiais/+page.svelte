<script lang="ts">
	import { Button } from '$lib/components/ui/button/index.js';
	import * as Card from '$lib/components/ui/card/index.js';
	import type { Material } from '$lib/services/material.service';
	import { Plus } from '@lucide/svelte';

	import DeleteConfirmDialog from '$lib/components/materiais/DeleteConfirmDialog.svelte';
	import MaterialForm from '$lib/components/materiais/MaterialForm.svelte';
	import MaterialTable from '$lib/components/materiais/MaterialTable.svelte';
	import TablePagination from '$lib/components/utils/TablePagination.svelte';
	import ListFilterBar from '$lib/components/utils/ListFilterBar.svelte';

	import { auth } from '$lib/states/auth.state.svelte';
	import { materialState, type SortBy } from '$lib/states/material.state.svelte';
	import { useCrudPage } from '$lib/composables/crudPage.svelte.ts';
	import type { MaterialInput } from '$lib/services/material.service';

	const crud = useCrudPage<Material, MaterialInput, SortBy>(materialState, { isAuthenticated: () => auth.isAuthenticated && auth.checked });
	const editingMaterial = $derived(crud.editingItem);
	const confirmDeleteMaterial = $derived(crud.confirmDeleteItem);
	// variável local mutável para binding do Sheet
	let sheetOpen = $state(false);
	let successMessage = $state('');

	const SUCCESS_DISPLAY_TIME = 1800;
	let successTimer: ReturnType<typeof setTimeout>;

	async function handleSubmitMaterial(data: MaterialInput) {
		try {
			await crud.submit(data);
			successMessage = 'Material salvo com sucesso';
			sheetOpen = false;
			
			clearTimeout(successTimer);
			successTimer = setTimeout(() => { 
				successMessage = ''; 
			}, SUCCESS_DISPLAY_TIME);
		} catch (error) {
			console.error('Erro ao salvar material:', error);
			// Tratamento adicional se necessário
		}
	}


</script>

<svelte:head>
	<title>Materiais</title>
</svelte:head>

<div class="flex items-center justify-between px-2 pb-5">
	<div>
		<Card.Title class="text-xl">Materiais</Card.Title>
		<Card.Description>Listagem e gerenciamento</Card.Description>
	</div>

	<Button
		class="inline-flex items-center gap-2 cursor-pointer"
		onclick={() => { crud.openCreateForm(); sheetOpen = true; }}
		disabled={!auth.isAuthenticated}
	>
		<Plus size={16} />
		Cadastrar Novo Material
	</Button>
</div>

<Card.Root class="w-full">
	<Card.Content>
		<ListFilterBar
			placeholder="Buscar por materias..."
			bind:search={materialState.search}
			bind:searchStatus={materialState.searchStatus}
			bind:perPage={materialState.perPage}
		/>

		<hr class="divide-y divide-gray-100" />

		{#if successMessage}
			<div class="mb-4 rounded-md bg-green-50 p-3 text-sm text-green-800">
				{successMessage}
			</div>
		{/if}

		{#if materialState.error}
			<div class="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-800">
				{materialState.error}
			</div>
		{/if}

		{#if materialState.loadingList && materialState.items.length === 0}
			<div class="py-10 text-center text-sm text-gray-600">Carregando...</div>
		{:else}
			<MaterialTable
				materials={materialState.items}
				sortBy={materialState.sortBy}
				sortAsc={materialState.sortAsc}
				loading={materialState.loadingList}
				onSort={crud.onSort}
				onEdit={(m) => { sheetOpen = true; crud.openEditForm(m); }}
				onToggleStatus={crud.toggleStatus}
				onDelete={crud.openDeleteDialog}
			/>

			<div class="mt-4">
				<TablePagination
					currentPage={materialState.page}
					totalPages={materialState.totalPages}
					perPage={materialState.perPage}
					totalItems={materialState.totalItems}
					onPageChange={crud.onPageChange}
				/>
			</div>
		{/if}
	</Card.Content>
</Card.Root>

{#key editingMaterial ? editingMaterial.id : 'new'}
	<MaterialForm
		bind:open={sheetOpen}
		editingMaterial={editingMaterial}
		loading={materialState.mutating}
		error={materialState.error}
		onClose={() => { sheetOpen = false; crud.closeForm(); }}
		onSubmit={handleSubmitMaterial}
	/>
{/key}

<DeleteConfirmDialog
	material={confirmDeleteMaterial}
	loading={materialState.mutating}
	onConfirm={crud.confirmDelete}
	onCancel={crud.cancelDelete}
/>

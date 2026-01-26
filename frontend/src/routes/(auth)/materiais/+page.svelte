<script lang="ts">
	import { Button } from '$lib/components/ui/button/index.js';
	import * as Card from '$lib/components/ui/card/index.js';
	import { Input } from '$lib/components/ui/input/index.js';
	import type { Material } from '$lib/services/material.service';
	import { Plus, Search } from '@lucide/svelte';

	import DeleteConfirmDialog from '$lib/components/materiais/DeleteConfirmDialog.svelte';
	import MaterialForm from '$lib/components/materiais/MaterialForm.svelte';
	import MaterialTable from '$lib/components/materiais/MaterialTable.svelte';
	import TablePagination from '$lib/components/materiais/TablePagination.svelte';

	import { auth } from '$lib/states/auth.state.svelte';
	import { materialState } from '$lib/states/material.state.svelte';

	// UI local
	let formOpen = $state(false);
	let editingMaterial = $state<Material | null>(null);
	let confirmDeleteMaterial = $state<Material | null>(null);

		
	// Recarrega quando mudar filtros/paginação/ordenação
	$effect(() => {
		// dependências reativas
		materialState.page;
		materialState.perPage;
		materialState.sortBy;
		materialState.sortAsc;
		materialState.search;
		materialState.searchStatus;

		void materialState.load();
	});

	// quando filtros mudam, volta pra página 1
	$effect(() => {
		materialState.search;
		materialState.searchStatus;
		materialState.resetPage();
	});

	function openCreateForm() {
		editingMaterial = null;
		formOpen = true;
	}

	function openEditForm(material: Material) {
		editingMaterial = material;
		formOpen = true;
	}

	function closeForm() {
		formOpen = false;
		editingMaterial = null;
	}

	async function handleFormSubmit(data: {
		produto: string;
		unidadeMedida: Material['unidadeMedida'];
		volumeEmbalagem: number;
		precoEmbalagem: number;
		custoUnitario: number;
		observacoes: string;
		ativo: boolean;
	}) {
		try {
			if (editingMaterial) {
				await materialState.update(editingMaterial.id, data);
			} else {
				await materialState.create(data);
			}
			closeForm();
		} catch (error) {
			console.error('Erro ao salvar material:', error);
		}
	}

	async function handleToggleStatus(material: Material) {
		await materialState.toggleStatus(material.id, !material.ativo);
	}

	function openDeleteDialog(material: Material) {
		confirmDeleteMaterial = material;
	}

	async function handleConfirmDelete(material: Material) {
		await materialState.delete(material.id);
		confirmDeleteMaterial = null;
	}

	function handleCancelDelete() {
		confirmDeleteMaterial = null;
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

			<Button class="inline-flex items-center gap-2 cursor-pointer" onclick={openCreateForm} disabled={!auth.isAuthenticated}>
				<Plus size={16} />
				Cadastrar Novo Material
			</Button>
		</div>

<Card.Root class="w-full">
	
	<Card.Content>
		<div class="mb-2 px-2 py-2">
			<div class="flex items-center gap-4">
				<!-- Busca -->
				<div class="relative flex-1">
					<Search size={16} class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
					<Input
						placeholder="Buscar por produto..."
						bind:value={materialState.search}
						class="h-10 w-1/2 rounded-xl pl-9"
					/>
				</div>

				<!-- Segmento de status -->
				<div class="flex items-center gap-3">
					<div class="flex items-center gap-1 rounded-xl bg-gray-50 ring-1 ring-pink-100 p-1">
						<button
							type="button"
							class="rounded-lg px-3 py-1.5 text-sm font-semibold transition-colors cursor-pointer"
							class:bg-white={materialState.searchStatus === ''}
							class:shadow-sm={materialState.searchStatus === ''}
							class:text-gray-800={materialState.searchStatus === ''}
							class:text-gray-400={materialState.searchStatus !== ''}
							aria-pressed={materialState.searchStatus === ''}
							onclick={() => (materialState.searchStatus = '')}
						>
							Todos
						</button>
						<button
							type="button"
							class="rounded-lg px-3 py-1.5 text-sm font-semibold transition-colors cursor-pointer"
							class:bg-white={materialState.searchStatus === 'ativo'}
							class:shadow-sm={materialState.searchStatus === 'ativo'}
							class:text-green-600={materialState.searchStatus === 'ativo'}
							class:text-gray-400={materialState.searchStatus !== 'ativo'}
							aria-pressed={materialState.searchStatus === 'ativo'}
							onclick={() => (materialState.searchStatus = 'ativo')}
						>
							Ativos
						</button>
						<button
							type="button"
							class="rounded-lg px-3 py-1.5 text-sm font-semibold transition-colors cursor-pointer"
							class:bg-white={materialState.searchStatus === 'inativo'}
							class:shadow-sm={materialState.searchStatus === 'inativo'}
							class:text-red-600={materialState.searchStatus === 'inativo'}
							class:text-gray-400={materialState.searchStatus !== 'inativo'}
							aria-pressed={materialState.searchStatus === 'inativo'}
							onclick={() => (materialState.searchStatus = 'inativo')}
						>
							Inativos
						</button>
					</div>

					<span class="mx-1 h-6 w-px bg-gray-200" aria-hidden="true"> </span>

					<!-- Exibir -->
					<label for="per-page-select" class="text-sm text-gray-600">Exibir</label>
					<select
						id="per-page-select"
						class="h-9 rounded-xl border border-pink-200 bg-white px-3"
						bind:value={materialState.perPage}
					>
						<option value={10}>10</option>
						<option value={25}>25</option>
						<option value={50}>50</option>
						<option value={100}>100</option>
					</select>
				</div>
			</div>
		</div>
		
		<hr class="divide-y divide-gray-100">

		{#if materialState.error}
			<div class="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-800">
				{materialState.error}
			</div>
		{/if}

		{#if materialState.loading && materialState.items.length === 0}
			<div class="py-10 text-center text-sm text-gray-600">Carregando...</div>
		{:else}
			<MaterialTable
				materials={materialState.items}
				sortBy={materialState.sortBy}
				sortAsc={materialState.sortAsc}
				onSort={(column) => materialState.setSortBy(column)}
				onEdit={openEditForm}
				onToggleStatus={handleToggleStatus}
				onDelete={openDeleteDialog}
			/>

			<div class="mt-4">
                <TablePagination
                    currentPage={materialState.page}
                    totalPages={materialState.totalPages}
                    perPage={materialState.perPage}
                    totalItems={materialState.totalItems}
                    onPageChange={(page) => (materialState.page = page)}
                />
			</div>
		{/if}
	</Card.Content>
</Card.Root>

<MaterialForm
	bind:open={formOpen}
	editingMaterial={editingMaterial}
	loading={materialState.loading}
	error={materialState.error}
	onClose={closeForm}
	onSubmit={handleFormSubmit}
/>

<DeleteConfirmDialog
	material={confirmDeleteMaterial}
	loading={materialState.loading}
	onConfirm={handleConfirmDelete}
	onCancel={handleCancelDelete}
/>

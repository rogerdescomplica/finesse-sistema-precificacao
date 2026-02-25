<script lang="ts">
  import { Button } from '$lib/components/ui/button/index.js';
  import * as Card from '$lib/components/ui/card/index.js';
  import ListFilterBar from '$lib/components/utils/ListFilterBar.svelte';
  import TablePagination from '$lib/components/utils/TablePagination.svelte';
  import { Plus } from '@lucide/svelte';
  import { auth } from '$lib/states/auth.state.svelte';
  import { useCrudPage } from '$lib/composables/crudPage.svelte.ts';
  import UsuarioForm from '$lib/components/usuarios/UsuarioForm.svelte';
  import UsuarioTable from '$lib/components/usuarios/UsuarioTable.svelte';
  import DeleteConfirmDialog from '$lib/components/usuarios/DeleteConfirmDialog.svelte';
  import { usuarioState, type SortBy } from '$lib/states/usuario.state.svelte';
  import type { Usuario, UsuarioInput } from '$lib/services/usuario.service';

  const crud = useCrudPage<Usuario, UsuarioInput, SortBy>( usuarioState, { isAuthenticated: () => auth.isAuthenticated && auth.checked } );
 
  const editingUsuario = $derived(crud.editingItem);
  const confirmDeleteUsuario = $derived(crud.confirmDeleteItem);
  let sheetOpen = $state(false);
  let successMessage = $state('');
  const SUCCESS_DISPLAY_TIME = 1800;
  let successTimer: ReturnType<typeof setTimeout>;

  async function handleSubmitUsuario(data: UsuarioInput) {
		try {
			await crud.submit(data);
			successMessage = 'Usuário salvo com sucesso';
			sheetOpen = false;
			clearTimeout(successTimer);
			successTimer = setTimeout(() => { successMessage = ''; }, SUCCESS_DISPLAY_TIME);
		} catch (error) {
			console.error('Erro ao salvar usuário:', error);
		}
	}
  
</script>

<svelte:head><title>Usuários</title></svelte:head>

<div class="flex items-center justify-between px-2 pb-5">
  <div>
    <Card.Title class="text-xl">Usuários</Card.Title>
    <Card.Description>Listagem e gerenciamento</Card.Description>
  </div>
  <Button class="inline-flex items-center gap-2 cursor-pointer" onclick={() => { crud.openCreateForm(); sheetOpen = true; }} disabled={!auth.isAuthenticated}>
    <Plus size={16} />
    Cadastrar Novo Usuário
  </Button>
  </div>

<Card.Root class="w-full">
  <Card.Content>
    <ListFilterBar
      placeholder="Buscar por nome ou email..."
      bind:search={usuarioState.search}
      bind:searchStatus={usuarioState.searchStatus}
      bind:perPage={usuarioState.perPage}
    >
      <label for="searchField"  class="ml-1 text-sm text-gray-600">Campo:</label>
      <select id="searchField" class="h-9 rounded-xl border border-pink-200 bg-white px-3 text-sm mr-2" bind:value={usuarioState.searchField}>
        <option value="nome">Nome</option>
        <option value="email">Email</option>
      </select>
    </ListFilterBar>

    <hr class="divide-y divide-gray-100" />

    {#if successMessage}
      <div class="mb-4 rounded-md bg-green-50 p-3 text-sm text-green-800">{successMessage}</div>
    {/if}
    {#if usuarioState.error}
      <div class="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-800">{usuarioState.error}</div>
    {/if}
    {#if usuarioState.loadingList && usuarioState.items.length === 0}
      <div class="py-10 text-center text-sm text-gray-600">Carregando...</div>
    {:else}
      <UsuarioTable
        usuarios={usuarioState.items}
        sortBy={usuarioState.sortBy}
        sortAsc={usuarioState.sortAsc}
        loading={usuarioState.loadingList}
        onSort={crud.onSort}
        onEdit={(u) => { sheetOpen = true; crud.openEditForm(u); }}
        onToggleStatus={crud.toggleStatus}
        onDelete={crud.openDeleteDialog}
      />
      <div class="mt-4">
        <TablePagination
          currentPage={usuarioState.page}
          totalPages={usuarioState.totalPages}
          perPage={usuarioState.perPage}
          totalItems={usuarioState.totalItems}
          onPageChange={crud.onPageChange}
        />
      </div>
    {/if}
  </Card.Content>
</Card.Root>

{#key editingUsuario ? editingUsuario.id : 'new'}
  <UsuarioForm
    bind:open={sheetOpen}
    editingUsuario={editingUsuario}
    loading={usuarioState.mutating}
    error={usuarioState.error}
    onClose={() => { sheetOpen = false; crud.closeForm(); }}
    onSubmit={handleSubmitUsuario}
  />
{/key}

<DeleteConfirmDialog
  usuario={confirmDeleteUsuario}
  loading={usuarioState.mutating}
  onConfirm={async (u) => {
    try {
      await crud.confirmDelete(u);
      successMessage = 'Usuário excluído com sucesso';
      clearTimeout(successTimer);
      successTimer = setTimeout(() => { successMessage = ''; }, SUCCESS_DISPLAY_TIME);
    } catch (error) {
      console.error('Erro ao excluir usuário:', error);
    }
  }}
  onCancel={crud.cancelDelete}
/>

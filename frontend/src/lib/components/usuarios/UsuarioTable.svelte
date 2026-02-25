<script lang="ts">
  import type { Usuario } from '$lib/services/usuario.service';
  import type { SortBy } from '$lib/states/usuario.state.svelte';
  import { PenLine, Trash2 } from '@lucide/svelte';
  import { getStatusClass, getStatusText } from '$lib/utils/formatters';

  interface Props {
    usuarios: Usuario[];
    sortBy: SortBy;
    sortAsc: boolean;
    onSort: (column: SortBy) => void;
    onEdit: (usuario: Usuario) => void;
    onToggleStatus: (usuario: Usuario) => void;
    onDelete: (usuario: Usuario) => void; 
    loading?: boolean;
  }

  let {
    usuarios,
    sortBy,
    sortAsc,
    onSort,
    onEdit,
    onToggleStatus,
    onDelete,
    loading = false
  }: Props = $props();

  const columns: { key: SortBy; label: string; sortable: boolean }[] = [
    { key: 'id', label: 'ID', sortable: true },
    { key: 'nome', label: 'Nome', sortable: true },
    { key: 'email', label: 'Email', sortable: true },
    { key: 'ativo', label: 'Status', sortable: true }
  ];

  function handleSort(column: SortBy) {
    if (!loading) onSort(column);
  }
</script>

<div class="overflow-x-auto">
  <table class="min-w-full divide-y divide-gray-200">
    <thead class="bg-pink-50/30">
      <tr>
        {#each columns as col}
          <th
            class="whitespace-nowrap px-4 py-2 text-left text-sm font-semibold text-gray-700"
            class:cursor-pointer={col.sortable && !loading}
            onclick={() => col.sortable && handleSort(col.key)}
            role={col.sortable ? 'button' : undefined}
            tabindex={col.sortable ? 0 : undefined}
          >
            <span class="flex items-center gap-1">
              {col.label}
              {#if col.sortable && sortBy === col.key}
                <span class="text-xs">{sortAsc ? '▲' : '▼'}</span>
              {/if}
            </span>
          </th>
        {/each}
        <th class="px-4 py-2 text-left text-sm font-semibold text-gray-700">Perfil</th>
        <th class="px-4 py-2 text-right text-sm font-semibold text-gray-700">Ações</th>
      </tr>
    </thead>

    <tbody class="divide-y divide-gray-100 bg-white">
      {#if usuarios.length === 0}
        <tr>
          <td colspan="8" class="px-4 py-8 text-center text-sm text-gray-500">Nenhum usuário encontrado</td>
        </tr>
      {:else}
        {#each usuarios as u (u.id)}
          <tr class="hover:bg-gray-50 transition-colors">
            <td class="px-4 py-2 text-sm text-gray-900">{u.id}</td>
            <td class="px-4 py-2 text-sm font-medium text-gray-900">{u.nome}</td>
            <td class="px-4 py-2 text-sm text-gray-700">{u.email}</td>
            <td class="px-4 py-2 text-sm {getStatusClass(u.ativo)}">{getStatusText(u.ativo)}</td>
            <td class="px-4 py-2 text-sm text-gray-700">{u.perfil}</td>
            <td class="px-4 py-2 text-right">
              <div class="flex items-center justify-end gap-2">
                <button
                  class="inline-flex h-9 items-center justify-center rounded-md border px-3 text-sm hover:bg-gray-50 transition-colors"
                  class:border-green-300={!u.ativo}
                  class:text-green-700={!u.ativo}
                  class:border-gray-300={u.ativo}
                  onclick={() => onToggleStatus(u)}
                  title={u.ativo ? 'Inativar' : 'Ativar'}
                >
                  {u.ativo ? 'Inativar' : 'Ativar'}
                </button>
                <button
                  class="inline-flex h-9 items-center justify-center rounded-md border border-gray-300 px-3 text-sm hover:bg-gray-50 transition-colors"
                  onclick={() => onEdit(u)}
                  title="Editar"
                >
                  <PenLine size={16} />
                </button>
                <button
                  class="inline-flex h-9 items-center justify-center rounded-md border border-red-300 px-3 text-sm text-red-700 hover:bg-red-50 transition-colors"
                  onclick={() => onDelete(u)}
                  title="Excluir"
                >
                  <Trash2 size={16} />
                </button>
              </div>
            </td>
          </tr>
        {/each}
      {/if}
    </tbody>
  </table>
</div>

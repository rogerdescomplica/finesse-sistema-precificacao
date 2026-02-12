<script lang="ts">
	import type { Servico } from '$lib/services/servico.service';
	import type { SortBy } from '$lib/states/servico.state.svelte.ts';
	import { PenLine, Trash2 } from '@lucide/svelte';
	import { getStatusClass, getStatusText, formatCurrency } from '$lib/utils/formatters';

	interface Props {
		servicos: Servico[];
		sortBy: SortBy;
		sortAsc: boolean;
		onSort: (column: SortBy) => void;
		onEdit: (servico: Servico) => void;
		onToggleStatus: (servico: Servico) => void;
		onDelete: (servico: Servico) => void;
		loading?: boolean;
	}

	let { 
		servicos, 
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
		{ key: 'grupo', label: 'Categoria', sortable: true },
		{ key: 'duracaoMinutos', label: 'Duração (min)', sortable: true }
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
				<th class="whitespace-nowrap px-4 py-2 text-right text-sm font-semibold text-gray-700">
					Valor
				</th>
				<th
					class="whitespace-nowrap px-4 py-2 text-left text-sm font-semibold text-gray-700 cursor-pointer"
					onclick={() => handleSort('ativo')}
					role="button"
					tabindex={0}
				>
					<span class="flex items-center gap-1">
						Status
						{#if sortBy === 'ativo'}
							<span class="text-xs">{sortAsc ? '▲' : '▼'}</span>
						{/if}
					</span>
				</th>
				<th class="px-4 py-2 text-right text-sm font-semibold text-gray-700">
					Ações
				</th>
			</tr>
		</thead>
		
		<tbody class="divide-y divide-gray-100 bg-white">
			{#if servicos.length === 0}
				<tr>
					<td colspan="7" class="px-4 py-8 text-center text-sm text-gray-500">
						Nenhum serviço encontrado
					</td>
				</tr>
			{:else}
				{#each servicos as servico (servico.id)}
					<tr class="hover:bg-gray-50 transition-colors">
						<td class="px-4 py-2 text-sm text-gray-900">{servico.id}</td>
						<td class="px-4 py-2 text-sm font-medium text-gray-900">{servico.nome}</td>
						<td class="px-4 py-2 text-sm text-gray-700">{servico.grupo}</td>
						<td class="px-4 py-2 text-sm text-gray-700">{servico.duracaoMinutos}</td>
						<td class="px-4 py-2 text-sm text-gray-700 text-right font-medium">
							{#if servico.precoVigente != null}
								{formatCurrency(Number(servico.precoVigente || 0))}
							{:else}
								—
							{/if}
						</td>
						<td class="px-4 py-2 text-sm {getStatusClass(servico.ativo)}">{getStatusText(servico.ativo)}</td>
						<td class="px-4 py-2 text-right">
							<div class="flex items-center justify-end gap-2">
								<button
									class="cursor-pointer inline-flex h-9 items-center justify-center rounded-md border px-3 text-sm hover:bg-gray-50 transition-colors"
									class:border-green-300={!servico.ativo}
									class:text-green-700={!servico.ativo}
									class:border-gray-300={servico.ativo}
									onclick={() => onToggleStatus(servico)}
									title={servico.ativo ? 'Inativar' : 'Ativar'}
								>
									{servico.ativo ? 'Inativar' : 'Ativar'}
								</button>
								<button
									class="cursor-pointer inline-flex h-9 items-center justify-center rounded-md border border-gray-300 px-3 text-sm hover:bg-gray-50 transition-colors"
									onclick={() => onEdit(servico)}
									title="Editar"
								>
									<PenLine size={16} />
								</button>
								<button
									class="cursor-pointer inline-flex h-9 items-center justify-center rounded-md border border-red-300 px-3 text-sm text-red-700 hover:bg-red-50 transition-colors"
									onclick={() => onDelete(servico)}
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

<script lang="ts">
	import type { Material } from '$lib/services/material.service';
	import type { SortBy } from '$lib/states/material.state.svelte.ts';
	import { PenLine, Trash2 } from '@lucide/svelte';
	import { formatCurrency, formatNumber, getStatusClass, getStatusText } from '$lib/utils/formatters';

	interface Props {
		materials: Material[];
		sortBy: SortBy;
		sortAsc: boolean;
		onSort: (column: SortBy) => void;
		onEdit: (material: Material) => void;
		onToggleStatus: (material: Material) => void;
		onDelete: (material: Material) => void;
		loading?: boolean;
	}

	let { 
		materials, 
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
		{ key: 'produto', label: 'Produto', sortable: true },
		{ key: 'unidadeMedida', label: 'Unidade', sortable: true },
		{ key: 'volumeEmbalagem', label: 'Volume', sortable: true },
		{ key: 'precoEmbalagem', label: 'Preço', sortable: true },
		{ key: 'custoUnitario', label: 'Custo Unitário', sortable: true },
		{ key: 'ativo', label: 'Status', sortable: true }
	];

	function handleSort(column: SortBy) {
		if (!loading) onSort(column);
	}
	const unitDescriptions: Record<Material['unidadeMedida'], string> = {
		UN: 'Unidade',
		KG: 'Quilograma',
		G: 'Grama',
		L: 'Litro',
		ML: 'Mililitro',
		M: 'Metro'
	};
	function unitDesc(u: Material['unidadeMedida']) {
		return unitDescriptions[u] ?? String(u);
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
				<th class="px-4 py-2 text-right text-sm font-semibold text-gray-700">
					Ações
				</th>
			</tr>
		</thead>
		
		<tbody class="divide-y divide-gray-100 bg-white">
			{#if materials.length === 0}
				<tr>
					<td colspan="8" class="px-4 py-8 text-center text-sm text-gray-500">
						Nenhum material encontrado
					</td>
				</tr>
			{:else}
				{#each materials as material (material.id)}
					<tr class="hover:bg-gray-50 transition-colors">
						<td class="px-4 py-2 text-sm text-gray-900">
							{material.id}
						</td>
						
						<td class="px-4 py-2 text-sm font-medium text-gray-900">
							{material.produto}
						</td>
						
						<td class="px-4 py-2 text-sm text-gray-700">
							<span class="relative inline-block group" title={unitDesc(material.unidadeMedida)}>
								{material.unidadeMedida}
								<span class="pointer-events-none absolute left-1/2 -translate-x-1/2 bottom-full mb-1 hidden group-hover:block whitespace-nowrap rounded-md bg-gray-800 px-2 py-1 text-xs text-white shadow">
									{unitDesc(material.unidadeMedida)}
								</span>
							</span>
						</td>
						
						<td class="px-4 py-2 text-sm text-gray-700">
							{formatNumber(material.volumeEmbalagem, 2)}
						</td>
						
						<td class="px-4 py-2 text-sm font-medium text-gray-700">
							{formatCurrency(material.precoEmbalagem)}
						</td>
						
						<td class="px-4 py-2 text-sm text-gray-700">
							{formatNumber(material.custoUnitario, 2)}
						</td>
						
						<td class="px-4 py-2 text-sm {getStatusClass(material.ativo)}">
							{getStatusText(material.ativo)}
						</td>
						
						<td class="px-4 py-2 text-right">
							<div class="flex items-center justify-end gap-2">
								
								<button
									class="cursor-pointer inline-flex h-9 items-center justify-center rounded-md border px-3 text-sm hover:bg-gray-50 transition-colors"
									class:border-green-300={!material.ativo}
									class:text-green-700={!material.ativo}
									class:border-gray-300={material.ativo}
									onclick={() => onToggleStatus(material)}
									title={material.ativo ? 'Inativar' : 'Ativar'}
								>
									{material.ativo ? 'Inativar' : 'Ativar'}
								</button>

								<button
									class="cursor-pointer inline-flex h-9 items-center justify-center rounded-md border border-gray-300 px-3 text-sm hover:bg-gray-50 transition-colors"
									onclick={() => onEdit(material)}
									title="Editar"
								>
									<PenLine size={16} />
								</button>
												
								<button
									class="cursor-pointerinline-flex h-9 items-center justify-center rounded-md border border-red-300 px-3 text-sm text-red-700 hover:bg-red-50 transition-colors"
									onclick={() => onDelete(material)}
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

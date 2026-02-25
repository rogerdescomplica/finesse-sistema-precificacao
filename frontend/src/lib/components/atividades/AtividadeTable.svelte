<script lang="ts">
	import type { Atividade } from '$lib/services/atividade.service';
	import type { SortBy } from '$lib/states/atividade.state.svelte.ts';
	import { PenLine, Trash2 } from '@lucide/svelte';
	import { getStatusClass, getStatusText, formatNumber } from '$lib/utils/formatters';

	interface Props {
		atividades: Atividade[];
		sortBy: SortBy;
		sortAsc: boolean;
		onSort: (column: SortBy) => void;
		onEdit: (atividade: Atividade) => void;
		onToggleStatus: (atividade: Atividade) => void;
		onDelete: (atividade: Atividade) => void;
		loading?: boolean;
	}

	let {
		atividades,
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
		{ key: 'cnae', label: 'CNAE', sortable: true },
		{ key: 'aliquotaTotalPct', label: 'Alíquota Total (%)', sortable: true },
		{ key: 'issPct', label: 'ISS (%)', sortable: true },
		{ key: 'ativo', label: 'Status', sortable: true }
	];

	function handleSort(column: SortBy) {
		if (!loading) onSort(column);
	}
	function fmtPct(n: number) {
		return `${formatNumber(n, 2)}%`;
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
			{#if atividades.length === 0}
				<tr>
					<td colspan="5" class="px-4 py-8 text-center text-sm text-gray-500">
						Nenhuma atividade encontrada
					</td>
				</tr>
			{:else}
				{#each atividades as atividade (atividade.id)}
					<tr class="hover:bg-gray-50 transition-colors">
						<td class="px-4 py-2 text-sm text-gray-900">
							{atividade.id}
						</td>
						<td class="px-4 py-2 text-sm font-medium text-gray-900">
							{atividade.nome}
						</td>
						<td class="px-4 py-2 text-sm text-gray-700">
							{atividade.cnae}
						</td>
						<td class="px-4 py-2 text-sm text-gray-700">
							{fmtPct(atividade.aliquotaTotalPct)}
						</td>
						<td class="px-4 py-2 text-sm text-gray-700">
							{fmtPct(atividade.issPct)}
						</td>
						<td class="px-4 py-2 text-sm {getStatusClass(atividade.ativo)}">
							{getStatusText(atividade.ativo)}
						</td>
						<td class="px-4 py-2 text-right">
							<div class="flex items-center justify-end gap-2">
								<button
									class="inline-flex h-9 items-center justify-center rounded-md border px-3 text-sm hover:bg-gray-50 transition-colors"
									class:border-green-300={!atividade.ativo}
									class:text-green-700={!atividade.ativo}
									class:border-gray-300={atividade.ativo}
									onclick={() => onToggleStatus(atividade)}
									title={atividade.ativo ? 'Inativar' : 'Ativar'}
								>
									{atividade.ativo ? 'Inativar' : 'Ativar'}
								</button>
								<button
									class="inline-flex h-9 items-center justify-center rounded-md border border-gray-300 px-3 text-sm hover:bg-gray-50 transition-colors"
									onclick={() => onEdit(atividade)}
									title="Editar"
								>
									<PenLine size={16} />
								</button>
								<button
									class="cursor-pointer inline-flex h-9 items-center justify-center rounded-md border border-red-300 px-3 text-sm text-red-700 hover:bg-red-50 transition-colors"
									onclick={() => onDelete(atividade)}
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

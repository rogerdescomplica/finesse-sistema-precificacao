<script lang="ts">
	import type { Configuracao } from '$lib/services/config.service';
	import { PenLine, Trash2 } from '@lucide/svelte';
	import { formatCurrency, formatNumber, getStatusClass, getStatusText } from '$lib/utils/formatters';

	interface Props {
		configs: Configuracao[];
		sortBy: keyof Configuracao;
		sortAsc: boolean;
		onSort: (column: keyof Configuracao) => void;
		onEdit: (config: Configuracao) => void;
		onToggleStatus: (config: Configuracao) => void;
		onDelete: (config: Configuracao) => void;
	}

	let { configs, sortBy, sortAsc, onSort, onEdit, onToggleStatus, onDelete }: Props = $props();

	const columns: { key: keyof Configuracao; label: string; sortable: boolean }[] = [
		{ key: 'id', label: 'ID', sortable: true },
		{ key: 'pretensaoSalarialMensal', label: 'Pretensão Mensal', sortable: true },
		{ key: 'horasSemanais', label: 'Horas Semanais', sortable: true },
		{ key: 'semanasMediaMes', label: 'Semanas/Mês', sortable: true },
		{ key: 'custoFixoPct', label: 'Custo Fixo (%)', sortable: true },
		{ key: 'margemLucroPadraoPct', label: 'Margem Lucro (%)', sortable: true },
		{ key: 'ativo', label: 'Status', sortable: true }
	];

	function handleSort(column: keyof Configuracao) { onSort(column); }
</script>

<div class="overflow-x-auto">
	<table class="min-w-full divide-y divide-gray-200">
		<thead class="bg-pink-50/30">
			<tr>
				{#each columns as col}
					<th class="whitespace-nowrap px-4 py-2 text-left text-sm font-semibold text-gray-700"
						class:cursor-pointer={col.sortable}
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
				<th class="px-4 py-2 text-right text-sm font-semibold text-gray-700">Ações</th>
			</tr>
		</thead>
		<tbody class="divide-y divide-gray-100 bg-white">
			{#if configs.length === 0}
				<tr>
					<td colspan="8" class="px-4 py-8 text-center text-sm text-gray-500">Nenhuma configuração encontrada</td>
				</tr>
			{:else}
				{#each configs as cfg (cfg.id)}
					<tr class="hover:bg-gray-50 transition-colors">
						<td class="px-4 py-2 text-sm text-gray-900">{cfg.id}</td>
						<td class="px-4 py-2 text-sm font-medium text-gray-900">{formatCurrency(cfg.pretensaoSalarialMensal)}</td>
						<td class="px-4 py-2 text-sm text-gray-700">{formatNumber(cfg.horasSemanais, 2)}</td>
						<td class="px-4 py-2 text-sm text-gray-700">{formatNumber(cfg.semanasMediaMes, 2)}</td>
						<td class="px-4 py-2 text-sm text-gray-700">{formatNumber(cfg.custoFixoPct, 4)}%</td>
						<td class="px-4 py-2 text-sm text-gray-700">{formatNumber(cfg.margemLucroPadraoPct, 4)}%</td>
						<td class="px-4 py-2 text-sm {getStatusClass(cfg.ativo)}">{getStatusText(cfg.ativo)}</td>
						<td class="px-4 py-2 text-right">
							<div class="flex items-center justify-end gap-2">
								<button
									class="inline-flex h-9 items-center justify-center rounded-md border px-3 text-sm hover:bg-gray-50 transition-colors"
									class:border-green-300={!cfg.ativo}
									class:text-green-700={!cfg.ativo}
									class:border-gray-300={cfg.ativo}
									onclick={() => onToggleStatus(cfg)}
									title={cfg.ativo ? 'Inativar' : 'Ativar'}
								>
									{cfg.ativo ? 'Inativar' : 'Ativar'}
								</button>
								<button
									class="inline-flex h-9 items-center justify-center rounded-md border border-gray-300 px-3 text-sm hover:bg-gray-50 transition-colors"
									onclick={() => onEdit(cfg)}
									title="Editar"
								>
									<PenLine size={16} />
								</button>
								<button
									class="inline-flex h-9 items-center justify-center rounded-md border border-red-300 px-3 text-sm text-red-700 hover:bg-red-50 transition-colors"
									onclick={() => onDelete(cfg)}
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

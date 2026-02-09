<script lang="ts">
	import { Input } from '$lib/components/ui/input/index.js';
	import { Button } from '$lib/components/ui/button/index.js';
	import { Plus, Trash2 } from '@lucide/svelte';
	import type { Material } from '$lib/services/material.service';
	import { materialService } from '$lib/services/material.service';
	import { formatCurrency, formatNumber } from '$lib/utils/formatters';

	type Item = { material: Material; quantidadeUsada: number };

	interface Props {
		items: Item[];
		onItemsChange?: (items: Item[]) => void;
	}

	let { items = $bindable<Item[]>([]), onItemsChange }: Props = $props();

	let qty = $state<number | ''>('');
	let suggestions = $state<Material[]>([]);
	let loading = $state(false);
	let error = $state('');
	let selectedId = $state<number | ''>('');

	$effect(() => {
		void loadSuggestions();
	});

	async function loadSuggestions() {
		loading = true;
		error = '';
		try {
			const params = new URLSearchParams();
			params.set('ativo', 'true');
			params.set('size', '200');
			params.set('sort', 'produto,asc');
			const page = await materialService.list(params);
			suggestions = page.content ?? [];
		} catch (e) {
			error = e instanceof Error ? e.message : 'Erro ao carregar insumos';
		} finally {
			loading = false;
		}
	}

	function addItem() {
		if (selectedId === '' || qty === '' || Number(qty) <= 0) return;
		const mat = suggestions.find((m) => m.id === Number(selectedId));
		if (!mat) return;
		const quantidadeUsada = Number(qty);
		const existing = items.find((it) => it.material.id === mat.id);
		if (existing) {
			existing.quantidadeUsada = quantidadeUsada;
			items = [...items];
		} else {
			items = [...items, { material: mat, quantidadeUsada }];
		}
		onItemsChange?.(items);
		qty = '';
	}

	function removeItem(id: number) {
		items = items.filter((it) => it.material.id !== id);
		onItemsChange?.(items);
	}
</script>

<div class="space-y-3 rounded-2xl border border-gray-100 bg-gray-50/40 p-3">
	<div class="px-1 text-xs font-semibold uppercase tracking-wide text-gray-400">
		Materiais associados
	</div>

	<div class="flex items-center gap-2">
		<div class="flex-1">
			<select
				class="h-10 w-full rounded-xl border border-gray-200 bg-white px-3 text-sm"
				bind:value={selectedId}
			>
				<option value=''>{loading ? 'Carregando...' : 'Selecione Insumo...'}</option>
				{#if !loading}
					{#each suggestions as s}
						<option value={s.id}>{s.produto}</option>
					{/each}
				{/if}
			</select>
		</div>

		<Input
			placeholder="Qtd"
			type="number"
			min="0"
			step="1"
			bind:value={qty}
			class="h-10 w-24 rounded-xl"
		/>

		<Button class="h-10 w-10 rounded-xl" onclick={addItem} title="Adicionar">
			<Plus size={16} />
		</Button>
	</div>

	{#if error}
		<div class="rounded-md bg-red-50 p-2 text-xs text-red-700">{error}</div>
	{/if}

	<div class="space-y-2">
		{#each items as it (it.material.id)}
			<div class="flex items-center justify-between rounded-xl bg-white px-4 py-3 shadow-sm ring-1 ring-gray-100">
				<div>
					<div class="text-sm font-semibold text-gray-900">{it.material.produto} - {it.material.unidadeMedida}</div>
					<div class="mt-1 flex items-center gap-2 text-xs text-gray-500">
						<span class="rounded-md bg-gray-100 px-2 py-0.5 text-gray-600">
							{formatNumber(it.quantidadeUsada, 0)}x
						</span>
						<span>Custo Unit: {formatCurrency(it.material.custoUnitario)}</span>
					</div>
				</div>
				<div class="flex items-center gap-3">
					<div class="text-sm font-semibold text-gray-800">
						{formatCurrency(it.material.custoUnitario * it.quantidadeUsada)}
					</div>
					<button
						class="cursor-pointer rounded-md border border-red-300 px-2 py-1 text-xs text-red-700 hover:bg-red-50"
						title="Remover"
						onclick={() => removeItem(it.material.id)}
					>
						<Trash2 size={14} />
					</button>
				</div>
			</div>
		{/each}
		{#if items.length === 0}
			<div class="px-2 py-4 text-center text-xs text-gray-500">Nenhum insumo adicionado</div>
		{/if}
	</div>
</div>

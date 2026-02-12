<script lang="ts">
	import { Input } from '$lib/components/ui/input/index.js';
	import { Search } from '@lucide/svelte';

	type Status = '' | 'ativo' | 'inativo';

	// binds (cada página liga no state dela)
	export let search: string;
	export let searchStatus: Status = '';
	export let perPage: number = 10;

	// customizações
	export let placeholder = 'Buscar...';
	export let inputClass = 'h-10 w-1/2 rounded-xl pl-9';
	export let showStatusTabs = true;
	export let showPerPage = true;

	const statusTabs: Array<{ label: string; value: Status; activeText: string }> = [
		{ label: 'Todos', value: '', activeText: 'text-gray-800' },
		{ label: 'Ativos', value: 'ativo', activeText: 'text-green-600' },
		{ label: 'Inativos', value: 'inativo', activeText: 'text-red-600' }
	];

	const perPageOptions = [10, 25, 50, 100];

	function tabClass(isActive: boolean, activeText: string) {
		return [
			'rounded-lg px-3 py-1.5 text-sm font-semibold transition-colors cursor-pointer',
			isActive ? `bg-white shadow-sm ${activeText}` : 'text-gray-400'
		].join(' ');
	}
</script>

<div class="mb-2 px-2 py-2">
	<div class="flex items-center gap-4">
		<div class="relative flex-1">
			<Search size={16} class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
			<Input placeholder={placeholder} bind:value={search} class={inputClass} />
		</div>

		<slot />

		<div class="flex items-center gap-3">
			{#if showStatusTabs}
				<div class="flex items-center gap-1 rounded-xl bg-gray-50 ring-1 ring-pink-100 p-1">
					{#each statusTabs as tab}
						<button
							type="button"
							class={tabClass(searchStatus === tab.value, tab.activeText)}
							aria-pressed={searchStatus === tab.value}
							onclick={() => (searchStatus = tab.value)}
						>
							{tab.label}
						</button>
					{/each}
				</div>
			{/if}

			{#if showPerPage}
				<span class="mx-1 h-6 w-px bg-gray-200" aria-hidden="true"></span>

				<label for="per-page-select" class="text-sm text-gray-600">Exibir</label>
				<select
					id="per-page-select"
					class="h-9 rounded-xl border border-pink-200 bg-white px-3"
					bind:value={perPage}
				>
					{#each perPageOptions as n}
						<option value={n}>{n}</option>
					{/each}
				</select>
			{/if}

			
		</div>
	</div>
</div>

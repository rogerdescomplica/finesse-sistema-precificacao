<script lang="ts">
	import { Button } from '$lib/components/ui/button/index.js';
	import * as Card from '$lib/components/ui/card/index.js';
	import { Input } from '$lib/components/ui/input/index.js';
	import { materialService, type Material, type UnidadeMedida } from '$lib/services/material.service';
	import { authStore } from '$lib/stores/auth.store';
	import { PenLine, Plus, Trash2 } from '@lucide/svelte';

	let items: Material[] = $state([])
	let loading = $state(false)
	let error = $state('')

	let page = $state(1)
	let perPage = $state(10)
	let search = $state('')
	let sortBy = $state<keyof Material>('id')
	let sortAsc = $state(true)

	let showModal = $state(false)
	let editing: Material | null = $state(null)
	let formProduto = $state('')
	let formUnidade: UnidadeMedida | '' = $state('')
	let formVolume: number | '' = $state('')
	let formPreco: number | '' = $state('')
	let formCusto: number | '' = $state('')
	let formObservacoes = $state('')
	let formAtivo = $state(true)
	let confirmDelete: Material | null = $state(null)

	function compare(a: Material, b: Material) {
		const av = a[sortBy] as any
		const bv = b[sortBy] as any
		if (av == null && bv != null) return sortAsc ? -1 : 1
		if (av != null && bv == null) return sortAsc ? 1 : -1
		if (typeof av === 'number' && typeof bv === 'number') return sortAsc ? av - bv : bv - av
		const res = String(av).localeCompare(String(bv))
		return sortAsc ? res : -res
	}

	let searchStatus = $state('') // '' | 'ativo' | 'inativo'
	const filtered = $derived(
		items
			.filter((m) => m.produto.toLowerCase().includes(search.toLowerCase()))
			.filter((m) =>
				searchStatus === '' ? true : searchStatus === 'ativo' ? m.ativo : !m.ativo
			)
			.sort(compare)
	)

	const totalPages = $derived(Math.max(1, Math.ceil(filtered.length / perPage)))
	const paged = $derived(filtered.slice((page - 1) * perPage, (page - 1) * perPage + perPage))

	async function load() {
		loading = true
		error = ''
		try {
			const page = await materialService.list()
			items = page.content
		} catch (e) {
			error = e instanceof Error ? e.message : 'Erro ao carregar'
		} finally {
			loading = false
		}
	}

	function openCreate() {
		editing = null
		formProduto = ''
		formUnidade = ''
		formVolume = ''
		formPreco = ''
		formCusto = ''
		formObservacoes = ''
		formAtivo = true
		showModal = true
	}

	function openEdit(m: Material) {
		editing = m
		formProduto = m.produto
		formUnidade = m.unidadeMedida
		formVolume = m.volumeEmbalagem
		formPreco = m.precoEmbalagem
		formCusto = m.custoUnitario
		formObservacoes = m.observacoes || ''
		formAtivo = m.ativo
		showModal = true
	}

	function closeModal() {
		showModal = false
	}

	async function submit() {
		error = ''
		if (!formProduto.trim()) {
			error = 'Produto é obrigatório'
			return
		}
		if (!formUnidade) {
			error = 'Unidade de Medida é obrigatória'
			return
		}
		if (formVolume === '' || Number(formVolume) <= 0) {
			error = 'Volume da Embalagem deve ser positivo'
			return
		}
		if (formPreco === '' || Number(formPreco) <= 0) {
			error = 'Preço da Embalagem deve ser positivo'
			return
		}
		if (formCusto === '') {
			error = 'Custo Unitário é obrigatório'
			return
		}
		loading = true
		try {
			if (editing) {
				const updated = await materialService.update(editing.id, {
					produto: formProduto,
					unidadeMedida: formUnidade as UnidadeMedida,
					volumeEmbalagem: Number(formVolume),
					precoEmbalagem: Number(formPreco),
					custoUnitario: Number(formCusto),
					observacoes: formObservacoes,
					ativo: formAtivo
				})
				items = items.map((i) => (i.id === updated.id ? updated : i))
			} else {
				const created = await materialService.create({
					produto: formProduto,
					unidadeMedida: formUnidade as UnidadeMedida,
					volumeEmbalagem: Number(formVolume),
					precoEmbalagem: Number(formPreco),
					custoUnitario: Number(formCusto),
					observacoes: formObservacoes,
					ativo: formAtivo
				})
				items = [created, ...items]
			}
			closeModal()
		} catch (e) {
			error = e instanceof Error ? e.message : 'Erro ao salvar'
		} finally {
			loading = false
		}
	}

	async function toggleStatus(m: Material) {
		loading = true
		try {
			const updated = await materialService.toggleStatus(m.id, !m.ativo)
			items = items.map((i) => (i.id === updated.id ? updated : i))
		} catch (e) {
			error = e instanceof Error ? e.message : 'Erro ao alterar status'
		} finally {
			loading = false
		}
	}

	async function doDelete(m: Material) {
		loading = true
		try {
			await materialService.remove(m.id)
			items = items.filter((i) => i.id !== m.id)
			confirmDelete = null
		} catch (e) {
			error = e instanceof Error ? e.message : 'Erro ao excluir'
		} finally {
			loading = false
		}
	}

	$effect(() => {
		load()
	})
</script>

<svelte:head>
	<title>Materiais</title>
</svelte:head>

<Card.Root class="w-full">
	<Card.Header>
		<div class="flex items-center justify-between gap-2">
			<div>
				<Card.Title class="text-xl">Materiais</Card.Title>
				<Card.Description>Listagem e gerenciamento</Card.Description>
			</div>
			<Button
				class="inline-flex items-center gap-2"
				onclick={openCreate}
				disabled={!$authStore.isAuthenticated}
			>
				<Plus size={16} /> Cadastrar Novo Material
			</Button>
		</div>
	</Card.Header>
	<Card.Content>
		<div class="mb-4 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
			<Input placeholder="Buscar por produto..." bind:value={search} class="md:w-80" />
			<div class="flex items-center gap-3">
				<label for="status-filter" class="text-sm text-gray-600">Status</label>
				<select
					id="status-filter"
					class="h-10 rounded-md border border-gray-300 px-2"
					bind:value={searchStatus}
				>
					<option value="">Todos</option>
					<option value="ativo">Ativo</option>
					<option value="inativo">Inativo</option>
				</select>
			</div>
			<div class="flex items-center gap-3">
				<label for="per-page-select" class="text-sm text-gray-600">Registros por página</label>
				<select
					id="per-page-select"
					class="h-10 rounded-md border border-gray-300 px-2"
					bind:value={perPage}
				>
					<option value={10}>10</option>
					<option value={25}>25</option>
					<option value={50}>50</option>
					<option value={100}>100</option>
				</select>
			</div>
		</div>

		{#if error}
			<div class="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-800">{error}</div>
		{/if}

		{#if loading}
			<div class="py-10 text-center text-sm text-gray-600">Carregando...</div>
		{:else}
			<div class="overflow-x-auto">
				<table class="min-w-full divide-y divide-gray-200">
					<thead class="bg-gray-50">
						<tr>
							{#each ['id', 'produto', 'unidadeMedida', 'volumeEmbalagem', 'precoEmbalagem', 'custoUnitario', 'ativo'] as col (col)}
								<th
									class="cursor-pointer whitespace-nowrap px-4 py-2 text-left text-sm font-semibold text-gray-700"
									onclick={() => {
										sortBy = col as any
										sortAsc = sortBy === (col as any) ? !sortAsc : true
									}}
								>
									{col}
								</th>
							{/each}
							<th class="px-4 py-2 text-right text-sm font-semibold text-gray-700">Ações</th>
						</tr>
					</thead>
					<tbody class="divide-y divide-gray-100 bg-white">
						{#each paged as m (m.id)}
							<tr>
								<td class="px-4 py-2 text-sm text-gray-900">{m.id}</td>
								<td class="px-4 py-2 text-sm text-gray-900">{m.produto}</td>
								<td class="px-4 py-2 text-sm text-gray-700">{m.unidadeMedida}</td>
								<td class="px-4 py-2 text-sm text-gray-700">
									{Number(m.volumeEmbalagem).toFixed(2)}
								</td>
								<td class="px-4 py-2 text-sm text-gray-700">
									{new Intl.NumberFormat('pt-BR', {
										style: 'currency',
										currency: 'BRL'
									}).format(Number(m.precoEmbalagem))}
								</td>
								<td class="px-4 py-2 text-sm text-gray-700">
									{Number(m.custoUnitario).toFixed(6)}
								</td>
								<td class="px-4 py-2 text-sm {m.ativo ? 'text-green-600' : 'text-gray-500'}">
									{m.ativo ? 'Ativo' : 'Inativo'}
								</td>
								<td class="px-4 py-2 text-right">
									<button
										class="inline-flex h-9 items-center justify-center rounded-md border border-gray-300 px-3 text-sm hover:bg-gray-50"
										onclick={() => openEdit(m)}
										title="Editar"
									>
										<PenLine size={16} />
									</button>
									<button
										class="ml-2 inline-flex h-9 items-center justify-center rounded-md border px-3 text-sm hover:bg-gray-50"
										onclick={() => toggleStatus(m)}
										title={m.ativo ? 'Inativar' : 'Ativar'}
									>
										{m.ativo ? 'Inativar' : 'Ativar'}
									</button>
									<button
										class="ml-2 inline-flex h-9 items-center justify-center rounded-md border border-red-300 px-3 text-sm text-red-700 hover:bg-red-50"
										onclick={() => (confirmDelete = m)}
										title="Excluir"
									>
										<Trash2 size={16} />
									</button>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>

			<div class="mt-4 flex items-center justify-between">
				<div class="text-sm text-gray-600">Página {page} de {totalPages}</div>
				<div class="flex items-center gap-2">
					<button
						class="rounded-md border border-gray-300 px-3 py-2 text-sm hover:bg-gray-50"
						onclick={() => {
							page = 1
						}}
						disabled={page === 1}>«</button
					>
					<button
						class="rounded-md border border-gray-300 px-3 py-2 text-sm hover:bg-gray-50"
						onclick={() => {
							page = Math.max(1, page - 1)
						}}
						disabled={page === 1}>‹</button
					>
					<button
						class="rounded-md border border-gray-300 px-3 py-2 text-sm hover:bg-gray-50"
						onclick={() => {
							page = Math.min(totalPages, page + 1)
						}}
						disabled={page === totalPages}>›</button
					>
					<button
						class="rounded-md border border-gray-300 px-3 py-2 text-sm hover:bg-gray-50"
						onclick={() => {
							page = totalPages
						}}
						disabled={page === totalPages}>»</button
					>
				</div>
			</div>
		{/if}
	</Card.Content>
</Card.Root>

{#if showModal}
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
		<div class="w-full max-w-lg rounded-xl bg-white p-6 shadow-panel">
			<h2 class="text-lg font-semibold">
				{editing ? 'Editar Material' : 'Cadastrar Material'}
			</h2>
			<div class="mt-4 space-y-3">
				<div>
					<label for="produto" class="mb-1 block text-sm text-gray-700">Produto</label>
					<Input id="produto" bind:value={formProduto} maxlength={200} placeholder="Produto" />
				</div>
				<div class="grid grid-cols-1 gap-3 md:grid-cols-2">
					<div>
						<label for="unidade" class="mb-1 block text-sm text-gray-700"
							>Unidade de Medida</label
						>
						<select
							id="unidade"
							class="h-10 w-full rounded-md border border-gray-300 px-2"
							bind:value={formUnidade}
						>
							<option value="">Selecione</option>
							<option value="UN">UN</option>
							<option value="KG">KG</option>
							<option value="G">G</option>
							<option value="L">L</option>
							<option value="ML">ML</option>
						</select>
					</div>
					<div>
						<label for="volume" class="mb-1 block text-sm text-gray-700"
							>Volume da Embalagem</label
						>
						<Input id="volume" type="number" step="0.01" min="0" bind:value={formVolume} />
					</div>
					<div>
						<label for="preco" class="mb-1 block text-sm text-gray-700"
							>Preço da Embalagem</label
						>
						<Input id="preco" type="number" step="0.01" min="0" bind:value={formPreco} />
					</div>
					<div>
						<label for="custo" class="mb-1 block text-sm text-gray-700">Custo Unitário</label>
						<Input id="custo" type="number" step="0.000001" bind:value={formCusto} />
					</div>
				</div>
				<div>
					<label for="obs" class="mb-1 block text-sm text-gray-700">Observações</label>
					<Input id="obs" bind:value={formObservacoes} maxlength={500} placeholder="Observações" />
				</div>
				<label for="ativo-checkbox" class="flex items-center gap-2 text-sm text-gray-700">
					<input id="ativo-checkbox" type="checkbox" bind:checked={formAtivo} /> Ativo
				</label>
			</div>
			<div class="mt-6 flex items-center justify-end gap-2">
				<Button variant="secondary" onclick={closeModal}>Cancelar</Button>
				<Button onclick={submit} disabled={loading}
					>{loading ? 'Salvando...' : 'Salvar'}</Button
				>
			</div>
		</div>
	</div>
{/if}

{#if confirmDelete}
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
		<div class="w-full max-w-md rounded-xl bg-white p-6 shadow-panel">
			<p class="text-sm">Confirma excluir o material "{confirmDelete.produto}"?</p>
			<div class="mt-6 flex items-center justify-end gap-2">
				<Button variant="secondary" onclick={() => (confirmDelete = null)}>Cancelar</Button>
				<Button
					class="bg-red-600 hover:bg-red-700"
					onclick={() => doDelete(confirmDelete!)}
					disabled={loading}
				>
					{loading ? 'Excluindo...' : 'Excluir'}
				</Button>
			</div>
		</div>
	</div>
{/if}
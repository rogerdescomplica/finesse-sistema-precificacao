<script lang="ts">
	import { Button } from '$lib/components/ui/button/index.js';
	import { Input } from '$lib/components/ui/input/index.js';
	import * as Sheet from '$lib/components/ui/sheet/index.ts';
	import type { Material, UnidadeMedida } from '$lib/services/material.service';
	import { CircleCheck, Package, Save, X } from '@lucide/svelte';
	import { FormValidation } from '$lib/components/materiais/validation/material.validation.svelte';

	interface Props {
		open: boolean;
		editingMaterial?: Material | null;
		loading?: boolean;
		error?: string;
		onClose: () => void;
		onSubmit: (data: { 
			produto: string;
			unidadeMedida: UnidadeMedida;
			volumeEmbalagem: number;
			precoEmbalagem: number;
			custoUnitario: number;
			observacoes: string;
			ativo: boolean;
		}) => Promise<void>;
	}

	let { 
		open = $bindable(),
		editingMaterial = null,
		loading = false,
		error = '',
		onClose,
		onSubmit
	}: Props = $props();

	// Instância de validação
	const form = new FormValidation();

	const isEditing = $derived(editingMaterial !== null);
	const title = $derived(isEditing ? 'Editar Material' : 'Cadastrar Material');

	// Quando abre o formulário, preenche ou reseta
	$effect(() => {
		if (open) {
			if (editingMaterial) {
				form.populate({
					produto: editingMaterial.produto,
					unidade: editingMaterial.unidadeMedida,
					volume: editingMaterial.volumeEmbalagem,
					preco: editingMaterial.precoEmbalagem,
					observacoes: editingMaterial.observacoes || '',
					ativo: editingMaterial.ativo
				});
			} else {
				form.reset();
			}
		}
	});

	async function handleSubmit() {
		if (!form.validateAll()) return;

		await onSubmit({
			produto: form.produto.trim(),
			unidadeMedida: form.unidade as UnidadeMedida,
			volumeEmbalagem: Number(form.volume),
			precoEmbalagem: Number(form.preco),
			custoUnitario: form.custoUnitario,
			observacoes: form.observacoes.trim(),
			ativo: form.ativo
		});
	}

	function handleClose() {
		form.reset();
		onClose();
	}
</script>

<Sheet.Root bind:open>
	<Sheet.Content side="right" hideDefaultClose class="flex flex-col gap-0 p-0 sm:max-w-md">
		<!-- Header Fixo -->
		<Sheet.Header
			class="sticky top-0 z-10 flex-row items-center justify-between gap-0 border-b border-pink-100 bg-white px-6 py-5"
		>
			<h2 class="text-xl font-bold text-gray-800">{title}</h2>
			
			<Sheet.Close
				class="rounded-full p-2 text-gray-400 transition-colors hover:bg-pink-50 hover:text-pink-500 focus:outline-none focus:ring-pink-200"
			>
				<X class="h-5 w-5" aria-hidden="true" />
			</Sheet.Close>
		</Sheet.Header>

		<!-- Conteúdo Rolável -->
		<div class="flex-1 space-y-6 overflow-y-auto px-6 py-6">
			<!-- Nome do Produto -->
			<div>
				<label for="produto" class="mb-1.5 block text-sm font-semibold text-gray-700">
					Nome do Produto
				</label>
				<Input
					id="produto"
					type="text"
					bind:value={form.produto}
					maxlength={200}
					placeholder="Ex: Sabonete Líquido Facial"
					autofocus
					oninput={() => form.scheduleErrorDisplay('produto')}
					onblur={() => form.displayError('produto')}
				/>
				{#if form.shouldShowError('produto')}
					<p class="mt-1 text-xs text-red-600">{form.errors.produto}</p>
				{/if}
			</div>

			<!-- Seção PRECIFICAÇÃO -->
			<div class="space-y-5 rounded-xl border border-pink-100 bg-white p-5 shadow-sm">
				<h3 class="flex items-center text-xs font-bold uppercase tracking-wider text-gray-400">
					<Package size={14} class="mr-2" />
					Precificação
				</h3>

				<!-- Grid Preço e Volume -->
				<div class="grid grid-cols-2 gap-4">
					<div>
						<label for="preco" class="mb-1.5 block text-xs font-medium text-gray-500">
							Preço da Embalagem
						</label>
						<div class="relative">
							<span class="absolute left-3 top-3 text-sm font-medium text-gray-400">R$</span>
							<Input
								id="preco"
								type="number"
								step="0.01"
								min="0"
								bind:value={form.preco}
								placeholder="0,00"
								class="w-full rounded-lg border border-gray-200 py-2.5 pl-9 pr-3 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-blue-300"
								oninput={() => form.scheduleErrorDisplay('preco')}
								onblur={() => form.displayError('preco')}
							/>
						</div>
						{#if form.shouldShowError('preco')}
							<p class="mt-1 text-xs text-red-600">{form.errors.preco}</p>
						{/if}
					</div>

					<div>
						<label for="volume" class="mb-1.5 block text-xs font-medium text-gray-500">
							Volume Total
						</label>
						<Input
							id="volume"
							type="number"
							step="0.01"
							min="0"
							bind:value={form.volume}
							placeholder="0"
							class="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-blue-300"
							oninput={() => form.scheduleErrorDisplay('volume')}
							onblur={() => form.displayError('volume')}
						/>
						{#if form.shouldShowError('volume')}
							<p class="mt-1 text-xs text-red-600">{form.errors.volume}</p>
						{/if}
					</div>
				</div>

				<!-- Unidade de Medida -->
				<div>
					<label for="unidade" class="mb-1.5 block text-xs font-medium text-gray-500">
						Unidade de Medida
					</label>
					<select
						id="unidade"
						class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300"
						bind:value={form.unidade}
						oninput={() => form.scheduleErrorDisplay('unidade')}
						onblur={() => form.displayError('unidade')}
					>
						<option value="" disabled>Selecione</option>
						<option value="UN">UN</option>
						<option value="KG">KG</option>
						<option value="G">G</option>
						<option value="L">L</option>
						<option value="ML">ML</option>
						<option value="M">M</option>
					</select>
					{#if form.shouldShowError('unidade')}
						<p class="mt-1 text-xs text-red-600">{form.errors.unidade}</p>
					{/if}
				</div>

				<!-- Custo Unitário Final -->
				<div class="border-t border-gray-100 pt-3">
					<div class="flex items-center justify-between">
						<span class="text-sm font-medium text-gray-600">Custo Unitário Final:</span>
						<span
							class="rounded-lg border border-blue-100 bg-blue-50 px-3 py-1 text-lg font-bold text-[#8eaaff]"
						>
							R$ {form.custoUnitarioFormatado}
						</span>
					</div>
				</div>
			</div>

			<!-- Observações -->
			<div>
				<label for="obs" class="mb-1.5 block text-sm font-semibold text-gray-700">
					Observações
				</label>
				<textarea
					id="obs"
					bind:value={form.observacoes}
					maxlength={500}
					placeholder="Informações adicionais..."
					class="min-h-[100px] w-full resize-none rounded-xl border border-red-200 bg-gray-50/50 px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300"
				></textarea>
			</div>

			<!-- Item Ativo -->
			<div
				class="cursor-pointer rounded-xl border border-gray-200 bg-gray-50/30 p-4 transition-colors hover:bg-white"
				onclick={() => (form.ativo = !form.ativo)}
				role="button"
				tabindex="0"
				onkeydown={(e) => e.key === 'Enter' && (form.ativo = !form.ativo)}
			>
				<div
					class="mr-3 flex h-5 w-5 items-center justify-center rounded border transition-colors"
					class:bg-green-500={form.ativo}
					class:border-green-500={form.ativo}
					class:bg-white={!form.ativo}
					class:border-gray-300={!form.ativo}
				>
					{#if form.ativo}
						<CircleCheck size={14} class="text-white" strokeWidth={2.5} />
					{/if}
				</div>
				<div>
					<span class="block text-sm font-medium text-gray-800">
						Item Ativo no Sistema
					</span>
					<span class="block text-xs text-gray-500">
						Ao desativar, este item não aparecerá em novos orçamentos.
					</span>
				</div>
			</div>

			{#if error}
				<div class="rounded-md bg-red-50 p-3 text-sm text-red-800">
					{error}
				</div>
			{/if}
		</div>

		<!-- Footer Fixo -->
		<Sheet.Footer class="sticky bottom-0 border-t bg-white px-6 py-4">
			<div class="flex w-full items-center justify-end gap-3">
				<Button variant="secondary" onclick={handleClose} class="min-w-[100px]">
					Cancelar
				</Button>
				
				<Button 
					onclick={handleSubmit} 
					disabled={loading || !form.isValid} 
					class="min-w-[100px]"
				>
					{#if loading}
						<span class="flex items-center gap-2">
							<span class="animate-spin">⏳</span>
							Salvando...
						</span>
					{:else}
						<span class="flex items-center gap-2">
							<Save size={16} />
							Salvar
						</span>
					{/if}
				</Button>
			</div>
		</Sheet.Footer>
	</Sheet.Content>
</Sheet.Root>

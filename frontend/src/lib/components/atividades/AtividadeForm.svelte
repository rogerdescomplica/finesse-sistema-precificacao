<script lang="ts">
	import { Button } from '$lib/components/ui/button/index.js';
	import { Input } from '$lib/components/ui/input/index.js';
	import { MoneyInput } from '$lib/components/ui/money-input/index.js';
	import * as Sheet from '$lib/components/ui/sheet/index.ts';
	import type { Atividade } from '$lib/services/atividade.service';
	import { Save, X, CircleCheck } from '@lucide/svelte';
	import { FormValidation } from '$lib/components/atividades/validation/atividade.validation.svelte.ts';

	interface Props {
		open: boolean;
		editingAtividade?: Atividade | null;
		loading?: boolean;
		error?: string;
		onClose: () => void;
		onSubmit: (data: { nome: string; cnae: string; aliquotaTotalPct: number; issPct: number; observacao?: string; ativo: boolean }) => Promise<void>;
	}

	let {
		open = $bindable(),
		editingAtividade = null,
		loading = false,
		error = '',
		onClose,
		onSubmit
	}: Props = $props();

	const form = new FormValidation();
	const isEditing = $derived(editingAtividade !== null);
	const title = $derived(isEditing ? 'Editar Atividade' : 'Cadastrar Atividade');

	$effect(() => {
		if (open) {
			if (editingAtividade) {
				form.populate({
					nome: editingAtividade.nome,
					cnae: editingAtividade.cnae,
					aliquotaTotalPct: editingAtividade.aliquotaTotalPct,
					issPct: editingAtividade.issPct,
					observacao: editingAtividade.observacao || '',
					ativo: editingAtividade.ativo
				});
			} else {
				form.reset();
			}
		}
	});

	async function handleSubmit() {
		if (!form.validateAll()) return;
		await onSubmit({
			nome: form.nome.trim(),
			cnae: form.cnae.trim(),
			aliquotaTotalPct: Number(form.aliquotaTotalPct),
			issPct: Number(form.issPct),
			observacao: form.observacao.trim(),
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

		<div class="flex-1 space-y-6 overflow-y-auto px-6 py-6">
			<div>
				<label for="nome" class="mb-1.5 block text-sm font-semibold text-gray-700">
					Nome
				</label>
				<Input
					id="nome"
					type="text"
					bind:value={form.nome}
					maxlength={200}
					placeholder="Ex: Fisioterapia"
					autofocus
					oninput={() => form.scheduleErrorDisplay('nome')}
					onblur={() => form.displayError('nome')}
				/>
				{#if form.shouldShowError('nome')}
					<p class="mt-1 text-xs text-red-600">{form.errors.nome}</p>
				{/if}
			</div>

			<div>
				<label for="cnae" class="mb-1.5 block text-sm font-semibold text-gray-700">
					CNAE
				</label>
				<Input
					id="cnae"
					type="text"
					bind:value={form.cnae}
					maxlength={20}
					placeholder="Ex: 8650001"
					oninput={() => form.scheduleErrorDisplay('cnae')}
					onblur={() => form.displayError('cnae')}
				/>
				{#if form.shouldShowError('cnae')}
					<p class="mt-1 text-xs text-red-600">{form.errors.cnae}</p>
				{/if}
			</div>

			<div class="grid grid-cols-2 gap-4">
				<div>
					<label for="aliquota" class="mb-1.5 block text-sm font-semibold text-gray-700">
						Alíquota Total (%)
					</label>
					<MoneyInput
						id="aliquota"
						bind:value={form.aliquotaTotalPct}
						oninput={() => form.scheduleErrorDisplay('aliquotaTotalPct')}
						onblur={() => form.displayError('aliquotaTotalPct')}
					/>
					{#if form.shouldShowError('aliquotaTotalPct')}
						<p class="mt-1 text-xs text-red-600">{form.errors.aliquotaTotalPct}</p>
					{/if}
				</div>

				<div>
					<label for="iss" class="mb-1.5 block text-sm font-semibold text-gray-700">
						ISS (%)
					</label>
					<MoneyInput
						id="iss"
						bind:value={form.issPct}
						oninput={() => form.scheduleErrorDisplay('issPct')}
						onblur={() => form.displayError('issPct')}
					/>
					{#if form.shouldShowError('issPct')}
						<p class="mt-1 text-xs text-red-600">{form.errors.issPct}</p>
					{/if}
				</div>
			</div>

			<div>
				<label for="observacao" class="mb-1.5 block text-sm font-semibold text-gray-700">
					Observação
				</label>
				<textarea
					id="observacao"
					bind:value={form.observacao}
					maxlength={500}
					placeholder="Observações..."
					class="min-h-[120px] w-full resize-none rounded-xl border border-gray-200 bg-gray-50/50 px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300"
				></textarea>
			</div>

			<div
				class="rounded-xl border border-gray-200 bg-gray-50/30 p-4 transition-colors hover:bg-white"
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
						Atividade Ativa
					</span>
					<span class="block text-xs text-gray-500">
						Desative para ocultar a atividade em fluxos operacionais.
					</span>
				</div>
			</div>

			{#if error}
				<div class="rounded-md bg-red-50 p-3 text-sm text-red-800">
					{error}
				</div>
			{/if}
		</div>

		<Sheet.Footer class="sticky bottom-0 border-t bg-white px-6 py-4">
			<div class="flex w-full items-center justify-end gap-3">
				<Button variant="secondary" onclick={handleClose} class="min-w-[100px]">
					Cancelar
				</Button>
				<Button onclick={handleSubmit} disabled={loading || !form.isValid} class="min-w-[100px]">
					{#if loading}
						<svg class="mr-3 size-5 animate-spin ..." viewBox="0 0 24 24">
							<!-- ... -->
						</svg>
							Salvando...
						
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

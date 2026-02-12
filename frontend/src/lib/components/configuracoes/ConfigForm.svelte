<script lang="ts">
	import { Button } from '$lib/components/ui/button/index.js';
	import { Input } from '$lib/components/ui/input/index.js';
	import { MoneyInput } from '$lib/components/ui/money-input/index.js';
	import { Save } from '@lucide/svelte';
	import * as Sheet from '$lib/components/ui/sheet/index.ts';
	import type { Configuracao } from '$lib/services/configuracoes.service';
	import { FormValidation } from '$lib/components/configuracoes/validation/config.validation.svelte';

	interface Props {
		open: boolean;
		editingConfig?: Configuracao | null;
		loading?: boolean;
		error?: string;
		onClose: () => void;
		onSubmit: (data: {
			pretensaoSalarialMensal: number;
			horasSemanais: number;
			semanasMediaMes: number;
			custoFixoPct: number;
			margemLucroPadraoPct: number;
			ativo: boolean;
		}) => Promise<void>;
	}

	let { open = $bindable(), editingConfig = null, loading = false, error = '', onClose, onSubmit }: Props = $props();

	const form = new FormValidation();
	const isEditing = $derived(editingConfig !== null);
	const title = $derived(isEditing ? 'Editar Configuração' : 'Cadastrar Configuração');

	$effect(() => {
		if (open) {
			if (editingConfig) {
				form.populate({
					pretensaoSalarialMensal: editingConfig.pretensaoSalarialMensal,
					horasSemanais: editingConfig.horasSemanais,
					semanasMediaMes: editingConfig.semanasMediaMes,
					custoFixoPct: editingConfig.custoFixoPct,
					margemLucroPadraoPct: editingConfig.margemLucroPadraoPct,
					ativo: editingConfig.ativo
				});
			} else {
				form.reset();
			}
		}
	});

	async function handleSubmit() {
		if (!form.validateAll()) return;
		await onSubmit({
			pretensaoSalarialMensal: Number(form.pretensaoSalarialMensal),
			horasSemanais: Number(form.horasSemanais),
			semanasMediaMes: Number(form.semanasMediaMes),
			custoFixoPct: Number(form.custoFixoPct),
			margemLucroPadraoPct: Number(form.margemLucroPadraoPct),
			ativo: form.ativo
		});
	}

	function handleClose() { form.reset(); onClose(); }
</script>

<Sheet.Root bind:open>
	<Sheet.Content side="right" hideDefaultClose class="flex flex-col gap-0 p-0 sm:max-w-md">
		<Sheet.Header class="sticky top-0 z-10 flex-row items-center justify-between gap-0 border-b border-pink-100 bg-white px-6 py-5">
			<h2 class="text-xl font-bold text-gray-800">{title}</h2>
			<Sheet.Close class="rounded-full p-2 text-gray-400 transition-colors hover:bg-pink-50 hover:text-pink-500 focus:outline-none focus:ring-pink-200">✕</Sheet.Close>
		</Sheet.Header>
		<div class="flex-1 space-y-6 overflow-y-auto px-6 py-6">
			<div class="grid grid-cols-2 gap-4">
				<div>
					<label for="pretensao" class="mb-1.5 block text-xs font-medium text-gray-500">Pretensão Mensal</label>
					<div class="relative">
						<span class="absolute left-3 top-3 text-sm font-medium text-gray-400">R$</span>
						<MoneyInput id="pretensao" 
									bind:value={form.pretensaoSalarialMensal} 
									class="w-full rounded-lg border border-gray-200 py-2.5 pl-9 pr-3 text-sm font-medium" 
									oninput={() => form.scheduleErrorDisplay('pretensaoSalarialMensal')} 
									onblur={() => form.displayError('pretensaoSalarialMensal')} 
						/>
					</div>
					{#if form.shouldShowError('pretensaoSalarialMensal')}<p class="mt-1 text-xs text-red-600">{form.errors.pretensaoSalarialMensal}</p>{/if}
				</div>
				<div>
					<label for="horas" class="mb-1.5 block text-xs font-medium text-gray-500">Horas Semanais</label>
					<Input id="horas" 
						   type="number" 
						   step="1" 
						   min="0" 
						   bind:value={form.horasSemanais} 
						   class="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-sm font-medium" 
						   oninput={() => form.scheduleErrorDisplay('horasSemanais')} 
						   onblur={() => form.displayError('horasSemanais')} 
					/>
					{#if form.shouldShowError('horasSemanais')}<p class="mt-1 text-xs text-red-600">{form.errors.horasSemanais}</p>{/if}
				</div>
				<div>
					<label for="semanas" class="mb-1.5 block text-xs font-medium text-gray-500">Semanas média/mês</label>
					<MoneyInput	id="semanas"	
						   bind:value={form.semanasMediaMes} 
						   class="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-sm font-medium" 
						   oninput={() => form.scheduleErrorDisplay('semanasMediaMes')} 
						   onblur={() => form.displayError('semanasMediaMes')} 
					/>
					{#if form.shouldShowError('semanasMediaMes')}<p class="mt-1 text-xs text-red-600">{form.errors.semanasMediaMes}</p>{/if}
				</div>
				<div>
					<label for="custofixo" class="mb-1.5 block text-xs font-medium text-gray-500">Custo Fixo (%)</label>
					<MoneyInput id="custofixo"	
						   bind:value={form.custoFixoPct} 
						   class="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-sm font-medium" 
						   oninput={() => form.scheduleErrorDisplay('custoFixoPct')} 
						   onblur={() => form.displayError('custoFixoPct')} 
					/>
					{#if form.shouldShowError('custoFixoPct')}<p class="mt-1 text-xs text-red-600">{form.errors.custoFixoPct}</p>{/if}
				</div>
				<div>
					<label for="margemlucro" class="mb-1.5 block text-xs font-medium text-gray-500">Margem Lucro (%)</label>
					<MoneyInput id="margemlucro"	
						   bind:value={form.margemLucroPadraoPct} 
						   class="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-sm font-medium" 
						   oninput={() => form.scheduleErrorDisplay('margemLucroPadraoPct')} 
						   onblur={() => form.displayError('margemLucroPadraoPct')} 
					/>
					{#if form.shouldShowError('margemLucroPadraoPct')}<p class="mt-1 text-xs text-red-600">{form.errors.margemLucroPadraoPct}</p>{/if}
				</div>
			</div>
			<div class="border-t border-gray-100 pt-3">
				<div class="flex items-center justify-end gap-2">
					<Button variant="secondary" onclick={handleClose} disabled={loading}>Cancelar</Button>

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
			</div>
		</div>
	</Sheet.Content>
</Sheet.Root>

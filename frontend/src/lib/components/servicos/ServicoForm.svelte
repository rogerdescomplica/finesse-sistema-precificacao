<script lang="ts">
	import { Button } from '$lib/components/ui/button/index.js';
	import { Input } from '$lib/components/ui/input/index.js';
	import * as Sheet from '$lib/components/ui/sheet/index.ts';
	import type { Servico } from '$lib/services/servico.service';
	import { servicoService, type ServicoDetail } from '$lib/services/servico.service';
	import type { Atividade } from '$lib/services/atividade.service';
	import { atividadeService } from '$lib/services/atividade.service';
	import { Save, X, CircleCheck } from '@lucide/svelte';
	import { FormValidation } from '$lib/components/servicos/validation/servico.validation.svelte.ts';
	import ServicoMateriaisEditor from '$lib/components/servicos/ServicoMateriaisEditor.svelte';
	import { materialService, type Material } from '$lib/services/material.service';
	import { configService, type Configuracao } from '$lib/services/configuracoes.service';
	import PrecoCalculatorPanel from '$lib/components/servicos/PrecoCalculatorPanel.svelte';
	

	interface Props {
		open: boolean;
		editingServico?: Servico | null;
		loading?: boolean;
		error?: string;
		onClose: () => void;
		onSubmit: (data: {
			nome: string;
			grupo: string;
			duracaoMinutos: number;
			atividadeId: number;
			margemLucroCustomPct?: number;
			ativo: boolean;
			materiais?: Array<{ materialId: number; quantidadeUsada: number }>;
			precoPraticado?: number;
		}) => Promise<void>;
	}

	let {
		open = $bindable(),
		editingServico = null,
		loading = false,
		error = '',
		onClose,
		onSubmit
	}: Props = $props();

	const form = new FormValidation();
	const isEditing = $derived(editingServico !== null);
	const title = $derived(isEditing ? 'Editar Serviço' : 'Cadastrar Serviço');

	let atividades = $state<Atividade[]>([]);
	let atividadesLoading = $state(false);
	let atividadesError = $state('');
	let materiaisItems = $state<Array<{ material: Material; quantidadeUsada: number }>>([]);
	let configAtual = $state<Configuracao | null>(null);
	let vendaPraticada = $state<number>(0);
	let descontoValor = $state<number>(0);
	let taxaAdicionalValor = $state<number>(0);
	let userEditedPrice = $state(false);

	async function loadAtividades() {
		atividadesLoading = true;
		atividadesError = '';
		try {
			const params = new URLSearchParams();
			params.set('ativo', 'true');
			params.set('size', '100');
			const page = await atividadeService.list(params);
			atividades = page.content || [];
		} catch (err) {
			atividadesError = err instanceof Error ? err.message : 'Erro ao carregar atividades';
		} finally {
			atividadesLoading = false;
		}
	}

	async function loadConfiguracao() {
		try {
			const params = new URLSearchParams();
			params.set('ativo', 'true');
			params.set('size', '1');
			const page = await configService.list(params);
			configAtual = page.content?.[0] ?? null;
		} catch {
			configAtual = null;
		}
	}

	$effect(() => {
		if (open) {
			void loadAtividades();
			void loadConfiguracao();
			if (editingServico) {
				form.populate({
					nome: editingServico.nome,
					grupo: editingServico.grupo,
					duracaoMinutos: editingServico.duracaoMinutos,
					atividadeId: editingServico.atividade?.id ?? '',
					margemLucroCustomPct: editingServico.margemLucroCustomPct ?? '',
					ativo: editingServico.ativo
				});
				(async () => {
					try {
						const detail: ServicoDetail = await servicoService.get(editingServico.id);
						const uniqueIds = Array.from(new Set((detail.materiais || []).map((m) => m.materialId)));
						const fetched = await Promise.all(uniqueIds.map((id) => materialService.get(id).catch(() => null)));
						const mapById = new Map<number, Material>();
						fetched.forEach((mat) => { if (mat) mapById.set(mat.id, mat); });
						materiaisItems = (detail.materiais || []).map((m) => {
							const full = mapById.get(m.materialId);
							return {
								material: {
									id: m.materialId,
									produto: m.produto,
									custoUnitario: m.custoUnitario,
									unidadeMedida: full?.unidadeMedida ?? 'UN',
									volumeEmbalagem: full?.volumeEmbalagem ?? 0,
									precoEmbalagem: full?.precoEmbalagem ?? 0,
									ativo: true
								},
								quantidadeUsada: m.quantidadeUsada
							};
						});
						if (detail.precoVigente != null) {
							vendaPraticada = Number(detail.precoVigente);
							userEditedPrice = true;
						}
					} catch (e) {
						console.error('Falha ao carregar materiais do serviço', e);
						materiaisItems = [];
					}
				})();
			} else {
				form.reset();
				materiaisItems = [];
				vendaPraticada = 0;
				descontoValor = 0;
				taxaAdicionalValor = 0;
				userEditedPrice = false;
			}
		}
	});

	// Atividade selecionada
	const atividadeSelecionada = $derived(() =>
		atividades.find((a) => a.id === Number(form.atividadeId)) || null
	);

	// Valor do minuto (R$/min)
	const valorMinuto = $derived(() => {
		if (!configAtual) return 0;

		const horasSemanais = Number(configAtual.horasSemanais || 0);
		const semanasMediaMes = Number(configAtual.semanasMediaMes || 0);
		const horasMes = horasSemanais * semanasMediaMes;
		if (horasMes <= 0) return 0;

		const pretensao = Number(configAtual.pretensaoSalarialMensal || 0);
		const valorHora = pretensao / horasMes;
		return valorHora / 60;
	});

	// Custos diretos
	const custoMaoObra = $derived(() => {
		const duracaoMin = Number(form.duracaoMinutos || 0);
		return duracaoMin * Number(valorMinuto() || 0);
	});

	const custoInsumos = $derived(() => {
		return materiaisItems.reduce((acc, it) => {
			const qtd = Number(it.quantidadeUsada || 0);
			const custoUnit = Number(it.material?.custoUnitario || 0);
			return acc + qtd * custoUnit;
	}, 0);
	});

	const custoDireto = $derived(() => Number(custoMaoObra()) + Number(custoInsumos()));


	// Frações (0..1) do PREÇO (igual planilha)
	const custoFixoFrac = $derived(() => Number(configAtual?.custoFixoPct ?? 0) / 100);

	//Por padrao é para usar a Margem Recomendada da Configuracao, no entanto, caso o usuario tenha informado uma margem customizada, é usada essa
	const margemFrac = $derived(() => {
		const custom = form.margemLucroCustomPct;
		if (custom !== '' && custom !== undefined && custom !== null) return Number(custom) / 100;
		return configAtual ? Number(configAtual.margemLucroPadraoPct || 0) / 100 : 0;
	});

	const impostoAliquota = $derived( () => atividadeSelecionada() ? Number(atividadeSelecionada()!.aliquotaTotalPct || 0) / 100 : 0 );

	// Markup operacional (planilha)
	const markup = $derived(() => {
		const sum = Number(impostoAliquota()) + Number(custoFixoFrac()) + Number(margemFrac());
		const denom = 1 - sum;
		if (!Number.isFinite(denom) || denom <= 0) return 1; // proteção
		return 1 / denom;
	});

	// Venda sugerida (planilha)
	const precoSugerido = $derived(() => Number(custoDireto()) * Number(markup()));
	
	// Auto-preencher venda praticada com sugerido (se usuário não editou)
	$effect(() => {
		if (!userEditedPrice) {
			vendaPraticada = Number(precoSugerido());
		}
	});

	function handleUserEditedPrice() {
		userEditedPrice = true;
	}

	function handleApplySuggestion(price: number) {
		vendaPraticada = price;
		userEditedPrice = false;
	}

	async function handleSubmit() {
		if (!form.validateAll()) return;
		form.materiais = materiaisItems.map((it) => ({
			materialId: it.material.id,
			quantidadeUsada: it.quantidadeUsada
		}));
		await onSubmit({
			nome: form.nome.trim(),
			grupo: form.grupo.trim(),
			duracaoMinutos: Number(form.duracaoMinutos || 0),
			atividadeId: Number(form.atividadeId),
			margemLucroCustomPct: form.margemLucroCustomPct === '' ? undefined : Number(form.margemLucroCustomPct),
			ativo: form.ativo,
			materiais: form.materiais,
			precoPraticado: Number(vendaPraticada)
		});
		
	}

	function handleClose() {
		form.reset();
		onClose();
	}
</script>

<Sheet.Root bind:open>
	<Sheet.Content side="right" hideDefaultClose class="flex flex-col gap-0 p-0 sm:max-w-3xl">
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
			<div class="grid grid-cols-1 gap-6 md:grid-cols-2">
				<div class="space-y-6">
					<div>
						<label for="nome" class="mb-1.5 block text-sm font-semibold text-gray-700">Nome</label>
						<Input
							id="nome"
							type="text"
							bind:value={form.nome}
							maxlength={180}
							placeholder="Ex: Limpeza de Pele"
							autofocus
							oninput={() => form.scheduleErrorDisplay('nome')}
							onblur={() => form.displayError('nome')}
						/>
						{#if form.shouldShowError('nome')}
							<p class="mt-1 text-xs text-red-600">{form.errors.nome}</p>
						{/if}
					</div>

					<div>
						<label for="grupo" class="mb-1.5 block text-sm font-semibold text-gray-700">
							Categoria (Grupo)
						</label>
						<select
							id="grupo"
							class="h-10 w-full rounded-xl border border-gray-200 bg-white px-3 text-sm"
							bind:value={form.grupo}
							oninput={() => form.scheduleErrorDisplay('grupo')}
							onblur={() => form.displayError('grupo')}
						>
							<option value="">Selecione...</option>
							<optgroup label="Acupuntura">
								<option value="Acupuntura - Tradicional">Tradicional</option>
							</optgroup>
							<optgroup label="Estética">
								<option value="Estética - Corporal">Corporal</option>
								<option value="Estética - Facial">Facial</option>
							</optgroup>
							<optgroup label="Fisioterapia">
								<option value="Fisioterapia - Pediátrica">Pediátrica</option>
								<option value="Fisioterapia - Reumatológica">Reumatológica</option>
								<option value="Fisioterapia - Traumato-Ortopédica">Traumato-Ortopédica</option>
							</optgroup>
							<optgroup label="Pilates">
								<option value="Pilates - Contemporâneo - Mensal">Contemporâneo - Mensal</option>
								<option value="Pilates - Contemporâneo - Semestral">Contemporâneo - Semestral</option>
								<option value="Pilates - Contemporâneo - Trimestral">Contemporâneo - Trimestral</option>
							</optgroup>
						</select>
						{#if form.shouldShowError('grupo')}
							<p class="mt-1 text-xs text-red-600">{form.errors.grupo}</p>
						{/if}
					</div>

					<div class="grid grid-cols-2 gap-4">
						<div>
							<label for="duracao" class="mb-1.5 block text-sm font-semibold text-gray-700"
								>Duração (min)</label
							>
							<Input
								id="duracao"
								type="number"
								min="1"
								step="1"
								bind:value={form.duracaoMinutos}
								placeholder="60"
								oninput={() => form.scheduleErrorDisplay('duracaoMinutos')}
								onblur={() => form.displayError('duracaoMinutos')}
							/>
							{#if form.shouldShowError('duracaoMinutos')}
								<p class="mt-1 text-xs text-red-600">{form.errors.duracaoMinutos}</p>
							{/if}
						</div>

						<div>
							<label for="margem" class="mb-1.5 block text-sm font-semibold text-gray-700">Margem Lucro (%)</label
							>
							<Input
								id="margem"
								type="number"
								min="0"
								step="1"
								bind:value={form.margemLucroCustomPct}
								placeholder="Opcional"
							/>
						</div>
					</div>

					<div>
						<label for="atividade" class="mb-1.5 block text-sm font-semibold text-gray-700"
							>Atividade</label
						>
						<select
							id="atividade"
							class="h-10 w-full rounded-xl border border-gray-200 bg-white px-3 text-sm"
							bind:value={form.atividadeId}
							oninput={() => form.scheduleErrorDisplay('atividadeId')}
							onblur={() => form.displayError('atividadeId')}
						>
							<option value="">Selecione...</option>
							{#if atividadesLoading}
								<option value="">Carregando...</option>
							{:else if atividadesError}
								<option value="">Erro ao carregar</option>
							{:else}
								{#each atividades as a}
									<option value={a.id}>{a.nome} - {Number(a.aliquotaTotalPct).toFixed(2)}%</option>
								{/each}
							{/if}
						</select>
						{#if form.shouldShowError('atividadeId')}
							<p class="mt-1 text-xs text-red-600">{form.errors.atividadeId}</p>
						{/if}
					</div>

					<ServicoMateriaisEditor bind:items={materiaisItems} />

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
							<span class="block text-sm font-medium text-gray-800">Serviço Ativo</span>
							<span class="block text-xs text-gray-500">Desative para ocultar o serviço em fluxos.</span
							>
						</div>
					</div>

					{#if error}
						<div class="rounded-md bg-red-50 p-3 text-sm text-red-800">{error}</div>
					{/if}
				</div>

				<div class="md:sticky md:top-2 md:self-start">
					<PrecoCalculatorPanel
						precoSugerido={precoSugerido()}
						impostoAliquota={impostoAliquota()}
						custoMaoObra={custoMaoObra()}
						custoInsumos={custoInsumos()}
						custoDireto={custoDireto()}
						custoFixoFrac={custoFixoFrac()}
						markup={markup()}
						recommendedMargin={margemFrac()}
						bind:vendaPraticada={vendaPraticada}
						bind:descontoValor={descontoValor}
						bind:taxaAdicionalValor={taxaAdicionalValor}
						onEditedPrice={handleUserEditedPrice}
						onApplySuggestion={handleApplySuggestion}
					/>
				</div>
			</div>
		</div>

		<Sheet.Footer class="sticky bottom-0 border-t bg-white px-6 py-4">
			<div class="flex w-full items-center justify-end gap-3">
				<Button variant="secondary" onclick={handleClose} class="min-w-[100px]">Cancelar</Button>
				<Button onclick={handleSubmit} disabled={loading || !form.isValid} class="min-w-[100px]">
					{#if loading}
						<span>Salvando...</span>
					{:else}
						<span class="flex items-center gap-2"><Save size={16} />Salvar</span>
					{/if}
				</Button>
			</div>
		</Sheet.Footer>
	</Sheet.Content>
</Sheet.Root>

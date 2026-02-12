<script lang="ts">
	import { MoneyInput } from '$lib/components/ui/money-input/index.js';
	import { formatCurrency, formatNumber } from '$lib/utils/formatters';
	import { CircleAlert, CircleDollarSign } from '@lucide/svelte';

	interface Props {
		precoSugerido: number;
		impostoAliquota: number;	
		custoFixoFrac: number;
		custoMaoObra: number;
		custoInsumos: number;
		custoDireto: number;
		markup: number;
		vendaPraticada?: number;
		descontoValor?: number;
		taxaAdicionalValor?: number;
		recommendedMargin?: number;
		onEditedPrice?: () => void;
		onApplySuggestion?: (price: number) => void;
	}

	let {
		precoSugerido,
		impostoAliquota,
		custoFixoFrac,
		custoMaoObra,
		custoInsumos,
		custoDireto,
		markup,
		vendaPraticada = $bindable<number>(0),
		descontoValor = $bindable<number>(0),
		taxaAdicionalValor = $bindable<number>(0),
		recommendedMargin,
		onEditedPrice,
		onApplySuggestion
	}: Props = $props();

	// number-safe: nunca NaN/Infinity
	const n = (v: unknown, fallback = 0) => {
		const x = Number(v);
		return Number.isFinite(x) ? x : fallback;
	};

	// Coerções seguras - uso direto de $derived (sem .by())
	const precoSugN = $derived(n(precoSugerido));
	const impostoN = $derived(n(impostoAliquota)); // fração (0..1)
	const maoObraN = $derived(n(custoMaoObra));
	const insumosN = $derived(n(custoInsumos));
	const diretoN = $derived(n(custoDireto));
	const markupN = $derived(n(markup)); // multiplicador (ex.: 1.90)
	const recommendedMargiN = $derived(n(recommendedMargin)); 

	// Venda ajustada (planilha)
	const vendaBruta = $derived(n(vendaPraticada));
	const ajustes = $derived(n(taxaAdicionalValor) - n(descontoValor));
	const vendaAjustada = $derived(vendaBruta + ajustes);

	// Componentes em R$ (planilha)
	const impostoRS = $derived(vendaAjustada * impostoN);
	const fixoRS = $derived(vendaAjustada * n(custoFixoFrac)); // custo fixo % do preço

	// Lucro líquido (planilha)
	const lucroLiquido = $derived( vendaAjustada - impostoRS - fixoRS - diretoN	);

	// Lucro líquido % (planilha)  igual sua coluna "Lucro Líquido (%)"
	const lucroLiquidoPct = $derived( vendaAjustada > 0 ? (lucroLiquido / vendaAjustada) * 100 : 0 );

	const lucroNegativo = $derived(lucroLiquido < 0);
	const margemBaixa = $derived(lucroLiquidoPct < recommendedMargiN * 100);


	function handleEditPrice() {
		onEditedPrice?.();
	}

	function handleApplySuggestion() {
		onApplySuggestion?.(precoSugN);
	}
</script>

<div class="md:sticky md:top-2 md:self-start">
	<div
		class="rounded-[28px] bg-linear-to-b from-slate-900 to-slate-800 p-6 text-white shadow-2xl space-y-6 ring-1 ring-slate-800"
	>
		<!-- Header -->
		<div>
			<div class="text-xs font-semibold uppercase tracking-wide text-slate-300">
				Preço Sugerido (Venda)
			</div>
			<div class="mt-2 text-4xl font-extrabold tracking-tight">{formatCurrency(precoSugN)}</div>

			<div class="mt-3 flex items-center gap-2 text-[11px]">
				<span class="rounded-full bg-slate-800/70 px-2.5 py-1 ring-1 ring-slate-700">
					Markup: {formatNumber(markupN, 2)}x
				</span>
				<span class="rounded-full bg-pink-900/60 px-2.5 py-1 ring-1 ring-pink-800">
					Alíquota: {formatNumber(impostoN * 100, 2)}%
				</span>
			</div>
		</div>

		<!-- Custos -->
		<div class="space-y-3">
			<div class="text-xs font-semibold uppercase tracking-wide text-slate-300">
				Custos do Atendimento
			</div>

			<div class="grid grid-cols-2 gap-3">
				<div class="rounded-2xl bg-slate-800/60 px-4 py-3 ring-1 ring-slate-700">
					<div class="flex items-center gap-2 text-[11px] text-slate-300">
						<CircleDollarSign size={14} class="text-slate-300" />
						<span>Mão de Obra</span>
					</div>
					<div class="mt-1 text-sm font-semibold">{formatCurrency(maoObraN)}</div>
				</div>

				<div class="rounded-2xl bg-slate-800/60 px-4 py-3 ring-1 ring-slate-700">
					<div class="flex items-center gap-2 text-[11px] text-slate-300">
						<CircleDollarSign size={14} class="text-slate-300" />
						<span>Insumos</span>
					</div>
					<div class="mt-1 text-sm font-semibold">{formatCurrency(insumosN)}</div>
				</div>
			</div>

			<div
				class="rounded-2xl bg-slate-800/60 px-4 py-3 ring-1 ring-slate-700 flex items-center justify-between"
			>
				<span class="text-[11px] text-slate-300">Custo Direto Total</span>
				<span class="text-sm font-semibold">{formatCurrency(diretoN)}</span>
			</div>
		</div>

		<!-- Venda praticada + ajustes -->
		<div class="space-y-2">
			<div class="flex items-center justify-between">
				<span class="text-xs font-semibold uppercase tracking-wide text-slate-300">Venda Praticada</span
				>
				<span class="text-[11px] text-slate-400">Pressione para editar</span>
			</div>

			<div class="flex items-center gap-2">
				<span class="text-slate-300 text-lg">$</span>
				<MoneyInput
					class="h-11 w-44 md:w-[220px] rounded-xl bg-slate-800 text-white ring-1 ring-slate-700"
					bind:value={vendaPraticada}
					onEdit={handleEditPrice}
				/>
			</div>

			<div class="h-px w-full bg-slate-700/60"></div>
		</div>

		<!-- Resultado -->
		<div class="grid grid-cols-2 gap-3">
			<div
				class="rounded-2xl px-4 py-3 text-center ring-1"
				class:bg-green-900={!lucroNegativo}
				class:ring-green-800={!lucroNegativo}
				class:bg-red-900={lucroNegativo}
				class:ring-red-800={lucroNegativo}
			>
				<div
					class="text-[11px]"
					class:text-green-300={!lucroNegativo}
					class:text-red-300={lucroNegativo}
				>
					Lucro Líq.
				</div>
				<div
					class="text-lg font-bold"
					class:text-green-200={!lucroNegativo}
					class:text-red-200={lucroNegativo}
				>
					{formatCurrency(lucroLiquido)}
				</div>
			</div>

			<div
				class="rounded-2xl px-4 py-3 text-center ring-1"
				class:bg-pink-900={margemBaixa}
				class:ring-pink-800={margemBaixa}
				class:bg-green-900={!margemBaixa}
				class:ring-green-800={!margemBaixa}
			>
				<div class="text-[11px]" class:text-pink-300={margemBaixa} class:text-green-300={!margemBaixa}>
					Margem %
				</div>
				<div
					class="text-lg font-bold"
					class:text-pink-200={margemBaixa}
					class:text-green-200={!margemBaixa}
				>
					{formatNumber(lucroLiquidoPct, 1)}%
				</div>
			</div>
		</div>

		<!-- Apoio à decisão (sempre visível) -->
		<div class="rounded-2xl bg-white text-slate-800 px-4 py-4 shadow-sm space-y-3">
			<div class="flex items-start gap-2">
				<CircleAlert size={18} class="text-blue-500 mt-0.5" />
				<div>
					<div class="text-sm font-semibold">Apoio à Decisão</div>
					<div class="text-xs text-slate-600">
						{#if margemBaixa}
							Cuidado! Sua margem está abaixo do recomendado ({formatNumber(
								recommendedMargiN * 100,
								0
							)}%). Considere reduzir custos ou reajustar o valor de venda.
						{:else}
							Excelente! Seu preço praticado oferece uma rentabilidade saudável para o negócio.
						{/if}
					</div>
				</div>
			</div>

			<button
				class="cursor-pointer w-full rounded-xl bg-slate-900 text-white px-4 py-2 text-sm shadow hover:bg-slate-800 transition"
				onclick={handleApplySuggestion}
			>
				Aplicar Sugestão
			</button>
		</div>
	</div>
</div>
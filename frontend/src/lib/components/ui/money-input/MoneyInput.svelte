<script lang="ts">
	import { Input } from '$lib/components/ui/input/index.js';
	import { formatNumber } from '$lib/utils/formatters';

	type Props = {
		value?: number;
		disabled?: boolean;
		class?: string;
		placeholder?: string;
		onEdit?: () => void;
	};

	let {
		value = $bindable<number>(0),
		disabled = false,
		class: className = '',
		placeholder = '0,00',
		onEdit
	}: Props = $props();

	function clamp2(n: number) {
		return Number.isFinite(n) ? Math.round(n * 100) / 100 : 0;
	}

	function parseDigitsToMoney(raw: string): number {
		// remove tudo que não é dígito
		const digits = raw.replace(/\D/g, '');
		if (!digits) return 0;

		return clamp2(Number(digits) / 100);
	}

	function handleInput(e: Event) {
		const el = e.currentTarget as HTMLInputElement;

		value = parseDigitsToMoney(el.value);
		onEdit?.();

		// reescreve com máscara (milhar + vírgula)
		el.value = formatNumber(value, 2);
	}

	function handleFocus(e: Event) {
		const el = e.currentTarget as HTMLInputElement;
		el.value = formatNumber(value, 2);
	}

	function handleBlur() {
		value = clamp2(value);
	}
</script>

<Input
	type="text"
	inputmode="numeric"
	autocomplete="off"
	{disabled}
	placeholder={placeholder}
	class={className}
	value={formatNumber(value, 2)}
	oninput={handleInput}
	onfocus={handleFocus}
	onblur={handleBlur}
/>

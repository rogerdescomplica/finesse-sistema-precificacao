<script lang="ts">
	import { Input } from '$lib/components/ui/input/index.js';
	import { formatNumber } from '$lib/utils/formatters';
	import type { HTMLAttributes } from 'svelte/elements';

	type Native = HTMLAttributes<HTMLInputElement>;

	type Props = Omit<
		Native,
		// controlados internamente para manter máscara e bind:value
		'value' | 'type' | 'inputmode' | 'autocomplete' | 'oninput' | 'onblur' | 'onfocus'
	> & {
		value?: number;
		disabled?: boolean;
		placeholder?: string;

		onEdit?: () => void;

		// ✅ use os tipos nativos do Svelte (sem InputEvent)
		oninput?: Native['oninput'];
		onblur?: Native['onblur'];
		onfocus?: Native['onfocus'];
	};

	let {
		value = $bindable<number>(0),
		class: className = '',
		disabled = false,
		placeholder = '0,00',

		onEdit,
		oninput,
		onfocus,
		onblur,

		...rest
	}: Props = $props();

	function clamp2(n: number) {
		return Number.isFinite(n) ? Math.round(n * 100) / 100 : 0;
	}

	function parseDigitsToMoney(raw: string): number {
		const digits = raw.replace(/\D/g, '');
		if (!digits) return 0;
		return clamp2(Number(digits) / 100);
	}

	function handleInput(e: Event) {
		const el = e.currentTarget as HTMLInputElement;

		value = parseDigitsToMoney(el.value);
		onEdit?.();

		// reescreve com máscara
		el.value = formatNumber(value, 2);

		// ✅ repassa (tipagem bate com Native['oninput'])
		oninput?.(e as never);
	}

	function handleFocus(e: FocusEvent) {
		const el = e.currentTarget as HTMLInputElement;
		el.value = formatNumber(value, 2);

		onfocus?.(e as never);
	}

	function handleBlur(e: FocusEvent) {
		value = clamp2(value);
		onblur?.(e as never);
	}
</script>

<Input
	{disabled}
	placeholder={placeholder}
	class={className}
	{...rest}
	type="text"
	inputmode="numeric"
	autocomplete="off"
	value={formatNumber(value, 2)}
	oninput={handleInput}
	onfocus={handleFocus}
	onblur={handleBlur}
/>
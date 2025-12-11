<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { Component } from 'svelte';
	import type { Icon as LucideIcon } from '@lucide/svelte';

	interface Props {
		href: string;
		icon: Component<LucideIcon>;
		current?: boolean;
		indented?: boolean;
		variant?: 'default' | 'danger';
		children?: Snippet;
	}

	let {
		href,
		icon: Icon,
		current = false,
		indented = false,
		variant = 'default',
		children
	}: Props = $props();

	const variants = {
		default: {
			active: 'bg-pink-50 text-pink-700 ',                   
			inactive: 'text-gray-500 hover:bg-gray-50 hover:text-gray-900',
			iconActive: 'text-pink-600',
			iconInactive: 'text-gray-400 group-hover:text-gray-600'
		},
		danger: {
			active: 'bg-red-50 text-red-500',
			inactive: 'text-red-500 hover:bg-red-50',
			iconActive: 'text-red-500',
			iconInactive: 'text-red-500'
		}
	};

	const linkClasses = $derived([
		'group flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors',
		indented ? 'pl-9' : '',
		current ? variants[variant].active : variants[variant].inactive
	]);

	const iconClasses = $derived([
		'size-5 shrink-0 transition-colors',
		current ? variants[variant].iconActive : variants[variant].iconInactive
	]);
</script>

<a {href} class={linkClasses} aria-current={current ? 'page' : undefined}>
	<Icon class={iconClasses} aria-hidden="true" strokeWidth={1.5} />
	{@render children?.()}
</a>
<script lang="ts">
    import { X } from '@lucide/svelte';
    import type { Snippet } from 'svelte';
    import { fade, slide } from 'svelte/transition';

	interface Props {
		open: boolean;
		onClose: () => void;
		children: Snippet;
	}

	let { open, onClose, children }: Props = $props();
</script>

{#if open}
	<div
		class="fixed inset-0 z-50 lg:hidden"
		role="dialog"
		aria-modal="true"
	>
		<!-- Backdrop -->
		<button
			type="button"
			class="fixed inset-0 bg-gray-900/80 cursor-default"
			transition:fade={{ duration: 300 }}
			onclick={onClose}
			aria-label="Fechar sidebar"
		></button>

		<!-- Sidebar panel -->
		<div
			class="fixed inset-y-0 left-0 flex w-full max-w-xs"
			transition:slide={{ duration: 300, axis: 'x' }}
		>
			<!-- Close button -->
			<div class="absolute top-0 left-full flex w-16 justify-center pt-5">
				<button
					type="button"
					class="-m-2.5 p-2.5"
					onclick={onClose}
				>
					<span class="sr-only">Fechar sidebar</span>
					<X class="size-6 text-white" aria-hidden="true" />
				</button>
			</div>

			<!-- Content -->
			<div class="relative flex grow flex-col gap-y-5 overflow-y-auto bg-white px-6 pb-2 dark:bg-gray-900 dark:ring dark:ring-white/10">
				{@render children()}
			</div>
		</div>
	</div>
{/if}

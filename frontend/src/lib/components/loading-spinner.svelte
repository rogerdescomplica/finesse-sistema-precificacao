<!-- src/lib/components/loading-spinner.svelte -->
<script lang="ts">
	interface Props {
		message?: string;
		size?: 'sm' | 'md' | 'lg';
	}

	let { message = 'Carregando...', size = 'md' }: Props = $props();

	const sizeClasses = {
		sm: 'h-5 w-5 border-2',
		md: 'h-8 w-8 border-4',
		lg: 'h-12 w-12 border-4'
	};
</script>

<div class="flex min-h-screen items-center justify-center bg-gray-50 dark:bg-gray-900">
	<div class="text-center">
		<div
			class={[
				'inline-block animate-spin rounded-full border-solid border-indigo-600 border-r-transparent',
				sizeClasses[size]
			]}
			role="status"
		>
			<span class="sr-only">Loading...</span>
		</div>
		{#if message}
			<p class="mt-2 text-sm text-gray-500 dark:text-gray-400">{message}</p>
		{/if}
	</div>
</div>
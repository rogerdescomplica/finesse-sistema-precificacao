<script lang="ts">
    import { Menu } from '@lucide/svelte';

	interface Props {
		title?: string;
		onMenuClick: () => void;
	}

	let { title = 'Dashboard', onMenuClick }: Props = $props();
</script>

<div class="sticky top-0 z-40 flex items-center gap-x-6 bg-white px-4 py-4 shadow-xs sm:px-6 lg:hidden dark:bg-gray-900 dark:shadow-none dark:after:pointer-events-none dark:after:absolute dark:after:inset-0 dark:after:border-b dark:after:border-white/10 dark:after:bg-black/10">
	<button
		type="button"
		class="-m-2.5 p-2.5 text-gray-700 hover:text-gray-900 lg:hidden dark:text-gray-400 dark:hover:text-white"
		onclick={onMenuClick}
	>
		<span class="sr-only">Abrir sidebar</span>
		<Menu class="size-6" aria-hidden="true" />
	</button>
	<div class="flex-1 text-sm/6 font-semibold text-gray-900 dark:text-white">{title}</div>
</div>

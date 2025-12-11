<script lang="ts">
	type ViewState = 'dashboard' | 'materiais' | 'servicos' | 'editor-magico' | 'configuracoes' | 'perfil';

	interface Props {
		currentView: ViewState;
		userName?: string;
		userEmail?: string;
		userInitials?: string;
		onSettingsClick?: () => void;
	}

	let {
		currentView,
		userName = 'Administrador',
		userEmail = 'finesse@admin.com',
		userInitials = 'AD',
		onSettingsClick = () => {}
	}: Props = $props();

	const viewTitles: Record<ViewState, string> = {
		dashboard: 'Visão Geral',
		materiais: 'Gestão de Materiais',
		servicos: 'Catálogo de Serviços',
		'editor-magico': 'Editor Inteligente',
		configuracoes: 'Configurações',
		perfil: 'Perfil'
	};

	let currentTitle = $derived(viewTitles[currentView] || '');
</script>

<header
    class="sticky top-0 z-10 left-0 w-full lg:left-[var(--sidebar-width)] lg:w-[calc(100%-var(--sidebar-width))] box-border flex h-20 items-center justify-between border-b border-pink-100/50 bg-white/60 px-8 backdrop-blur-sm"
>
	<div class="flex items-center text-sm text-gray-500">
		<span class="cursor-pointer font-medium transition-colors hover:text-pink-600">
			Finesse
		</span>

		<svg class="mx-2 h-4 w-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
			<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
		</svg>

		<span
			class="rounded-full border border-gray-100 bg-white px-3 py-1 font-semibold text-gray-800 shadow-sm"
		>
			{currentTitle}
		</span>
	</div>

	<div class="flex items-center space-x-4">
		<div class="flex items-center border-l border-gray-200 pl-4">
			<div class="mr-3 hidden text-right md:block">
				<p class="text-sm font-bold text-gray-800">{userName}</p>
				<p class="text-xs text-gray-500">{userEmail}</p>
			</div>
			<button
				type="button"
				onclick={onSettingsClick}
				class="flex h-10 w-10 items-center justify-center rounded-full border-2 border-white bg-gradient-to-tr from-blue-200 to-blue-100 text-sm font-bold text-blue-600 shadow-sm transition-transform hover:scale-105"
				aria-label="Configurações do usuário"
			>
				{userInitials}
			</button>
		</div>
	</div>
</header>

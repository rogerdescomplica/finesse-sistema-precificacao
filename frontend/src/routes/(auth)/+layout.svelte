<!-- src/routes/(auth)/+layout.svelte -->
<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import { onMount } from 'svelte';

	import { PageHeader } from '$lib/components/ui/sidebar';
	import type { ViewState } from '$lib/components/ui/sidebar/page-header.svelte';
	import Sidebar from '$lib/components/ui/sidebar/sidebar.svelte';

	import { authService } from '$lib/services/auth.service';
	import { auth, clearUser, setUser, setLoading, setChecked } from '$lib/states/auth.state.svelte';
	import type { Snippet } from 'svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();

	// ==================== COMPUTED VALUES ====================

	const currentView = $derived.by<ViewState>(() => {
		const pathname = page.url.pathname;
		if (pathname.includes('/materiais')) return 'materiais';
		if (pathname.includes('/servicos')) return 'servicos';
		if (pathname.includes('/configuracoes')) return 'configuracoes';
		if (pathname.includes('/perfil')) return 'perfil';
		return 'dashboard';
	});

	const userName = $derived(auth.user?.nome ?? 'Administrador');
	const userEmail = $derived(auth.user?.email ?? 'finesse@admin.com');
	const userInitials = $derived.by(() => {
		const nome = auth.user?.nome?.trim();
		if (!nome) return 'AD';
		const parts = nome.split(/\s+/).filter(Boolean);
		if (parts.length >= 2) {
			return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
		}
		return parts[0].slice(0, 2).toUpperCase();
	});

	// ==================== LIFECYCLE ====================

	/**
	 * Verifica autenticação APENAS UMA VEZ ao montar
	 * A CHAVE: Se já tem user, não verifica de novo!
	 */
	onMount(async () => {
	   // só checa uma vez por boot do app
		if (auth.checked) return;

		setChecked(true);
		setLoading(true);

		try {

			let usuario = await authService.checkSession();

			if (!usuario) {
				const refreshed = await authService.refresh(); // retorna boolean
				if (refreshed) {
					usuario = await authService.checkSession();
				}
			}
    
			if (!usuario) {
				clearUser();
				goto('/login', { replaceState: true });
				return;
			}
			
			setUser(usuario);

		} catch (error) {
			console.error('Erro ao verificar autenticação:', error);
			clearUser();
			goto('/login', { replaceState: true });
		} finally {
			setLoading(false);
		}
	});
	
	function handleSettingsClick() {
		goto('/configuracoes');
	}
</script>

<!-- ==================== TEMPLATE ==================== -->

{#if auth.isLoading}
	<div class="flex min-h-screen items-center justify-center bg-gray-50 dark:bg-gray-900">
		<div class="text-center">
			<div
				class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-indigo-600 border-r-transparent"
				role="status"
				aria-label="Carregando"
			></div>
			<p class="mt-2 text-sm text-gray-500 dark:text-gray-400">
				Verificando autenticação...
			</p>
		</div>
	</div>

{:else if !auth.user}
	<div class="flex min-h-screen items-center justify-center bg-gray-50 dark:bg-gray-900">
		<div class="text-center">
			<p class="text-sm text-gray-500 dark:text-gray-400">Redirecionando para login...</p>
		</div>
	</div>

{:else}
	<div class="min-h-screen bg-gray-50 dark:bg-gray-900">
		<Sidebar />

		<PageHeader
			{currentView}
			{userName}
			{userEmail}
			{userInitials}
			onSettingsClick={handleSettingsClick}
		/>

		<main
			class="py-10 lg:pl-(--sidebar-width) bg-linear-to-br from-red-50 via-white to-blue-50"
		>
			<div class="px-4 sm:px-6 lg:px-15">
				{@render children()}
			</div>
		</main>
	</div>
{/if}
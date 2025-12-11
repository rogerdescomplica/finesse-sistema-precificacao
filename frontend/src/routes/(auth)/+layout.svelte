<!-- src/routes/(auth)/+layout.svelte -->
<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import { PageHeader } from '$lib/components/ui/sidebar';
	import Sidebar from '$lib/components/ui/sidebar/sidebar.svelte';
	import { authService } from '$lib/services/auth.service';
	import { authStore } from '$lib/stores/auth.store';
	import type { Snippet } from 'svelte';
	import { onMount } from 'svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();

	const auth = $derived($authStore);

	// Detectar a rota atual para o breadcrumb
	let currentView = $derived.by(() => {
		const pathname = page.url.pathname;

		if (pathname.includes('/materiais')) return 'materiais';
		if (pathname.includes('/servicos')) return 'servicos';
		if (pathname.includes('/editor-magico')) return 'editor-magico';
		if (pathname.includes('/configuracoes')) return 'configuracoes';
		if (pathname.includes('/perfil')) return 'perfil';
		if (pathname.includes('/dashboard')) return 'dashboard';

		return 'dashboard'; // fallback
	});

	// Dados do usuário para o header
	let userName = $derived(auth.usuario?.nome || 'Administrador');
	let userEmail = $derived(auth.usuario?.email || 'finesse@admin.com');
	let userInitials = $derived.by(() => {
		if (!auth.usuario?.nome) return 'AD';

		const names = auth.usuario.nome.split(' ');
		if (names.length >= 2) {
			return `${names[0][0]}${names[names.length - 1][0]}`.toUpperCase();
		}
		return auth.usuario.nome.substring(0, 2).toUpperCase();
	});

	async function handleLogout() {
		await authService.logout();
		authStore.clearUser();
		goto('/login');
	}

	function handleSettingsClick() {
		goto('/configuracoes');
	}

	onMount(async () => {
		authStore.setLoading(true);

		try {
			if (auth.usuario) {
				const isValid = await authService.checkSession();
				if (!isValid) {
					try {
						const response = await authService.refresh();
						authStore.setUser(response.usuario);
					} catch {
						authStore.clearUser();
						goto('/login');
					}
				}
			} else {
				const isValid = await authService.checkSession();
				if (!isValid) {
					goto('/login');
				}
			}
		} catch (error) {
			console.error('Erro ao verificar autenticação:', error);
			authStore.clearUser();
			goto('/login');
		} finally {
			authStore.setLoading(false);
		}
	});
</script>

<!-- LOADING STATE -->
{#if auth.isLoading}
	<div class="flex min-h-screen items-center justify-center bg-gray-50 dark:bg-gray-900">
		<div class="text-center">
			<div
				class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-indigo-600 border-r-transparent"
			></div>
			<p class="mt-2 text-sm text-gray-500 dark:text-gray-400">Verificando autenticação...</p>
		</div>
	</div>
	<!-- USUÁRIO AUTENTICADO - COM SIDEBAR E HEADER -->
{:else if auth.isAuthenticated && auth.usuario}
	<div class="min-h-screen bg-gray-50 dark:bg-gray-900">
		<!-- Sidebar Component -->
		<Sidebar />

        <!-- Page Header (sticky) -->
        <PageHeader
            {currentView}
            {userName}
            {userEmail}
            {userInitials}
            onSettingsClick={handleSettingsClick}
        />

		<!-- Main Content -->
        <main class="py-10 lg:pl-[var(--sidebar-width)]">
			<div class="px-4 sm:px-6 lg:px-8">
				{@render children()}
			</div>
		</main>
	</div>
	<!-- REDIRECIONANDO -->
{:else}
	<div class="flex min-h-screen items-center justify-center bg-gray-50 dark:bg-gray-900">
		<p class="text-sm text-gray-500 dark:text-gray-400">Redirecionando...</p>
	</div>
{/if}

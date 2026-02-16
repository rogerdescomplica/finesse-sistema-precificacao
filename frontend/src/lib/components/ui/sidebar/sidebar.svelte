<script lang="ts">
    import { page } from '$app/state';
    import { auth } from '$lib/states/auth.state.svelte';
    import { LayoutDashboard, LogOut, Package, Wrench, Settings, ClipboardList, ChartBar, Users } from '@lucide/svelte';
    import MobileHeader from './mobile-header.svelte';
    import MobileSidebar from './mobile-sidebar.svelte';
    import NavItem from './nav-item.svelte';

    let sidebarOpen = $state(false);
       
</script>

{#snippet sidebarContent()}
	<header class="p-8 flex items-center space-x-3">
		<div
			class="w-10 h-10 bg-linear-to-br from-red-300 to-pink-400 rounded-xl flex items-center justify-center shadow-md shadow-pink-200 transform rotate-3"
			aria-hidden="true"
		>
			<span class="text-white font-bold text-lg">FI</span>
		</div>
		<div>
			<h1 class="font-bold text-gray-800 text-lg leading-tight">Finesse</h1>
			<p class="text-[10px] font-semibold text-gray-400 uppercase tracking-wider">
				Centro Integrado
			</p>
		</div>
	</header>

	<nav class="flex flex-1 flex-col px-4 py-4" aria-label="Menu principal">
		<ul role="list" class="flex flex-1 flex-col gap-y-1">
			<li>
				<NavItem
					href="/dashboard"
					icon={LayoutDashboard}
					current={page.url.pathname.startsWith('/dashboard')}
				>
					Dashboard
				</NavItem>
			</li>
											
            {#if !(auth.user && auth.user.roles?.includes('ROLE_VISUALIZADOR'))}
			<li role="presentation" class="mt-6 mb-2 px-4 text-[11px] font-bold text-gray-400 uppercase tracking-widest">
				Gestão
			</li>

			<li>
				<NavItem
					href="/servicos"
					icon={Wrench}
					current={page.url.pathname.startsWith('/servicos')}
				>
					Serviços
				</NavItem>
			</li>

            <li>
                <NavItem
                    href="/materiais"
                    icon={Package}
                    current={page.url.pathname.startsWith('/materiais')}
                >
                    Materiais
                </NavItem>
            </li>
            {/if}
            <li role="presentation" class="mt-6 mb-2 px-4 text-[11px] font-bold text-gray-400 uppercase tracking-widest">
                Análise
            </li>
            <li>
                <NavItem
                    href="/precificacao"
                    icon={ChartBar}
                    current={page.url.pathname.startsWith('/precificacao')}
                >
                    Precificação
                </NavItem>
            </li>

            {#if !(auth.user && auth.user.roles?.includes('ROLE_VISUALIZADOR'))}
            <li role="presentation" class="mt-6 mb-2 px-4 text-[11px] font-bold text-gray-400 uppercase tracking-widest">
                Configurações
            </li>
            <li>
                <NavItem
                    href="/configuracoes/geral"
                    icon={Settings}
                    current={page.url.pathname.startsWith('/configuracoes/geral')}
                >
                    Financeiras
                </NavItem>
            </li>
            <li>
                <NavItem
                    href="/atividades"
                    icon={ClipboardList}
                    current={page.url.pathname.startsWith('/atividades')}
                >
                    Atividades(CNAE)
                </NavItem>
            </li>
            <li>
                <NavItem
                    href="/usuarios"
                    icon={Users}
                    current={page.url.pathname.startsWith('/usuarios')}
                >
                    Usuários
                </NavItem>
            </li>
            {/if}
          

			<li role="presentation" class="mt-auto border-t border-pink-100"></li>

			<li class="mt-6 mb-2 px-4">
				<NavItem href="/logout" icon={LogOut} variant="danger">
					Sair do Sistema
				</NavItem>
			</li>
		</ul>
	</nav>
{/snippet}

<MobileSidebar open={sidebarOpen} onClose={() => (sidebarOpen = false)}>
	{@render sidebarContent()}
</MobileSidebar>

<aside 	class="hidden lg:fixed lg:inset-y-0 lg:z-50 lg:flex lg:w-(--sidebar-width) lg:flex-col" aria-label="Barra lateral">
	<div class="flex grow flex-col overflow-y-auto border-r border-pink-100 bg-white/80 backdrop-blur-md shadow-[4px_0_24px_rgba(0,0,0,0.02)]">
		{@render sidebarContent()}
	</div>
</aside>

<MobileHeader onMenuClick={() => (sidebarOpen = true)} />

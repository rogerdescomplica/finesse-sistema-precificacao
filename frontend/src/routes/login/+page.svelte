<!-- src/routes/login/+page.svelte -->
<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import { onMount } from 'svelte';
	
	import { Button } from '$lib/components/ui/button/index.js';
	import * as Card from '$lib/components/ui/card/index.js';
	import { Field, FieldGroup, FieldLabel } from '$lib/components/ui/field/index.js';
	import { Input } from '$lib/components/ui/input/index.js';
	
	import { authService } from '$lib/services/auth.service';
	import { auth, setUser } from '$lib/states/auth.state.svelte';
	
	import { ArrowRight, Eye, EyeOff, Lock, Mail } from '@lucide/svelte';

	// ==================== PROPS ====================
	
	interface Props {
		id?: string;
	}

	let { id = 'login' }: Props = $props();

	// ==================== STATE ====================
	
	let email = $state('');
	let senha = $state('');
	let mostrarSenha = $state(false);
	let isLoading = $state(false);
	let errorMessage = $state('');
	let infoMessage = $state('');

	// ==================== DERIVED ====================
	
	const isEmailValid = $derived(email.length > 0 && email.includes('@'));
	const isPasswordValid = $derived(senha.length >= 6);
	const canSubmit = $derived(isEmailValid && isPasswordValid && !isLoading);

	// ==================== LIFECYCLE ====================
	
	/**
	 * Verifica se já está autenticado ao montar
	 */
	onMount(async () => {
		if (auth.isAuthenticated) {
			goto('/dashboard');
			return;
		}
		
        try {
            const ok = await authService.checkSession();
            if (ok) goto('/dashboard');
        } catch (error) {
            console.error('Erro ao verificar sessão:', error);
        }
		
		// Mensagem de reautenticação
		const params = page.url.searchParams;
		const reason = params.get('reason');
		if (reason === 'expired') {
			infoMessage = 'Sua sessão expirou. Faça login novamente para continuar.';
		}
	});

	// ==================== HANDLERS ====================
	
	/**
	 * Processa o submit do formulário de login
	 */
	async function handleSubmit(e: Event) {
		e.preventDefault();
		errorMessage = '';
		isLoading = true;

		try {
			const response = await authService.login({ email, senha });
			setUser(response.usuario);
			
			// retorno à URL original, se salva em sessionStorage ou query
			const params = page.url.searchParams;
			const qsReturn = params.get('returnTo');
			const ssReturn = sessionStorage.getItem('returnTo');
			const returnTo = qsReturn || ssReturn;
			if (returnTo) {
				sessionStorage.removeItem('returnTo');
				goto(returnTo, { replaceState: true });
			} else {
				goto('/dashboard', { replaceState: true });
			}
		} catch (error) {
			errorMessage = error instanceof Error ? error.message : 'Erro ao fazer login';
		} finally {
			isLoading = false;
		}
	}

	/**
	 * Alterna visibilidade da senha
	 */
	function togglePassword() {
		mostrarSenha = !mostrarSenha;
	}
</script>

<!-- ==================== TEMPLATE ==================== -->

<div class="min-h-screen bg-gradient-to-b from-[#E68088] to-rose-200 px-4 py-8 md:py-16">
	<!-- Header -->
	<div class="mb-8 text-center">
		<div
			class="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full border border-white/40 bg-white/30 text-xl font-bold text-white shadow-lg backdrop-blur-sm"
		>
			FI
		</div>
		<h2 class="mb-1 text-sm font-medium uppercase tracking-wider text-white opacity-90">
			Finesse Centro Integrado
		</h2>
		<h1 class="text-4xl font-bold tracking-tight text-white">Bem-vindo de volta</h1>
	</div>

	<!-- Card de Login -->
	<Card.Root class="mx-auto mt-6 w-full max-w-md rounded-xl shadow-2xl">
		<Card.Content>
			<form onsubmit={handleSubmit} class="space-y-4">
				<!-- Mensagem de Erro -->
				{#if errorMessage}
					<div
						class="rounded-md bg-red-50 p-3 text-sm text-red-800"
						role="alert"
						aria-live="polite"
					>
						{errorMessage}
					</div>
				{/if}
				{#if infoMessage}
					<div class="rounded-md bg-yellow-50 p-3 text-sm text-yellow-800" role="status" aria-live="polite">
						{infoMessage}
					</div>
				{/if}

				<FieldGroup>
					<!-- Campo Email -->
					<Field>
						<FieldLabel for="email-{id}">Email</FieldLabel>
						<div class="relative">
							<span
								class="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
								aria-hidden="true"
							>
								<Mail size={18} />
							</span>
							<Input
								id="email-{id}"
								bind:value={email}
								type="email"
								placeholder="seu@email.com"
								required
								autocomplete="email"
								aria-invalid={email.length > 0 && !isEmailValid}
								aria-describedby={!isEmailValid ? 'email-error' : undefined}
								class="h-11 pl-10"
								disabled={isLoading}
							/>
						</div>
						{#if email.length > 0 && !isEmailValid}
							<p id="email-error" class="mt-1 text-sm text-red-600">
								Email inválido
							</p>
						{/if}
					</Field>

					<!-- Campo Senha -->
					<Field>
						<div class="flex items-center">
							<FieldLabel for="password-{id}">Senha</FieldLabel>
							
							<a	href="/recuperar-senha"
								class="ml-auto inline-block text-sm text-primary underline"
							>
								Esqueceu a senha?
							</a>
						</div>
						<div class="relative">
							<span
								class="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
								aria-hidden="true"
							>
								<Lock size={18} />
							</span>
							<Input
								id="password-{id}"
								bind:value={senha}
								type={mostrarSenha ? 'text' : 'password'}
								placeholder="••••••••"
								required
								minlength={6}
								autocomplete="current-password"
								aria-invalid={senha.length > 0 && !isPasswordValid}
								aria-describedby={!isPasswordValid ? 'password-error' : undefined}
								class="h-11 pl-10 pr-10"
								disabled={isLoading}
							/>
							<button
								type="button"
								class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:ring-offset-2"
								onclick={togglePassword}
								aria-label={mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}
								tabindex={-1}
							>
								{#if mostrarSenha}
									<EyeOff size={18} />
								{:else}
									<Eye size={18} />
								{/if}
							</button>
						</div>
						{#if senha.length > 0 && !isPasswordValid}
							<p id="password-error" class="mt-1 text-sm text-red-600">
								A senha deve ter pelo menos 6 caracteres
							</p>
						{/if}
					</Field>

					<!-- Botão Submit -->
					<Field>
						<Button
							type="submit"
							disabled={!canSubmit}
							class="h-11 w-full rounded-md bg-[#1f60f0] text-white shadow transition-all hover:bg-[#1b54d6] active:bg-[#1647b8] disabled:cursor-not-allowed disabled:opacity-50"
						>
							{#if isLoading}
								<span class="mr-2">Entrando...</span>
								<div
									class="inline-block h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"
									role="status"
									aria-label="Carregando"
								> </div>
							{:else}
								<span class="mr-2">Entrar</span>
								<ArrowRight size={18} />
							{/if}
						</Button>
					</Field>
				</FieldGroup>
			</form>
		</Card.Content>
	</Card.Root>
</div>

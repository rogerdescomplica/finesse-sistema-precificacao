<script lang="ts">
	import { Button } from '$lib/components/ui/button/index.js';
	import * as Card from '$lib/components/ui/card/index.js';
	import { Input } from '$lib/components/ui/input/index.js';
	import { FieldGroup, Field, FieldLabel } from '$lib/components/ui/field/index.js';
	import { Mail, Lock, Eye, EyeOff, ArrowRight } from '@lucide/svelte';
    import { authService } from '$lib/services/auth.service';
    import { authStore } from '$lib/stores/auth.store';
    import { goto } from '$app/navigation';
    import { onMount } from 'svelte';

	const id = $props.id();

	let email = $state('');
	let senha = $state('');
	let mostrarSenha = $state(false);
	let lembrar = $state(false);
	let isLoading = $state(false);
	let errorMessage = $state('');

	const isEmailValid = $derived(/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email));
	const isPasswordValid = $derived(senha.length >= 6);
	const canSubmit = $derived(isEmailValid && isPasswordValid && !isLoading);

    onMount(async () => {
        if ($authStore.isAuthenticated) {
            goto('/dashboard');
            return;
        }
        try {
            const ok = await authService.checkSession();
            if (ok) goto('/dashboard');
        } catch {}
    });

    async function handleSubmit(e: Event) {
		e.preventDefault();
		errorMessage = '';
		isLoading = true;

		try {
            const response = await authService.login({ email, senha });
            authStore.setUser(response.usuario);
            goto('/dashboard');
		} catch (error) {
			errorMessage = error instanceof Error ? error.message : 'Erro ao fazer login';
		} finally {
			isLoading = false;
		}
	}
</script>

<div class="min-h-screen bg-gradient-to-b from-[#E68088] to-rose-200 px-4 py-8 md:py-16">
	<div class="mb-8 text-center">
        <div class="w-12 h-12 bg-white/30 rounded-full flex items-center justify-center mx-auto mb-4 text-white font-bold text-xl backdrop-blur-sm shadow-lg border border-white/40">
          FI
        </div>
        <h2 class="text-white text-sm font-medium uppercase tracking-wider mb-1 opacity-90">Finesse Centro Integrado</h2>	
        <h1 class="text-white text-4xl font-bold tracking-tight">Bem-vindo de volta</h1>	
      </div>

	<Card.Root class="mx-auto mt-6 w-full max-w-md rounded-xl shadow-2xl">
		<Card.Content>
			<form onsubmit={handleSubmit} class="space-y-4">
				{#if errorMessage}
					<div class="rounded-md bg-red-50 p-3 text-sm text-red-800">{errorMessage}</div>
				{/if}

				<FieldGroup>
					<Field>
						<FieldLabel for="email-{id}">Email</FieldLabel>
						<div class="relative">
							<span
								class="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-gray-400"
							>
								<Mail size={18} />
							</span>
							<Input
								id="email-{id}"
								bind:value={email}
								type="email"
								placeholder="seu@email.com"
								required
								aria-invalid={!isEmailValid}
								class="h-11 pl-10"
								disabled={isLoading}
							/>
						</div>
					</Field>

					<Field>
						<div class="flex items-center">
							<FieldLabel for="password-{id}">Senha</FieldLabel>
                            <a
                                href="/recuperar-senha"
                                class="text-primary ml-auto inline-block text-sm underline">Esqueceu a senha?</a
                            >
						</div>
						<div class="relative">
							<span
								class="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-gray-400"
							>
								<Lock size={18} />
							</span>
							<Input
								id="password-{id}"
								bind:value={senha}
								type={mostrarSenha ? 'text' : 'password'}
								required
								minlength={6}
								aria-invalid={!isPasswordValid}
								class="h-11 pr-10 pl-10"
								disabled={isLoading}
							/>
							<button
								type="button"
								class="absolute top-1/2 right-3 -translate-y-1/2 text-gray-500 hover:text-gray-700"
								onclick={() => (mostrarSenha = !mostrarSenha)}
								aria-label={mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}
							>
								{#if mostrarSenha}
									<EyeOff size={18} />
								{:else}
									<Eye size={18} />
								{/if}
							</button>
						</div>
					</Field>

					<div class="flex items-center justify-between">
						<label class="flex items-center gap-2 text-sm text-gray-600">
							<input
								type="checkbox"
								bind:checked={lembrar}
								class="text-primary focus-visible:ring-primary/50 size-4 rounded border-gray-300 focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:outline-none"
							/>
							Lembrar-me
						</label>
					</div>

					<Field>
						<Button
							type="submit"
							disabled={!canSubmit}
							class="h-11 w-full rounded-md bg-[#1f60f0] text-white shadow transition-all hover:bg-[#1b54d6] active:bg-[#1647b8] disabled:cursor-not-allowed disabled:opacity-50"
						>
							<span class="mr-2">Entrar</span>
							<ArrowRight size={18} />
						</Button>
					</Field>
				</FieldGroup>
			</form>

			
        </Card.Content>
    </Card.Root>
</div>

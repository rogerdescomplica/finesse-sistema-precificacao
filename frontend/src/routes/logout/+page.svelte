<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { authService } from '$lib/services/auth.service';
  import { clearUser } from '$lib/states/auth.state.svelte';

  let loading = $state(true);
  let error = $state('');

  async function efetuarLogout() {
    loading = true;
    error = '';
    try {
      await authService.logout();
    } catch (e) {
      error = e instanceof Error ? e.message : 'Falha ao encerrar sessão';
    } finally {
      clearUser();
      // força limpeza de qualquer cookie residual com path /api
      try {
        await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
      } catch {}
      goto('/login', { replaceState: true });
    }
  }

  onMount(efetuarLogout);
</script>

<div class="flex min-h-screen items-center justify-center bg-gray-50">
  <div class="text-center">
    <div class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-indigo-600 border-r-transparent" aria-label="Encerrando sessão"></div>
    <p class="mt-2 text-sm text-gray-600">
      {error ? `Erro: ${error}` : 'Saindo...'}
    </p>
  </div>
  <noscript>
    <p class="mt-4 text-sm text-gray-600">Ative JavaScript para concluir o logout.</p>
  </noscript>
</div>

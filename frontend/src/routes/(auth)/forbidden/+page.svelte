<script lang="ts">
  import { goto } from '$app/navigation';
  import { page } from '$app/state';
  import { Button } from '$lib/components/ui/button/index.js';
  const from = $derived(page.url.searchParams.get('from') || '');
  function goDashboard() { goto('/dashboard', { replaceState: true }); }
</script>

<div class="mx-auto max-w-xl rounded-2xl border border-red-100 bg-red-50/40 p-6 text-center shadow-sm">
  <h1 class="text-xl font-bold text-red-800">Acesso não autorizado</h1>
  {#if from}
    <p class="mt-2 text-sm text-red-700">Você não possui permissão para acessar: <span class="font-mono">{from}</span></p>
  {:else}
    <p class="mt-2 text-sm text-red-700">Você não possui permissão para acessar esta página.</p>
  {/if}
  <div class="mt-4">
    <Button onclick={goDashboard} class="cursor-pointer">Ir para Dashboard</Button>
  </div>
</div>

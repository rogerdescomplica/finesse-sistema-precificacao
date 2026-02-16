<script lang="ts">
  import { Button } from '$lib/components/ui/button/index.js';
  import type { Usuario } from '$lib/services/usuario.service';

  interface Props {
    usuario?: Usuario | null;
    loading?: boolean;
    onConfirm: (u: Usuario) => void;
    onCancel: () => void;
  }

  let { usuario = null, loading = false, onConfirm, onCancel }: Props = $props();
</script>

{#if usuario}
  <div class="fixed inset-0 bg-black/30 flex items-center justify-center">
    <div class="w-[420px] rounded-xl bg-white p-5 shadow-xl">
      <h3 class="text-lg font-semibold">Confirmar exclusão</h3>
      <p class="mt-1 text-sm text-gray-600">Você está prestes a desativar o usuário {usuario.nome}.</p>
      <div class="mt-4 flex justify-end gap-2">
        <Button variant="secondary" onclick={onCancel}>Cancelar</Button>
        <Button onclick={() => onConfirm(usuario)} disabled={loading}>Desativar</Button>
      </div>
    </div>
  </div>
{/if}

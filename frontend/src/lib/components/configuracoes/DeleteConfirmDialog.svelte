<script lang="ts">
	import { Button } from '$lib/components/ui/button/index.js';
	import type { Configuracao } from '$lib/services/config.service';

	interface Props {
		config: Configuracao | null;
		loading?: boolean;
		onConfirm: (config: Configuracao) => void;
		onCancel: () => void;
	}

	let { config, loading = false, onConfirm, onCancel }: Props = $props();
	const isOpen = $derived(config !== null);

	function handleConfirm() { if (config) onConfirm(config); }
</script>

{#if isOpen && config}
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
		<div class="w-full max-w-md rounded-xl bg-white p-6 shadow-lg">
			<h3 class="mb-2 text-lg font-semibold text-gray-900">Confirmar Exclusão</h3>
			<p class="text-sm text-gray-600">Tem certeza que deseja excluir esta configuração (ID {config.id})?</p>
			<p class="mt-2 text-xs text-gray-500">Esta ação não pode ser desfeita.</p>
			<div class="mt-6 flex items-center justify-end gap-2">
				<Button variant="secondary" onclick={onCancel} disabled={loading}>Cancelar</Button>
				<Button class="bg-red-600 hover:bg-red-700" onclick={handleConfirm} disabled={loading}>
					{loading ? 'Excluindo...' : 'Excluir'}
				</Button>
			</div>
		</div>
	</div>
{/if}

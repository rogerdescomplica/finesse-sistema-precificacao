<script lang="ts">
	import { Button } from '$lib/components/ui/button/index.js';
	import type { Atividade } from '$lib/services/atividade.service';

	interface Props {
		atividade: Atividade | null;
		loading?: boolean;
		onConfirm: (atividade: Atividade) => void;
		onCancel: () => void;
	}

	let { atividade, loading = false, onConfirm, onCancel }: Props = $props();
	const isOpen = $derived(atividade !== null);

	function handleConfirm() {
		if (atividade) {
			onConfirm(atividade);
		}
	}
</script>

{#if isOpen && atividade}
	<div class="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
		<div class="w-full max-w-md rounded-xl bg-white p-6 shadow-lg">
			<h3 class="mb-2 text-lg font-semibold text-gray-900">Confirmar Exclusão</h3>
			<p class="text-sm text-gray-600">
				Tem certeza que deseja excluir a atividade <strong>"{atividade.nome}"</strong>?
			</p>
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

<script lang="ts">
  import type { Snippet } from 'svelte';
  import '../app.css';

  interface Props {
    children: Snippet;
  }

  let { children }: Props = $props();
</script>

<!-- Apenas renderiza o conteúdo, SEM sidebar -->
{@render children()}
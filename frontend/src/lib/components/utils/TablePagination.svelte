<script lang="ts">
  import { ChevronLeft, ChevronRight } from '@lucide/svelte';

  interface Props {
    currentPage: number;
    totalPages: number;
    perPage: number;
    totalItems: number;
    onPageChange: (page: number) => void;
  }

  let { currentPage, totalPages, perPage, totalItems, onPageChange }: Props = $props();

  const isFirstPage = $derived(currentPage === 1);
  const isLastPage = $derived(currentPage === totalPages);

  const pageInfo = $derived.by(() => {
    const start = totalItems > 0 ? (currentPage - 1) * perPage + 1 : 0;
    const end = Math.min(currentPage * perPage, totalItems);
    const plural = totalItems === 1 ? 'resultado' : 'resultados';
    return `Mostrando ${start} a ${end} de ${totalItems} ${plural}`;
  });

  function goPrev() {
    onPageChange(Math.max(1, currentPage - 1));
  }
  function goNext() {
    onPageChange(Math.min(totalPages, currentPage + 1));
  }

  const pages = $derived.by(() => {
    const out: Array<number | 'ellipsis'> = [];
    const total = totalPages;
    const current = currentPage;
    if (total <= 7) {
      for (let i = 1; i <= total; i++) out.push(i);
      return out;
    }
    const range = 2;
    const start = Math.max(2, current - range);
    const end = Math.min(total - 1, current + range);
    out.push(1);
    if (start > 2) out.push('ellipsis');
    for (let i = start; i <= end; i++) out.push(i);
    if (end < total - 1) out.push('ellipsis');
    out.push(total);
    return out;
  });
</script>

<hr class="divide-y divide-gray-100 mb-5">

<div class="flex items-center justify-between gap-4">
  
  <div class="text-sm text-gray-600">
    {pageInfo}
  </div>

  <nav aria-label="Navegação de páginas">
    <ul class="flex items-center gap-2">
      <li>
        <button
          type="button"
          class="cursor-pointer inline-flex h-8 w-8 items-center justify-center rounded-md border border-gray-200 bg-white text-gray-500 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          onclick={goPrev}
          disabled={isFirstPage}
          aria-label="Página anterior"
          title="Página anterior"
        >
          <ChevronLeft size={16} aria-hidden="true" />
        </button>
      </li>

      {#each pages as p}
        {#if p === 'ellipsis'}
          <li>
            <span class="inline-flex h-8 min-w-[24px] items-center justify-center px-1 text-sm text-gray-400">…</span>
          </li>
        {:else}
          <li>
            {#if p === currentPage}
              <span class="inline-flex h-8 min-w-[32px] items-center justify-center rounded-md border border-pink-200 bg-pink-100 px-2 text-sm font-semibold text-pink-700">
                {p}
              </span>
            {:else}
              <button
                type="button"
                class="cursor-pointer inline-flex h-8 min-w-[32px] items-center justify-center rounded-md border border-transparent bg-white px-2 text-sm font-semibold text-blue-600 transition-colors hover:bg-gray-50"
                onclick={() => onPageChange(p)}
                aria-label={`Página ${p}`}
                title={`Página ${p}`}
              >
                {p}
              </button>
            {/if}
          </li>
        {/if}
      {/each}

      <li>
        <button
          type="button"
          class="cursor-pointer inline-flex h-8 w-8 items-center justify-center rounded-md border border-gray-200 bg-white text-gray-500 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          onclick={goNext}
          disabled={isLastPage}
          aria-label="Próxima página"
          title="Próxima página"
        >
          <ChevronRight size={16} aria-hidden="true" />
        </button>
      </li>
    </ul>
  </nav>
</div>

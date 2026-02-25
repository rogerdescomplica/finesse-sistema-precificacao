<script lang="ts">
  import { onMount } from 'svelte';
  
  import * as Card from '$lib/components/ui/card/index.js';
  import { Input } from '$lib/components/ui/input/index.js';
  import { MoneyInput } from '$lib/components/ui/money-input/index.js';
  import { Button } from '$lib/components/ui/button/index.js';
  import { Separator } from '$lib/components/ui/separator/index.ts';
  import { formatCurrency, formatNumber } from '$lib/utils/formatters';

  import { servicoService, type Servico } from '$lib/services/servico.service';
  import { atividadeService, type Atividade } from '$lib/services/atividade.service';
  import { configService, type Configuracao } from '$lib/services/configuracoes.service';
  import { precoPraticadoService, type PrecoAtual } from '$lib/services/preco-praticado.service';

  let loading = $state(true);
  let error = $state('');
  let actionLoading = $state(false);
  let actionError = $state('');
  let actionSuccess = $state('');

  let servicos = $state<Servico[]>([]);
  let atividades = $state<Atividade[]>([]);
  let config: Configuracao | null = $state(null);
  let precosAtuais = $state<PrecoAtual[]>([]);

  let search = $state('');
  let grupoFilter = $state<string>('todos');
  let modoDetalhado = $state(false);

  type StatusFilter = 'todos' | 'prejuizo' | 'abaixo' | 'saudavel';
  let statusFilter = $state<StatusFilter>('todos');

  const STATUS_MAP: Record<StatusFilter, Row['status'] | null> = {
    todos: null,
    prejuizo: 'PREJUÍZO',
    abaixo: 'ABAIXO',
    saudavel: 'SAUDÁVEL'
  }

  type Row = {
    id: number
    nome: string
    grupo: string
    custoMaoObra: number
    custoMateriais: number
    custoDireto: number
    impostoFrac: number
    fixoFrac: number
    margemFrac: number
    fatorPreco: number
    vendaAtual: number
    vendaSugerida: number
    lucro: number
    margemPct: number
    status: 'SAUDÁVEL' | 'ABAIXO' | 'PREJUÍZO'
  }
  let rows = $state<Row[]>([])

  function valorMinuto(cfg: Configuracao | null): number {
    if (!cfg) return 0
    const horasMes = Number(cfg.horasSemanais || 0) * Number(cfg.semanasMediaMes || 0)
    if (horasMes <= 0) return 0
    const valorHora = Number(cfg.pretensaoSalarialMensal || 0) / horasMes
    return valorHora / 60
  }

  function calcRow(s: Servico, precoMap: Map<number, number | null>, matCost = 0): Row {
    const atv = atividades.find(a => a.id === (s.atividade?.id ?? 0)) || null;
    const imposto = atv ? Number(atv.aliquotaTotalPct || 0)/100 : 0;
    const fixo = config ? Number(config.custoFixoPct || 0)/100 : 0;
    const margem = (s.margemLucroCustomPct != null ? Number(s.margemLucroCustomPct) : Number(config?.margemLucroPadraoPct || 0)) / 100;
    const vm = valorMinuto(config);
    const mao = Number(s.duracaoMinutos || 0) * vm;
    const vendaAtual = Number(precoMap.get(s.id) ?? 0);
    const mat = matCost;
    const direto = mao + mat;
    const sum = imposto + fixo + margem;
    const denom = 1 - sum;
    const fator = denom > 0 ? 1/denom : (1 + sum);
    const sugerida = direto * fator;
    const lucro = vendaAtual * (1 - imposto - fixo) - direto;
    const margemPct = vendaAtual > 0 ? (lucro / vendaAtual) * 100 : 0;
    let status: Row['status'] = 'SAUDÁVEL';
    const priceClose = Math.abs(vendaAtual - sugerida) <= 0.01;
    if (lucro < -1e-6) status = 'PREJUÍZO';
    else if (priceClose) status = 'SAUDÁVEL';
    else if (margemPct < margem*100 - 0.05) status = 'ABAIXO';
    return {
      id: s.id, nome: s.nome, grupo: s.grupo,
      custoMaoObra: mao, custoMateriais: mat, custoDireto: direto,
      impostoFrac: imposto, fixoFrac: fixo, margemFrac: margem, fatorPreco: fator,
      vendaAtual: vendaAtual, vendaSugerida: sugerida,
      lucro, margemPct, status
    }
  }

  async function loadAll() {
    loading = true; error = ''
    try {
      const [srvPage, atvs, cfgPage, precos] = await Promise.all([
        servicoService.list(new URLSearchParams('page=0&size=100&sort=nome,asc')),
        atividadeService.list(new URLSearchParams('page=0&size=100&sort=nome,asc')),
        configService.list(new URLSearchParams('ativo=true&size=1')),
        precoPraticadoService.listarPrecosAtuais()
      ])
      servicos = srvPage.content ?? []
      atividades = atvs.content ?? []
      config = (cfgPage.content ?? [null])[0]
      precosAtuais = precos
      const precoMap = new Map<number, number | null>(precosAtuais.map(p => [p.servicoId, p.precoAtual]))
      // busca custos de materiais por serviço
      const mats = await Promise.all(servicos.map(async (s) => {
        try {
          const detail = await servicoService.get(s.id)
          const total = (detail.materiais || []).reduce((acc, m) => acc + (Number(m.custoUnitario||0) * Number(m.quantidadeUsada||0)), 0)
          return { id: s.id, total }
        } catch { return { id: s.id, total: 0 } }
      }))
      const matMap = new Map<number, number>(mats.map(m => [m.id, m.total]))
      rows = servicos.map(s => calcRow(s, precoMap, matMap.get(s.id) || 0))
    } catch (e) {
      error = e instanceof Error ? e.message : 'Erro ao carregar dados'
    } finally { loading = false }
  }

  onMount(loadAll)

  const resumoPrejuizo = $derived(rows.filter(r => r.status === 'PREJUÍZO').length)
  const resumoAbaixo = $derived(rows.filter(r => r.status === 'ABAIXO').length)
  const lucroMensalAtual = $derived(rows.reduce((acc, r) => acc + Math.max(0, r.lucro), 0))
  const lucroPotencial = $derived(rows.reduce((acc, r) => acc + Math.max(0, r.vendaSugerida - r.vendaAtual), 0))

  function filteredRows(): Row[] {
      let r = rows
      const q = search.trim().toLowerCase()
      if (q) r = r.filter(x => x.nome.toLowerCase().includes(q))
      if (grupoFilter !== 'todos') r = r.filter(x => (x.grupo || '') === grupoFilter)

      if (statusFilter !== 'todos') {
        const s = STATUS_MAP[statusFilter]
        if (s) r = r.filter(x => x.status === s)
      }
      return r
  }

  let showConfirm = $state(false)
  let selectedIds = $state<number[]>([])
  let selectedPrices = $state<Record<number, number>>({})
  function openConfirm(ids: number[]) {
    selectedIds = ids;
    const map: Record<number, number> = {};
    for (const id of ids) {
      const r = rows.find(x => x.id === id);
      if (r) map[id] = Number(r.vendaSugerida || r.vendaAtual || 0);
    }
    selectedPrices = map;
    showConfirm = true;
  }
  function closeConfirm() { showConfirm = false; selectedIds = [] }
  async function confirmApply() {
    actionLoading = true;
    actionError = '';
    actionSuccess = '';
    try {
      const ids = selectedIds.slice();
      for (const id of ids) {
        const detail = await servicoService.get(id);
        const preco = Number(selectedPrices[id] ?? rows.find(r => r.id === id)?.vendaSugerida ?? 0);
        const materiais = (detail.materiais || []).map(m => ({
          materialId: m.materialId,
          quantidadeUsada: m.quantidadeUsada
        }));
        await servicoService.update(id, {
          nome: detail.nome,
          grupo: detail.grupo,
          duracaoMinutos: detail.duracaoMinutos,
          atividadeId: Number(detail.atividadeId ?? 0),
          margemLucroCustomPct: detail.margemLucroCustomPct ?? undefined,
          materiais,
          precoPraticado: preco
        });
      }
      await loadAll();
      actionSuccess = 'Preço(s) atualizado(s) com sucesso';
    } catch (e) {
      actionError = e instanceof Error ? e.message : 'Falha ao atualizar preços';
    } finally {
      actionLoading = false;
      closeConfirm();
    }
  }

  function minPriceForRow(r: Row): number {
    const denom = 1 - (Number(r.impostoFrac || 0) + Number(r.fixoFrac || 0));
    return denom > 0 ? Number(r.custoDireto || 0) / denom : Number(r.vendaSugerida || 0);
  }
  function maxPriceForRow(r: Row): number {
    const min = minPriceForRow(r);
    return Math.max(min, Number(r.vendaSugerida || 0) * 1.5);
  }

  function segTabClass(active: boolean, activeText: string) {
    return [
      'rounded-lg px-4 py-1.5 text-sm font-semibold transition-colors cursor-pointer whitespace-nowrap',
      active ? `bg-white shadow-sm ${activeText}` : 'text-gray-400'
    ].join(' ')
  }
</script>

<div class="px-4 sm:px-6 lg:px-10 py-6">
  <h1 class="text-3xl font-bold">Precificação / Análise</h1>
  <p class="text-sm text-gray-600">Visão gerencial dos custos, preços e rentabilidade dos serviços</p>

  <div class="mt-6 grid grid-cols-4 gap-4">
    <Card.Root class="rounded-2xl">
      <Card.Content>
        <div class="text-sm text-gray-500">Serviços em prejuízo</div>
        <div class="mt-2 text-3xl font-bold text-red-600">{resumoPrejuizo}</div>
      </Card.Content>
    </Card.Root>
    <Card.Root class="rounded-2xl">
      <Card.Content>
        <div class="text-sm text-gray-500">Abaixo da margem ideal</div>
        <div class="mt-2 text-3xl font-bold text-amber-600">{resumoAbaixo}</div>
      </Card.Content>
    </Card.Root>
    <Card.Root class="rounded-2xl">
      <Card.Content>
        <div class="text-sm text-gray-500">Lucro mensal atual (estimado)</div>
        <div class="mt-2 text-3xl font-bold text-green-600">{formatCurrency(lucroMensalAtual)}</div>
      </Card.Content>
    </Card.Root>
    <Card.Root class="rounded-2xl bg-slate-900 text-white">
      <Card.Content>
        <div class="text-sm text-slate-300">Lucro potencial com ajustes</div>
        <div class="mt-2 text-3xl font-bold">{formatCurrency(lucroPotencial)}</div>
      </Card.Content>
    </Card.Root>
  </div>

  <div class="mt-6 rounded-2xl border bg-white">
    <div class="flex items-center gap-3 p-4">
      <Input class="h-10" placeholder="Buscar serviço..." bind:value={search} />
      <select class="h-10 rounded-xl border px-3 text-sm" bind:value={grupoFilter}>
        <option value="todos">Todos os Grupos</option>
        {#each Array.from(new Set(rows.map(r => r.grupo).filter(Boolean))) as g}
          <option value={g}>{g}</option>
        {/each}
      </select>
      <div class="flex items-center gap-2 text-sm">
        <div class="flex items-center gap-1 rounded-xl bg-gray-50 ring-1 ring-pink-100 p-1">
          <button
            type="button"
            class={segTabClass(statusFilter==='todos','text-gray-800')}
            aria-pressed={statusFilter==='todos'}
            onclick={() => statusFilter='todos'}
          >Todos</button>
          <button
            type="button"
            class={segTabClass(statusFilter==='prejuizo','text-red-600')}
            aria-pressed={statusFilter==='prejuizo'}
            onclick={() => statusFilter='prejuizo'}
          >Prejuízo</button>
          <button
            type="button"
            class={segTabClass(statusFilter==='abaixo','text-amber-600')}
            aria-pressed={statusFilter==='abaixo'}
            onclick={() => statusFilter='abaixo'}
          >Abaixo da Margem</button>
          <button
            type="button"
            class={segTabClass(statusFilter==='saudavel','text-green-600')}
            aria-pressed={statusFilter==='saudavel'}
            onclick={() => statusFilter='saudavel'}
          >Saudáveis</button>
        </div>
      </div>
      <div class="ml-auto">
        <div class="flex items-center rounded-2xl bg-slate-100 p-1">
         <button  class="flex items-center justify-center
                  w-11 h-10
                  rounded-xl
                  transition-all
                  focus:outline-none
                  {modoDetalhado
                    ? 'text-slate-400 hover:bg-slate-200'
                    : 'bg-indigo-500 text-white'}"
            aria-label="Visualização compacta"
            aria-pressed={!modoDetalhado}
            title="Modo Compacto: visão rápida para decisão"
            onclick={() => (modoDetalhado = false)}
          >
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"
                class="w-5 h-5"
                aria-hidden="true">
              <rect x="4" y="5" width="7" height="7" rx="2" fill="currentColor" />
              <rect x="13" y="5" width="7" height="7" rx="2" fill="currentColor" />
              <rect x="4" y="13" width="7" height="7" rx="2" fill="currentColor" />
              <rect x="13" y="13" width="7" height="7" rx="2" fill="currentColor" />
            </svg>
          </button>


     <button  class="flex items-center justify-center
                w-11 h-10
                rounded-xl
                transition-all
                focus:outline-none
                {modoDetalhado
                  ? 'bg-indigo-500 text-white'
                  : 'text-slate-400 hover:bg-slate-200'}"
          aria-label="Visualização detalhada"
          aria-pressed={modoDetalhado}
          title="Modo Detalhado: visão completa de custos e composição"
          onclick={() => (modoDetalhado = true)}
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"
              class="w-5 h-5"
              aria-hidden="true">
            <rect x="4" y="5" width="4" height="4" rx="1" fill="currentColor" />
            <rect x="10" y="5.5" width="10" height="3" rx="1.5" fill="currentColor" />
            <rect x="4" y="10" width="4" height="4" rx="1" fill="currentColor" />
            <rect x="10" y="10.5" width="10" height="3" rx="1.5" fill="currentColor" />
            <rect x="4" y="15" width="4" height="4" rx="1" fill="currentColor" />
            <rect x="10" y="15.5" width="10" height="3" rx="1.5" fill="currentColor" />
          </svg>
        </button>


        </div>
      </div>
    </div>
    <Separator />
    <div class="p-4">
      {#if actionSuccess}
        <div class="mb-3 rounded-md bg-green-50 p-3 text-sm text-green-800">{actionSuccess}</div>
      {/if}
      {#if actionError}
        <div class="mb-3 rounded-md bg-red-50 p-3 text-sm text-red-800">{actionError}</div>
      {/if}
      {#if loading}
        <div class="text-sm text-gray-500">Carregando...</div>
      {:else if error}
        <div class="rounded-md bg-red-50 p-3 text-sm text-red-800">{error}</div>
      {:else}
        <table class="w-full text-sm">
          <thead class="text-gray-500">
            {#if !modoDetalhado}
              <tr>
                <th class="text-left">Serviço</th>
                <th class="text-left">Grupo</th>
                <th class="text-right">Custo Direto</th>
                <th class="text-right">Venda Atual</th>
                <th class="text-right">Venda Sugerida</th>
                <th class="text-right">Lucro (R$)</th>
                <th class="text-right">Margem (%)</th>
                <th class="text-center">Status</th>
                <th class="text-center">Ação</th>
              </tr>
            {:else}
              <tr>
                <th class="text-left">Serviço</th>
                <th class="text-left">Grupo</th>
                <th class="text-right">Mão de Obra</th>
                <th class="text-right">Materiais</th>
                <th class="text-right">Custo Direto</th>
                <th class="text-right">Imp. (%)</th>
                <th class="text-right">Fixo (%)</th>
                <th class="text-right">Margem (%)</th>
                <th class="text-right">Markup</th>
                <th class="text-right">Venda Atual</th>
                <th class="text-right">Venda Sugerida</th>
                <th class="text-right">Lucro (R$)</th>
                <th class="text-right">Margem (%)</th>
                <th class="text-center">Status</th>
                <th class="text-center">Ação</th>
              </tr>
            {/if}
          </thead>
          <tbody>
            {#each filteredRows() as r}
              {#if !modoDetalhado}
                <tr class="border-t">
                  <td>{r.nome}</td>
                  <td class="text-gray-500">{r.grupo}</td>
                  <td class="text-right">{formatCurrency(r.custoDireto)}</td>
                  <td class="text-right">{formatCurrency(r.vendaAtual)}</td>
                  <td class="text-right text-blue-600 font-semibold">{formatCurrency(r.vendaSugerida)}</td>
                  <td class="text-right text-green-600 font-semibold">{formatCurrency(r.lucro)}</td>
                  <td class="text-right">{formatNumber(r.margemPct, 0)}%</td>
                  <td class="text-center">
                    {#if r.status==='SAUDÁVEL'}
                      <span class="rounded-md bg-green-100 text-green-700 px-2 py-1">Saudável</span>
                    {:else if r.status==='ABAIXO'}
                      <span class="rounded-md bg-amber-100 text-amber-700 px-2 py-1">Abaixo da margem</span>
                    {:else}
                      <span class="rounded-md bg-red-100 text-red-700 px-2 py-1">Prejuízo</span>
                    {/if}
                  </td>
                  <td class="text-center">
                    {#if r.status==='SAUDÁVEL'}
                      <Button class="inline-flex items-center gap-2" variant="ghost" title="Ajustar preço" onclick={() => openConfirm([r.id])}>Ajustar preço</Button>
                    {:else if r.status==='ABAIXO'}
                      <Button class="inline-flex items-center gap-2" variant="default" onclick={() => openConfirm([r.id])} title="Aplicar sugestão">Aplicar sugestão</Button>
                    {:else}
                      <Button class="inline-flex items-center gap-2" variant="destructive" onclick={() => openConfirm([r.id])} title="Corrigir preço">Corrigir preço</Button>
                    {/if}
                  </td>
                </tr>
              {:else}
                <tr class="border-t">
                  <td>{r.nome}</td>
                  <td class="text-gray-500">{r.grupo}</td>
                  <td class="text-right">{formatCurrency(r.custoMaoObra)}</td>
                  <td class="text-right">{formatCurrency(r.custoMateriais)}</td>
                  <td class="text-right">{formatCurrency(r.custoDireto)}</td>
                  <td class="text-right">{formatNumber(r.impostoFrac*100,1)}%</td>
                  <td class="text-right">{formatNumber(r.fixoFrac*100,1)}%</td>
                  <td class="text-right">{formatNumber(r.margemFrac*100,1)}%</td>
                  <td class="text-right">{formatNumber(r.fatorPreco,2)}x</td>
                  <td class="text-right">{formatCurrency(r.vendaAtual)}</td>
                  <td class="text-right text-blue-600 font-semibold">{formatCurrency(r.vendaSugerida)}</td>
                  <td class="text-right text-green-600 font-semibold">{formatCurrency(r.lucro)}</td>
                  <td class="text-right">{formatNumber(r.margemPct,0)}%</td>
                  <td class="text-center">
                    {#if r.status==='SAUDÁVEL'}
                      <span class="rounded-md bg-green-100 text-green-700 px-2 py-1">Saudável</span>
                    {:else if r.status==='ABAIXO'}
                      <span class="rounded-md bg-amber-100 text-amber-700 px-2 py-1">Abaixo da margem</span>
                    {:else}
                      <span class="rounded-md bg-red-100 text-red-700 px-2 py-1">Prejuízo</span>
                    {/if}
                  </td>
                  <td class="text-center">
                    <Button class="inline-flex items-center gap-2" variant={r.status==='SAUDÁVEL'?'ghost':(r.status==='ABAIXO'?'default':'destructive')} onclick={() => openConfirm([r.id])}>
                      {r.status==='SAUDÁVEL' ? 'Ajustar preço' : (r.status==='ABAIXO' ? 'Aplicar sugestão' : 'Corrigir preço')}
                    </Button>
                  </td>
                </tr>
                <tr class="bg-gray-50">
                  <td colspan="15" class="text-sm p-2">
                    {#if r.status==='SAUDÁVEL'}
                      <span class="text-green-700">Margem saudável. O serviço está bem posicionado.</span>
                    {:else if r.status==='ABAIXO'}
                      <span class="text-amber-700">O serviço gera lucro, mas pode ser otimizado.</span>
                    {:else}
                      <span class="text-red-700">Atenção: este serviço está gerando prejuízo.</span>
                    {/if}
                  </td>
                </tr>
              {/if}
            {/each}
          </tbody>
        </table>
      {/if}
    </div>
  </div>

  {#if showConfirm}
    <div class="fixed inset-0 bg-black/30 flex items-center justify-center">
      <div class="w-[520px] rounded-xl bg-white p-5 shadow-xl">
        <h3 class="text-lg font-semibold">Confirmar ajuste de preço</h3>
        <p class="mt-1 text-sm text-gray-600">Você está prestes a atualizar o preço de {selectedIds.length} serviço(s).</p>
        <div class="mt-3 max-h-[220px] overflow-auto text-sm">
          {#each rows.filter(r => selectedIds.includes(r.id)) as r}
            {#key r.id}
              <div class="mb-3 rounded-lg border p-3">
                <div class="mb-2 font-semibold">{r.nome}</div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-3 items-center">
                  <div class="flex items-center gap-2">
                    <span class="text-gray-600 text-sm">Valor:</span>
                    <MoneyInput class="h-10 w-[180px] rounded-xl border" bind:value={selectedPrices[r.id]} />
                    <span class="text-xs text-gray-500">Sugerido {formatCurrency(r.vendaSugerida)}</span>
                  </div>
                  <div class="flex items-center gap-2">
                    <input type="range" min={minPriceForRow(r)} max={maxPriceForRow(r)} step="0.5" bind:value={selectedPrices[r.id]} class="w-full" />
                    <span class="text-xs text-gray-500">{formatCurrency(selectedPrices[r.id] || 0)}</span>
                  </div>
                </div>
              </div>
            {/key}
          {/each}
        </div>
        <div class="mt-3 rounded-lg bg-green-50 p-3 text-green-800 text-sm">
          Impacto estimado no lucro mensal: {formatCurrency(rows.filter(r => selectedIds.includes(r.id)).reduce((acc, r) => acc + Math.max(0, Number(selectedPrices[r.id] ?? r.vendaSugerida) - r.vendaAtual), 0))}
        </div>
        <div class="mt-4 flex justify-end gap-2">
          <Button class="inline-flex items-center gap-2" variant="secondary" onclick={closeConfirm}>Cancelar</Button>
          <Button class="inline-flex items-center gap-2" onclick={confirmApply} disabled={actionLoading}>
            {#if actionLoading}
              Aplicando...
            {:else}
              Confirmar ajuste
            {/if}
          </Button>
        </div>
      </div>
    </div>
  {/if}
</div>

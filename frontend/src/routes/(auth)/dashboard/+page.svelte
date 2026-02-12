<script lang="ts">
    import * as Breadcrumb from '$lib/components/ui/breadcrumb/index.js';
    import { Separator } from '$lib/components/ui/separator/index.js';
    import * as Card from '$lib/components/ui/card/index.js';
    import { materialState } from '$lib/states/material.state.svelte';
    import { formatCurrency, formatNumber } from '$lib/utils/formatters';
    import { servicoService, type Servico } from '$lib/services/servico.service';
    import { atividadeService, type Atividade } from '$lib/services/atividade.service';
    import { configService, type Configuracao } from '$lib/services/configuracoes.service';
    import { precoPraticadoService, type PrecoAtual } from '$lib/services/preco-praticado.service';
    import { Wrench, Package as Cube, CircleCheck } from '@lucide/svelte';

    let loading = $state(true);
    let error = $state('');

    let servicosCount = $state(0);
    let materiaisCount = $state(0);
    let saudeCount = $state(0);
    let saudePct = $state(0);

    type Row = {
        id: number
        nome: string
        grupo: string
        vendaAtual: number
        vendaSugerida: number
        lucro: number
        margemPct: number
    }
    let topRows = $state<Row[]>([]);

    let servicos = $state<Servico[]>([]);
    let atividades = $state<Atividade[]>([]);
    let config: Configuracao | null = $state(null);
    let precosAtuais = $state<PrecoAtual[]>([]);

    function valorMinuto(cfg: Configuracao | null): number {
        if (!cfg) return 0;
        const horasMes = Number(cfg.horasSemanais || 0) * Number(cfg.semanasMediaMes || 0);
        if (horasMes <= 0) return 0;
        const valorHora = Number(cfg.pretensaoSalarialMensal || 0) / horasMes;
        return valorHora / 60;
    }

    async function loadDashboard() {
        loading = true; error = '';
        try {
            const [srvPage, atvs, cfgPage, precos] = await Promise.all([
                servicoService.list(new URLSearchParams('page=0&size=100&sort=nome,asc')),
                atividadeService.list(new URLSearchParams('page=0&size=100&sort=nome,asc')),
                configService.list(new URLSearchParams('ativo=true&size=1')),
                precoPraticadoService.listarPrecosAtuais()
            ]);
            servicos = srvPage.content ?? [];
            servicosCount = Number(srvPage.totalElements || servicos.length || 0);
            atividades = atvs.content ?? [];
            config = (cfgPage.content ?? [null])[0];
            precosAtuais = precos;
            const precoMap = new Map<number, number | null>(precosAtuais.map(p => [p.servicoId, p.precoAtual]));

            // custos de materiais por serviço
            const mats = await Promise.all(servicos.map(async (s) => {
                try {
                    const detail = await servicoService.get(s.id);
                    const total = (detail.materiais || []).reduce((acc, m) => acc + (Number(m.custoUnitario||0) * Number(m.quantidadeUsada||0)), 0);
                    return { id: s.id, total, detail };
                } catch { return { id: s.id, total: 0, detail: null as unknown as any }; }
            }));
            const matMap = new Map<number, number>(mats.map(m => [m.id, m.total]));

            const vm = valorMinuto(config);
            const rows: Row[] = servicos.map(s => {
                const atv = atividades.find(a => a.id === (s.atividade?.id ?? 0)) || null;
                const imposto = atv ? Number(atv.aliquotaTotalPct || 0)/100 : 0;
                const fixo = config ? Number(config.custoFixoPct || 0)/100 : 0;
                const margem = (s.margemLucroCustomPct != null ? Number(s.margemLucroCustomPct) : Number(config?.margemLucroPadraoPct || 0)) / 100;
                const mao = Number(s.duracaoMinutos || 0) * vm;
                const mat = matMap.get(s.id) || 0;
                const direto = mao + mat;
                const sum = imposto + fixo + margem;
                const denom = 1 - sum;
                const fator = denom > 0 ? 1/denom : (1 + sum);
                const sugerida = direto * fator;
                const vendaAtual = Number(precoMap.get(s.id) ?? 0);
                const lucro = vendaAtual * (1 - imposto - fixo) - direto;
                const margemPct = vendaAtual > 0 ? (lucro / vendaAtual) * 100 : 0;
                return {
                    id: s.id, nome: s.nome, grupo: s.grupo,
                    vendaAtual, vendaSugerida: sugerida, lucro, margemPct
                };
            });

            const validRows = rows.filter(r => r.vendaAtual > 0);
            saudeCount = validRows.filter(r => {
                const atv = atividades.find(a => a.id === (servicos.find(s => s.nome === r.nome)?.atividade?.id ?? 0)) || null;
                const margemAlvo = (servicos.find(s => s.nome === r.nome)?.margemLucroCustomPct != null
                  ? Number(servicos.find(s => s.nome === r.nome)?.margemLucroCustomPct)
                  : Number(config?.margemLucroPadraoPct || 0)) / 100;
                return r.lucro >= 0 && r.margemPct >= margemAlvo * 100 - 0.05;
            }).length;
            saudePct = servicosCount > 0 ? Math.round((saudeCount / servicosCount) * 100) : 0;
            topRows = rows.sort((a, b) => b.lucro - a.lucro).slice(0, 10);
        } catch (e) {
            error = e instanceof Error ? e.message : 'Erro ao carregar dashboard';
        } finally { loading = false; }
    }

    $effect(() => {
        materialState.requestLoad(0);
    });

    $effect(() => {
        materiaisCount = materialState.totalItems;
    });

    $effect(() => {
        loadDashboard();
    });
</script>

<header class="flex h-16 shrink-0 items-center gap-2 px-4">
    <Separator orientation="vertical" class="mr-2 data-[orientation=vertical]:h-4" />
    <Breadcrumb.Root>
        <Breadcrumb.List>
            <Breadcrumb.Item class="hidden md:block">
                <Breadcrumb.Link href="##">Dashboard</Breadcrumb.Link>
            </Breadcrumb.Item>
            <Breadcrumb.Separator class="hidden md:block" />
            <Breadcrumb.Item>
                <Breadcrumb.Page>Início</Breadcrumb.Page>
            </Breadcrumb.Item>
        </Breadcrumb.List>
    </Breadcrumb.Root>
    </header>

<div class="flex flex-1 flex-col gap-4 p-4 pt-0">
    <div class="grid grid-cols-1 gap-3 md:grid-cols-3">
        <Card.Root class="rounded-2xl bg-white">
            <Card.Content class="p-5">
                <div class="flex items-center gap-3">
                    <span class="rounded-xl bg-blue-50 p-2 text-blue-500"><Wrench size={18} /></span>
                    <span class="text-[11px] font-semibold uppercase tracking-wider text-gray-400">Total de Serviços</span>
                </div>
                <div class="mt-3 text-3xl font-extrabold text-gray-800">{servicosCount}</div>
            </Card.Content>
        </Card.Root>
        <Card.Root class="rounded-2xl bg-white">
            <Card.Content class="p-5">
                <div class="flex items-center gap-3">
                    <span class="rounded-xl bg-pink-50 p-2 text-pink-500"><Cube size={18} /></span>
                    <span class="text-[11px] font-semibold uppercase tracking-wider text-gray-400">Insumos cadastrados</span>
                </div>
                <div class="mt-3 text-3xl font-extrabold text-gray-800">{materiaisCount}</div>
            </Card.Content>
        </Card.Root>
        <Card.Root class="rounded-2xl bg-white">
            <Card.Content class="p-5">
                <div class="flex items-center gap-3">
                    <span class="rounded-xl bg-green-50 p-2 text-green-500"><CircleCheck size={18} /></span>
                    <span class="text-[11px] font-semibold uppercase tracking-wider text-gray-400">Saúde da margem</span>
                </div>
                <div class="mt-3 text-3xl font-extrabold text-green-600">{saudePct}%</div>
                <div class="text-xs text-gray-500">{saudeCount} de {servicosCount} serviços saudáveis</div>
            </Card.Content>
        </Card.Root>
    </div>

    <Card.Root class="rounded-xl bg-white">
        <Card.Content class="p-4">
            <div class="mb-3 text-sm font-semibold text-gray-700">Top 10 Serviços por Lucratividade</div>
            <div class="overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th class="px-4 py-2 text-left text-xs font-semibold text-gray-600">Serviço</th>
                            <th class="px-4 py-2 text-left text-xs font-semibold text-gray-600">Preço atual</th>
                            <th class="px-4 py-2 text-left text-xs font-semibold text-gray-600">Lucro líquido (R$)</th>
                            <th class="px-4 py-2 text-left text-xs font-semibold text-gray-600">Margem %</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-gray-100 bg-white">
                        {#if loading}
                          <tr><td colspan="4" class="px-4 py-6 text-center text-sm text-gray-500">Carregando...</td></tr>
                        {:else if error}
                          <tr><td colspan="4" class="px-4 py-6 text-center text-sm text-red-600">{error}</td></tr>
                        {:else if topRows.length === 0}
                          <tr><td colspan="4" class="px-4 py-6 text-center text-sm text-gray-500">Sem dados</td></tr>
                        {:else}
                          {#each topRows as r}
                            <tr>
                              <td class="px-4 py-2 text-sm text-gray-900">{r.nome}</td>
                              <td class="px-4 py-2 text-sm text-gray-900">{formatCurrency(r.vendaAtual)}</td>
                              <td class="px-4 py-2 text-sm text-green-600">{formatCurrency(r.lucro)}</td>
                              <td class="px-4 py-2 text-sm text-gray-900">{formatNumber(r.margemPct, 0)}%</td>
                            </tr>
                          {/each}
                        {/if}
                    </tbody>
                </table>
            </div>
        </Card.Content>
    </Card.Root>
</div>

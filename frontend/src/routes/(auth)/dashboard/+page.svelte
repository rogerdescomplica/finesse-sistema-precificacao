<script lang="ts">
    import * as Breadcrumb from '$lib/components/ui/breadcrumb/index.js';
    import { Separator } from '$lib/components/ui/separator/index.js';
    import * as Card from '$lib/components/ui/card/index.js';
    import { materialState } from '$lib/states/material.state.svelte';
    import { formatCurrency } from '$lib/utils/formatters';

    let servicosCount = 1;
    let materiaisCount = 0;
    let custoMedioPct = 10.62;
    let valorMedio = 28.87;
    let valorOrcamento = 0.48;

    $effect(() => {
        materialState.requestLoad(0);
    });

    $effect(() => {
        materiaisCount = materialState.totalItems;
        const items = materialState.items;
        if (items.length > 0) {
            const avgCusto = items.reduce((s, m) => s + Number(m.custoUnitario ?? 0), 0) / items.length;
            valorMedio = Number.isFinite(avgCusto) ? Number(avgCusto.toFixed(2)) : valorMedio;
        }
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
    <div class="grid grid-cols-1 gap-3 md:grid-cols-5">
        <Card.Root class="rounded-xl bg-white">
            <Card.Content class="p-4">
                <div class="text-xs font-semibold text-gray-400">Serviços</div>
                <div class="mt-2 text-2xl font-bold text-gray-800">{servicosCount}</div>
            </Card.Content>
        </Card.Root>
        <Card.Root class="rounded-xl bg-white">
            <Card.Content class="p-4">
                <div class="text-xs font-semibold text-gray-400">Materiais</div>
                <div class="mt-2 text-2xl font-bold text-gray-800">{materiaisCount}</div>
            </Card.Content>
        </Card.Root>
        <Card.Root class="rounded-xl bg-white">
            <Card.Content class="p-4">
                <div class="text-xs font-semibold text-gray-400">Custo médio (%)</div>
                <div class="mt-2 text-2xl font-bold text-gray-800">{custoMedioPct.toFixed(2)}%</div>
            </Card.Content>
        </Card.Root>
        <Card.Root class="rounded-xl bg-white">
            <Card.Content class="p-4">
                <div class="text-xs font-semibold text-gray-400">Valor médio</div>
                <div class="mt-2 text-2xl font-bold text-gray-800">{formatCurrency(valorMedio)}</div>
            </Card.Content>
        </Card.Root>
        <Card.Root class="rounded-xl bg-white">
            <Card.Content class="p-4">
                <div class="text-xs font-semibold text-gray-400">Valor orçamento</div>
                <div class="mt-2 text-2xl font-bold text-gray-800">{formatCurrency(valorOrcamento)}</div>
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
                        <tr>
                            <td class="px-4 py-2 text-sm text-gray-900">Limpeza da Pele Profunda</td>
                            <td class="px-4 py-2 text-sm text-gray-900">{formatCurrency(181)}</td>
                            <td class="px-4 py-2 text-sm text-green-600">{formatCurrency(84.84)}</td>
                            <td class="px-4 py-2 text-sm text-gray-900">47%</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </Card.Content>
    </Card.Root>
</div>

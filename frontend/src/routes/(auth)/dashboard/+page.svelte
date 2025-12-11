<script lang="ts">
    import * as Breadcrumb from '$lib/components/ui/breadcrumb/index.js';
    import { Separator } from '$lib/components/ui/separator/index.js';
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
    <div class="bg-muted/50 min-h-screen flex-1 rounded-xl md:min-h-min"></div>
</div>

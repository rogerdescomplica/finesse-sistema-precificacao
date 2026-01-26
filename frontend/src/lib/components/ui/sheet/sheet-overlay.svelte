<script lang="ts">
	import { Dialog as SheetPrimitive } from 'bits-ui';
	import { cn } from '$lib/utils.js';

	let {
		ref = $bindable(null),
		class: className,
		...restProps
	}: SheetPrimitive.OverlayProps = $props();
</script>

<SheetPrimitive.Overlay
    bind:ref
    data-slot="sheet-overlay"
    class={cn(
        'fixed inset-0 z-50 bg-red-900/10 backdrop-blur-[2px] transition-opacity duration-300 ease-out data-[state=open]:opacity-100 data-[state=closed]:opacity-0',
        className
    )}
    {...restProps}
/>

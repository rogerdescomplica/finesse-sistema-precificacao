<script lang="ts">
  import type {
    HTMLInputTypeAttribute,
    HTMLInputAttributes
  } from "svelte/elements";
  import { cn } from "$lib/utils.js";

  // Qualquer tipo de input, exceto "file"
  type InputType = Exclude<HTMLInputTypeAttribute, "file">;

  type Props = HTMLInputAttributes & {
    type?: "file" | InputType;
    files?: FileList;
    ref?: HTMLInputElement | null;

    /**
     * Controle de classes:
     * - Sem `class`              → usa APENAS o class padrão do componente
     * - Com `class`              → concatena: padrão + class passado
     * - Com `replaceClass` + class → ignora padrão e usa APENAS o class passado
     */
    replaceClass?: boolean;
  };

  let {
    ref = $bindable<HTMLInputElement | null>(null),
    value = $bindable(),
    type,
    files = $bindable<FileList | undefined>(),
    class: className,
    replaceClass = false,
    "data-slot": dataSlot = "input",
    ...restProps
  }: Props = $props();

/**
border border-red-200 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-blue-300 focus:border-transparent transition-all bg-gray-50/50
border border-red-200 rounded-xl focus:ring-2 focus:ring-blue-400 focus:border-transparent transition-all outline-none text-gray-700 bg-gray-50/50

border border-gray-300 focus:border-transparent rounded-xl px-4 py-3 bg-gray-50/70 text-gray-700 placeholder:text-gray-400 shadow-xs transition-all focus:outline-none focus:ring-2 focus:ring-primary/40  disabled:opacity-50 disabled:cursor-not-allowed 

*/


  // Classes padrão para input type="file"
  const defaultFileClasses = cn(
    // BASE
    "border border-red-200 rounded-xl px-4 py-3 bg-gray-50/50 text-gray-700 placeholder:text-muted-foreground shadow-xs flex w-full min-w-0 selection:bg-primary selection:text-primary-foreground outline-none transition-all disabled:cursor-not-allowed disabled:opacity-50 md:text-sm",
    // FOCUS
    "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-300 focus-visible:border-transparent",
    // ERRO (aria-invalid)
    "aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive",
  );

  // Classes padrão para input "normal"
  const defaultInputClasses = cn(
    // BASE
    "border border-red-200 rounded-xl px-4 py-3 bg-gray-50/50 text-gray-700 placeholder:text-muted-foreground shadow-xs flex w-full min-w-0 selection:bg-primary selection:text-primary-foreground outline-none transition-all disabled:cursor-not-allowed disabled:opacity-50 md:text-sm",
    // FOCUS
    "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-300 focus-visible:border-transparent",
    // ERRO (aria-invalid)
    "aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive",
  );

  // Regra dos 3 cenários: padrão / concat / substituir
  const getInputClass = (defaultClasses: string) => {
    if (replaceClass && className) {
      // 3) usar apenas o class passado
      return className;
    }
    if (className) {
      // 2) concatenar padrão + class passado
      return cn(defaultClasses, className);
    }
    // 1) usar apenas o padrão
    return defaultClasses;
  };

  const fileInputClass = $derived(getInputClass(defaultFileClasses));
  const normalInputClass = $derived(getInputClass(defaultInputClasses));
</script>

{#if type === "file"}
  <input
    bind:this={ref}
    data-slot={dataSlot}
    class={fileInputClass}
    type="file"
    bind:files
    bind:value
    {...restProps}
  />
{:else}
  <input
    bind:this={ref}
    data-slot={dataSlot}
    class={normalInputClass}
    {type}
    bind:value
    {...restProps}
  />
{/if}

<script lang="ts">
  import { Button } from '$lib/components/ui/button/index.js';
  import { Input } from '$lib/components/ui/input/index.js';
  import * as Sheet from '$lib/components/ui/sheet/index.ts';
  import type { Usuario, Perfil } from '$lib/services/usuario.service';
  import { usuarioService, type UsuarioInput } from '$lib/services/usuario.service';


  interface Props {
    open: boolean;
    editingUsuario?: Usuario | null;  
    loading?: boolean;
    error?: string;
    onClose: () => void;
   onSubmit: (data: UsuarioInput) => Promise<void>;
  }

  let {
    open = $bindable(),
    editingUsuario = null,
    loading = false,
    error = '',
    onClose,
    onSubmit
  }: Props = $props();

  let nome = $state('');
  let email = $state('');
  let senha = $state('');
  let confirmSenha = $state('');
  let perfil = $state<Perfil>('VISUALIZADOR');
  let emailError = $state('');
  let senhaError = $state('');
  let senhaChecks = $state({ len: false, upper: false, lower: false, digit: false, special: false });
  let emailCheckTimer: ReturnType<typeof setTimeout> | null = null;
  const isEditing = $derived(editingUsuario !== null);
  const title = $derived(isEditing ? 'Editar Usuário' : 'Cadastrar Usuário');

  $effect(() => {
    if (open) {
      if (editingUsuario) {
        nome = editingUsuario.nome;
        email = editingUsuario.email;
        perfil = editingUsuario.perfil ? editingUsuario.perfil : 'VISUALIZADOR';
        senha = '';
        confirmSenha = '';
        emailError = '';
        senhaError = '';
      } else {
        nome = '';
        email = '';
        perfil = 'VISUALIZADOR';
        senha = '';
        confirmSenha = '';
        emailError = '';
        senhaError = '';
      }
    }
  });

  function isValidEmailFormat(e: string) {
    const re = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    return re.test(e);
  }

  async function validateEmail({ showEmptyError = true } = {}) {
    emailError = '';
    const e = email.trim();
    if (!e) {
      if (showEmptyError) {
        emailError = 'Email é obrigatório';
      } else {
        emailError = '';
      }
      return false;
    }
    if (!isValidEmailFormat(e)) {
      emailError = 'Email inválido. Use o formato nome@dominio.com';
      return false;
    }
    if (!isEditing) {
      try {
        await usuarioService.getByEmail(e);
        emailError = 'Este email já está em uso. Por favor utilize outro endereço.';
        return false;
      } catch {
        // ok: não encontrado
      }
    }
    return true;
  }

  function updateSenhaChecks() {
    const s = senha || '';
    senhaChecks = {
      len: s.length >= 8,
      upper: /[A-Z]/.test(s),
      lower: /[a-z]/.test(s),
      digit: /\d/.test(s),
      special: /[!@#$%^&*()_+\-=[\]{}|;':",.<>/?`~]/.test(s)
    };
  }

  function validateSenha() {
    senhaError = '';
    updateSenhaChecks();
    const needsValidate = !isEditing || (!!senha || !!confirmSenha);
    if (needsValidate) {
      const strong = senhaChecks.len && senhaChecks.upper && senhaChecks.lower && senhaChecks.digit && senhaChecks.special;
      if (!strong) { senhaError = 'Senha fraca: mínimo 8 caracteres com maiúscula, minúscula, número e especial'; return false; }
      if (senha !== confirmSenha) { senhaError = 'Senhas não coincidem'; return false; }
    }
    return true;
  }

  async function handleSubmit() {
    const okEmail = await validateEmail({ showEmptyError: true });
    const okSenha = validateSenha();
    if (!okEmail || !okSenha) return;
    try {
      await onSubmit({
        nome: nome.trim(),
        email: isEditing ? undefined : email.trim(),
        senha: senha ? senha : undefined,
        perfil: perfil
      });
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Erro ao salvar';
      if (msg.toLowerCase().includes('email')) {
        emailError = 'Este email já está em uso. Por favor utilize outro endereço.';
      }
      throw e;
    }
  }

  function togglePerfil(p: Perfil) { perfil = p; }

  function handleClose() {
    onClose();
  }

  $effect(() => {
    // debounce para checar email duplicado em tempo real
    if (!open) return;
    if (emailCheckTimer) { clearTimeout(emailCheckTimer); emailCheckTimer = null; }
    emailCheckTimer = setTimeout(() => {
      if ((email || '').trim().length > 0) void validateEmail();
    }, 400);
  });

  $effect(updateSenhaChecks);
</script>

<Sheet.Root bind:open>
  <Sheet.Content side="right" hideDefaultClose class="flex flex-col gap-0 p-0 sm:max-w-md">
    <Sheet.Header class="sticky top-0 z-10 flex-row items-center justify-between gap-0 border-b border-pink-100 bg-white px-6 py-5">
      <h2 class="text-xl font-bold text-gray-800">{title}</h2>
      <Sheet.Close class="rounded-full p-2 text-gray-400 transition-colors hover:bg-pink-50 hover:text-pink-500 focus:outline-none focus:ring-pink-200" onclick={handleClose} />
    </Sheet.Header>

    <div class="flex-1 space-y-6 overflow-y-auto px-6 py-6">
      <div>
        <label for="nome" class="mb-1.5 block text-sm font-semibold text-gray-700">Nome completo</label>
        <Input id="nome" type="text" bind:value={nome} maxlength={100} placeholder="Ex: Joana Silva" />
      </div>

      <div>
        <label for="email" class="mb-1.5 block text-sm font-semibold text-gray-700">Email</label>
        <Input
          id="email"
          type="email"
          bind:value={email}
          placeholder="exemplo@dominio.com"
          disabled={isEditing}
          onblur={() => void validateEmail({ showEmptyError: true })}
          aria-invalid={!!emailError}
          aria-describedby="email-error"
        />
        {#if emailError}
          <p id="email-error" class="mt-1 text-xs text-red-600">{emailError}</p>
        {/if}
      </div>

      <div>
        <label class="mb-1.5 block text-sm font-semibold text-gray-700">Senha</label>
        <Input type="password" bind:value={senha} placeholder={isEditing ? 'Opcional: informar nova senha' : 'Defina uma senha'} oninput={updateSenhaChecks} />
        <div class="mt-2 grid grid-cols-2 gap-1 text-[11px]">
          <span class="{senhaChecks.len ? 'text-green-600' : 'text-gray-400'}">• 8+ caracteres</span>
          <span class="{senhaChecks.upper ? 'text-green-600' : 'text-gray-400'}">• Maiúscula</span>
          <span class="{senhaChecks.lower ? 'text-green-600' : 'text-gray-400'}">• Minúscula</span>
          <span class="{senhaChecks.digit ? 'text-green-600' : 'text-gray-400'}">• Número</span>
          <span class="{senhaChecks.special ? 'text-green-600' : 'text-gray-400'}">• Especial</span>
        </div>
      </div>
      <div>
        <label class="mb-1.5 block text-sm font-semibold text-gray-700">Confirmar senha</label>
        <Input type="password" bind:value={confirmSenha} placeholder={isEditing ? 'Repita a nova senha' : 'Repita a senha'} oninput={validateSenha} />
        {#if senhaError}
          <p class="mt-1 text-xs text-red-600">{senhaError}</p>
        {/if}
      </div>

      <div>
        <label class="mb-1.5 block text-sm font-semibold text-gray-700">Perfil</label>

        <div class="flex items-center gap-4">
          <label class="flex items-center gap-2 text-sm">
            <input type="radio" name="perfil" value="ADMIN" checked={perfil === 'ADMIN'} onchange={() => togglePerfil('ADMIN')} />
            <span>Admin</span>
          </label>

          <label class="flex items-center gap-2 text-sm">
            <input type="radio" name="perfil" value="VISUALIZADOR" checked={perfil === 'VISUALIZADOR'} onchange={() => togglePerfil('VISUALIZADOR')} />
            <span>Visualizador</span>
          </label>
        </div>
      </div>

      {#if error}
        <div class="rounded-md bg-red-50 p-3 text-sm text-red-800">{error}</div>
      {/if}
    </div>

    <Sheet.Footer class="sticky bottom-0 border-t bg-white px-6 py-4">
      <div class="flex w-full items-center justify-end gap-3">
        <Button variant="secondary" onclick={handleClose} class="min-w-[100px]">Cancelar</Button>
        <Button onclick={handleSubmit} disabled={loading || !nome.trim() || (!isEditing && (!email.trim() || !senha)) || !!emailError || !!senhaError} class="min-w-[100px]">
          {#if loading}
            Salvando...
          {:else}
            Salvar
          {/if}
        </Button>
      </div>
    </Sheet.Footer>
  </Sheet.Content>
  </Sheet.Root>

import { goto } from '$app/navigation'
import { clearUser } from '$lib/states/auth.state.svelte'
import { authService } from '$lib/services/auth.service'

function isAuthFailureMessage(msg: string | undefined) {
  if (!msg) return false
  const m = msg.toLowerCase()
  return (
    m.includes('não autenticado') ||
    m.includes('unauthorized') ||
    m.includes('sessão expirada') ||
    m.includes('refresh') ||
    m.includes('token')
  )
}

export async function handleAuthFailure(errorMessage?: string) {
  if (!isAuthFailureMessage(errorMessage)) return
  try {
    // preserva rota atual para retorno pós-login
    const returnTo = typeof location !== 'undefined' ? location.href : '/'
    sessionStorage.setItem('returnTo', returnTo)
    // tentativa de logout para limpar cookies (melhor esforço)
    await authService.logout().catch(() => {})
  } finally {
    // limpa estado local e redireciona
    clearUser()
    const reason = 'expired'
    goto(`/login?reason=${reason}`, { replaceState: true })
  }
}

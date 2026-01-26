import type { UsuarioInfo } from '$lib/services/auth.service';

interface AuthState {
  usuario: UsuarioInfo | null;
  isLoading: boolean;
  checked: boolean;
}

const state = $state<AuthState>({
  usuario: null,
  isLoading: false,
  checked: false
});

export const auth = {
  get isAuthenticated() {
    return !!state.usuario;
  },
  get user() {
    return state.usuario;
  },
  get isLoading() {
    return state.isLoading;
  },
  get checked() {
    return state.checked;
  },
  get userName() {
    return state.usuario?.nome ?? 'Visitante';
  }
};

export function setUser(usuario: UsuarioInfo | null) {
  state.usuario = usuario;
}

export function clearUser() {
  state.usuario = null;
  state.checked = false;
}

export function setLoading(isLoading: boolean) {
  state.isLoading = isLoading;
}

export function setChecked(value: boolean) {
  state.checked = value;
}

export function login(usuario: UsuarioInfo) {
  setUser(usuario);
}

export function logout() {
  clearUser();
}

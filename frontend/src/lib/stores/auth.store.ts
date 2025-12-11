// src/lib/stores/auth.store.ts

import { writable } from 'svelte/store';
import { browser } from '$app/environment';
import type { UsuarioInfo } from '$lib/services/auth.service';

interface AuthState {
	usuario: UsuarioInfo | null;
	isAuthenticated: boolean;
	isLoading: boolean;
}

// Recupera usuário do localStorage
function getStoredUser(): UsuarioInfo | null {
	if (!browser) return null;

	try {
		const stored = localStorage.getItem('usuario');
		return stored ? JSON.parse(stored) : null;
	} catch {
		return null;
	}
}

function createAuthStore() {
	const storedUser = getStoredUser();

	const { subscribe, set, update } = writable<AuthState>({
		usuario: storedUser,
		isAuthenticated: !!storedUser,
		isLoading: false
	});

	return {
		subscribe,

		setUser: (usuario: UsuarioInfo) => {
			// Salva no localStorage
			if (browser) {
				localStorage.setItem('usuario', JSON.stringify(usuario));
			}

			update((state) => ({
				...state,
				usuario,
				isAuthenticated: true,
				isLoading: false
			}));
		},

		clearUser: () => {
			// Remove do localStorage
			if (browser) {
				localStorage.removeItem('usuario');
			}

			set({
				usuario: null,
				isAuthenticated: false,
				isLoading: false
			});
		},

		setLoading: (isLoading: boolean) => {
			update((state) => ({ ...state, isLoading }));
		}
	};
}

export const authStore = createAuthStore();

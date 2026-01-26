// src/lib/services/auth.service.ts
import { isValidEmail, isValidPassword } from '$lib/utils/validators';

export interface LoginCredentials {
	email: string;
	senha: string;
}

export interface RegisterData {
	nome: string;
	email: string;
	senha: string;
	confirmarSenha: string;
	nomeClinica: string;
	cnpj: string;
	telefone: string;
	cidade: string;
	estado: string;
	cep: string;
}

export interface UsuarioInfo {
	id: number;
	nome: string;
	email: string;
	roles: string[];
}

type CheckResponse = {
  authenticated: boolean;
  usuario?: UsuarioInfo;
};

/**
 * Resposta de autenticação do backend
 * Com HttpOnly cookies, tokens não são retornados
 */
export interface LoginResponse {
	tokenAcesso: string;
	tokenRefresh: string;
	expiraEm: string;
	usuario: UsuarioInfo;
}

export interface ErrorResponse {
	error?: string;
	message?: string;
	details?: unknown;
}

// ==================== SERVICE ====================

class AuthService {
	private readonly baseUrl = '/api/auth';

	/**
	 * Realiza login com email e senha
	 * @throws {Error} Se credenciais inválidas ou erro de rede
	 */
	/**
	 * Faz login chamando o endpoint interno /api/auth/login
	 * Service faz fetch para o PROXY INTERNO
	 */
	async login(credentials: LoginCredentials): Promise<LoginResponse> {
		const response = await fetch(`${this.baseUrl}/login`, {
			// ← Chama o proxy
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			credentials: 'include', // importante para cookies
			body: JSON.stringify(credentials)
		});

		// Se o backend retornar erro
		if (!response.ok) {
			const message = await this.extractErrorMessage(response, 'Erro ao fazer login');
			throw new Error(message);
		}

		return response.json();
	}

	/**
	 * Realiza logout (limpa cookie de sessão)
	 * @throws {Error} Se erro ao comunicar com o servidor
	 */
	async logout(): Promise<void> {
		const response = await fetch(`${this.baseUrl}/logout`, {
			method: 'POST',
			credentials: 'include'
		});

		if (!response.ok) {
			const message = await this.extractErrorMessage(response, 'Erro ao fazer logout');
			throw new Error(message);
		}
	}

	/**
	 * Registra novo usuário
	 * @throws {Error} Se dados inválidos ou erro no servidor
	 */
	async register(data: RegisterData): Promise<LoginResponse> {
		if (data.senha !== data.confirmarSenha) {
			throw new Error('As senhas não coincidem');
		}

		if (!isValidPassword(data.senha)) {
			throw new Error('A senha deve ter pelo menos 6 caracteres');
		}

		if (!isValidEmail(data.email)) {
			throw new Error('Email inválido');
		}

		const response = await fetch(`${this.baseUrl}/register`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			credentials: 'include',
			body: JSON.stringify(data)
		});

		if (!response.ok) {
			const message = await this.extractErrorMessage(response, 'Erro ao registrar');
			throw new Error(message);
		}

		return response.json();
	}

	/**
	 * Atualiza token de sessão (se backend suportar)
	 * Opcional com HttpOnly cookies
	 * @throws {Error} Se sessão expirada ou erro de rede
	 */
	async refresh(): Promise<LoginResponse> {
		const response = await fetch(`${this.baseUrl}/refresh`, {
			method: 'POST',
			credentials: 'include'
		});

		if (!response.ok) {
			const message = await this.extractErrorMessage(response, 'Sessão expirada');
			throw new Error(message);
		}

		return response.json();
	}

	/**
	 * Verifica se há uma sessão ativa (cookies presentes)
	 */
	async checkSession(): Promise<UsuarioInfo | null> {
		try {
			const response = await fetch(`${this.baseUrl}/check`, {
			method: 'GET',
			credentials: 'include'
			});

			if (!response.ok) return null;

			const data = (await response.json()) as CheckResponse;
			return data.usuario ?? null;
		} catch {
			return null;
		}
	}

	/**
	 * Extrai mensagem de erro da resposta HTTP
	 * @private
	 */
	private async extractErrorMessage(response: Response, fallback: string): Promise<string> {
		try {
			const contentType = response.headers.get('content-type');
			if (contentType?.includes('application/json')) {
				const data = (await response.json()) as ErrorResponse;
				return data?.error || data?.message || fallback;
			}

			const text = await response.text();
			return text.trim() || fallback;
		} catch (error) {
			console.error('Erro ao extrair mensagem:', error);
			return fallback;
		}
	}
}


export const authService = new AuthService();
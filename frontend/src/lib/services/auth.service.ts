// src/lib/services/auth.service.ts

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
	papel: string;
	nomeClinica: string;
	clinicaId: number;
}

export interface LoginResponse {
	tokenAcesso: string;
	tokenRefresh: string;
	expiraEm: string;
	usuario: UsuarioInfo;
}

export interface ErrorResponse {
	error: string;
	message?: string;
	details?: unknown;
}

class AuthService {
	private readonly baseUrl = '/api/auth';

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
	 * Faz logout
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
	 * Registra novo usuário e clínica
	 */
	async register(data: RegisterData): Promise<LoginResponse> {
		if (data.senha !== data.confirmarSenha) {
			throw new Error('As senhas não coincidem');
		}

		if (data.senha.length < 6) {
			throw new Error('A senha deve ter pelo menos 6 caracteres');
		}

		if (!data.email.includes('@')) {
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
	 * Atualiza o token usando refresh token
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
	async checkSession(): Promise<boolean> {
		try {
			const response = await fetch(`${this.baseUrl}/check`, {
				method: 'GET',
				credentials: 'include'
			});

			return response.ok;
		} catch {
			return false;
		}
	}

	/**
	 * Obtém informações do usuário atual
	 */
	async getCurrentUser(): Promise<UsuarioInfo | null> {
		try {
			const response = await fetch(`${this.baseUrl}/me`, {
				method: 'GET',
				credentials: 'include'
			});

			if (!response.ok) {
				return null;
			}

			return response.json();
		} catch {
			return null;
		}
	}

	/**
	 * Função padronizada para extrair mensagens de erro
	 */
	private async extractErrorMessage(response: Response, fallback: string): Promise<string> {
		try {
			// Tenta parsear como JSON primeiro
			const data = (await response.clone().json()) as ErrorResponse;
			return data?.error || data?.message || fallback;
		} catch {
			// Se falhar, lê como texto
			const text = await response.text();
			return text || fallback;
		}
	}
}

export const authService = new AuthService();

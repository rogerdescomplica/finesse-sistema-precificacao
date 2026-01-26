import type { UnidadeMedida } from '$lib/services/material.service';

/**
 * Estado de validação de um campo individual
 */
export interface FieldValidation {
	error: string;
	showError: boolean;
}

/**
 * Dados do formulário de material
 */
export interface MaterialFormData {
	produto: string;
	unidade: UnidadeMedida | '';
	volume: number | '';
	preco: number | '';
	observacoes: string;
	ativo: boolean;
}

/**
 * Store reativa para gerenciar validação de formulário
 */
export class FormValidation {
	// Estado do formulário
	produto = $state('');
	unidade = $state<UnidadeMedida | ''>('');
	volume = $state<number | ''>('');
	preco = $state<number | ''>('');
	observacoes = $state('');
	ativo = $state(true);
	
	// Controle de exibição de erros
	submitted = $state(false);
	showErrors = $state<Record<string, boolean>>({
		produto: false,
		unidade: false,
		volume: false,
		preco: false
	});
	
	// Timers para debounce
	private timers: Record<string, any> = {};
	private debounceMs = 500;

	/**
	 * Validações reativas para cada campo
	 */
	get errors() {
		return {
			produto: !this.produto.trim() ? 'Produto é obrigatório' : '',
			unidade: !this.unidade ? 'Unidade de Medida é obrigatória' : '',
			volume: this.volume === '' || Number(this.volume) <= 0 ? 'Volume deve ser positivo' : '',
			preco: this.preco === '' || Number(this.preco) <= 0 ? 'Preço deve ser positivo' : ''
		};
	}

	/**
	 * Verifica se o formulário é válido
	 */
	get isValid() {
		return Object.values(this.errors).every(error => !error);
	}

	/**
	 * Calcula o custo unitário com 2 casas decimais
	 */
	get custoUnitario() {
		if (this.preco && this.volume) {
			return Number((Number(this.preco) / Number(this.volume)).toFixed(2));
		}
		return 0;
	}

	/**
	 * Formata o custo unitário para exibição (formato brasileiro)
	 */
	get custoUnitarioFormatado() {
		return this.custoUnitario.toFixed(2).replace('.', ',');
	}

	/**
	 * Retorna os dados do formulário
	 */
	get data(): MaterialFormData {
		return {
			produto: this.produto,
			unidade: this.unidade,
			volume: this.volume,
			preco: this.preco,
			observacoes: this.observacoes,
			ativo: this.ativo
		};
	}

	/**
	 * Preenche o formulário com dados existentes
	 */
	populate(data: MaterialFormData) {
		this.produto = data.produto;
		this.unidade = data.unidade;
		this.volume = data.volume;
		this.preco = data.preco;
		this.observacoes = data.observacoes;
		this.ativo = data.ativo;
	}

	/**
	 * Reseta o formulário para valores iniciais
	 */
	reset() {
		this.produto = '';
		this.unidade = '';
		this.volume = '';
		this.preco = '';
		this.observacoes = '';
		this.ativo = true;
		this.submitted = false;
		this.showErrors = {
			produto: false,
			unidade: false,
			volume: false,
			preco: false
		};
	}

	/**
	 * Agenda a exibição de erro após debounce
	 */
	scheduleErrorDisplay(fieldName: keyof typeof this.showErrors) {
		clearTimeout(this.timers[fieldName]);
		this.timers[fieldName] = setTimeout(() => {
			this.showErrors[fieldName] = !!this.errors[fieldName];
		}, this.debounceMs);
	}

	/**
	 * Mostra erro imediatamente (usado no blur)
	 */
	displayError(fieldName: keyof typeof this.showErrors) {
		this.showErrors[fieldName] = !!this.errors[fieldName];
	}

	/**
	 * Valida o formulário inteiro (usado no submit)
	 */
	validateAll() {
		this.submitted = true;
		Object.keys(this.showErrors).forEach(key => {
			this.showErrors[key as keyof typeof this.showErrors] = !!this.errors[key as keyof typeof this.errors];
		});
		return this.isValid;
	}

	/**
	 * Verifica se deve mostrar erro para um campo específico
	 */
	shouldShowError(fieldName: keyof typeof this.errors) {
		return (this.submitted || this.showErrors[fieldName]) && this.errors[fieldName];
	}
}
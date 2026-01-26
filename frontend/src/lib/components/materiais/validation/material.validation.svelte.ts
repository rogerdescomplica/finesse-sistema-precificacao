import type { UnidadeMedida } from '$lib/services/material.service';

type FieldName = 'produto' | 'unidade' | 'volume' | 'preco';
type Errors = Record<FieldName, string>;

export class FormValidation {
	produto = $state('');
	unidade = $state<UnidadeMedida | ''>('');
	volume = $state<number | ''>('');
	preco = $state<number | ''>('');
	observacoes = $state('');
	ativo = $state(true);

	submitted = $state(false);

	showErrors = $state<Record<FieldName, boolean>>({
		produto: false,
		unidade: false,
		volume: false,
		preco: false
	});

	private timers: Partial<Record<FieldName, ReturnType<typeof setTimeout>>> = {};
	private debounceMs = 500;

	// Lista dos campos para validação
	private readonly fields: FieldName[] = ['produto', 'unidade', 'volume', 'preco'];

	get errors(): Errors {
		return {
			produto: !this.produto.trim() ? 'Produto é obrigatório' : '',
			unidade: !this.unidade ? 'Unidade de Medida é obrigatória' : '',
			volume: this.volume === '' || Number(this.volume) <= 0 ? 'Volume deve ser positivo' : '',
			preco: this.preco === '' || Number(this.preco) <= 0 ? 'Preço deve ser positivo' : ''
		};
	}

	get isValid() {
		return Object.values(this.errors).every((error) => !error);
	}

	get custoUnitario() {
		if (this.preco && this.volume) {
			return Number((Number(this.preco) / Number(this.volume)).toFixed(2));
		}
		return 0;
	}

	get custoUnitarioFormatado() {
		return this.custoUnitario.toFixed(2).replace('.', ',');
	}

	scheduleErrorDisplay(fieldName: FieldName) {
		const t = this.timers[fieldName];
		if (t) clearTimeout(t);

		this.timers[fieldName] = setTimeout(() => {
			this.showErrors[fieldName] = !!this.errors[fieldName];
		}, this.debounceMs);
	}

	displayError(fieldName: FieldName) {
		this.showErrors[fieldName] = !!this.errors[fieldName];
	}

	validateAll() {
		this.submitted = true;

		for (const field of this.fields) {
			this.showErrors[field] = !!this.errors[field];
		}

		return this.isValid;
	}

	shouldShowError(fieldName: FieldName) {
		return (this.submitted || this.showErrors[fieldName]) && this.errors[fieldName];
	}

	populate(data: {
		produto: string;
		unidade: UnidadeMedida | '';
		volume: number | '';
		preco: number | '';
		observacoes: string;
		ativo: boolean;
	}) {
		this.produto = data.produto;
		this.unidade = data.unidade;
		this.volume = data.volume;
		this.preco = data.preco;
		this.observacoes = data.observacoes;
		this.ativo = data.ativo;
	}

	reset() {
		this.produto = '';
		this.unidade = '';
		this.volume = '';
		this.preco = '';
		this.observacoes = '';
		this.ativo = true;
		this.submitted = false;
		this.showErrors = { produto: false, unidade: false, volume: false, preco: false };
	}
}
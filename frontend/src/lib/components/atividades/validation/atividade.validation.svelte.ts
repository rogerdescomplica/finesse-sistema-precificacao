type FieldName = 'nome' | 'cnae' | 'aliquotaTotalPct' | 'issPct';
type Errors = Record<FieldName, string>;

export class FormValidation {
	nome = $state('');
	cnae = $state('');
	aliquotaTotalPct = $state<number | ''>('');
	issPct = $state<number | ''>('');
	observacao = $state('');
	ativo = $state(true);

	submitted = $state(false);
	showErrors = $state<Record<FieldName, boolean>>({ nome: false, cnae: false, aliquotaTotalPct: false, issPct: false });
	private timers: Partial<Record<FieldName, ReturnType<typeof setTimeout>>> = {};
	private debounceMs = 400;

	private readonly fields: FieldName[] = ['nome', 'cnae', 'aliquotaTotalPct', 'issPct'];

	get errors(): Errors {
		return {
			nome: !this.nome.trim() ? 'Nome é obrigatório' : '',
			cnae: !this.cnae.trim() ? 'CNAE é obrigatório' : '',
			aliquotaTotalPct: this.aliquotaTotalPct === '' || Number(this.aliquotaTotalPct) < 0 ? 'Alíquota inválida' : '',
			issPct: this.issPct === '' || Number(this.issPct) < 0 ? 'ISS inválido' : ''
		};
	}

	get isValid() {
		return Object.values(this.errors).every((e) => !e);
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

	populate(data: { nome: string; cnae: string; aliquotaTotalPct: number; issPct: number; observacao: string; ativo: boolean }) {
		this.nome = data.nome;
		this.cnae = data.cnae;
		this.aliquotaTotalPct = data.aliquotaTotalPct;
		this.issPct = data.issPct;
		this.observacao = data.observacao;
		this.ativo = data.ativo;
	}

	reset() {
		this.nome = '';
		this.cnae = '';
		this.aliquotaTotalPct = '';
		this.issPct = '';
		this.observacao = '';
		this.ativo = true;
		this.submitted = false;
		this.showErrors = { nome: false, cnae: false, aliquotaTotalPct: false, issPct: false };
	}
}

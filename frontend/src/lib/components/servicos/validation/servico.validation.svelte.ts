type FieldName = 'nome' | 'grupo' | 'duracao' | 'atividade';
type Errors = Record<FieldName, string>;

export class FormValidation {
	nome = $state('');
	grupo = $state('');
	duracao = $state<number | ''>('');
	atividadeId = $state<number | ''>('');
	margemLucroCustomPct = $state<number | ''>('');
	ativo = $state(true);
	materiais = $state<Array<{ materialId: number; quantidadeUsada: number }>>([]);

	submitted = $state(false);
	showErrors = $state<Record<FieldName, boolean>>({
		nome: false,
		grupo: false,
		duracao: false,
		atividade: false
	});

	private timers: Partial<Record<FieldName, ReturnType<typeof setTimeout>>> = {};
	private debounceMs = 500;
	private readonly fields: FieldName[] = ['nome', 'grupo', 'duracao', 'atividade'];

	get errors(): Errors {
		return {
			nome: !this.nome.trim() ? 'Nome é obrigatório' : '',
			grupo: !this.grupo.trim() ? 'Categoria (grupo) é obrigatória' : '',
			duracao: this.duracao === '' || Number(this.duracao) <= 0 ? 'Duração deve ser positiva' : '',
			atividade: this.atividadeId === '' || Number(this.atividadeId) <= 0 ? 'Selecione uma atividade' : ''
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
		for (const f of this.fields) this.showErrors[f] = !!this.errors[f];
		return this.isValid;
	}

	shouldShowError(fieldName: FieldName) {
		return (this.submitted || this.showErrors[fieldName]) && this.errors[fieldName];
	}

	populate(data: {
		nome: string;
		grupo: string;
		duracaoMinutos: number | '';
		atividadeId: number | '';
		margemLucroCustomPct?: number | '';
		ativo: boolean;
	}) {
		this.nome = data.nome;
		this.grupo = data.grupo;
		this.duracao = data.duracaoMinutos;
		this.atividadeId = data.atividadeId;
		this.margemLucroCustomPct = data.margemLucroCustomPct ?? '';
		this.ativo = data.ativo;
	}

	reset() {
		this.nome = '';
		this.grupo = '';
		this.duracao = '';
		this.atividadeId = '';
		this.margemLucroCustomPct = '';
		this.ativo = true;
		this.materiais = [];
		this.submitted = false;
		this.showErrors = { nome: false, grupo: false, duracao: false, atividade: false };
	}
}

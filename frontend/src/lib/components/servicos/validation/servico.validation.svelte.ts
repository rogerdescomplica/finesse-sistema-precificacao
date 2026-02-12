import { FormValidationBase } from '$lib/utils/form-validation.base.svelte'

export interface ServicoFormData {
	nome: string
	grupo: string
	duracaoMinutos: number | ''
	atividadeId: number | ''
	margemLucroCustomPct?: number | ''
	ativo: boolean
	materiais: Array<{ materialId: number; quantidadeUsada: number }>
}

type ValidatedField = Exclude<keyof ServicoFormData, 'margemLucroCustomPct' | 'ativo' | 'materiais'>

const validatedFields = ['nome', 'grupo', 'duracaoMinutos', 'atividadeId'] as const satisfies readonly ValidatedField[]

export class FormValidation extends FormValidationBase<ServicoFormData, ValidatedField> {
	nome = $state('')
	grupo = $state('')
	duracaoMinutos = $state<number | ''>('')
	atividadeId = $state<number | ''>('')
	margemLucroCustomPct = $state<number | ''>('')
	ativo = $state(true)
	materiais = $state<Array<{ materialId: number; quantidadeUsada: number }>>([])

	constructor() {
		super(
			validatedFields,
			() => ({
				nome: !this.nome.trim() ? 'Nome é obrigatório' : '',
				grupo: !this.grupo.trim() ? 'Categoria (grupo) é obrigatória' : '',
				duracaoMinutos:
					this.duracaoMinutos === '' || Number(this.duracaoMinutos) <= 0 ? 'Duração deve ser positiva' : '',
				atividadeId: this.atividadeId === '' || Number(this.atividadeId) <= 0 ? 'Selecione uma atividade' : ''
			}),
			500
		)
	}

	// Mantive assinatura bem próxima da sua
	populate(data: {
		nome: string
		grupo: string
		duracaoMinutos: number | ''
		atividadeId: number | ''
		margemLucroCustomPct?: number | ''
		ativo: boolean
		materiais?: Array<{ materialId: number; quantidadeUsada: number }>
	}) {
		this.nome = data.nome
		this.grupo = data.grupo
		this.duracaoMinutos = data.duracaoMinutos
		this.atividadeId = data.atividadeId
		this.margemLucroCustomPct = data.margemLucroCustomPct ?? ''
		this.ativo = data.ativo
		this.materiais = data.materiais ?? this.materiais
	}

	reset() {
		this.nome = ''
		this.grupo = ''
		this.duracaoMinutos = ''
		this.atividadeId = ''
		this.margemLucroCustomPct = ''
		this.ativo = true
		this.materiais = []

		this.resetValidationState()
	}
}
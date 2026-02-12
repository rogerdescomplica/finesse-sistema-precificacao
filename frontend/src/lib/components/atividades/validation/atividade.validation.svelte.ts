import { FormValidationBase } from '$lib/utils/form-validation.base.svelte'

export interface AtividadeFormData {
	nome: string
	cnae: string
	aliquotaTotalPct: number
	issPct: number
	observacao: string
	ativo: boolean
}

type ValidatedField = Exclude<keyof AtividadeFormData, 'observacao' | 'ativo'>

const validatedFields = ['nome', 'cnae', 'aliquotaTotalPct', 'issPct'] as const satisfies readonly ValidatedField[]

export class FormValidation extends FormValidationBase<AtividadeFormData, ValidatedField> {
	nome = $state('')
	cnae = $state('')
	aliquotaTotalPct = $state<number>(0)
	issPct = $state<number>(0)
	observacao = $state('')
	ativo = $state(true)

	constructor() {
		super(
			validatedFields,
			() => ({
				nome: !this.nome.trim() ? 'Nome é obrigatório' : '',
				cnae: !this.cnae.trim() ? 'CNAE é obrigatório' : '',
				aliquotaTotalPct: this.aliquotaTotalPct < 0 ? 'Alíquota inválida' : '',
				issPct: this.issPct < 0 ? 'ISS inválido' : ''
			}),
			400
		)
	}

	populate(data: AtividadeFormData) {
		this.nome = data.nome
		this.cnae = data.cnae
		this.aliquotaTotalPct = data.aliquotaTotalPct
		this.issPct = data.issPct
		this.observacao = data.observacao
		this.ativo = data.ativo

		// opcional: se quiser limpar erros ao carregar dados
		// this.resetValidationState()
	}

	reset() {
		this.nome = ''
		this.cnae = ''
		this.aliquotaTotalPct = 0
		this.issPct = 0
		this.observacao = ''
		this.ativo = true

		this.resetValidationState()
	}
}
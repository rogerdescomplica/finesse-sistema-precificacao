import { FormValidationBase } from '$lib/utils/form-validation.base.svelte'

export interface ConfigFormData {
  pretensaoSalarialMensal: number
  horasSemanais: number
  semanasMediaMes: number
  custoFixoPct: number
  margemLucroPadraoPct: number
  ativo: boolean
}

type ValidatedField = Exclude<keyof ConfigFormData, 'ativo'>

const validatedFields = [
  'pretensaoSalarialMensal',
  'horasSemanais',
  'semanasMediaMes',
  'custoFixoPct',
  'margemLucroPadraoPct'
] as const satisfies readonly ValidatedField[]

export class FormValidation extends FormValidationBase<ConfigFormData, ValidatedField> {
  pretensaoSalarialMensal = $state<number>(0)
  horasSemanais = $state<number>(0)
  semanasMediaMes = $state<number>(4.33)
  custoFixoPct = $state<number>(0)
  margemLucroPadraoPct = $state<number>(0)
  ativo = $state(true)

  constructor() {
    super(
      validatedFields,
      () => ({
        pretensaoSalarialMensal: this.pretensaoSalarialMensal <= 0 ? 'Informe valor mensal' : '',
        horasSemanais: this.horasSemanais <= 0 ? 'Informe horas semanais' : '',
        semanasMediaMes: this.semanasMediaMes <= 0 ? 'Informe semanas/mês' : '',
        custoFixoPct: this.custoFixoPct < 0 ? 'Informe custo fixo (%)' : '',
        margemLucroPadraoPct: this.margemLucroPadraoPct < 0 ? 'Informe margem (%)' : ''
      }),
      500
    )
  }

  populate(data: ConfigFormData) {
    this.pretensaoSalarialMensal = data.pretensaoSalarialMensal
    this.horasSemanais = data.horasSemanais
    this.semanasMediaMes = data.semanasMediaMes
    this.custoFixoPct = data.custoFixoPct
    this.margemLucroPadraoPct = data.margemLucroPadraoPct
    this.ativo = data.ativo

    // opcional: se você quiser limpar erros ao carregar dados
    // this.resetValidationState()
  }

  reset() {
    this.pretensaoSalarialMensal = 0
    this.horasSemanais = 0
    this.semanasMediaMes = 4.33
    this.custoFixoPct = 0
    this.margemLucroPadraoPct = 0
    this.ativo = true

    this.resetValidationState()
  }
}
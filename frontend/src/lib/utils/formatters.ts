export function formatCurrency(value: number) {
  try {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value)
  } catch {
    return `R$ ${value.toFixed(2)}`
  }
}

export function formatNumber(value: number, digits = 2) {
  return Number(value).toFixed(digits)
}

export function getStatusClass(ativo: boolean) {
  return ativo ? 'text-green-600' : 'text-gray-500'
}

export function getStatusText(ativo: boolean) {
  return ativo ? 'Ativo' : 'Inativo'
}

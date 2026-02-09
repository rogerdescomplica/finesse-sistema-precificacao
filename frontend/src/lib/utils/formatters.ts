export function formatCurrency(value: number) {
  try {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
  } catch {
    return `R$ ${Number(value).toFixed(2)}`;
  }
}

export function formatNumber(value: number, digits = 2) {
  const n = Number(value);
  if (!Number.isFinite(n)) return '0,00';

  return n.toLocaleString('pt-BR', {
    useGrouping: true, // milhar                
    minimumFractionDigits: digits,
    maximumFractionDigits: digits
  });
}

export function getStatusClass(ativo: boolean) {
  return ativo ? 'text-green-600' : 'text-gray-500'
}

export function getStatusText(ativo: boolean) {
  return ativo ? 'Ativo' : 'Inativo'
}

export function weekLabel(w) {
  return `Week of ${new Date(w.startDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}`
}

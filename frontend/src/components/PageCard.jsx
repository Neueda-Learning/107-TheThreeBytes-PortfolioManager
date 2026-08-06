const BASE_CARD_CLASSES = 'pm-glass-card rounded-[24px] border border-slate-200 bg-white shadow-[0_10px_30px_rgba(15,23,42,0.05)]'

export default function PageCard({ as: Component = 'div', className = '', children }) {
  return (
    <Component className={`${BASE_CARD_CLASSES} ${className}`.trim()}>
      {children}
    </Component>
  )
}

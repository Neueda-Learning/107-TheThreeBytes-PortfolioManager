import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts'
import InfoTooltip from './InfoTooltip'

const palette = [
  '#4F86F7', // Soft Blue
  '#5BAE9C', // Muted Teal
  '#8C7AE6', // Lavender
  '#F2B880', // Soft Orange
  '#7F8C9A', // Slate
  '#6FBF73', // Sage Green
  '#C97B84', // Dusty Rose
  '#A786DF', // Soft Purple
  '#7CC7D9', // Muted Cyan
  '#D6A85B'  // Warm Gold
]
export default function AllocationPieChart({ title, data, info }) {
  return (
    <div className="rounded-[24px] border border-slate-200 bg-white p-4 shadow-[0_10px_30px_rgba(15,23,42,0.05)] sm:p-5">
      <div className="flex items-center gap-1.5">
        <h3 className="text-lg font-semibold text-slate-900">{title}</h3>
        {info ? <InfoTooltip {...info} /> : null}
      </div>
      <div className="mt-4 h-[280px] w-full sm:h-[320px]">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie data={data} dataKey="value" nameKey="name" innerRadius={60} outerRadius={100} paddingAngle={2}>
              {data.map((entry, index) => (
                <Cell key={`${entry.name}-${index}`} fill={palette[index % palette.length]} />
              ))}
            </Pie>
            <Tooltip />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}

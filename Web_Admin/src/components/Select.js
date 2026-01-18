import React from 'react'
import { ChevronDown } from 'lucide-react'

export function Select({
  label,
  options,
  error,
  className = '',
  ...props
}) {
  return (
    <div className="flex flex-col gap-1.5 w-full">
      <label className="text-sm font-medium text-slate-700">{label}</label>
      <div className="relative">
        <select
          className={`
            flex h-10 w-full appearance-none rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 
            focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent
            disabled:cursor-not-allowed disabled:opacity-50 transition-all duration-200 pr-10
            ${error ? 'border-red-500 focus:ring-red-500' : ''}
            ${className}
          `}
          {...props}
        >
          {options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
        <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-500 pointer-events-none" />
      </div>
      {error && <span className="text-xs text-red-500">{error}</span>}
    </div>
  )
}

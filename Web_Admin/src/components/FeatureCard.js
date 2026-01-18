import React from 'react'

export function FeatureCard({
  title,
  description,
  icon: Icon,
  onClick,
  colorClass = 'text-indigo-600 bg-indigo-50',
}) {
  return (
    <button
      onClick={onClick}
      className="group flex flex-col items-start p-8 bg-white rounded-2xl border border-slate-200 shadow-sm hover:shadow-md hover:border-indigo-200 transition-all duration-300 text-left w-full h-full"
    >
      <div
        className={`p-3 rounded-xl mb-6 ${colorClass} group-hover:scale-110 transition-transform duration-300`}
      >
        <Icon size={32} strokeWidth={1.5} />
      </div>

      <h3 className="text-xl font-semibold text-slate-900 mb-2 group-hover:text-indigo-700 transition-colors">
        {title}
      </h3>

      <p className="text-slate-500 leading-relaxed">{description}</p>

      <div className="mt-auto pt-6 flex items-center text-sm font-medium text-indigo-600 opacity-0 -translate-x-2 group-hover:opacity-100 group-hover:translate-x-0 transition-all duration-300">
        Truy cập ngay &rarr;
      </div>
    </button>
  )
}

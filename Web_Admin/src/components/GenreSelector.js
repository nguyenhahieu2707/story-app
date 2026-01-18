import React from 'react'
import { Check } from 'lucide-react'

const AVAILABLE_GENRES = [
  { value: 'THE_THAO', label: 'Thể thao' },
  { value: 'VO_THUAT', label: 'Võ thuật' },
  { value: 'HAI_HUOC', label: 'Hài hước' },
  { value: 'KHOA_HOC_VIEN_TUONG', label: 'Khoa học viễn tưởng' },
  { value: 'CO_TICH', label: 'Cổ tích' },
  { value: 'KHOA_HOC', label: 'Khoa học' },
  { value: 'GIA_TUONG', label: 'Giả tưởng' },
]

export function GenreSelector({
  selectedGenres,
  onChange,
}) {
  const toggleGenre = (genreValue) => {
    if (selectedGenres.includes(genreValue)) {
      onChange(selectedGenres.filter((g) => g !== genreValue))
    } else {
      onChange([...selectedGenres, genreValue])
    }
  }

  return (
    <div className="flex flex-col gap-3 w-full">
      <label className="text-sm font-medium text-slate-700">Thể loại</label>
      <div className="flex flex-wrap gap-2">
        {AVAILABLE_GENRES.map((genre) => {
          const isSelected = selectedGenres.includes(genre.value)
          return (
            <button
              key={genre.value}
              type="button"
              onClick={() => toggleGenre(genre.value)}
              className={`
                flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium transition-all duration-200 border
                ${isSelected ? 'bg-indigo-600 border-indigo-600 text-white shadow-sm hover:bg-indigo-700' : 'bg-white border-slate-200 text-slate-600 hover:border-indigo-300 hover:text-indigo-600'}
              `}
            >
              {isSelected && <Check size={14} strokeWidth={3} />}
              {genre.label}
            </button>
          )
        })}
      </div>
    </div>
  )
}

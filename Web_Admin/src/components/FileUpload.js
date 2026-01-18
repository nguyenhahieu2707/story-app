import React, { useState, useRef, useEffect } from 'react'
import { Upload, X } from 'lucide-react'

export function FileUpload({ label, onChange, error, initialPreviewUrl }) {
  const [preview, setPreview] = useState(initialPreviewUrl || null)
  const fileInputRef = useRef(null)

  useEffect(() => {
    if (initialPreviewUrl) {
      setPreview(initialPreviewUrl)
    }
  }, [initialPreviewUrl])

  const handleFileChange = (e) => {
    const file = e.target.files?.[0]
    if (file) {
      const objectUrl = URL.createObjectURL(file)
      setPreview(objectUrl)
      onChange(file)
    }
  }

  const handleRemove = () => {
    setPreview(null)
    onChange(null)
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }

  return (
    <div className="flex flex-col gap-1.5 w-full">
      <label className="text-sm font-medium text-slate-700">{label}</label>

      {!preview ? (
        <div
          onClick={() => fileInputRef.current?.click()}
          className={`
            border-2 border-dashed rounded-xl p-8 flex flex-col items-center justify-center cursor-pointer
            transition-all duration-200 group hover:bg-slate-50
            ${error ? 'border-red-300 bg-red-50' : 'border-slate-300'}
          `}
        >
          <div className="h-12 w-12 bg-indigo-50 text-indigo-600 rounded-full flex items-center justify-center mb-3 group-hover:scale-110 transition-transform duration-200">
            <Upload size={20} />
          </div>
          <p className="text-sm font-medium text-slate-700">
            Nhấn để tải ảnh lên
          </p>
          <p className="text-xs text-slate-500 mt-1">PNG, JPG tối đa 5MB</p>
        </div>
      ) : (
        <div className="relative rounded-xl overflow-hidden border border-slate-200 group">
          <img
            src={preview}
            alt="Cover preview"
            className="w-full h-64 object-cover"
          />
          <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity duration-200 flex items-center justify-center">
            <button
              onClick={handleRemove}
              className="bg-white/90 hover:bg-white text-red-600 px-4 py-2 rounded-lg text-sm font-medium shadow-sm transition-all transform hover:scale-105 flex items-center gap-2"
            >
              <X size={16} />
              Xóa ảnh
            </button>
          </div>
        </div>
      )}

      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        className="hidden"
        onChange={handleFileChange}
      />
      {error && <span className="text-xs text-red-500">{error}</span>}
    </div>
  )
}

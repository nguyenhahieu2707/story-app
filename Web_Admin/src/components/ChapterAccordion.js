import React from 'react'
import { Trash2, ChevronDown, Plus } from 'lucide-react'
import { Input } from './Input'
import { Textarea } from './Textarea'

export function ChapterAccordion({
  chapters,
  expandedId,
  onToggleExpand,
  onUpdateChapter,
  onDeleteChapter,
  onAddChapter,
}) {
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between mb-4">
        <label className="text-lg font-semibold text-slate-900">
          Danh sách chương ({chapters.length})
        </label>
        <button
          type="button"
          onClick={onAddChapter}
          className="flex items-center gap-2 px-4 py-2 bg-indigo-50 text-indigo-700 rounded-lg text-sm font-medium hover:bg-indigo-100 transition-colors"
        >
          <Plus size={16} />
          Thêm chương mới
        </button>
      </div>

      <div className="space-y-3">
        {chapters.length === 0 && (
          <div className="text-center py-12 bg-slate-50 rounded-xl border border-dashed border-slate-300 text-slate-500">
            Chưa có chương nào. Nhấn "Thêm chương mới" để bắt đầu viết.
          </div>
        )}

        {chapters.map((chapter, index) => {
          const isExpanded = expandedId === chapter.id
          const chapterNumber = index + 1
          return (
            <div
              key={chapter.id}
              className={`
                bg-white border rounded-xl overflow-hidden transition-all duration-300
                ${isExpanded ? 'border-indigo-200 shadow-md ring-1 ring-indigo-100' : 'border-slate-200 hover:border-indigo-200'}
              `}
            >
              {/* Header - Always visible */}
              <div
                onClick={() => onToggleExpand(chapter.id)}
                className="flex items-center justify-between p-4 cursor-pointer bg-white hover:bg-slate-50 transition-colors"
              >
                <div className="flex items-center gap-3">
                  <div
                    className={`
                    w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold transition-colors
                    ${isExpanded ? 'bg-indigo-600 text-white' : 'bg-slate-100 text-slate-600'}
                  `}
                  >
                    {chapterNumber}
                  </div>
                  <div className="flex flex-col">
                    <span
                      className={`font-medium ${isExpanded ? 'text-indigo-900' : 'text-slate-900'}`}
                    >
                      {chapter.title || `Chương ${chapterNumber}`}
                    </span>
                    <span className="text-xs text-slate-500">
                      {chapter.content
                        ? `${chapter.content.length} ký tự`
                        : 'Chưa có nội dung'}
                    </span>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation()
                      onDeleteChapter(chapter.id)
                    }}
                    className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    title="Xóa chương"
                  >
                    <Trash2 size={18} />
                  </button>
                  <ChevronDown
                    size={20}
                    className={`text-slate-400 transition-transform duration-300 ${isExpanded ? 'rotate-180' : ''}`}
                  />
                </div>
              </div>

              {/* Expanded Content */}
              <div
                className={`
                  overflow-hidden transition-all duration-300 ease-in-out
                  ${isExpanded ? 'max-h-[800px] opacity-100' : 'max-h-0 opacity-0'}
                `}
              >
                <div className="p-6 border-t border-slate-100 bg-slate-50/50 space-y-5">
                  <Input
                    label="Tiêu đề chương"
                    placeholder={`Ví dụ: Chương ${chapterNumber}: Sự khởi đầu`}
                    value={chapter.title}
                    onChange={(e) =>
                      onUpdateChapter(chapter.id, 'title', e.target.value)
                    }
                  />
                  <Textarea
                    label="Nội dung chương"
                    placeholder="Nhập nội dung chương truyện tại đây..."
                    value={chapter.content}
                    onChange={(e) =>
                      onUpdateChapter(chapter.id, 'content', e.target.value)
                    }
                    className="min-h-[300px] font-mono text-base"
                  />
                </div>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}

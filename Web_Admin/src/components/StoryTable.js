import React from 'react'
import {
  Edit2,
  Trash2,
  Eye,
  EyeOff,
  BookOpen,
  User,
  Calendar,
  ImageOff
} from 'lucide-react'

const statusConfig = {
  DRAFT: {
    label: 'Chưa phát hành',
    class: 'bg-slate-100 text-slate-600',
  },
  ONGOING: {
    label: 'Đang phát hành',
    class: 'bg-blue-100 text-blue-700',
  },
  COMPLETED: {
    label: 'Đã hoàn thành',
    class: 'bg-emerald-100 text-emerald-700',
  },
}

export function StoryTable({
  stories,
  onEdit,
  onDelete,
  onToggleVisibility,
  isUserStory = false
}) {
  if (stories.length === 0) {
    return (
      <div className="text-center py-16 bg-white rounded-2xl border border-slate-200 shadow-sm">
        <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-slate-100 mb-4">
          <BookOpen className="h-8 w-8 text-slate-400" />
        </div>
        <h3 className="text-lg font-medium text-slate-900 mb-1">
          Không tìm thấy truyện nào
        </h3>
        <p className="text-slate-500">
          Thử thay đổi từ khóa tìm kiếm hoặc thêm truyện mới.
        </p>
      </div>
    )
  }
  return (
    <div className="w-full">
      {/* Desktop Table View */}
      <div className="hidden md:block bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50 border-b border-slate-200 text-xs uppercase text-slate-500 font-semibold tracking-wider">
              <th className="px-6 py-4 w-20">Ảnh bìa</th>
              <th className="px-6 py-4">Thông tin truyện</th>
              <th className="px-6 py-4">Trạng thái</th>
              <th className="px-6 py-4 text-right">Hành động</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {stories.map((story) => (
              <tr
                key={story.id}
                className={`group hover:bg-slate-50 transition-colors ${!story.webView ? 'opacity-60 bg-slate-50/50' : ''}`}
              >
                <td className="px-6 py-4">
                  <div className="h-12 w-12 rounded-lg bg-slate-200 overflow-hidden shadow-sm border border-slate-100 flex items-center justify-center">
                    {story.coverImageUrl ? (
                      <img
                        src={story.coverImageUrl}
                        alt={story.title}
                        className="h-full w-full object-cover"
                        onError={(e) => {
                          e.target.style.display = 'none';
                          e.target.nextSibling.style.display = 'flex';
                        }}
                      />
                    ) : null}
                    <div 
                      className="w-full h-full flex items-center justify-center bg-slate-100 text-slate-400"
                      style={{ display: story.coverImageUrl ? 'none' : 'flex' }}
                    >
                      <ImageOff size={20} />
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <div className="flex flex-col">
                    <span className="font-semibold text-slate-900 group-hover:text-indigo-600 transition-colors">
                      {story.title}
                    </span>
                    <span className="text-sm text-slate-500 flex items-center gap-1 mt-1">
                      <User size={12} />
                      {story.author}
                    </span>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <span
                    className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${statusConfig[story.status]?.class || 'bg-gray-100 text-gray-600'}`}
                  >
                    {statusConfig[story.status]?.label || story.status}
                  </span>
                </td>
                <td className="px-6 py-4 text-right">
                  <div className="flex items-center justify-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                    {!isUserStory && (
                      <>
                        <button
                          onClick={() => onToggleVisibility(story.id)}
                          className="p-2 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
                          title={!story.webView ? 'Hiện truyện' : 'Ẩn truyện'}
                        >
                          {!story.webView ? (
                            <EyeOff size={18} />
                          ) : (
                            <Eye size={18} />
                          )}
                        </button>
                        <button
                          onClick={() => onEdit(story.id)}
                          className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                          title="Chỉnh sửa"
                        >
                          <Edit2 size={18} />
                        </button>
                      </>
                    )}
                    <button
                      onClick={() => onDelete(story.id)}
                      className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                      title="Xóa truyện"
                    >
                      <Trash2 size={18} />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Mobile Card View */}
      <div className="md:hidden space-y-4">
        {stories.map((story) => (
          <div
            key={story.id}
            className={`bg-white rounded-xl border border-slate-200 p-4 shadow-sm ${!story.webView ? 'opacity-75 bg-slate-50' : ''}`}
          >
            <div className="flex gap-4">
              <div className="h-20 w-16 rounded-lg bg-slate-200 overflow-hidden flex-shrink-0 border border-slate-100 flex items-center justify-center">
                {story.coverImageUrl ? (
                  <img
                    src={story.coverImageUrl}
                    alt={story.title}
                    className="h-full w-full object-cover"
                    onError={(e) => {
                      e.target.style.display = 'none';
                      e.target.nextSibling.style.display = 'flex';
                    }}
                  />
                ) : null}
                <div 
                  className="w-full h-full flex items-center justify-center bg-slate-100 text-slate-400"
                  style={{ display: story.coverImageUrl ? 'none' : 'flex' }}
                >
                  <ImageOff size={24} />
                </div>
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex justify-between items-start mb-1">
                  <h3 className="font-semibold text-slate-900 truncate pr-2">
                    {story.title}
                  </h3>
                  <button
                    onClick={() => onDelete(story.id)}
                    className="text-slate-400 hover:text-red-500 p-1 -mr-1"
                  >
                    <Trash2 size={16} />
                  </button>
                </div>
                <p className="text-sm text-slate-500 mb-2 flex items-center gap-1">
                  <User size={12} /> {story.author}
                </p>
                <div className="flex flex-wrap gap-2 items-center">
                  <span
                    className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${statusConfig[story.status]?.class || 'bg-gray-100 text-gray-600'}`}
                  >
                    {statusConfig[story.status]?.label || story.status}
                  </span>
                </div>
              </div>
            </div>

            {!isUserStory && (
              <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-end gap-3">
                <button
                  onClick={() => onToggleVisibility(story.id)}
                  className="text-sm font-medium text-slate-500 hover:text-indigo-600 flex items-center gap-1.5"
                >
                  {!story.webView ? <EyeOff size={16} /> : <Eye size={16} />}
                  {!story.webView ? 'Hiện' : 'Ẩn'}
                </button>
                <button
                  onClick={() => onEdit(story.id)}
                  className="text-sm font-medium text-indigo-600 hover:text-indigo-700 flex items-center gap-1.5"
                >
                  <Edit2 size={16} />
                  Sửa
                </button>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}

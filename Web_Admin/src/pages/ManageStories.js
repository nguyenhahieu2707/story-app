import React, { useEffect, useState, useCallback } from 'react'
import { ArrowLeft, Plus } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { SearchBar } from '../components/SearchBar'
import { StoryTable } from '../components/StoryTable'
import { ConfirmDialog } from '../components/ConfirmDialog'
import storyService from '../services/storyService'

// Debounce hook implementation
function useDebounce(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}

export default function ManageStories() {
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState('admin') // 'admin' or 'user'
  const [searchQuery, setSearchQuery] = useState('')
  const [stories, setStories] = useState([])
  const [deleteId, setDeleteId] = useState(null)
  const [isLoading, setIsLoading] = useState(false)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  const debouncedSearchQuery = useDebounce(searchQuery, 500);

  const fetchStories = useCallback(async () => {
    setIsLoading(true)
    try {
      let response;
      if (debouncedSearchQuery) {
        response = await storyService.searchStoriesForAdmin(debouncedSearchQuery, activeTab, page, 10)
      } else {
        response = await storyService.getStoriesForAdmin(activeTab, page, 10)
      }
      setStories(response.content)
      setTotalPages(response.totalPages)
    } catch (error) {
      console.error('Error fetching stories:', error)
    } finally {
      setIsLoading(false)
    }
  }, [activeTab, page, debouncedSearchQuery])

  useEffect(() => {
    fetchStories()
  }, [fetchStories])

  // Reset page when tab or search query changes
  useEffect(() => {
    setPage(0)
  }, [activeTab, debouncedSearchQuery])

  // Handlers
  const handleDelete = async () => {
    if (deleteId) {
      try {
        await storyService.deleteStory(deleteId)
        setStories((prev) => prev.filter((s) => s.id !== deleteId))
        setDeleteId(null)
        alert('Đã xóa truyện thành công')
      } catch (error) {
        console.error('Error deleting story:', error)
        alert('Có lỗi xảy ra khi xóa truyện')
      }
    }
  }

  const handleToggleVisibility = async (id) => {
    try {
      await storyService.toggleWebView(id)
      // Update local state to reflect change
      setStories((prev) =>
        prev.map((s) =>
          s.id === id
            ? {
                ...s,
                webView: !s.webView,
              }
            : s,
        ),
      )
    } catch (error) {
      console.error('Error toggling visibility:', error)
      alert('Có lỗi xảy ra khi thay đổi trạng thái hiển thị')
    }
  }

  const handleEdit = (id) => {
    navigate(`/stories/edit/${id}`)
  }

  return (
    <div className="min-h-screen bg-slate-50 pb-20">
      {/* Header */}
      <div className="bg-white border-b border-slate-200 sticky top-0 z-20">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate('/dashboard')}
              className="p-2 -ml-2 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-full transition-colors"
            >
              <ArrowLeft size={20} />
            </button>
            <h1 className="text-xl font-bold text-slate-900 whitespace-nowrap">Quản Lý Truyện</h1>
          </div>
          <div className="flex-shrink-0">
            <button
              onClick={() => navigate('/stories/new')}
              className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 shadow-sm hover:shadow transition-all whitespace-nowrap"
            >
              <Plus size={18} />
              <span className="hidden sm:inline">Thêm Truyện Mới</span>
            </button>
          </div>
        </div>
      </div>

      <main className="max-w-6xl mx-auto px-6 py-8">
        {/* Tabs & Search */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 mb-8">
          {/* Tabs */}
          <div className="bg-slate-100 p-1 rounded-xl inline-flex">
            <button
              onClick={() => setActiveTab('admin')}
              className={`
                px-6 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 whitespace-nowrap
                ${activeTab === 'admin' ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-500 hover:text-slate-700 hover:bg-slate-200/50'}
              `}
            >
              Truyện Hệ Thống
            </button>
            <button
              onClick={() => setActiveTab('user')}
              className={`
                px-6 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 whitespace-nowrap
                ${activeTab === 'user' ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-500 hover:text-slate-700 hover:bg-slate-200/50'}
              `}
            >
              Truyện Người Dùng
            </button>
          </div>

          {/* Search */}
          <div className="w-full md:w-80">
            <SearchBar
              value={searchQuery}
              onChange={setSearchQuery}
              placeholder="Tìm theo tên truyện hoặc tác giả..."
            />
          </div>
        </div>

        {/* Content */}
        {isLoading ? (
          <div className="text-center py-12">Đang tải dữ liệu...</div>
        ) : (
          <>
            <StoryTable
              stories={stories}
              onEdit={handleEdit}
              onDelete={setDeleteId}
              onToggleVisibility={handleToggleVisibility}
              isUserStory={activeTab === 'user'}
            />
            
            {/* Pagination Controls */}
            <div className="flex justify-center mt-8 gap-2">
              <button
                disabled={page === 0}
                onClick={() => setPage(p => p - 1)}
                className="px-4 py-2 border rounded disabled:opacity-50"
              >
                Trước
              </button>
              <span className="px-4 py-2">Trang {page + 1} / {totalPages || 1}</span>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage(p => p + 1)}
                className="px-4 py-2 border rounded disabled:opacity-50"
              >
                Sau
              </button>
            </div>
          </>
        )}
      </main>

      {/* Delete Confirmation Dialog */}
      <ConfirmDialog
        isOpen={!!deleteId}
        onClose={() => setDeleteId(null)}
        onConfirm={handleDelete}
        title="Xóa truyện này?"
        message="Hành động này không thể hoàn tác. Toàn bộ dữ liệu về truyện và các chương sẽ bị xóa vĩnh viễn khỏi hệ thống."
        confirmText="Xóa vĩnh viễn"
        isDangerous={true}
      />
    </div>
  )
}

import React, { useEffect, useState } from 'react'
import { ArrowLeft } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { UserTable } from '../components/UserTable'
import userService from '../services/userService'

export default function ManageUsers() {
  const navigate = useNavigate()
  const [users, setUsers] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  const fetchUsers = async () => {
    setIsLoading(true)
    try {
      const response = await userService.getAllUsers(page, 10)
      setUsers(response.content)
      setTotalPages(response.totalPages)
    } catch (error) {
      console.error('Error fetching users:', error)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchUsers()
  }, [page])

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
            <h1 className="text-xl font-bold text-slate-900 whitespace-nowrap">Quản Lý Người Dùng</h1>
          </div>
        </div>
      </div>

      <main className="max-w-6xl mx-auto px-6 py-8">
        {isLoading ? (
          <div className="text-center py-12">Đang tải dữ liệu...</div>
        ) : (
          <>
            <UserTable users={users} />
            
            {/* Pagination Controls */}
            <div className="flex justify-center mt-8 gap-2">
              <button
                disabled={page === 0}
                onClick={() => setPage(p => p - 1)}
                className="px-4 py-2 border rounded disabled:opacity-50 bg-white hover:bg-slate-50 transition-colors"
              >
                Trước
              </button>
              <span className="px-4 py-2 bg-white border rounded flex items-center">
                Trang {page + 1} / {totalPages || 1}
              </span>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage(p => p + 1)}
                className="px-4 py-2 border rounded disabled:opacity-50 bg-white hover:bg-slate-50 transition-colors"
              >
                Sau
              </button>
            </div>
          </>
        )}
      </main>
    </div>
  )
}

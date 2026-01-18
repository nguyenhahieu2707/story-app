import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { BookPlus, Library, Users, LayoutDashboard, LogOut } from 'lucide-react'
import { FeatureCard } from '../components/FeatureCard'
import authService from '../services/authService'
import dashboardService from '../services/dashboardService'

export default function AdminDashboard() {
  const navigate = useNavigate()
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalStories: 0
  })

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const data = await dashboardService.getStats()
        setStats(data)
      } catch (error) {
        console.error('Error fetching dashboard stats:', error)
      }
    }
    fetchStats()
  }, [])

  const handleLogout = async () => {
    await authService.logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Header */}
      <header className="bg-white border-b border-slate-200 sticky top-0 z-10">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="bg-indigo-600 p-2 rounded-lg">
              <LayoutDashboard className="text-white h-5 w-5" />
            </div>
            <span className="font-bold text-lg text-slate-900 tracking-tight">
              Story Speaker Admin
            </span>
          </div>
          <div className="flex items-center gap-4">
            <div className="h-8 w-8 rounded-full bg-slate-200 flex items-center justify-center text-slate-600 font-medium text-sm">
              AD
            </div>
            <button 
              onClick={handleLogout}
              className="p-2 text-slate-500 hover:text-red-600 hover:bg-red-50 rounded-full transition-colors"
              title="Đăng xuất"
            >
              <LogOut size={20} />
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-6xl mx-auto px-6 py-12">
        <div className="mb-12 text-center max-w-2xl mx-auto">
          <h1 className="text-3xl md:text-4xl font-bold text-slate-900 mb-4 tracking-tight">
            Chào mừng trở lại
          </h1>
          <p className="text-lg text-slate-500">
            Quản lý toàn bộ hệ thống Story Speaker từ một nơi. Chọn một chức
            năng bên dưới để bắt đầu.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 lg:gap-8">
          <FeatureCard
            title="Tạo Truyện Mới"
            description="Thêm nội dung truyện mới vào hệ thống. Hỗ trợ nhập liệu văn bản và cấu hình âm thanh."
            icon={BookPlus}
            onClick={() => navigate('/stories/new')}
            colorClass="text-emerald-600 bg-emerald-50"
          />

          <FeatureCard
            title="Quản Lý Truyện"
            description="Xem danh sách, chỉnh sửa hoặc xóa các truyện đã có. Quản lý trạng thái xuất bản."
            icon={Library}
            onClick={() => navigate('/stories')}
            colorClass="text-blue-600 bg-blue-50"
          />

          <FeatureCard
            title="Quản Lý User"
            description="Kiểm soát tài khoản người dùng, phân quyền và xem thống kê hoạt động."
            icon={Users}
            onClick={() => navigate('/users')}
            colorClass="text-violet-600 bg-violet-50"
          />
        </div>

        {/* Quick Stats Section */}
        <div className="mt-16 pt-12 border-t border-slate-200">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div>
              <div className="text-sm font-medium text-slate-500 mb-1">
                Tổng số truyện
              </div>
              <div className="text-2xl font-bold text-slate-900">{stats.totalStories}</div>
            </div>
            
            <div>
              <div className="text-sm font-medium text-slate-500 mb-1">
                Số lượng người dùng
              </div>
              <div className="text-2xl font-bold text-slate-900">{stats.totalUsers}</div>
            </div>
            
            <div>
              <div className="text-sm font-medium text-slate-500 mb-1">
                Trạng thái hệ thống
              </div>
              <div className="flex items-center text-emerald-600 font-medium">
                <span className="w-2 h-2 bg-emerald-500 rounded-full mr-2"></span>
                Hoạt động tốt
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}

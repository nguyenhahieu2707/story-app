import React from 'react'
import { User, Mail, Calendar, Shield } from 'lucide-react'

export function UserTable({ users }) {
  if (users.length === 0) {
    return (
      <div className="text-center py-16 bg-white rounded-2xl border border-slate-200 shadow-sm">
        <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-slate-100 mb-4">
          <User className="h-8 w-8 text-slate-400" />
        </div>
        <h3 className="text-lg font-medium text-slate-900 mb-1">
          Chưa có người dùng nào
        </h3>
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
              <th className="px-6 py-4">Tên người dùng</th>
              <th className="px-6 py-4">Email</th>
              <th className="px-6 py-4">Vai trò</th>
              <th className="px-6 py-4">Ngày tham gia</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {users.map((user) => (
              <tr key={user.id} className="hover:bg-slate-50 transition-colors">
                <td className="px-6 py-4">
                  <div className="flex items-center gap-3">
                    <div className="h-10 w-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 font-bold text-sm">
                      {user.name ? user.name.charAt(0).toUpperCase() : 'U'}
                    </div>
                    <span className="font-medium text-slate-900">{user.name || 'Chưa đặt tên'}</span>
                  </div>
                </td>
                <td className="px-6 py-4 text-slate-600">
                  <div className="flex items-center gap-2">
                    <Mail size={16} className="text-slate-400" />
                    {user.email}
                  </div>
                </td>
                <td className="px-6 py-4">
                  <span
                    className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium ${
                      user.role === 'ADMIN' 
                        ? 'bg-purple-100 text-purple-700' 
                        : 'bg-blue-100 text-blue-700'
                    }`}
                  >
                    <Shield size={12} />
                    {user.role}
                  </span>
                </td>
                <td className="px-6 py-4 text-slate-500">
                  <div className="flex items-center gap-2">
                    <Calendar size={16} className="text-slate-400" />
                    {user.createDate ? new Date(user.createDate).toLocaleDateString('vi-VN') : 'N/A'}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Mobile Card View */}
      <div className="md:hidden space-y-4">
        {users.map((user) => (
          <div key={user.id} className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
            <div className="flex items-center gap-4 mb-3">
              <div className="h-12 w-12 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 font-bold text-lg">
                {user.name ? user.name.charAt(0).toUpperCase() : 'U'}
              </div>
              <div>
                <h3 className="font-semibold text-slate-900">{user.name || 'Chưa đặt tên'}</h3>
                <span
                  className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium mt-1 ${
                    user.role === 'ADMIN' 
                      ? 'bg-purple-100 text-purple-700' 
                      : 'bg-blue-100 text-blue-700'
                  }`}
                >
                  {user.role}
                </span>
              </div>
            </div>
            
            <div className="space-y-2 text-sm text-slate-600">
              <div className="flex items-center gap-2">
                <Mail size={16} className="text-slate-400" />
                {user.email}
              </div>
              <div className="flex items-center gap-2">
                <Calendar size={16} className="text-slate-400" />
                {user.createDate ? new Date(user.createDate).toLocaleDateString('vi-VN') : 'N/A'}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

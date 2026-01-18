import React, { useState, useEffect } from 'react'
import { ArrowLeft, Save } from 'lucide-react'
import { useNavigate, useParams } from 'react-router-dom'
import { Input } from '../components/Input'
import { Textarea } from '../components/Textarea'
import { Select } from '../components/Select'
import { FileUpload } from '../components/FileUpload'
import { GenreSelector } from '../components/GenreSelector'
import { ChapterAccordion } from '../components/ChapterAccordion'
import storyService from '../services/storyService'

// Helper to map category names to enum values
const mapCategoryNameToEnum = (name) => {
  const mapping = {
    'Thể thao': 'THE_THAO',
    'Võ thuật': 'VO_THUAT',
    'Hài hước': 'HAI_HUOC',
    'Khoa học viễn tưởng': 'KHOA_HOC_VIEN_TUONG',
    'Cổ tích': 'CO_TICH',
    'Khoa học': 'KHOA_HOC',
    'Giả tưởng': 'GIA_TUONG'
  };
  return mapping[name] || name;
};

export default function EditStory() {
  const navigate = useNavigate()
  const { id } = useParams()
  const [isLoading, setIsLoading] = useState(false)
  const [isFetching, setIsFetching] = useState(true)

  // Form State
  const [formData, setFormData] = useState({
    title: '',
    author: '',
    description: '',
    ageRating: '',
    status: 'DRAFT',
    coverImage: null, // File object if manual upload
    coverImageUrl: '', // URL string from DB
    genres: [],
  })
  
  // Chapter State
  const [chapters, setChapters] = useState([])
  const [expandedChapterId, setExpandedChapterId] = useState(null)
  
  // Fetch Story Data
  useEffect(() => {
    const fetchStory = async () => {
      try {
        const data = await storyService.getStoryById(id)
        
        // Map response to form state
        setFormData({
          title: data.title || '',
          author: data.author || '',
          description: data.description || '',
          ageRating: data.ageRating || '',
          status: data.status || 'DRAFT',
          coverImage: null,
          coverImageUrl: data.coverImageUrl || '',
          genres: data.categoryNames ? data.categoryNames.map(mapCategoryNameToEnum) : [],
        })

        // Map chapters
        if (data.chapters) {
          setChapters(data.chapters.map(ch => ({
            id: ch.id, // Keep original ID for update logic
            title: ch.title,
            content: ch.content,
            chapterNumber: ch.chapterNumber
          })))
        }
      } catch (error) {
        console.error('Error fetching story:', error)
        alert('Không thể tải thông tin truyện.')
        navigate('/stories')
      } finally {
        setIsFetching(false)
      }
    }

    if (id) {
      fetchStory()
    }
  }, [id, navigate])

  // Handlers
  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
  }
  
  const handleAddChapter = () => {
    const newId = `temp-${crypto.randomUUID()}` // Temp ID for frontend
    const newChapter = {
      id: newId,
      title: '',
      content: '',
      isNew: true // Flag to identify new chapters
    }
    setChapters((prev) => [...prev, newChapter])
    setExpandedChapterId(newId)
  }
  
  const handleUpdateChapter = (id, field, value) => {
    setChapters((prev) =>
      prev.map((ch) =>
        ch.id === id
          ? {
              ...ch,
              [field]: value,
            }
          : ch,
      ),
    )
  }
  
  const handleDeleteChapter = (id) => {
    setChapters((prev) => prev.filter((ch) => ch.id !== id))
    if (expandedChapterId === id) {
      setExpandedChapterId(null)
    }
  }
  
  const handleSubmit = async (e) => {
    e.preventDefault()
    setIsLoading(true)

    try {
      const data = new FormData();
      data.append('title', formData.title);
      data.append('author', formData.author);
      data.append('description', formData.description);
      if (formData.ageRating) data.append('ageRating', formData.ageRating);
      data.append('storyStatus', formData.status);
      
      if (formData.coverImage) {
        data.append('coverImage', formData.coverImage);
      }
      
      formData.genres.forEach((genre, index) => {
        data.append(`categories[${index}]`, genre);
      });
      
      chapters.forEach((chapter, index) => {
        // If chapter has a real ID (not temp), send it to update
        if (!chapter.id.startsWith('temp-')) {
            data.append(`chapters[${index}].id`, chapter.id);
        }
        // If it's a temp ID, we don't send ID so backend creates new one
        
        data.append(`chapters[${index}].title`, chapter.title);
        data.append(`chapters[${index}].chapterNumber`, index + 1);
        data.append(`chapters[${index}].content`, chapter.content);
      });

      await storyService.updateStory(id, data);
      alert('Đã cập nhật truyện thành công!');
      navigate('/stories');
    } catch (error) {
      console.error('Error updating story:', error);
      alert('Có lỗi xảy ra khi cập nhật truyện. Vui lòng thử lại.');
    } finally {
      setIsLoading(false)
    }
  }
  
  if (isFetching) {
    return <div className="min-h-screen flex items-center justify-center">Đang tải dữ liệu...</div>
  }

  return (
    <div className="min-h-screen bg-slate-50 pb-20">
      {/* Header */}
      <div className="bg-white border-b border-slate-200 sticky top-0 z-20">
        <div className="max-w-5xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate('/stories')}
              className="p-2 -ml-2 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-full transition-colors"
            >
              <ArrowLeft size={20} />
            </button>
            <h1 className="text-xl font-bold text-slate-900 whitespace-nowrap">Chỉnh Sửa Truyện</h1>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={() => navigate('/stories')}
              className="px-4 py-2 text-sm font-medium text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded-lg transition-colors whitespace-nowrap"
            >
              Hủy bỏ
            </button>
            <button
              onClick={handleSubmit}
              disabled={isLoading}
              className="flex items-center gap-2 px-5 py-2 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 shadow-sm hover:shadow transition-all disabled:opacity-70 whitespace-nowrap"
            >
              <Save size={18} />
              {isLoading ? 'Đang lưu...' : 'Lưu Thay Đổi'}
            </button>
          </div>
        </div>
      </div>

      <main className="max-w-5xl mx-auto px-6 py-8">
        <form
          onSubmit={handleSubmit}
          className="grid grid-cols-1 lg:grid-cols-3 gap-8"
        >
          {/* Left Column: Main Info */}
          <div className="lg:col-span-2 space-y-8">
            {/* Basic Info Section */}
            <section className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
              <h2 className="text-lg font-semibold text-slate-900 mb-6 flex items-center gap-2">
                <span className="w-1 h-6 bg-indigo-500 rounded-full"></span>
                Thông tin chung
              </h2>

              <div className="space-y-5">
                <Input
                  label="Tiêu đề truyện"
                  name="title"
                  placeholder="Nhập tên truyện..."
                  value={formData.title}
                  onChange={handleInputChange}
                  required
                />

                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                  <Input
                    label="Tác giả"
                    name="author"
                    placeholder="Tên tác giả..."
                    value={formData.author}
                    onChange={handleInputChange}
                    required
                  />
                  <Select
                    label="Trạng thái"
                    name="status"
                    value={formData.status}
                    onChange={handleInputChange}
                    options={[
                      {
                        value: 'DRAFT',
                        label: 'Chưa phát hành',
                      },
                      {
                        value: 'ONGOING',
                        label: 'Đang phát hành',
                      },
                      {
                        value: 'COMPLETED',
                        label: 'Đã hoàn thành',
                      },
                    ]}
                  />
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                  <Input
                    label="Độ tuổi giới hạn"
                    name="ageRating"
                    type="number"
                    placeholder="Ví dụ: 13, 16, 18"
                    value={formData.ageRating}
                    onChange={handleInputChange}
                  />
                </div>

                <Textarea
                  label="Mô tả tóm tắt"
                  name="description"
                  placeholder="Giới thiệu nội dung chính của truyện..."
                  value={formData.description}
                  onChange={handleInputChange}
                  className="min-h-[150px]"
                />

                <GenreSelector
                  selectedGenres={formData.genres}
                  onChange={(genres) =>
                    setFormData((prev) => ({
                      ...prev,
                      genres,
                    }))
                  }
                />
              </div>
            </section>

            {/* Chapters Section */}
            <section className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
              <h2 className="text-lg font-semibold text-slate-900 mb-6 flex items-center gap-2">
                <span className="w-1 h-6 bg-indigo-500 rounded-full"></span>
                Nội dung chi tiết
              </h2>

              <ChapterAccordion
                chapters={chapters}
                expandedId={expandedChapterId}
                onToggleExpand={(id) =>
                  setExpandedChapterId((prev) => (prev === id ? null : id))
                }
                onUpdateChapter={handleUpdateChapter}
                onDeleteChapter={handleDeleteChapter}
                onAddChapter={handleAddChapter}
              />
            </section>
          </div>

          {/* Right Column: Cover Image & Meta */}
          <div className="space-y-8">
            <section className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm sticky top-24">
              <h2 className="text-lg font-semibold text-slate-900 mb-6 flex items-center gap-2">
                <span className="w-1 h-6 bg-indigo-500 rounded-full"></span>
                Ảnh bìa
              </h2>

              <FileUpload
                label="Tải lên ảnh bìa"
                initialPreviewUrl={formData.coverImageUrl}
                onChange={(file) =>
                  setFormData((prev) => ({
                    ...prev,
                    coverImage: file,
                    coverImageUrl: '' // Clear URL if user uploads new file manually
                  }))
                }
              />

              <div className="mt-6 p-4 bg-slate-50 rounded-xl text-sm text-slate-500">
                <p className="font-medium text-slate-700 mb-2">Lưu ý:</p>
                <ul className="list-disc pl-4 space-y-1">
                  <li>Kích thước khuyến nghị: 600x900px</li>
                  <li>Dung lượng tối đa: 5MB</li>
                  <li>Định dạng: JPG, PNG</li>
                </ul>
              </div>
            </section>
          </div>
        </form>
      </main>
    </div>
  )
}

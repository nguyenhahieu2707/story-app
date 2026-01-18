import React, { useState, useRef } from 'react'
import { ArrowLeft, Save, UploadCloud } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { Input } from '../components/Input'
import { Textarea } from '../components/Textarea'
import { Select } from '../components/Select'
import { FileUpload } from '../components/FileUpload'
import { GenreSelector } from '../components/GenreSelector'
import { ChapterAccordion } from '../components/ChapterAccordion'
import storyService from '../services/storyService'

export default function CreateStory() {
  const navigate = useNavigate()
  const epubInputRef = useRef(null)
  const [isExtracted, setIsExtracted] = useState(false)
  const [isLoading, setIsLoading] = useState(false)

  // Form State
  const [formData, setFormData] = useState({
    title: '',
    author: '',
    description: '',
    ageRating: '',
    status: 'DRAFT',
    coverImage: null, // File object if manual upload
    coverImageUrl: '', // URL string if extracted
    genres: [],
  })
  
  // Chapter State
  const [chapters, setChapters] = useState([])
  const [expandedChapterId, setExpandedChapterId] = useState(null)
  
  // Handlers
  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
  }
  
  const handleAddChapter = () => {
    const newId = crypto.randomUUID()
    const newChapter = {
      id: newId,
      title: '',
      content: '',
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

  const handleEpubUpload = async (e) => {
    const file = e.target.files?.[0]
    if (!file) return

    setIsLoading(true)
    try {
      const extractedData = await storyService.extractEpub(file)
      
      // Fill form with extracted data
      setFormData(prev => ({
        ...prev,
        title: extractedData.title || '',
        author: extractedData.author || '',
        description: extractedData.description || '',
        ageRating: extractedData.ageRating || '',
        coverImageUrl: extractedData.coverImageUrl || '',
        genres: extractedData.categories || [],
        coverImage: null // Reset manual file if any
      }))

      // Fill chapters
      if (extractedData.chapters) {
        const newChapters = extractedData.chapters.map(ch => ({
          id: crypto.randomUUID(),
          title: ch.title,
          content: ch.content,
          chapterNumber: ch.chapterNumber
        }))
        setChapters(newChapters)
      }

      setIsExtracted(true)
      alert('Đã import EPUB thành công!')
    } catch (error) {
      console.error('Error extracting EPUB:', error)
      alert('Lỗi khi đọc file EPUB. Vui lòng thử lại.')
    } finally {
      setIsLoading(false)
      if (epubInputRef.current) epubInputRef.current.value = ''
    }
  }
  
  const handleSubmit = async (e) => {
    e.preventDefault()
    setIsLoading(true)

    try {
      if (isExtracted) {
        // Use JSON API for extracted story
        const payload = {
          title: formData.title,
          author: formData.author,
          description: formData.description,
          ageRating: formData.ageRating ? parseInt(formData.ageRating) : null,
          coverImageUrl: formData.coverImageUrl,
          status: formData.status, // Added status field
          categories: formData.genres,
          chapters: chapters.map((ch, index) => ({
            title: ch.title,
            chapterNumber: index + 1,
            content: ch.content
          }))
        }
        await storyService.saveExtractedStory(payload)
      } else {
        // Use Multipart API for manual creation
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
          data.append(`chapters[${index}].title`, chapter.title);
          data.append(`chapters[${index}].chapterNumber`, index + 1);
          data.append(`chapters[${index}].content`, chapter.content);
        });

        await storyService.createStory(data);
      }

      alert('Đã lưu truyện thành công!');
      navigate('/dashboard');
    } catch (error) {
      console.error('Error saving story:', error);
      alert('Có lỗi xảy ra khi lưu truyện. Vui lòng thử lại.');
    } finally {
      setIsLoading(false)
    }
  }
  
  return (
    <div className="min-h-screen bg-slate-50 pb-20">
      {/* Header */}
      <div className="bg-white border-b border-slate-200 sticky top-0 z-20">
        <div className="max-w-5xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate('/dashboard')}
              className="p-2 -ml-2 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-full transition-colors"
            >
              <ArrowLeft size={20} />
            </button>
            <h1 className="text-xl font-bold text-slate-900 whitespace-nowrap">
              {isExtracted ? 'Biên tập truyện từ EPUB' : 'Tạo Truyện Mới'}
            </h1>
          </div>
          <div className="flex items-center gap-3">
            <input
              type="file"
              accept=".epub"
              ref={epubInputRef}
              className="hidden"
              onChange={handleEpubUpload}
            />
            <button
              type="button"
              onClick={() => epubInputRef.current?.click()}
              disabled={isLoading}
              className="flex items-center gap-2 px-4 py-2 bg-emerald-50 text-emerald-700 text-sm font-medium rounded-lg hover:bg-emerald-100 transition-colors border border-emerald-200 whitespace-nowrap"
            >
              <UploadCloud size={18} />
              {isLoading ? 'Đang xử lý...' : 'Import EPUB'}
            </button>
            
            <div className="h-6 w-px bg-slate-300 mx-1"></div>

            <button
              onClick={() => navigate('/dashboard')}
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
              {isLoading ? 'Đang lưu...' : 'Lưu Truyện'}
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
                    // Removed disabled={isExtracted} to allow status selection
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

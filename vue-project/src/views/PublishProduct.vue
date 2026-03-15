<template>
  <div class="publish-page">
    <h1>发布商品</h1>

    <el-form
      :model="productForm"
      :rules="rules"
      ref="formRef"
      label-width="100px"
      class="publish-form"
    >
      <el-form-item label="商品标题" prop="title">
        <el-input
          v-model="productForm.title"
          placeholder="请输入商品标题"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="商品描述" prop="description">
        <el-input
          v-model="productForm.description"
          type="textarea"
          :rows="4"
          placeholder="请输入商品描述"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="价格" prop="price">
        <el-input-number
          v-model="productForm.price"
          :min="0"
          :precision="2"
          placeholder="请输入价格"
        />
        <span style="margin-left: 8px">元</span>
      </el-form-item>

      <el-form-item label="联系电话" prop="phone">
        <el-input
          v-model="productForm.phone"
          placeholder="请输入联系电话（11位手机号）"
          maxlength="11"
        />
      </el-form-item>

      <el-form-item label="商品图片" prop="images">
        <el-upload
          v-model:file-list="productForm.images"
          action="/api/upload/image"
          :headers="uploadHeaders"
          list-type="picture-card"
          :limit="5"
          :on-exceed="handleExceed"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          multiple
          accept="image/*"
        >
          <el-icon><Plus /></el-icon>
          <div class="upload-text">点击上传图片</div>
          <template #tip>
            <div class="upload-tip">最多上传5张图片，支持JPG、PNG格式，单张图片不超过2MB</div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="loading">发布商品</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>

import { reactive, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { productAPI } from '@/services/api'

const formRef = ref()
const loading = ref(false)
const uploadedImages = ref([]) // 存储上传成功的图片URL

// 上传请求的headers，包含token
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const productForm = reactive({
  title: '',
  description: '',
  price: null,
  phone: '',
  images: [],
})

const rules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    { min: 10, max: 200, message: '长度在 10 到 200 个字符', trigger: 'blur' },
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', message: '价格必须为数字值' },
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { min: 11, max: 11, message: '联系电话必须为11位手机号', trigger: 'blur' },
  ],
}

// 图片上传超过限制时的处理
const handleExceed = () => {
  ElMessage.warning('最多只能上传5张图片')
}

// 图片上传前的验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2MB = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt2MB) {
    ElMessage.error('图片大小不能超过2MB！')
    return false
  }
  return true
}

// 上传成功处理
const handleUploadSuccess = (response, file) => {
  console.log('上传成功响应:', JSON.stringify(response, null, 2))
  console.log('上传文件信息:', file)

  // 根据实际返回数据结构获取图片URL
  // 测试返回格式: { "data": { "imageUrl": "/uploads/images/..." }, "success": true, "message": "上传成功" }
  let imageUrl = null

  // 尝试多种可能的路径获取URL
  if (response?.data?.imageUrl) {
    // 主要路径：response.data.imageUrl
    imageUrl = response.data.imageUrl
    console.log('从 response.data.imageUrl 获取图片URL:', imageUrl)
  } else if (response?.imageUrl) {
    // 备用路径：response.imageUrl
    imageUrl = response.imageUrl
    console.log('从 response.imageUrl 获取图片URL:', imageUrl)
  } else if (response?.data?.url) {
    // 备用路径：response.data.url
    imageUrl = response.data.url
    console.log('从 response.data.url 获取图片URL:', imageUrl)
  } else if (response?.url) {
    // 备用路径：response.url
    imageUrl = response.url
    console.log('从 response.url 获取图片URL:', imageUrl)
  } else if (file?.url) {
    // 备用路径：file.url
    imageUrl = file.url
    console.log('从 file.url 获取图片URL:', imageUrl)
  } else if (file?.response?.data?.imageUrl) {
    // Element Upload组件可能将响应存储在file.response中
    imageUrl = file.response.data.imageUrl
    console.log('从 file.response.data.imageUrl 获取图片URL:', imageUrl)
  }

  // 保存原始URL（用于发送给后端）
  const originalUrl = imageUrl
  let displayUrl = imageUrl // 用于预览显示的URL

  // 将相对路径转换为绝对URL（前端8080端口，后端8088端口）
  if (imageUrl) {
    // 如果URL以/开头且不是完整的http/https URL，则添加后端地址
    if (imageUrl.startsWith('/') && !imageUrl.startsWith('http://') && !imageUrl.startsWith('https://')) {
      // 后端运行在http://localhost:8088
      displayUrl = `http://localhost:8088${imageUrl}`
      console.log('转换相对路径为绝对URL（用于预览）:', originalUrl, '->', displayUrl)
    } else {
      console.log('图片URL已是绝对路径或不需要转换:', imageUrl)
    }
  }

  if (originalUrl) {
    // 确保文件对象有正确的URL（使用显示URL），以便Element Upload组件正确显示预览
    if (file && !file.url) {
      file.url = displayUrl
      console.log('已设置 file.url（用于预览）:', displayUrl)
    }

    // 添加到已上传图片列表（使用原始URL，用于发送给后端）
    uploadedImages.value.push(originalUrl)
    console.log('原始图片URL已添加到 uploadedImages（用于后端）:', originalUrl, '当前列表:', uploadedImages.value)

    // 确保productForm.images中的对应文件也有正确的URL（使用显示URL，用于预览）
    // Element Upload组件通过v-model:file-list管理productForm.images
    // 但为了确保数据一致，我们可以尝试更新
    const fileIndex = productForm.images.findIndex(item => item.uid === file?.uid)
    if (fileIndex !== -1 && file) {
      productForm.images[fileIndex] = { ...productForm.images[fileIndex], url: displayUrl }
      console.log('已更新 productForm.images 中对应文件的URL（用于预览）')
    }

    ElMessage.success('图片上传成功')
  } else {
    console.warn('无法获取图片URL，响应结构:', response)
    console.warn('文件信息:', file)
    ElMessage.warning('图片上传成功，但未获取到URL，请检查控制台日志')
  }
}

// 上传失败处理
const handleUploadError = (error) => {
  console.error('上传失败:', error)
  ElMessage.error('图片上传失败，请重试')
}

const submitForm = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    loading.value = true

    // 调试：打印上传的图片信息
    console.log('准备发布商品，已上传的图片列表:', uploadedImages.value)
    console.log('上传图片数量:', uploadedImages.value.length)
    console.log('商品表单数据:', {
      title: productForm.title,
      description: productForm.description,
      price: productForm.price,
      phone: productForm.phone
    })

    // 准备商品数据
    const productData = {
      title: productForm.title,
      description: productForm.description,
      price: productForm.price,
      phone: productForm.phone,
      images: uploadedImages.value // 使用上传成功的图片URL
    }

    console.log('准备发送的商品数据:', productData)

    // 调用API发布商品
    await productAPI.createProduct(productData)

    ElMessage.success('商品发布成功！')
    resetForm()
  } catch (error) {
    console.error('发布商品失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    if (error.response?.status === 401) {
      ElMessage.error('请先登录')
    } else {
      ElMessage.error('发布商品失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
  uploadedImages.value = []
  productForm.images = []
}
</script>

<style scoped>
.publish-page {
  padding: 20px;
  background: white;
  border-radius: 8px;
  min-height: 300px;
}

.publish-page h1 {
  color: #333;
  margin-bottom: 30px;
  font-size: 24px;
}

.publish-form {
  max-width: 600px;
}

.upload-text {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}
</style>

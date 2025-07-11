<template>
  <div class="profile-page">
    <div class="container">
      <!-- Header -->
      <div class="profile-header glass-card">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <img :src="userProfile.avatar || '/default-avatar.png'" class="avatar" alt="avatar" />
            <button class="avatar-edit-btn" @click="openAvatarModal" title="Đổi ảnh đại diện">
              📷
            </button>
          </div>
        </div>
        <div class="info-section">
          <h2>{{ userProfile.fullName }}</h2>
          <p class="email">{{ userProfile.email }}</p>
          <div class="badges">
            <span v-if="userProfile.isVip" class="badge vip">👑 VIP</span>
            <span v-if="userProfile.isVerified" class="badge verified">✅ Đã xác thực</span>
          </div>
        </div>
      </div>

      <!-- Tabs -->
      <div class="profile-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          :class="{ active: activeTab === tab.id }"
          @click="activeTab = tab.id"
        >
          <span class="tab-icon">{{ tab.icon }}</span> {{ tab.label }}
        </button>
      </div>

      <!-- Tab content -->
      <transition name="fade-slide" mode="out-in">
        <div class="profile-content glass-card" :key="activeTab">
          <!-- Thông tin cá nhân -->
          <div v-if="activeTab === 'info'">
            <form class="profile-form" @submit.prevent="updateProfile">
              <div class="form-row">
                <label>Họ tên</label>
                <input v-model="profileForm.fullName" required />
              </div>
              <div class="form-row">
                <label>Email</label>
                <input v-model="profileForm.email" disabled />
              </div>
              <div class="form-row">
                <label>Số điện thoại</label>
                <input v-model="profileForm.phone" />
              </div>
              <div class="form-row">
                <label>Ngày sinh</label>
                <input v-model="profileForm.birthday" type="date" />
              </div>
              <div class="form-row">
                <label>Địa chỉ</label>
                <input v-model="profileForm.address" />
              </div>
              <div class="form-actions">
                <button type="submit" class="btn btn-primary" :disabled="updating">
                  {{ updating ? 'Đang lưu...' : 'Lưu thay đổi' }}
                </button>
                <button type="button" class="btn btn-secondary" @click="showPasswordModal = true">
                  Đổi mật khẩu
                </button>
              </div>
            </form>
          </div>
        </div>
      </transition>
    </div>

    <!-- Modal đổi avatar -->
    <div v-if="showAvatarModal" class="modal-overlay" @click.self="closeAvatarModal">
      <div class="modal-content avatar-modal">
        <h3>Cập nhật ảnh đại diện</h3>
        <div
          class="avatar-upload-area"
          @click="triggerFileInput"
          :class="{ hovered: isUploadHover }"
        >
          <img v-if="previewAvatar" :src="previewAvatar" class="avatar-preview" />
          <div v-else class="avatar-placeholder">📷<br />Nhấn để chọn ảnh</div>
          <input
            ref="avatarInput"
            type="file"
            accept="image/*"
            @change="handleAvatarSelect"
            style="display: none"
          />
        </div>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="closeAvatarModal">Hủy</button>
          <button
            class="btn btn-primary"
            @click="uploadAvatar"
            :disabled="!previewAvatar || uploading"
          >
            {{ uploading ? 'Đang tải...' : 'Cập nhật' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Modal đổi mật khẩu -->
    <div v-if="showPasswordModal" class="modal-overlay" @click.self="showPasswordModal = false">
      <div class="modal-content">
        <h3>Đổi mật khẩu</h3>
        <form @submit.prevent="changePassword">
          <div class="form-row">
            <label>Mật khẩu hiện tại</label>
            <input type="password" v-model="passwordForm.currentPassword" required />
          </div>
          <div class="form-row">
            <label>Mật khẩu mới</label>
            <input type="password" v-model="passwordForm.newPassword" required />
          </div>
          <div class="form-row">
            <label>Xác nhận mật khẩu mới</label>
            <input type="password" v-model="passwordForm.confirmPassword" required />
          </div>
          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="showPasswordModal = false">
              Hủy
            </button>
            <button type="submit" class="btn btn-primary" :disabled="passwordChanging">
              {{ passwordChanging ? 'Đang đổi...' : 'Đổi mật khẩu' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { profileAPI } from '@/services/api'

const userProfile = ref({
  fullName: '',
  email: '',
  avatar: '',
  phone: '',
  birthday: '',
  address: '',
  isVip: false,
  isVerified: false,
})
const profileForm = ref({
  fullName: '',
  email: '',
  phone: '',
  birthday: '',
  address: '',
})
const updating = ref(false)
const showAvatarModal = ref(false)
const showPasswordModal = ref(false)
const uploading = ref(false)
const previewAvatar = ref('')
const avatarInput = ref(null)
const passwordForm = ref({ currentPassword: '', newPassword: '', confirmPassword: '' })
const passwordChanging = ref(false)
const activeTab = ref('info')
const isUploadHover = ref(false)
const tabs = [{ id: 'info', label: 'Thông tin', icon: '👤' }]
const orders = ref([])
const wishlist = ref([])
const settings = ref({ language: 'vi', darkMode: false })

const openAvatarModal = () => {
  showAvatarModal.value = true
  previewAvatar.value = ''
}
const closeAvatarModal = () => {
  showAvatarModal.value = false
  previewAvatar.value = ''
}
const triggerFileInput = () => {
  if (avatarInput.value) avatarInput.value.click()
}
const handleAvatarSelect = (e) => {
  const file = e.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (ev) => {
      previewAvatar.value = ev.target.result
    }
    reader.readAsDataURL(file)
  }
}
const uploadAvatar = async () => {
  if (!avatarInput.value.files[0]) return
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', avatarInput.value.files[0])
    const res = await profileAPI.uploadAvatar(formData)
    userProfile.value.avatar = res.data.avatarUrl
    profileForm.value.avatar = res.data.avatarUrl
    previewAvatar.value = ''
    closeAvatarModal()
    alert('Cập nhật ảnh đại diện thành công!')
  } catch (e) {
    alert('Lỗi upload ảnh!')
  } finally {
    uploading.value = false
  }
}
const updateProfile = async () => {
  updating.value = true
  try {
    const [firstName, ...lastArr] = (profileForm.value.fullName || '').trim().split(' ')
    const lastName = lastArr.join(' ') || ''
    const payload = {
      firstName,
      lastName,
      phone: profileForm.value.phone,
      birthday: profileForm.value.birthday,
      address: profileForm.value.address,
      avatar: profileForm.value.avatar || userProfile.value.avatar,
    }
    await profileAPI.updateProfile(payload)
    userProfile.value.firstName = firstName
    userProfile.value.lastName = lastName
    userProfile.value.phone = payload.phone
    userProfile.value.birthday = payload.birthday
    userProfile.value.address = payload.address
    userProfile.value.avatar = payload.avatar
    userProfile.value.fullName = firstName + ' ' + lastName
    alert('Cập nhật thành công!')
  } catch (e) {
    alert('Lỗi cập nhật!')
  } finally {
    updating.value = false
  }
}
const changePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    alert('Mật khẩu xác nhận không khớp!')
    return
  }
  passwordChanging.value = true
  try {
    await profileAPI.changePassword(passwordForm.value)
    showPasswordModal.value = false
    alert('Đổi mật khẩu thành công!')
  } catch (e) {
    alert('Lỗi đổi mật khẩu!')
  } finally {
    passwordChanging.value = false
  }
}
const formatCurrency = (v) =>
  v ? v.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' }) : ''

onMounted(async () => {
  try {
    const res = await profileAPI.getProfile()
    userProfile.value = {
      ...res.data,
      fullName: (res.data.firstName || '') + ' ' + (res.data.lastName || ''),
    }
    profileForm.value = {
      fullName: userProfile.value.fullName,
      email: userProfile.value.email,
      phone: userProfile.value.phone,
      birthday: userProfile.value.birthday,
      address: userProfile.value.address,
      avatar: userProfile.value.avatar,
    }
  } catch {}
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #23244a 0%, #181a2a 100%);
  color: #fff;
  font-family: 'Inter', Arial, sans-serif;
}
.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}
.glass-card {
  background: rgba(30, 40, 80, 0.85);
  border-radius: 1.5rem;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.2);
  padding: 2rem;
  margin-bottom: 2rem;
}
.profile-header {
  display: flex;
  align-items: center;
  gap: 2.5rem;
  margin-bottom: 2rem;
}
.avatar-section {
  position: relative;
}
.avatar-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}
.avatar {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  border: 4px solid #00d4ff;
  object-fit: cover;
  box-shadow: 0 4px 24px #00d4ff44;
  transition: box-shadow 0.3s;
}
.avatar-wrapper:hover .avatar {
  box-shadow: 0 8px 32px #00d4ff99;
}
.avatar-edit-btn {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: linear-gradient(135deg, #00d4ff 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 50%;
  width: 44px;
  height: 44px;
  cursor: pointer;
  font-size: 1.3rem;
  box-shadow: 0 2px 8px #00d4ff44;
  transition: background 0.2s, transform 0.2s;
  z-index: 2;
}
.avatar-edit-btn:hover {
  background: linear-gradient(135deg, #764ba2 0%, #00d4ff 100%);
  transform: scale(1.1);
}
.info-section h2 {
  margin: 0;
  font-size: 2.2rem;
  font-weight: 700;
  letter-spacing: 1px;
}
.info-section .email {
  color: #b3e0ff;
  margin-bottom: 0.5rem;
}
.badges {
  margin-top: 0.5rem;
}
.badge {
  display: inline-block;
  padding: 0.3rem 1rem;
  border-radius: 1rem;
  margin-right: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  box-shadow: 0 2px 8px #00d4ff22;
}
.badge.vip {
  background: linear-gradient(90deg, gold 60%, #fffbe6 100%);
  color: #222;
  border: 1px solid #ffe066;
}
.badge.verified {
  background: linear-gradient(90deg, #10b981 60%, #34d399 100%);
  color: #fff;
  border: 1px solid #10b981;
}
.profile-tabs {
  display: flex;
  gap: 1.2rem;
  margin-bottom: 2rem;
  justify-content: center;
}
.profile-tabs button {
  background: none;
  border: none;
  color: #b3e0ff;
  padding: 0.8rem 2rem;
  border-radius: 1.2rem;
  cursor: pointer;
  font-weight: 600;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  gap: 0.7rem;
  transition: background 0.2s, color 0.2s;
}
.profile-tabs .active,
.profile-tabs button:hover {
  background: linear-gradient(90deg, #00d4ff 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 2px 8px #00d4ff33;
}
.tab-icon {
  font-size: 1.3rem;
}
.profile-content {
  min-height: 320px;
  font-size: 1.1rem;
  transition: box-shadow 0.3s;
}
.profile-form {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}
.form-row {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  margin-bottom: 1rem;
}
.form-row label {
  color: #b3e0ff;
  font-weight: 500;
  font-size: 1rem;
}
.form-row input,
.form-row select {
  padding: 0.8rem 1rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1.5px solid #00d4ff55;
  border-radius: 10px;
  color: #fff;
  font-size: 1.1rem;
  transition: border 0.2s;
}
.form-row input:focus,
.form-row select:focus {
  outline: none;
  border-color: #00d4ff;
  box-shadow: 0 0 0 2px #00d4ff33;
}
.form-actions {
  display: flex;
  gap: 1.2rem;
  margin-top: 1rem;
}
.btn {
  padding: 0.8rem 2rem;
  border-radius: 1rem;
  border: none;
  font-weight: 700;
  cursor: pointer;
  font-size: 1.1rem;
  transition: background 0.2s, color 0.2s, transform 0.2s;
}
.btn-primary {
  background: linear-gradient(90deg, #00d4ff 0%, #764ba2 100%);
  color: #fff;
}
.btn-primary:hover {
  background: linear-gradient(90deg, #764ba2 0%, #00d4ff 100%);
  color: #fff;
  transform: translateY(-2px) scale(1.04);
}
.btn-secondary {
  background: #23244a;
  color: #00d4ff;
  border: 1.5px solid #00d4ff;
}
.btn-secondary:hover {
  background: #00d4ff;
  color: #23244a;
}
.empty-block {
  color: #b3e0ff;
  text-align: center;
  margin: 2rem 0;
  font-size: 1.2rem;
}
.order-list,
.wishlist-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.order-item,
.wishlist-item {
  background: rgba(0, 212, 255, 0.08);
  border-radius: 0.8rem;
  margin-bottom: 1rem;
  padding: 1rem 1.5rem;
  display: flex;
  align-items: center;
  gap: 1.5rem;
  box-shadow: 0 2px 8px #00d4ff11;
}
.order-id {
  font-weight: 700;
  color: #00d4ff;
}
.order-status {
  color: #10b981;
  font-weight: 600;
}
.order-total {
  margin-left: auto;
  font-weight: 700;
  color: #fff;
}
.wishlist-name {
  color: #ff6bcb;
  font-weight: 600;
  font-size: 1.1rem;
}
/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #000a;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content {
  background: #23244a;
  border-radius: 1.2rem;
  padding: 2.2rem;
  min-width: 320px;
  max-width: 95vw;
  box-shadow: 0 8px 32px #00d4ff33;
  position: relative;
}
.avatar-modal {
  text-align: center;
}
.avatar-upload-area {
  border: 2.5px dashed #00d4ff88;
  border-radius: 1.2rem;
  padding: 2.5rem;
  text-align: center;
  cursor: pointer;
  margin-bottom: 1.5rem;
  transition: border-color 0.2s, background 0.2s;
}
.avatar-upload-area:hover,
.avatar-upload-area.hovered {
  border-color: #00d4ff;
  background: rgba(0, 212, 255, 0.05);
}
.avatar-preview {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  object-fit: cover;
  margin: 0 auto;
  box-shadow: 0 4px 24px #00d4ff44;
}
.avatar-placeholder {
  color: #00d4ff;
  font-size: 2.2rem;
  font-weight: 600;
}
.modal-actions {
  display: flex;
  gap: 1.2rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(30px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-30px);
}
@media (max-width: 600px) {
  .container {
    padding: 0.5rem;
  }
  .profile-header {
    flex-direction: column;
    gap: 1.2rem;
  }
  .avatar {
    width: 90px;
    height: 90px;
  }
  .avatar-preview {
    width: 90px;
    height: 90px;
  }
  .glass-card {
    padding: 1rem;
  }
  .modal-content {
    padding: 1rem;
  }
}
</style>

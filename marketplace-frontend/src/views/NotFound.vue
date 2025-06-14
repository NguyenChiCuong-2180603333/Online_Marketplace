<template>
  <div class="not-found">
    <div class="container text-center">
      <h1>🚀 404 - Không tìm thấy trang</h1>
      <p>Trang bạn tìm kiếm không tồn tại trong vũ trụ này</p>
      <router-link to="/" class="btn btn-primary">🏠 Về trang chủ</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotFound'
}
</script>
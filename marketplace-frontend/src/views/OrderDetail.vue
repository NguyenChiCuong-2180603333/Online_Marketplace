<template>
  <div class="order-detail">
    <div class="container">
      <h1>📄 Chi tiết đơn hàng</h1>
      <p>Chi tiết đơn hàng {{ $route.params.id }}</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OrderDetail'
}
</script>
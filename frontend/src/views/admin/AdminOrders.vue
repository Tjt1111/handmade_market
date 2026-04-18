<template>
  <div class="admin-orders">
    <h2>订单管理</h2>
    <div class="filters"><input v-model="searchOrderNo" placeholder="订单号"><select v-model="filterStatus"><option value="all">全部状态</option><option value="pending">待支付</option><option value="paid">已支付</option><option value="shipped">已发货</option></select></div>
    <div class="order-table"><table><thead><tr><th>订单号</th><th>买家</th><th>卖家</th><th>金额</th><th>状态</th><th>下单时间</th><th>操作</th></tr></thead><tbody><tr v-for="order in filteredOrders" :key="order.id"><td>{{ order.no }}</td><td>{{ order.buyer }}</td><td>{{ order.seller }}</td><td>¥{{ order.amount }}</td><td><span :class="['status', order.status]">{{ order.status === 'pending' ? '待支付' : order.status === 'paid' ? '已支付' : '已发货' }}</span></td><td>{{ order.time }}</td><td><button class="view-btn" @click="viewDetail(order)">详情</button></td></tr></tbody></table></div>
    <div v-if="showDetailModal" class="modal" @click="showDetailModal=false"><div class="modal-content" @click.stop><h3>订单详情</h3><p>订单号：{{ currentOrder.no }}</p><p>买家：{{ currentOrder.buyer }}</p><p>卖家：{{ currentOrder.seller }}</p><p>金额：¥{{ currentOrder.amount }}</p><p>收货地址：{{ currentOrder.address }}</p><button @click="showDetailModal=false">关闭</button></div></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const searchOrderNo = ref('')
const filterStatus = ref('all')
const showDetailModal = ref(false)
const currentOrder = ref({})

const orders = ref([])

const fetchOrders = async () => {
  try {
    const res = await axios.get('/api/admin/orders', {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    if (res.data.code === 200) {
      orders.value = res.data.data
    }
  } catch (err) {
    console.error('获取订单失败', err)
    orders.value = []
  }
}

const filteredOrders = computed(() => {
  let result = orders.value
  if (searchOrderNo.value) result = result.filter(o => o.no.includes(searchOrderNo.value))
  if (filterStatus.value !== 'all') result = result.filter(o => o.status === filterStatus.value)
  return result
})

const viewDetail = (order) => { currentOrder.value = order; showDetailModal.value = true }

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.admin-orders { padding: 0; }
h2 { font-size: 22px; margin-bottom: 20px; }
.filters { display: flex; gap: 10px; margin-bottom: 20px; }
.filters input, .filters select { padding: 8px 12px; border: 1px solid #e5e7eb; border-radius: 8px; }
.order-table { background: white; border-radius: 16px; overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #f0f0f0; }
th { background: #fafafa; }
.status { padding: 4px 10px; border-radius: 20px; font-size: 12px; }
.status.pending { background: #fef3c7; color: #d97706; }
.status.paid { background: #d1fae5; color: #065f46; }
.status.shipped { background: #e0e7ff; color: #4338ca; }
.view-btn { background: #f3f4f6; color: #666; padding: 5px 12px; }
.modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content { background: white; border-radius: 16px; padding: 30px; width: 400px; }
.modal-content p { margin: 10px 0; }
.modal-content button { margin-top: 20px; width: 100%; background: #f3f4f6; color: #666; }
</style>
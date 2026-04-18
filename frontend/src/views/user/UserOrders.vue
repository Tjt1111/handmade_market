<template>
  <div class="user-orders">
    <h3>我的订单</h3>
    <div class="order-tabs">
      <button v-for="t in tabs" :key="t.value" :class="{ active: activeTab === t.value }" @click="activeTab = t.value">
        {{ t.label }} ({{ getCount(t.value) }})
      </button>
    </div>

    <div class="order-list" v-if="loading">
      <div class="loading">加载中...</div>
    </div>
    <div class="order-list" v-else-if="orders.length > 0">
      <div v-for="order in filteredOrders" :key="order.id" class="order-card">
        <div class="order-header"><span>订单号：{{ order.no }}</span><span>下单时间：{{ order.time }}</span><span :class="['status', order.status]">{{ getStatusText(order.status) }}</span></div>
        <div class="order-goods"><img :src="order.image"><div><h4>{{ order.name }}</h4><p>数量：{{ order.quantity }} | 单价：¥{{ order.price }}</p></div></div>
        <div class="order-footer"><span>实付：¥{{ order.total }}</span><div><button v-if="order.status === 'pending'" @click="payOrder(order)" class="pay-btn">去支付</button><button v-if="order.status === 'shipped'" @click="confirmReceipt(order)" class="confirm-btn">确认收货</button><button v-if="order.status === 'completed' && !order.commented" @click="commentOrder(order)" class="comment-btn">评价</button></div></div>
      </div>
    </div>
    <div class="empty" v-else>暂无订单</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const activeTab = ref('all')
const loading = ref(true)
const orders = ref([])

const tabs = [{ label: '全部', value: 'all' }, { label: '待支付', value: 'pending' }, { label: '已支付', value: 'paid' }, { label: '已发货', value: 'shipped' }, { label: '已完成', value: 'completed' }]

const fetchOrders = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/user/orders', {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    if (response.data.code === 200) {
      orders.value = response.data.data
    }
  } catch (err) {
    console.error('获取订单失败', err)
    orders.value = []
  } finally {
    loading.value = false
  }
}

const getCount = (status) => status === 'all' ? orders.value.length : orders.value.filter(o => o.status === status).length
const filteredOrders = computed(() => activeTab.value === 'all' ? orders.value : orders.value.filter(o => o.status === activeTab.value))
const getStatusText = (s) => ({ pending: '待支付', paid: '已支付', shipped: '已发货', completed: '已完成' }[s])

const payOrder = async (order) => {
  if (!confirm(`确认支付订单 ${order.no}，金额 ¥${order.total}？`)) return
  try {
    const res = await axios.put(`/api/orders/${order.no}/pay`, { payType: '模拟支付宝' }, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    if (res.data.code === 200) {
      order.status = 'paid'
      alert('支付成功')
    } else {
      alert(res.data.message || '支付失败')
    }
  } catch (err) {
    alert('支付失败：' + (err.response?.data?.message || err.message))
  }
}

const confirmReceipt = async (order) => {
  if (!confirm('确认已收到商品？')) return
  try {
    const res = await axios.put(`/api/orders/${order.no}/confirm`, {}, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    if (res.data.code === 200) {
      order.status = 'completed'
      alert('确认收货成功')
    } else {
      alert(res.data.message || '操作失败')
    }
  } catch (err) {
    alert('操作失败：' + (err.response?.data?.message || err.message))
  }
}

const commentOrder = async (order) => {
  const score = prompt('请给订单评分（1-5）：')
  if (!score) return
  const content = prompt('请输入评价内容：')
  if (!content) return
  try {
    const res = await axios.post(`/api/orders/${order.no}/evaluate`, {
      score: parseInt(score),
      content: content
    }, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    if (res.data.code === 200) {
      order.commented = true
      alert('评价成功')
    } else {
      alert(res.data.message || '评价失败')
    }
  } catch (err) {
    alert('评价失败：' + (err.response?.data?.message || err.message))
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.user-orders { padding: 0; }
h3 { margin-bottom: 20px; }
.order-tabs { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 20px; }
.order-tabs button { background: #f3f4f6; padding: 8px 20px; border-radius: 25px; font-size: 14px; cursor: pointer; }
.order-tabs button.active { background: linear-gradient(135deg, #667eea, #764ba2); color: white; }
.order-card { background: white; border-radius: 16px; margin-bottom: 15px; overflow: hidden; }
.order-header { display: flex; justify-content: space-between; padding: 12px 20px; background: #fafafa; border-bottom: 1px solid #f0f0f0; font-size: 14px; flex-wrap: wrap; gap: 10px; }
.status { padding: 2px 10px; border-radius: 20px; font-size: 12px; }
.status.pending { background: #fef3c7; color: #d97706; }
.status.paid { background: #d1fae5; color: #065f46; }
.status.shipped { background: #e0e7ff; color: #4338ca; }
.status.completed { background: #d1fae5; color: #065f46; }
.order-goods { display: flex; gap: 15px; padding: 15px 20px; }
.order-goods img { width: 60px; height: 60px; border-radius: 8px; object-fit: cover; }
.order-goods h4 { font-size: 16px; margin-bottom: 5px; }
.order-goods p { font-size: 13px; color: #999; }
.order-footer { display: flex; justify-content: space-between; align-items: center; padding: 12px 20px; background: #fafafa; border-top: 1px solid #f0f0f0; flex-wrap: wrap; gap: 10px; }
.order-footer button { padding: 6px 16px; font-size: 13px; }
.pay-btn { background: #f59e0b; color: white; }
.confirm-btn { background: #10b981; color: white; }
.comment-btn { background: #667eea; color: white; }
.loading, .empty { text-align: center; padding: 60px; background: white; border-radius: 16px; color: #999; }
</style>
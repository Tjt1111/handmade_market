<template>
  <div class="shopping-cart">
    <h3>我的购物车</h3>
    
    <div class="cart-header">
      <label class="select-all">
        <input type="checkbox" v-model="selectAll" @change="toggleSelectAll" /> 全选
      </label>
      <span>商品信息</span>
      <span>单价</span>
      <span>数量</span>
      <span>小计</span>
      <span>操作</span>
    </div>

    <div class="cart-list" v-if="cartItems.length > 0">
      <div v-for="item in cartItems" :key="item.id" class="cart-item">
        <input type="checkbox" v-model="item.selected" class="item-checkbox" @change="updateSelected" />
        <div class="item-info">
          <img :src="item.imageUrl" :alt="item.name">
          <div class="item-detail">
            <h4>{{ item.name }}</h4>
            <p>规格：{{ item.spec || '标准款' }}</p>
          </div>
        </div>
        <div class="item-price">¥{{ item.price }}</div>
        <div class="item-quantity">
          <button @click="decreaseQuantity(item)" :disabled="item.quantity <= 1">-</button>
          <span>{{ item.quantity }}</span>
          <button @click="increaseQuantity(item)">+</button>
        </div>
        <div class="item-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
        <div class="item-actions">
          <button class="delete-btn" @click="removeItem(item)">删除</button>
        </div>
      </div>
    </div>

    <div class="cart-empty" v-else>
      <div class="empty-icon">🛒</div>
      <p>购物车还是空的</p>
      <router-link to="/" class="go-shop">去逛逛</router-link>
    </div>

    <div class="cart-footer" v-if="cartItems.length > 0">
      <div class="total-info">
        <span>已选 <strong>{{ selectedCount }}</strong> 件商品</span>
        <span>合计：<strong class="total-price">¥{{ totalPrice.toFixed(2) }}</strong></span>
      </div>
      <button class="checkout-btn" @click="checkout" :disabled="selectedCount === 0">去结算</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { requireTransactionAuth } from '@/utils/auth'
import axios from 'axios'

const router = useRouter()

// 购物车数据
const cartItems = ref([])
const selectAll = ref(true)

// 计算选中商品数量
const selectedCount = computed(() => {
  return cartItems.value.filter(item => item.selected).reduce((sum, item) => sum + item.quantity, 0)
})

// 计算总金额
const totalPrice = computed(() => {
  return cartItems.value.filter(item => item.selected).reduce((sum, item) => sum + (item.price * item.quantity), 0)
})

// 从localStorage加载购物车数据
const loadCart = () => {
  const cart = JSON.parse(localStorage.getItem('shoppingCart') || '[]')
  cartItems.value = cart.map(item => ({ ...item, selected: true }))
  updateSelected()
}

// 保存购物车到localStorage
const saveCart = () => {
  const cartToSave = cartItems.value.map(({ selected, ...item }) => item)
  localStorage.setItem('shoppingCart', JSON.stringify(cartToSave))
}

// 全选/取消全选
const toggleSelectAll = () => {
  cartItems.value.forEach(item => item.selected = selectAll.value)
}

// 更新全选状态
const updateSelected = () => {
  if (cartItems.value.length === 0) {
    selectAll.value = false
    return
  }
  selectAll.value = cartItems.value.every(item => item.selected)
}

// 增加数量
const increaseQuantity = (item) => {
  item.quantity++
  saveCart()
  updateSelected()
}

// 减少数量
const decreaseQuantity = (item) => {
  if (item.quantity > 1) {
    item.quantity--
    saveCart()
    updateSelected()
  }
}

// 删除商品
const removeItem = (item) => {
  if (confirm(`确定删除「${item.name}」吗？`)) {
    const index = cartItems.value.findIndex(i => i.id === item.id && i.spec === item.spec)
    if (index > -1) {
      cartItems.value.splice(index, 1)
      saveCart()
      updateSelected()
      alert('已删除')
    }
  }
}

// 去结算
const checkout = () => {
  requireTransactionAuth(router, async () => {
    const selectedItems = cartItems.value.filter(item => item.selected)
    if (selectedItems.length === 0) {
      alert('请选择要结算的商品')
      return
    }

    const address = prompt('请输入收货地址：')
    if (!address) return

    try {
      const res = await axios.post('/api/orders', {
        items: selectedItems.map(item => ({
          goodsId: item.id,
          quantity: item.quantity,
          spec: item.spec || '标准款'
        })),
        deliveryAddress: address,
        payType: '模拟支付宝'
      }, {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
      })

      if (res.data.code === 200) {
        alert('订单创建成功！')
        // 清空已结算的购物车商品
        const remainingItems = cartItems.value.filter(item => !item.selected)
        cartItems.value = remainingItems
        saveCart()
        updateSelected()
        router.push('/user/orders')
      } else {
        alert(res.data.message || '创建订单失败')
      }
    } catch (err) {
      alert('创建订单失败：' + (err.response?.data?.message || err.message))
    }
  })
}

// 页面加载时加载购物车
onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.shopping-cart { padding: 0; }
h3 { font-size: 22px; margin-bottom: 20px; color: #333; }

/* 表头 */
.cart-header {
  display: grid;
  grid-template-columns: 50px 1fr 100px 120px 100px 80px;
  background: #f8f9fa;
  padding: 14px 16px;
  border-radius: 12px;
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
  align-items: center;
  font-weight: 500;
}

.select-all {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: normal;
}

/* 商品列表 */
.cart-list {
  max-height: 500px;
  overflow-y: auto;
}

.cart-item {
  display: grid;
  grid-template-columns: 50px 1fr 100px 120px 100px 80px;
  background: white;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 12px;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
}

.cart-item:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.item-checkbox {
  width: 20px;
  height: 20px;
  cursor: pointer;
}

/* 商品信息 */
.item-info {
  display: flex;
  gap: 15px;
  align-items: center;
}

.item-info img {
  width: 70px;
  height: 70px;
  border-radius: 10px;
  object-fit: cover;
  background: #f5f5f5;
}

.item-detail h4 {
  font-size: 16px;
  margin-bottom: 6px;
  color: #333;
}

.item-detail p {
  font-size: 12px;
  color: #999;
}

/* 价格 */
.item-price {
  color: #333;
  font-weight: 500;
}

/* 数量控制器 */
.item-quantity {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-quantity button {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: #f3f4f6;
  color: #666;
  font-size: 16px;
  font-weight: bold;
  padding: 0;
  cursor: pointer;
  transition: all 0.2s;
}

.item-quantity button:hover:not(:disabled) {
  background: #667eea;
  color: white;
  transform: none;
}

.item-quantity button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.item-quantity span {
  min-width: 30px;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
}

/* 小计 */
.item-total {
  color: #ff5757;
  font-weight: bold;
  font-size: 16px;
}

/* 操作按钮 */
.item-actions .delete-btn {
  background: none;
  color: #999;
  font-size: 14px;
  padding: 0;
  cursor: pointer;
}

.item-actions .delete-btn:hover {
  color: #ef4444;
  transform: none;
  background: none;
}

/* 空购物车 */
.cart-empty {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 20px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.cart-empty p {
  color: #999;
  margin-bottom: 25px;
  font-size: 16px;
}

.go-shop {
  display: inline-block;
  padding: 12px 40px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 30px;
  text-decoration: none;
  font-size: 16px;
  transition: all 0.3s;
}

.go-shop:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102,126,234,0.4);
}

/* 底部结算栏 */
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 18px 24px;
  border-radius: 16px;
  margin-top: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.total-info {
  display: flex;
  gap: 40px;
  font-size: 16px;
  color: #666;
}

.total-info strong {
  font-size: 18px;
  color: #333;
}

.total-price {
  color: #ff5757;
  font-size: 28px;
  font-weight: bold;
  margin-left: 5px;
}

.checkout-btn {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
  padding: 14px 50px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 40px;
  cursor: pointer;
  transition: all 0.3s;
}

.checkout-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(245,158,11,0.4);
}

.checkout-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* 滚动条样式 */
.cart-list::-webkit-scrollbar {
  width: 6px;
}

.cart-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.cart-list::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 10px;
}

.cart-list::-webkit-scrollbar-thumb:hover {
  background: #999;
}

/* 响应式 */
@media (max-width: 900px) {
  .cart-header, .cart-item {
    grid-template-columns: 40px 1fr 80px 100px 80px 60px;
    font-size: 12px;
  }
  .item-info img { width: 50px; height: 50px; }
  .item-detail h4 { font-size: 14px; }
  .item-price, .item-total { font-size: 14px; }
}
</style>
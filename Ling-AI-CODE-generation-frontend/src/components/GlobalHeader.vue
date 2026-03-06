<template>
  <div class="header-inner">
    <div class="header-left">
      <router-link to="/">
        <div class="logo-area">
          <span class="site-title">Ling Clinic</span>
        </div>
      </router-link>
    </div>

    <div class="header-center">
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="horizontal"
        :items="menuItems"
        @click="handleMenuClick"
        class="nav-menu"
      />
    </div>

    <div class="header-right">
      <div v-if="loginUserStore.loginUser.id">
        <a-dropdown>
          <a-space>
            <a-avatar :src="loginUserStore.loginUser.userAvatar" />
            {{ loginUserStore.loginUser.userName ?? 'Anonymous' }}
          </a-space>
          <template #overlay>
            <a-menu>
              <a-menu-item @click="doLogout">Sign Out</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
      <div v-else>
        <a-button type="primary" @click="() => $router.push('/user/login')">
          Sign In
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser'
import { userLogout } from '@/api/userController'
import { message } from 'ant-design-vue'
import ACCESS_ENUM from '@/access/accessEnum'
import type { MenuProps } from 'ant-design-vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const selectedKeys = ref<string[]>(['/'])

router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

const originItems = [
  { key: '/', label: 'Home', title: 'Home' },
  { key: '/appointment', label: 'Book Appointment', title: 'Book Appointment' },
  { key: '/my-appointments', label: 'My Appointments', title: 'My Appointments' },
  { key: '/ai-chat', label: 'AI Health Assistant', title: 'AI Health Assistant' },
  { key: '/admin/userManage', label: 'User Management', title: 'User Management' },
  { key: '/admin/documents', label: 'Document Management', title: 'Document Management' },
]

const filterMenus = (menus: typeof originItems) => {
  return menus.filter((menu) => {
    if (menu.key.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== ACCESS_ENUM.ADMIN) {
        return false
      }
    }
    return true
  })
}

const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

const handleMenuClick = ({ key }: { key: string }) => {
  selectedKeys.value = [key]
  router.push(key)
}

const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({ userName: 'Not logged in' })
    message.success('Signed out successfully')
    await router.push('/user/login')
  } else {
    message.error('Sign out failed: ' + res.data.message)
  }
}
</script>

<style scoped>
.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
}
.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}
.site-title {
  font-size: 18px;
  font-weight: 700;
  color: #1677ff;
}
.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}
.nav-menu {
  border-bottom: none;
  min-width: 400px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>

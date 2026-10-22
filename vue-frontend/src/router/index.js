import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Update from '../views/Update.vue'
import CreateTie from '../views/CreateTie.vue'
import TieList from '../views/TieList.vue'
import AllTies from '../views/AllTies.vue'
import TieDetail from '../views/TieDetail.vue'
import Netdisk from '../views/Netdisk.vue'
import AdminFileManagement from '../views/AdminFileManagement.vue'
import AiConfigManagement from '../views/AiConfigManagement.vue'
import Checkin from '../views/Checkin.vue'
import BossDashboard from '../views/BossDashboard.vue'
import AIChat from '../views/AIChat.vue'
import SecondHandList from '../views/SecondHandList.vue'
import SecondHandCreate from '../views/SecondHandCreate.vue'
import OrderList from '../views/OrderList.vue'
import PrivateChat from '../views/PrivateChat.vue'
import ProductDetail from '../views/ProductDetail.vue'
import HomeworkList from '../views/HomeworkList.vue'
import HomeworkSubmit from '../views/HomeworkSubmit.vue'
import HomeworkGrading from '../views/HomeworkGrading.vue'
import GrabRedPacket from '../views/GrabRedPacket.vue'
import PublishRedPacket from '../views/PublishRedPacket.vue'
// 好友功能后端尚未实现，暂时下线 /friends 入口；后端补齐后取消下方两处注释即可恢复
// import FriendList from '../views/FriendList.vue'
import EditTie from '../views/EditTie.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/update',
    name: 'Update',
    component: Update
  },
  {
    path: '/createtie',
    name: 'CreateTie',
    component: CreateTie
  },
  {
    path: '/tielist',
    name: 'TieList',
    component: TieList
  },
  {
    path: '/allties',
    name: 'AllTies',
    component: AllTies
  },
  {
    path: '/tiedetail',
    name: 'TieDetail',
    component: TieDetail
  },
  {
    path: '/netdisk',
    name: 'Netdisk',
    component: Netdisk
  },
  {
    path: '/admin/files',
    name: 'AdminFileManagement',
    component: AdminFileManagement
  },
  {
    path: '/admin/ai-config',
    name: 'AiConfigManagement',
    component: AiConfigManagement
  },
  {
    path: '/checkin',
    name: 'Checkin',
    component: Checkin
  },
  {
    path: '/boss-dashboard',
    name: 'BossDashboard',
    component: BossDashboard
  },
  {
    path: '/ai-chat',
    name: 'AIChat',
    component: AIChat
  },
  {
    path: '/secondhand',
    name: 'SecondHandList',
    component: SecondHandList
  },
  {
    path: '/secondhand/create',
    name: 'SecondHandCreate',
    component: SecondHandCreate
  },
  {
    path: '/secondhand/detail',
    name: 'ProductDetail',
    component: ProductDetail
  },
  {
    path: '/secondhand/my',
    name: 'MySecondHand',
    component: SecondHandList
  },
  {
    path: '/order/my/buy',
    name: 'OrderBuy',
    component: OrderList
  },
  {
    path: '/order/my/sell',
    name: 'OrderSell',
    component: OrderList
  },
  {
    path: '/chat',
    name: 'PrivateChat',
    component: PrivateChat
  },
  {
    path: '/homework',
    name: 'HomeworkList',
    component: HomeworkList
  },
  {
    path: '/homework/submit/:questionId',
    name: 'HomeworkSubmit',
    component: HomeworkSubmit
  },
  {
    path: '/homework/grading/:submissionId',
    name: 'HomeworkGrading',
    component: HomeworkGrading
  },
  {
    path: '/redpacket',
    name: 'GrabRedPacket',
    component: GrabRedPacket
  },
  {
    path: '/redpacket/publish',
    name: 'PublishRedPacket',
    component: PublishRedPacket
  },
  // 好友功能后端未实现，暂时隐藏 /friends 路由
  // {
  //   path: '/friends',
  //   name: 'FriendList',
  //   component: FriendList
  // },
  {
    path: '/edittie',
    name: 'EditTie',
    component: EditTie
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

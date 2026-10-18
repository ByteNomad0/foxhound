<template>
  <div class="home">
    <!-- 1. 主横幅 -->
    <div class="banner">
      <div class="banner-content">
        <h1>408 计算机学科考研学习平台</h1>
        <p>系统学习 · 刷题巩固 · 数据分析 · AI 辅导，一站式备战计算机统考</p>
        <div class="subject-tags">
          <el-tag v-for="s in subjectTags" :key="s" effect="dark" size="large" class="subject-tag">{{ s }}</el-tag>
        </div>
        <div class="banner-actions">
          <el-button type="primary" size="large" @click="$router.push('/practice')">开始刷题</el-button>
          <el-button v-if="!store.isLogin" size="large" class="ghost-btn" @click="$router.push('/register')">免费注册</el-button>
        </div>
        <div class="banner-highlights">
          <div v-for="h in highlights" :key="h.title" class="hl-item">
            <el-icon><component :is="h.icon" /></el-icon>
            <span>{{ h.title }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 每日一题 -->
    <h2 class="section-title">每日一题</h2>
    <DailyQuestion />

    <!-- 2. 功能快捷入口 -->
    <h2 class="section-title">功能入口</h2>
    <el-row :gutter="20">
      <el-col v-for="entry in quickEntries" :key="entry.path" :xs="12" :sm="8" :md="6">
        <el-card class="quick-card" shadow="hover" @click="$router.push(entry.path)">
          <div class="quick-icon" :style="{ background: entry.color }">
            <el-icon :size="26"><component :is="entry.icon" /></el-icon>
          </div>
          <div class="quick-title">{{ entry.title }}</div>
          <div class="quick-desc">{{ entry.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 未登录引导 -->
    <div v-if="!store.isLogin" class="guest-cta">
      <div class="guest-cta-text">
        <span class="guest-cta-title">登录后解锁完整学习体验</span>
        <span class="guest-cta-desc">学习日历 · 正确率统计 · 答题历史 · 薄弱点 AI 分析</span>
      </div>
      <el-button type="primary" round @click="$router.push('/login')">立即登录</el-button>
    </div>

    <!-- 3. 学习概览（登录后） -->
    <template v-if="store.isLogin">
      <h2 class="section-title">我的学习概览</h2>
      <el-card class="overview-card" v-loading="loading">
        <el-row :gutter="20" class="stat-row">
          <el-col :xs="12" :sm="6">
            <div class="stat-item"><div class="stat-num">{{ stats?.totalAnswered ?? 0 }}</div><div class="stat-label">累计答题</div></div>
          </el-col>
          <el-col :xs="12" :sm="6">
            <div class="stat-item"><div class="stat-num">{{ stats?.correctCount ?? 0 }}</div><div class="stat-label">答对题数</div></div>
          </el-col>
          <el-col :xs="12" :sm="6">
            <div class="stat-item"><div class="stat-num accent">{{ stats?.accuracy ?? 0 }}%</div><div class="stat-label">正确率</div></div>
          </el-col>
          <el-col :xs="12" :sm="6">
            <div class="stat-item"><div class="stat-num accent">{{ stats?.overallProgress ?? 0 }}%</div><div class="stat-label">学习进度</div></div>
          </el-col>
        </el-row>
        <div class="progress-bar">
          <span class="progress-label">整体学习进度</span>
          <el-progress :percentage="stats?.overallProgress ?? 0" :stroke-width="14" />
        </div>
        <el-button class="detail-btn" text type="primary" @click="$router.push('/stats')">
          查看详细统计 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </el-card>

      <!-- 学习日历 -->
      <h2 class="section-title">学习日历</h2>
      <StudyCalendar />

      <!-- 4. 薄弱知识点（登录后） -->
      <h2 class="section-title">薄弱知识点</h2>
      <el-card class="weak-card" v-loading="loading">
        <div class="weak-header">
          <span class="weak-tip">以下知识点正确率偏低，建议重点复习</span>
          <el-button size="small" type="primary" @click="$router.push('/ai')">用 AI 分析</el-button>
        </div>
        <el-empty v-if="weakPoints.length === 0" description="暂无薄弱点，继续保持！" :image-size="60" />
        <div v-else class="weak-list">
          <div v-for="w in weakPoints" :key="w.knowledgePointId" class="weak-item">
            <div class="weak-info">
              <span class="weak-kp">{{ w.knowledgePointTitle }}</span>
              <span class="weak-course">{{ w.courseName }} · 正确率 {{ w.accuracy }}%</span>
            </div>
            <el-progress class="weak-progress" :percentage="w.accuracy" :status="w.accuracy < 60 ? 'exception' : ''" />
            <el-button size="small" @click="$router.push(`/practice?knowledgePointId=${w.knowledgePointId}`)">去练习</el-button>
          </div>
        </div>
      </el-card>
    </template>

    <!-- 5. 课程列表 -->
    <h2 class="section-title">课程列表</h2>
    <el-row :gutter="20">
      <el-col v-for="course in courses" :key="course.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="course-card" shadow="hover" @click="$router.push(`/course/${course.id}`)">
          <div class="course-icon" :style="{ background: colorOf(course.id) }">
            {{ (course.name || '课')[0] }}
          </div>
          <div class="course-name">{{ course.name }}</div>
          <div class="course-subject">{{ course.subject }}</div>
          <div class="course-desc">{{ course.description }}</div>
          <el-progress v-if="store.isLogin" class="course-progress" :percentage="courseProgressMap[course.id] || 0"
            :stroke-width="8" :format="() => (courseProgressMap[course.id] || 0) + '%'" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 6. 408 考试结构介绍 -->
    <h2 class="section-title">408 考试结构</h2>
    <el-row :gutter="20">
      <el-col v-for="s in examSubjects" :key="s.name" :xs="24" :sm="12" :md="6">
        <el-card class="subject-card" shadow="hover">
          <div class="subject-score">{{ s.score }}<span class="subject-unit">分</span></div>
          <div class="subject-name">{{ s.name }}</div>
          <div class="subject-desc">{{ s.desc }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCourses, getStats, getWeakPoints } from '@/api'
import { useUserStore } from '@/store/user'
import DailyQuestion from '@/components/DailyQuestion.vue'
import StudyCalendar from '@/components/StudyCalendar.vue'

const store = useUserStore()
const courses = ref([])
const stats = ref(null)
const weakPoints = ref([])
const loading = ref(false)

const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#7b68ee']
const colorOf = (id) => colors[(id - 1) % colors.length]

const subjectTags = ['数据结构', '计算机组成原理', '操作系统', '计算机网络']

const highlights = [
  { icon: 'Collection', title: '四门核心科目' },
  { icon: 'EditPen', title: '单选 / 多选 / 判断' },
  { icon: 'MagicStick', title: 'AI 智能答疑' },
  { icon: 'DataAnalysis', title: '学习数据分析' }
]

const examSubjects = [
  { name: '数据结构', score: 45, desc: '线性表 · 树 · 图 · 查找 · 排序' },
  { name: '计算机组成原理', score: 45, desc: '数据表示 · 存储系统 · 指令系统 · CPU' },
  { name: '操作系统', score: 35, desc: '进程管理 · 内存管理 · 文件系统' },
  { name: '计算机网络', score: 25, desc: '体系结构 · 各层协议与应用' }
]

const quickEntries = computed(() => {
  const list = [
    { title: '刷题练习', desc: '选择课程题型开始', icon: 'EditPen', path: '/practice', color: '#409eff' },
    { title: '答题历史', desc: '回顾错题与解析', icon: 'Clock', path: '/history', color: '#67c23a' },
    { title: '学习统计', desc: '正确率与学习进度', icon: 'DataAnalysis', path: '/stats', color: '#e6a23c' },
    { title: 'AI 助手', desc: '答疑与薄弱点分析', icon: 'MagicStick', path: '/ai', color: '#7b68ee' }
  ]
  if (store.isAdmin) {
    list.push({ title: '后台管理', desc: '内容与用户管理', icon: 'Setting', path: '/admin', color: '#f56c6c' })
  }
  return list
})

const courseProgressMap = computed(() => {
  const map = {}
  for (const cp of stats.value?.courseProgress || []) {
    map[cp.courseId] = cp.progress
  }
  return map
})

onMounted(async () => {
  const res = await getCourses()
  courses.value = res.data || []
  if (store.isLogin) {
    loading.value = true
    try {
      const [s, w] = await Promise.all([getStats(), getWeakPoints()])
      stats.value = s.data
      weakPoints.value = (w.data || []).filter((x) => x.accuracy < 80).slice(0, 4)
    } catch (e) {
      // 忽略，保持空状态
    } finally {
      loading.value = false
    }
  }
})
</script>

<style scoped>
.home {
  padding-bottom: 10px;
}

/* 横幅 */
.banner {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #1e4fd8 0%, #409eff 55%, #6fb3ff 100%);
  border-radius: 14px;
  padding: 48px 44px;
  color: #fff;
  margin-bottom: 30px;
}

.banner::before {
  content: '';
  position: absolute;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  top: -120px;
  right: -60px;
}

.banner::after {
  content: '';
  position: absolute;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
  bottom: -90px;
  right: 180px;
}

.banner-content {
  position: relative;
  z-index: 1;
}

.banner h1 {
  font-size: 30px;
  margin-bottom: 12px;
  letter-spacing: 1px;
}

.banner p {
  margin-bottom: 18px;
  opacity: 0.92;
  font-size: 15px;
}

.subject-tags {
  margin-bottom: 20px;
}

.subject-tag {
  margin-right: 10px;
  background: rgba(255, 255, 255, 0.18);
  border-color: rgba(255, 255, 255, 0.3);
}

.banner-actions {
  display: flex;
  gap: 12px;
}

.ghost-btn {
  color: #fff;
  border-color: rgba(255, 255, 255, 0.7);
  background: transparent;
}

.banner-highlights {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 26px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.22);
}

.hl-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  opacity: 0.95;
}

/* 通用标题 */
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  margin: 32px 0 16px;
  color: #303133;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 20px;
  border-radius: 2px;
  background: #409eff;
}

/* 未登录引导条 */
.guest-cta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin: 4px 0 8px;
  padding: 18px 24px;
  border-radius: 12px;
  background: linear-gradient(90deg, #ecf5ff 0%, #f0f9ff 100%);
  border: 1px solid #d9ecff;
}

.guest-cta-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.guest-cta-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.guest-cta-desc {
  font-size: 13px;
  color: #909399;
}

/* 快捷入口 */
.quick-card {
  cursor: pointer;
  text-align: center;
  margin-bottom: 16px;
  transition: transform 0.25s ease;
}

.quick-card:hover {
  transform: translateY(-4px);
}

.quick-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  margin: 4px auto 12px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.25s ease;
}

.quick-card:hover .quick-icon {
  transform: scale(1.06);
}

.quick-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.quick-desc {
  font-size: 12px;
  color: #909399;
}

/* 学习概览 */
.overview-card {
  margin-bottom: 8px;
}

.stat-row {
  text-align: center;
}

.stat-item {
  padding: 6px 0;
}

.stat-num {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-num.accent {
  color: #409eff;
}

.stat-label {
  margin-top: 4px;
  color: #909399;
  font-size: 13px;
}

.progress-bar {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-label {
  white-space: nowrap;
  color: #606266;
  font-size: 14px;
}

.progress-bar .el-progress {
  flex: 1;
}

.detail-btn {
  margin-top: 12px;
}

/* 薄弱知识点 */
.weak-card {
  margin-bottom: 8px;
}

.weak-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}

.weak-tip {
  color: #909399;
  font-size: 13px;
}

.weak-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.weak-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 4px;
  border-bottom: 1px solid #f0f2f5;
}

.weak-item:last-child {
  border-bottom: none;
}

.weak-info {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.weak-kp {
  font-weight: 600;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.weak-course {
  font-size: 12px;
  color: #909399;
}

.weak-progress {
  flex: 1;
  min-width: 80px;
}

/* 课程卡片 */
.course-card {
  cursor: pointer;
  margin-bottom: 20px;
  text-align: center;
  transition: transform 0.25s ease;
}

.course-card:hover {
  transform: translateY(-4px);
}

.course-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  margin: 0 auto 12px;
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.course-name {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 4px;
}

.course-subject {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.course-desc {
  font-size: 13px;
  color: #606266;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 12px;
}

.course-progress {
  margin-top: 4px;
}

/* 考试结构 */
.subject-card {
  text-align: center;
  margin-bottom: 20px;
  transition: transform 0.25s ease;
}

.subject-card:hover {
  transform: translateY(-4px);
}

.subject-score {
  font-size: 34px;
  font-weight: 700;
  color: #409eff;
}

.subject-unit {
  font-size: 14px;
  color: #909399;
  margin-left: 2px;
}

.subject-name {
  font-size: 16px;
  font-weight: 600;
  margin: 8px 0 6px;
}

.subject-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}

/* 移动端 */
@media (max-width: 767px) {
  .banner {
    padding: 28px 20px;
    margin-bottom: 24px;
  }

  .banner h1 {
    font-size: 22px;
  }

  .banner p {
    font-size: 14px;
  }

  .banner-highlights {
    gap: 12px;
  }

  .hl-item {
    font-size: 12px;
  }

  .section-title {
    font-size: 18px;
    margin-top: 24px;
  }

  .guest-cta {
    flex-direction: column;
    align-items: flex-start;
  }

  .stat-num {
    font-size: 22px;
  }

  .weak-item {
    flex-wrap: wrap;
  }

  .weak-info {
    flex: 1 1 100%;
  }

  .weak-progress {
    flex: 1 1 100%;
  }

  .subject-score {
    font-size: 26px;
  }
}
</style>

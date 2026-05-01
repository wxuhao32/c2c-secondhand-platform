<template>
  <div class="search-bar" :class="{ 'is-focused': isFocused, 'is-compact': compact }">
    <div class="search-input-wrapper">
      <el-icon class="search-icon"><Search /></el-icon>
      <input
        ref="inputRef"
        v-model="keyword"
        :placeholder="placeholder"
        class="search-input"
        @focus="handleFocus"
        @blur="handleBlur"
        @keyup.enter="handleSearch"
        @input="handleInput"
      />
      <el-icon v-if="keyword" class="clear-icon" @click="clearKeyword"><CircleClose /></el-icon>
      <button v-if="!compact" class="search-btn" @click="handleSearch">搜索</button>
    </div>

    <Transition name="dropdown">
      <div v-if="showDropdown && (searchHistory.length || hotKeywords.length)" class="search-dropdown">
        <div v-if="searchHistory.length" class="dropdown-section">
          <div class="section-header">
            <span class="section-title">搜索历史</span>
            <span class="clear-history" @click="clearHistory">清除</span>
          </div>
          <div class="tag-list">
            <span
              v-for="item in searchHistory"
              :key="'h-' + item"
              class="search-tag history-tag"
              @mousedown.prevent="selectKeyword(item)"
            >{{ item }}</span>
          </div>
        </div>

        <div v-if="hotKeywords.length" class="dropdown-section">
          <div class="section-header">
            <span class="section-title">热门搜索</span>
          </div>
          <div class="tag-list">
            <span
              v-for="(item, index) in hotKeywords"
              :key="'hot-' + item"
              class="search-tag hot-tag"
              @mousedown.prevent="selectKeyword(item)"
            >
              <span class="hot-rank" :class="{ 'top3': index < 3 }">{{ index + 1 }}</span>
              {{ item }}
            </span>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, CircleClose } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '搜索你想要的宝贝...' },
  compact: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'search', 'select'])

const keyword = ref(props.modelValue)
const isFocused = ref(false)
const showDropdown = ref(false)
const inputRef = ref(null)

const searchHistory = ref([])
const hotKeywords = ref(['iPhone', 'MacBook', 'Switch', '戴森', 'Kindle', 'iPad', 'AirPods', '相机'])

const handleFocus = () => {
  isFocused.value = true
  showDropdown.value = true
}

const handleBlur = () => {
  isFocused.value = false
  setTimeout(() => { showDropdown.value = false }, 200)
}

const handleInput = () => {
  emit('update:modelValue', keyword.value)
}

const handleSearch = () => {
  const kw = keyword.value.trim()
  if (!kw) return
  addToHistory(kw)
  emit('search', kw)
  showDropdown.value = false
}

const selectKeyword = (kw) => {
  keyword.value = kw
  emit('update:modelValue', kw)
  emit('select', kw)
  handleSearch()
}

const clearKeyword = () => {
  keyword.value = ''
  emit('update:modelValue', '')
}

const addToHistory = (kw) => {
  const list = searchHistory.value.filter(item => item !== kw)
  list.unshift(kw)
  searchHistory.value = list.slice(0, 10)
  localStorage.setItem('search_history', JSON.stringify(searchHistory.value))
}

const clearHistory = () => {
  searchHistory.value = []
  localStorage.removeItem('search_history')
}

onMounted(() => {
  try {
    const saved = localStorage.getItem('search_history')
    if (saved) searchHistory.value = JSON.parse(saved)
  } catch {}
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;
@use '@/assets/styles/mixins' as *;

.search-bar {
  position: relative;
  width: 100%;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  height: 44px;
  background: $color-bg-card;
  border: 2px solid $color-border;
  border-radius: $radius-full;
  padding: 0 $spacing-4;
  transition: all $transition-base;

  .is-focused & {
    border-color: $color-primary;
    box-shadow: 0 0 0 3px rgba($primary-500, 0.1);
  }

  .is-compact & {
    height: 36px;
    border-radius: $radius-lg;
    padding: 0 $spacing-3;
  }
}

.search-icon {
  font-size: 18px;
  color: $color-text-placeholder;
  flex-shrink: 0;
  margin-right: $spacing-2;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: $font-size-md;
  color: $color-text-primary;
  font-family: $font-family;

  &::placeholder {
    color: $color-text-placeholder;
  }

  .is-compact & {
    font-size: $font-size-sm;
  }
}

.clear-icon {
  font-size: 16px;
  color: $color-text-placeholder;
  cursor: pointer;
  margin-right: $spacing-2;
  transition: color $transition-fast;

  &:hover {
    color: $color-text-secondary;
  }
}

.search-btn {
  flex-shrink: 0;
  height: 32px;
  padding: 0 $spacing-5;
  border: none;
  border-radius: $radius-full;
  @include gradient-primary;
  color: #fff;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  cursor: pointer;
  transition: all $transition-base;

  &:hover {
    opacity: 0.9;
    transform: scale(1.02);
  }

  &:active {
    transform: scale(0.98);
  }
}

.search-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: $color-bg-card;
  border-radius: $radius-xl;
  box-shadow: $shadow-xl;
  border: 1px solid $color-border-light;
  padding: $spacing-4;
  z-index: $z-dropdown;
}

.dropdown-section {
  &:not(:last-child) {
    margin-bottom: $spacing-4;
    padding-bottom: $spacing-4;
    border-bottom: 1px solid $color-border-light;
  }
}

.section-header {
  @include flex-between;
  margin-bottom: $spacing-3;
}

.section-title {
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
}

.clear-history {
  font-size: $font-size-xs;
  color: $color-text-placeholder;
  cursor: pointer;
  transition: color $transition-fast;

  &:hover {
    color: $color-primary;
  }
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-2;
}

.search-tag {
  display: inline-flex;
  align-items: center;
  padding: $spacing-1 $spacing-3;
  font-size: $font-size-sm;
  border-radius: $radius-full;
  cursor: pointer;
  transition: all $transition-fast;
}

.history-tag {
  background: $neutral-50;
  color: $color-text-secondary;
  border: 1px solid $color-border-light;

  &:hover {
    background: $primary-50;
    color: $color-primary;
    border-color: $primary-100;
  }
}

.hot-tag {
  background: $neutral-50;
  color: $color-text-regular;

  &:hover {
    background: $primary-50;
    color: $color-primary;
  }
}

.hot-rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: $radius-sm;
  font-size: 11px;
  font-weight: $font-weight-semibold;
  margin-right: $spacing-1;
  background: $neutral-200;
  color: $color-text-secondary;

  &.top3 {
    background: $primary-600;
    color: #fff;
  }
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>

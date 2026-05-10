<script setup lang="ts">
import { useRouter } from 'vue-router';
const router = useRouter();
</script>
<template>
  <div class="nf">
    <h2>页面走丢了</h2>
    <button class="btn" @click="router.replace('/home')">回首页</button>
  </div>
</template>
<style lang="scss" scoped>
.nf {
  padding: 80px 24px;
  text-align: center;
}
.btn {
  margin-top: 16px;
  padding: 10px 24px;
  border: none;
  background: var(--bd-primary);
  color: #fff;
  border-radius: 999px;
}
</style>

<template>
  <div>
    <svg-icon icon-class="question" @click="goto" />
  </div>
</template>

<script>
export default {
  name: 'RuoYiDoc',
  data() {
    return {
      url: 'https://mp.weixin.qq.com/mp/appmsgalbum?__biz=MjM5OTMzMDQ0MQ==&action=getalbum&album_id=3893467750584352778#wechat_redirect'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>
<template>
  <div class="bs-template-mag-wrap">
    <SourceMag type="template" />
  </div>
</template>
<script>
import SourceMag from 'data-room-ui/SourceMag'
export default {
  name: '',
  props: {},
  components: {
    SourceMag
  },
  data () {
    return {}
  },
  mounted () { },
  methods: {}
}
</script>

<style lang="scss" scoped>
.bs-template-mag-wrap {
  height:  calc(100vh - 108px);
  width: 100%;
  position: relative;
}
</style>

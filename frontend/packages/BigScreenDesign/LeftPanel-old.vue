<template>
  <transition name="slide-fade">
    <div class="bs-left-panel" @click.stop>
      <div :class="fold ? 'page-left page-left-fold' : 'page-left'" :style="{ height }">
        <el-tabs v-model="activeName" tab-position="left" style="height: 200px" class="left-tabs-box"
          @tab-click="tabClick">

          <el-tab-pane name="default" @click.native="changeActiveCode('')">
            <span slot="label" class="menu-slot" name="default" @click="toggleSidebar">
              <i class="iconfont-bigscreen menu-icon" :class="fold ? 'icon-zhankaicaidan' : 'icon-shouqicaidan'" />
              <span class="menu-title-span">{{ foldText }}</span>
            </span>
          </el-tab-pane>
          <el-tab-pane name="layer">
            <div slot="label" class="menu-slot" name="layer" @dbclick="toggleSidebar">
              <i :class="['iconfont-bigscreen', 'icon-layer']" class="menu-icon" />
              <span class="menu-title-span">图层</span>
            </div>
            <div class="page-left-content">
              <div class="page-left-content-title">
                <div class="page-left-content-title-text">
                  图层
                </div>
              </div>
              <div class="page-left-content-components">
                <el-scrollbar>
                  <LayerList @openRightPanel="openRightPanel" />
                </el-scrollbar>
              </div>
            </div>
          </el-tab-pane>


          <el-tab-pane v-for="menu in menuList" :key="menu.id" :name="menu.name" @click.stop.native="
            fold = false
          changeActiveCode('')
            ">
            <div slot="label" class="menu-slot" @dbclick="toggleSidebar">
              <i :class="['iconfont-bigscreen', menu.icon]" class="menu-icon" />
              <span class="menu-title-span">{{ menu.title }}</span>
            </div>
            <!-- 侧边栏内容区域 -->
            <div class="page-left-content">
              <!--  侧边栏 标题 -->
              <div class="page-left-content-title">
                <div class="page-left-content-title-text">
                  {{ menu.title }}
                </div>
              </div>

              <el-menu default-active="2" class="el-menu-vertical-demo" @open="handleOpen" @close="handleClose"
                background-color="#151A26" text-color="#fff" active-text-color="#ffd04b">
                <el-submenu index="1">
                  <template slot="title">
                    <i class="el-icon-location"></i>
                    <span>类型一</span>
                  </template>
                  <div class="t-collapse-panel__body">
                    <div class="t-collapse-panel__content">
                      <div class="graphic" v-for="(item, index) in items" :key="index" draggable="true">
                        <div class="t-image__wrapper t-image__wrapper--shape-square">
                          <img :src="item.imgSrc" class="t-image t-image--fit-contain t-image--position-center"
                            alt="" />
                        </div>
                        <p>{{ item.title }}</p>
                      </div>
                    </div>
                  </div>
                </el-submenu>
              </el-menu>

              <div class="t-collapse-panel">
                <div class="t-collapse-panel__wrapper">
                  <div class="t-collapse-panel__header t-is-clickable" @click="toggleCollapse">
                    <div class="t-collapse-panel__icon" :class="{ 't-collapse-panel__icon--active': isCollapsed }">
                      <svg class="t-fake-arrow t-collapse-panel__icon--default" width="16" height="16"
                        viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M3.75 5.7998L7.99274 10.0425L12.2361 5.79921" stroke="black" stroke-opacity="0.9"
                          stroke-width="1.3">
                        </path>
                      </svg>
                    </div>
                    <div class="flex middle">
                      <span>2.5D-智慧港口</span>
                    </div>
                  </div>
                  <!-- 控制折叠和展开 -->
                  <div v-show="isCollapsed" class="t-collapse-panel__body">
                    <div class="t-collapse-panel__content">
                      <div class="graphic" v-for="(item, index) in items" :key="index" draggable="true">
                        <div class="t-image__wrapper t-image__wrapper--shape-square">
                          <img :src="item.imgSrc" class="t-image t-image--fit-contain t-image--position-center"
                            alt="" />
                        </div>
                        <p>{{ item.title }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 侧边栏 元素 -->
              <el-scrollbar>
                <div class="page-left-content-components">
                  <div class="draggable chat-list">
                    <div v-for="element in menu.components" :key="element.type + element.name" :class="element.component
                      ? 'item menu-component drag-node'
                      : 'item drag-node'
                      " draggable="true" :data-type="element.type" :data-name="element.name">
                      <div class="component-name">
                        {{ element.title || element.name }}
                      </div>
                      <div class="img_dispaly chooseDragNode" @click.stop="addComponent(element)">
                        <icon-svg v-if="element.icon" :name="element.icon" class="page-opt-list-icon" />
                        <img v-else-if="element.img" :src="element.img" class="page-opt-list-img" alt="">
                        <component :is="element.component" :key="new Date().getTime() + 1"
                          class="page-opt-list-component" />
                      </div>
                    </div>
                  </div>
                </div>
              </el-scrollbar>

            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </transition>
</template>
<script>
import cloneDeep from 'lodash/cloneDeep'
import basicComponents from 'data-room-ui/js/config/basicComponentsConfig'
import g2PlotComponents, { getCustomPlots } from '../G2Plots/plotList'
import echartsComponents from '../Echarts/echartList'
import borderComponents from 'data-room-ui/js/config/borderComponentsConfig'
import decorationComponents from 'data-room-ui/js/config/decorationComponentsConfig'
import configurationComponents from 'data-room-ui/js/config/configurationComponentsConfig'
import LayerList from './LayerList/index.vue'
import { mapMutations } from 'vuex'
import IconSvg from 'data-room-ui/SvgIcon'
import { customSerialize } from 'data-room-ui/js/utils/jsonSerialize.js'
export default {
  name: 'PageLeftPanel',
  components: {
    LayerList,
    IconSvg
  },
  props: {
    headerShow: {
      type: Boolean,
      default: true
    },
    height: {
      type: String,
      default: '100vh'
    }
  },
  data() {
    return {
      // 控制面板展开或折叠的状态
      isCollapsed: false,
      // 图像和标题的数据
      items: [
        { title: '冷却塔', imgSrc: 'https://assets.le5lecdn.com/png/2.5D-制冷站/冷却塔.svg' },
        { title: '冷却塔（远程控制）', imgSrc: 'https://assets.le5lecdn.com/png/2.5D-制冷站/冷却塔（远程控制）.svg' },
        { title: '控制器', imgSrc: 'https://assets.le5lecdn.com/png/2.5D-制冷站/控制器.svg' },
        { title: '控制器（远程控制）', imgSrc: 'https://assets.le5lecdn.com/png/2.5D-制冷站/控制器（远程控制）.svg' },
        { title: '放冷板块', imgSrc: 'https://assets.le5lecdn.com/png/2.5D-制冷站/放冷板块.svg' },
        { title: '水泵', imgSrc: 'https://assets.le5lecdn.com/png/2.5D-制冷站/水泵.gif' },
        { title: '水泵（远程控制）', imgSrc: 'https://assets.le5lecdn.com/png/2.5D-制冷站/水泵（远程控制）.gif' },
        { title: '水泵（远程控制）', imgSrc: 'https://assets.le5lecdn.com/png/2.5D-制冷站/水泵（远程控制）.svg' },
        { title: '离心机', imgSrc: 'https://assets.le5lecdn.com/png/2.5D-制冷站/离心机.svg' }
      ],
      echartsComponents,
      g2PlotComponents,
      activeName: 'chart', // 设置左侧tab栏的默认值
      fold: false, // 控制左侧菜单栏伸缩
      currentTab: 'basic',
      menuList: [
        {
          id: 1,
          name: 'chart',
          title: '基础',
          icon: 'icon-zujian',
          components: basicComponents
        },
        {
          id: 2,
          name: 'g2PlotComponents',
          title: '图表',
          icon: 'icon-jichushuju',
          components: this.g2PlotComponents
        },
        {
          id: 7,
          name: 'echart',
          title: '3D',
          icon: 'icon-jichushuju',
          components: this.echartsComponents
        },
        {
          id: 3,
          name: 'dataV',
          title: '边框',
          icon: 'icon-border-outer',
          components: borderComponents
        },
        {
          id: 4,
          name: 'decoration',
          title: '装饰',
          icon: 'icon-a-1',
          components: decorationComponents
        },
        {
          id: 5,
          name: 'source',
          title: '资源',
          icon: 'icon-tupian',
          components: []
        },
        {
          id: 6,
          name: 'component',
          title: '组件',
          icon: 'icon-zujian1',
          components: ''
        }, {
          id: 8,
          name: 'configuration',
          title: '组态',
          icon: 'icon-zujian',
          components: configurationComponents
        }
      ],
      currentActive: 'chart'
    }
  },
  computed: {
    // 获取当前类型的组件
    currentComponentList() {
      return this.componentList.filter((item) => item.type === this.currentTab)
    },
    foldText() {
      return this.fold ? '展开' : '收起'
    }
  },
  watch: {
    fold(isExpand) {
      if (isExpand && this.activeName === 'default') {
        this.activeName = 'chart'
      }
    }
  },
  created() {
    this.initList()
    this.g2PlotComponents = [...this.g2PlotComponents, ...getCustomPlots()]
    this.menuList[1].components = this.g2PlotComponents
    this.menuList[2].components = this.echartsComponents
  },
  mounted() {
    this.nodeDrag()
  },
  methods: {
    // 切换折叠面板的显示/隐藏
    toggleCollapse() {
      this.isCollapsed = !this.isCollapsed;
    },
    ...mapMutations('bigScreen', ['changeActiveCode']),
    nodeDrag() {
      this.$nextTick(() => {
        const nodes = document.querySelectorAll('.drag-node')
        nodes.forEach((node) => {
          node.addEventListener('dragstart', (event) => {
            const type = node.getAttribute('data-type')
            const name = node.getAttribute('data-name')
            // 从menuList中获取当前拖拽的组件
            const element = this.menuList
              .find((item) => item.name === this.activeName)
              ?.components.find(
                (item) => item.type === type && item.name === name
              )
            /* 设置拖拽传输数据 */
            event.dataTransfer.setData(
              'dragComponent',
              customSerialize({
                ...element,
                offsetX: event.offsetX,
                offsetY: event.offsetY
              })
            )
          })
        })
        // 阻止默认动作
        document.addEventListener(
          'drop',
          (e) => {
            e.preventDefault()
          },
          false
        )
      })
    },
    onClone(e) {
      return cloneDeep(e)
    },
    onStart(e) {
      // this.$emit('onStart', e)
    },
    // 拖拽组件时触发
    onEnd(e) { },
    // 点击左侧组件时触发
    addComponent(element) {
      console.log('element: ', element);
      this.$store.commit('bigScreen/changeActiveItem', element)
      this.$emit('addComponent', element)
    },
    // 初始化
    initList() { },
    // 点击tab标签
    tabClick(tab) {
      this.nodeDrag()
      if (tab.index !== '0') {
        this.fold = false
        this.currentActive = this.activeName
      }
      if (tab.name === 'source') {
        this.fold = true
        this.$emit('toggleLeftSidebar')
        this.$emit('openResource')
        this.$emit('toggleLeftSidebar')
      }
      if (tab.name === 'component') {
        this.fold = true
        this.$emit('toggleLeftSidebar')
        this.$emit('openComponent')
      }
    },
    toggleSidebar() {
      this.fold = !this.fold
      this.$emit('toggleLeftSidebar')
      setTimeout(() => {
        this.activeName = this.currentActive
      })
    },
    openRightPanel(config) {
      this.$emit('openRightPanel', config)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../BigScreenDesign/fonts/iconfont.css';

.bs-left-panel {
  display: flex;
  background-color: var(--bs-background-1);

  .bs-folder-wrap {
    width: 20px;
    position: relative;

    i {
      position: absolute;
      top: 50%;
      left: 0;
      transform: translateY(-50%);
      font-size: 20px;
      color: #fff;
      cursor: pointer;
      z-index: 1;
    }

    &:hover {
      background: rgba(143, 225, 255, 0.1);
    }
  }

  .page-left {
    box-sizing: border-box;

    >* {
      color: #fff;
    }

    .iconfont-bigscreen {
      color: #fff;
    }

    .flexible {
      width: 45px;
      /* border-right: 1px solid #ccc; */
      text-align: center;
    }

    .el-tabs {
      width: 250;
      position: relative;
      height: 100% !important;
      overflow: visible;

      .is-active {
        .iconfont-bigscreen {
          color: var(--bs-el-color-primary);
        }

        .menu-title-span {
          color: var(--bs-el-color-primary);
        }
      }

      .el-tab-pane {
        height: 100%;
      }

      .page-left-content {
        height: 100%;
      }

      ::v-deep .el-tabs__content {
        height: 100%;
        width: 160px;

        .page-left-content-title {
          background-color: var(--bs-background-2);
          color: var(--bs-el-title);
          font-size: 14px;
          margin: 8px;
          padding: 8px 0;

          .page-left-content-title-text {
            /*border-left: 4px solid #007aff;*/
            position: relative;
            padding-left: 12px;

            &:after {
              position: absolute;
              left: 0;
              top: 50%;
              transform: translateY(-50%);
              content: '';
              width: 4px;
              height: 14px;
              background-color: var(--bs-el-color-primary);
            }
          }
        }

        .el-scrollbar__view {
          height: calc(100vh - 55px);
        }

        .page-left-content-components {
          width: 100%;
          text-align: center;
          padding-bottom: 20px;
          margin-bottom: 20px;

          .draggable {
            display: flex;
            flex-wrap: wrap;
            cursor: pointer;
            box-sizing: border-box;
            justify-content: center;
            padding: 8px;
            cursor: move;

            .item {
              width: 100%;
              background: var(--bs-background-2);
              margin-bottom: 8px;

              .component-name {
                background: var(--bs-el-background-3);
                color: var(--bs-el-title);
                font-size: 12px;
                padding: 4px 8px;
                text-align: left;
              }

              .sampleImg {
                margin: 0 auto;
                width: 102px;
                height: 73px;
                display: block;
              }

              .img_dispaly {
                padding: 8px 0;
                margin: 0 auto;
                text-align: center;
                width: 120px;

                .icon-svg {
                  width: 60px !important;
                  height: 60px !important;
                  vertical-align: -0.15em;
                  fill: currentColor;
                  overflow: hidden;
                }

                img {
                  height: 60px;
                  max-width: 100%;
                }
              }
            }

            .menu-component {
              .page-opt-list-component {
                width: 102px;
                height: 75px;
                margin: 0 auto;
              }

              .img_dispaly {
                height: 80px;
              }
            }
          }
        }
      }
    }

    ::v-deep .el-tabs__header {
      width: 45px;
      height: 100%;
      margin-right: 0 !important;
    }

    ::v-deep .el-tabs--left .el-tabs__nav-wrap.is-left::after {
      width: 0 !important;
    }

    .el-tabs__active-bar {
      transform: none !important;
      height: 0 !important;
    }

    .el-tabs__nav-wrap.is-left::after {
      left: 0;
    }

    .el-tabs__nav-wrap {
      height: 100%;
      /* border-right: 1px solid #ccc; */
    }

    ::v-deep .el-tabs__nav-scroll {
      background-color: var(--bs-background-2);
    }
  }

  .page-left-fold {
    width: 45px;
    overflow: hidden;
    /* border-right: 1px solid #ccc; */

    .el-tabs__content {
      border: none;
    }
  }

  .left-tabs-box {
    ::v-deep .el-tabs__item {
      height: 70px !important;

      .menu-slot {
        height: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        flex-wrap: wrap;
        color: #bcc9d4;

        .menu-icon {
          height: 20px;
        }

        .menu-title-span {
          display: block;
          width: 100%;
          font-size: 12px;
          text-align: center;
        }
      }
    }
  }
}

.slide-fade-enter-active {
  transition: all 0.3s ease;
}

.slide-fade-leave-active {
  transition: all 0.8s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter,
.slide-fade-leave-to

/* .slide-fade-leave-active for below version 2.1.8 */
  {
  transform: translateX(10px);
  opacity: 0;
}

::v-deep .el-tabs__item.is-left {
  text-align: center;
  padding: 0;
}

.t-collapse-panel__body {
  max-height: 100vh;
  /* 或设置为你需要的高度 */
  overflow-y: auto;
  /* 当内容超出时启用垂直滚动 */
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  /* 可根据需要调整间隔 */
}

.t-collapse-panel__content {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  /* 同上，调整间隔 */
  width: 100%;
  /* 确保内容区域能容纳两个元素 */
}

.graphic {
  flex: 0 0 calc(50% - 8px);
  /* 每个元素占据一半的宽度，减去间隔 */
  box-sizing: border-box;
}

.t-image__wrapper {
  position: relative;
  height: 120px;
  /* 你可以调整这个高度 */
}

.t-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

p {
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
  margin: 8px 0 0;
}
</style>

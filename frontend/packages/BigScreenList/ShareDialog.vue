<template>
  <el-dialog
    title="分享设置"
    :visible.sync="visible"
    width="600px"
    custom-class="bs-el-dialog"
    class="bs-dialog-wrap bs-el-dialog"
    append-to-body
    @close="handleClose"
  >
    <div class="share-dialog-content">
      <!-- 选项卡：新建分享 / 历史分享 -->
      <el-tabs v-model="activeTab" class="share-tabs">
        <el-tab-pane label="生成分享链接" name="create">
          <el-form :model="form" ref="shareForm" label-width="120px" size="small" class="bs-el-form">
            <el-form-item label="分享类型">
              <el-radio-group v-model="form.shareType" class="bs-el-radio-group">
                <el-radio :label="1">公开分享 (直接访问)</el-radio>
                <el-radio :label="2">加密分享 (密码保护)</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item v-if="form.shareType === 2" label="访问密码" prop="accessCode" :rules="[{ required: true, message: '请输入4位及以上密码', trigger: 'blur' }]">
              <el-input v-model="form.accessCode" placeholder="请输入访问密码" style="width: 220px;" class="bs-el-input" show-password />
            </el-form-item>

            <el-form-item label="有效期">
              <el-select v-model="form.expireType" style="width: 220px;" class="bs-el-select">
                <el-option label="永久有效" value="forever" />
                <el-option label="1天" value="1" />
                <el-option label="7天" value="7" />
                <el-option label="30天" value="30" />
                <el-option label="自定义时间" value="custom" />
              </el-select>
            </el-form-item>

            <el-form-item v-if="form.expireType === 'custom'" label="过期时间">
              <el-date-picker
                v-model="form.expireTime"
                type="datetime"
                placeholder="选择过期时间"
                style="width: 220px;"
                class="bs-el-date-picker"
                value-format="timestamp"
              />
            </el-form-item>

            <el-form-item label="最大访问次数">
              <el-input-number v-model="form.maxViews" :min="0" placeholder="0 表示不限制" style="width: 220px;" class="bs-el-input-number" />
              <span class="help-text">0 表示不限制访问次数</span>
            </el-form-item>

            <el-form-item label="备注说明">
              <el-input v-model="form.remark" type="textarea" placeholder="可选备注说明" rows="2" class="bs-el-input" />
            </el-form-item>
          </el-form>

          <div class="form-actions">
            <el-button size="small" class="bs-el-button-default" @click="visible = false">取消</el-button>
            <el-button type="primary" size="small" :loading="loading" @click="handleCreateShare">生成链接</el-button>
          </div>
        </el-tab-pane>

        <el-tab-pane label="已生成分享链接" name="list">
          <el-table :data="shareList" size="small" style="width: 100%;" v-loading="listLoading" class="bs-el-table">
            <el-table-column prop="shareToken" label="链接" min-width="180">
              <template slot-scope="scope">
                <span class="share-url-text">{{ getShareUrl(scope.row.shareToken) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="密码" width="80">
              <template slot-scope="scope">
                <span>{{ scope.row.shareType === 2 ? '有' : '无' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="viewCount" label="访问次数" width="80" align="center" />
            <el-table-column label="状态" width="100" align="center">
              <template slot-scope="scope">
                <el-switch
                  v-model="scope.row.status"
                  :active-value="1"
                  :inactive-value="0"
                  @change="handleStatusChange(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="copyShareLink(scope.row)">复制</el-button>
                <el-button type="text" size="small" class="delete-btn" @click="handleDeleteShare(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <!-- 成功弹窗/复制提示 -->
      <el-dialog
        title="生成分享链接成功"
        :visible.sync="successVisible"
        width="450px"
        append-to-body
        custom-class="bs-el-dialog"
        class="bs-dialog-wrap bs-el-dialog"
      >
        <div class="success-content">
          <i class="el-icon-success success-icon"></i>
          <p class="success-tip">大屏分享链接已成功生成！</p>
          <div class="url-box">
            <span class="url-text">{{ successUrl }}</span>
            <el-button type="primary" size="mini" @click="copyToClipboard(successUrl)">一键复制</el-button>
          </div>
        </div>
      </el-dialog>
    </div>
  </el-dialog>
</template>

<script>
import Clipboard from 'clipboard'

export default {
  name: 'ShareDialog',
  data() {
    return {
      visible: false,
      activeTab: 'create',
      pageCode: '',
      loading: false,
      listLoading: false,
      successVisible: false,
      successUrl: '',
      form: {
        shareType: 1,
        accessCode: '',
        expireType: 'forever',
        expireTime: null,
        maxViews: 0,
        remark: ''
      },
      shareList: []
    }
  },
  methods: {
    init(pageCode) {
      this.pageCode = pageCode
      this.visible = true
      this.activeTab = 'create'
      this.resetForm()
      this.getShareList()
    },
    resetForm() {
      this.form = {
        shareType: 1,
        accessCode: '',
        expireType: 'forever',
        expireTime: null,
        maxViews: 0,
        remark: ''
      }
      if (this.$refs.shareForm) {
        this.$refs.shareForm.clearValidate()
      }
    },
    handleClose() {
      this.visible = false
    },
    // 获取已生成的分享链接列表
    getShareList() {
      this.listLoading = true
      this.$dataRoomAxios.get('/api/share/list')
        .then(res => {
          // 只筛选出对应当前 pageCode 的分享链接
          this.shareList = res.filter(item => item.code === this.pageCode)
        })
        .finally(() => {
          this.listLoading = false
        })
    },
    // 计算过期时间绝对毫秒数
    calcExpireTime() {
      if (this.form.expireType === 'forever') {
        return null
      }
      if (this.form.expireType === 'custom') {
        return this.form.expireTime
      }
      const days = parseInt(this.form.expireType)
      return Date.now() + days * 24 * 3600 * 1000
    },
    // 创建分享
    handleCreateShare() {
      this.$refs.shareForm.validate(valid => {
        if (!valid) return
        
        this.loading = true
        const params = {
          pageCode: this.pageCode,
          shareType: this.form.shareType,
          accessCode: this.form.shareType === 2 ? this.form.accessCode : null,
          expireTime: this.calcExpireTime(),
          maxViews: this.form.maxViews || null,
          remark: this.form.remark
        }

        this.$dataRoomAxios.post('/api/share/create', params)
          .then(res => {
            this.$message.success('分享链接生成成功')
            this.successUrl = this.getShareUrl(res.shareToken)
            this.successVisible = true
            this.activeTab = 'list'
            this.getShareList()
          })
          .catch(err => {
            this.$message.error(err.message || '生成失败')
          })
          .finally(() => {
            this.loading = false
          })
      })
    },
    // 启用/禁用分享状态
    handleStatusChange(row) {
      this.$dataRoomAxios.put(`/api/share/${row.id}/status`, { status: row.status })
        .then(() => {
          this.$message.success(row.status === 1 ? '分享已启用' : '分享已禁用')
        })
        .catch(() => {
          this.$message.error('修改状态失败')
          row.status = row.status === 1 ? 0 : 1 // 还原状态
        })
    },
    // 删除分享
    handleDeleteShare(row) {
      this.$confirm('确定删除该分享链接？删除后该链接将立即无法访问。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'bs-el-message-box'
      }).then(() => {
        this.$dataRoomAxios.delete(`/api/share/${row.id}`)
          .then(() => {
            this.$message.success('删除成功')
            this.getShareList()
          })
          .catch(() => {
            this.$message.error('删除失败')
          })
      }).catch(() => {})
    },
    // 组装分享链接地址
    getShareUrl(token) {
      const portStr = window.location.port ? `:${window.location.port}` : ''
      return `${window.location.protocol}//${window.location.hostname}${portStr}/s/${token}`
    },
    copyShareLink(row) {
      const url = this.getShareUrl(row.shareToken)
      this.copyToClipboard(url)
    },
    copyToClipboard(text) {
      const input = document.createElement('textarea')
      input.value = text
      document.body.appendChild(input)
      input.select()
      try {
        document.execCommand('copy')
        this.$message.success('已复制到剪贴板')
      } catch (err) {
        this.$message.error('复制失败，请手动复制')
      } finally {
        document.body.removeChild(input)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.share-dialog-content {
  padding: 10px 0;
}

.share-tabs {
  ::v-deep .el-tabs__item {
    font-size: 14px;
    font-weight: 600;
  }
}

.help-text {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  gap: 12px;
}

.share-url-text {
  font-family: monospace;
  word-break: break-all;
}

.delete-btn {
  color: #f56c6c;
  &:hover {
    color: #ff8989;
  }
}

/* 成功生成后的弹窗样式 */
.success-content {
  text-align: center;
  padding: 15px 0;

  .success-icon {
    font-size: 50px;
    color: #67c23a;
    margin-bottom: 12px;
  }

  .success-tip {
    font-size: 16px;
    font-weight: bold;
    color: var(--bs-el-text);
    margin-bottom: 20px;
  }

  .url-box {
    display: flex;
    background: var(--bs-background-1);
    border: 1px solid var(--bs-el-border);
    border-radius: 8px;
    padding: 10px;
    align-items: center;
    justify-content: space-between;
    gap: 10px;

    .url-text {
      font-size: 13px;
      font-family: monospace;
      color: var(--bs-el-title);
      word-break: break-all;
      text-align: left;
      flex: 1;
    }
  }
}
</style>

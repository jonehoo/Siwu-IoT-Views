<template>
  <el-dialog
    width="700px"
    :title="title"
    :visible.sync="setDatasourceVisible"
    :append-to-body="true"
    :close-on-click-modal="false"
    :before-close="handleClose"
    class="bs-dialog-wrap bs-el-dialog"
  >
    <div
      v-loading="linkLoading"
      element-loading-text="正在测试连接..."
      style="padding-right: 80px;"
    >
      <el-form
        ref="dataForm"
        class="bs-el-form"
        :model="dataForm"
        :rules="dataForm.id ? updateRules : rules"
        size="small"
        label-position="right"
        :label-width="dataForm.advanceSettingFlag ? '200px' : '150px'"
      >
        <el-form-item
          label="类型"
          prop="sourceType"
        >
          <el-select
            v-model="dataForm.sourceType"
            placeholder="请选择类型"
            class="bs-el-select"
            popper-class="bs-el-select"
            clearable
            filterable
            @change="sourceTypeChange"
          >
            <el-option
              v-for="sourceType in sourceTypeList"
              :key="sourceType.code"
              :label="sourceType.label"
              :value="sourceType.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="数据源名称"
          prop="sourceName"
        >
          <el-input
            v-model="dataForm.sourceName"
            placeholder="请输入数据源名称"
            class="bs-el-input"
            maxlength="200"
          />
        </el-form-item>

        <!-- Excel 数据源特定字段 -->
        <template v-if="dataForm.sourceType === 'xlxs'">
          <el-form-item
            label="Excel 文件"
            prop="config"
          >
            <el-upload
              ref="upload"
              class="upload-demo"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :file-list="fileList"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
              :limit="1"
              :on-exceed="handleExceed"
              accept=".xlsx,.xls,.csv"
            >
              <el-button
                size="small"
                type="primary"
              >点击上传</el-button>
              <div
                slot="tip"
                class="el-upload__tip"
              >只能上传 xlsx/xls/csv 文件，且不超过10MB</div>
            </el-upload>
          </el-form-item>
          <el-form-item
            label="表头行号"
            prop="headerRowNum"
          >
            <el-input-number
              v-model="dataForm.headerRowNum"
              :min="1"
              :max="10"
            />
          </el-form-item>
        </template>

        <!-- 常规数据库连接字段 -->
        <template v-else>
          <el-form-item
            label="JDBC URL"
            prop="url"
          >
            <el-input
              v-model="dataForm.url"
              placeholder="请输入JDBC URL"
              class="bs-el-input"
              type="textarea"
              rows="4"
              @keydown.enter.native="textareaKeydown"
            />
          </el-form-item>
          <el-form-item
            label="用户名"
            prop="username"
          >
            <el-input
              v-model="dataForm.username"
              placeholder="请输入用户名"
              class="bs-el-input"
            />
          </el-form-item>
          <el-form-item
            label="密码"
            prop="password"
          >
            <el-input
              v-model="dataForm.password"
              :placeholder="dataForm.id ? '请输入密码，不输入代表不更新' : '请输入密码'"
              class="bs-el-input"
              type="password"
              show-password
            />
          </el-form-item>
        </template>

        <el-form-item
          label="备注"
          prop="remark"
        >
          <el-input
            v-model="dataForm.remark"
            placeholder="请输入备注"
            class="bs-el-input"
            type="textarea"
            rows="4"
            maxlength="200"
            @keydown.enter.native="textareaKeydown"
          />
        </el-form-item>
        <template v-if="dataForm.advanceSettingFlag">
          <el-form-item label="初始化连接数">
            <el-input v-model="dataForm.initConnNum" />
          </el-form-item>
          <el-form-item label="最大活动连接数">
            <el-input v-model="dataForm.maxActiveConnNum" />
          </el-form-item>
          <el-form-item label="最大空闲连接数">
            <el-input v-model="dataForm.maxIdleConnNum" />
          </el-form-item>
          <el-form-item label="最小空闲连接数">
            <el-input v-model="dataForm.minIdleConnNum" />
          </el-form-item>
          <el-form-item label="最大等待时间 (ms)">
            <el-input v-model="dataForm.maxWaitConnNum" />
          </el-form-item>
          <el-form-item label="SQL验证查询">
            <el-input
              v-model="dataForm.sqlCheck"
              type="text"
            />
          </el-form-item>
          <el-form-item label="获取连接前校验">
            <el-select
              v-model="dataForm.getconnCheckFlag"
              clearable
            >
              <el-option
                label="是"
                :value="1"
              />
              <el-option
                label="否"
                :value="0"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="归还连接前校验">
            <el-select
              v-model="dataForm.returnCheckFlag"
              clearable
            >
              <el-option
                label="是"
                :value="1"
              />
              <el-option
                label="否"
                :value="0"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="开启空闲回收器校验">
            <el-select
              v-model="dataForm.startIdleCheckFlag"
              clearable
            >
              <el-option
                label="是"
                :value="1"
              />
              <el-option
                label="否"
                :value="0"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="空闲连接回收器休眠时间 (ms)">
            <el-input v-model="dataForm.idleConnDormantTime" />
          </el-form-item>
          <el-form-item label="空闲连接回收检查数">
            <el-input v-model="dataForm.idleConnCheckNum" />
          </el-form-item>
          <el-form-item label="保持空闲最小时间值 (s)">
            <el-input v-model="dataForm.keepIdleMinTime" />
          </el-form-item>
        </template>
      </el-form>
    </div>

    <span
      slot="footer"
      class="dialog-footer"
    >
      <el-button
        type="primary"
        @click="sourceLinkCheck"
      >
        测试
      </el-button>
      <el-button
        class="bs-el-button-default"
        @click="handleClose"
      >
        取消
      </el-button>
      <el-button
        type="primary"
        @click="submitForm"
      >
        确定
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
import { checkRepeat, sourceLinkTest, add, update } from 'data-room-ui/js/utils/dataSourceService'
import { getToken } from '@/utils/auth'

export default {
  props: {
    appCode: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      setDatasourceVisible: false,
      title: '',
      linkLoading: false,
      fileList: [],
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload', // 若依上传接口
      uploadHeaders: { jettaauthtoken: 'Bearer ' + getToken() },
      dataForm: {
        id: '',
        sourceName: '',
        sourceType: 'mysql',
        driverClassName: 'com.mysql.jdbc.Driver',
        username: '',
        password: '',
        url: 'jdbc:mysql://localhost:3306/db_name?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=utf-8&useSSL=false&useOldAliasMetadataBehavior=true',
        remark: '',
        // Excel 特定字段
        config: null,
        headerRowNum: 1,
        // 其他字段...
      },
      rules: {
        sourceType: [
          { required: true, message: '请选择数据源类型', trigger: 'blur' }
        ],
        sourceName: [
          { required: true, message: '请输入数据源名称', trigger: 'blur' },
          { validator: this.validateSourceName, trigger: 'blur' }
        ],
        driverClassName: [
          { required: true, message: '请选择连接驱动', trigger: 'blur' }
        ],
        database: [
          { required: true, message: '请输入数据库名称', trigger: 'blur' },
          { pattern: /^[^\u4e00-\u9fa5]+$/, message: '数据库名称不能包含汉字' }
        ],
        host: [
          { required: true, message: '请输入主机号', trigger: 'blur' },
          {
            pattern: /((25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))/,
            message: '主机号格式有误'
          }
        ],
        port: [
          { required: true, message: '请输入端口', trigger: 'blur' },
          { pattern: /^[0-9]*$/, message: '端口号只能为数字' }
        ],
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { pattern: /^[^\u4e00-\u9fa5]+$/, message: '用户名不能包含汉字' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ],
        url: [
          { required: true, message: '请输入连接url', trigger: 'blur' }
        ],
        coding: [
          { required: true, message: '请选择编码', trigger: 'blur' }
        ],
        // Excel 特定验证规则
        config: [
          { required: true, message: '请上传 Excel 文件', trigger: 'blur' }
        ],
        headerRowNum: [
          { required: true, message: '请输入表头行号', trigger: 'blur' }
        ]
      },
      updateRules: {
        sourceType: [
          { required: true, message: '请选择数据源类型', trigger: 'blur' }
        ],
        sourceName: [
          { required: true, message: '请输入数据源名称', trigger: 'blur' },
          { validator: this.validateSourceName, trigger: 'blur' }
        ],
        driverClassName: [
          { required: true, message: '请选择连接驱动', trigger: 'blur' }
        ],
        database: [
          { required: true, message: '请输入数据库名称', trigger: 'blur' },
          { pattern: /^[^\u4e00-\u9fa5]+$/, message: '数据库名称不能包含汉字' }
        ],
        url: [
          { required: true, message: '请输入连接url', trigger: 'blur' }
        ],
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { pattern: /^[^\u4e00-\u9fa5]+$/, message: '用户名不能包含汉字' }
        ],
        // Excel 特定验证规则
        config: [
          { required: true, message: '请上传 Excel 文件', trigger: 'blur' }
        ],
        headerRowNum: [
          { required: true, message: '请输入表头行号', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    sourceTypeList () {
      return  [
        { label: 'Mysql', code: 'mysql', name: 'com.mysql.jdbc.Driver', url: 'jdbc:mysql://localhost:3306/db_name?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=utf-8&useSSL=false&useOldAliasMetadataBehavior=true' },
        { label: 'ClickHouse', code: 'clickhouse', name: 'ru.yandex.clickhouse.ClickHouseDriver', url: 'jdbc:clickhouse://localhost:8123/db_name' },
        { label: 'PostgreSQL', code: 'postgresql', name: 'org.postgresql.Driver', url: 'jdbc:postgresql://localhost:13308/db_name' },
        { label: 'Oracle', code: 'oracle', name: 'oracle.jdbc.driver.OracleDriver', url: 'jdbc:oracle:thin:@localhost:1521:orcl' },
        { label: 'Sqlserver', code: 'sqlserver', name: 'com.microsoft.sqlserver.jdbc.SQLServerDriver', url: 'jdbc:sqlserver://localhost:1433;databaseName=db_name' },
        { label: 'Sqllite', code: 'sqllite', name: 'org.sqlite.JDBC', url: 'jdbc:sqlite:/path/to/database.db' },
        { label: 'TDengine', code: 'tdengine', name: 'com.taosdata.jdbc.TSDBDriver', url: 'jdbc:TAOS://taosdemo.com:6030/test?user=root&password=taosdata' },
        { label: 'Excel 文件', code: 'xlxs', name: 'excel', url: '' }
      ]                                               
    }
  },
  methods: {
    // 初始化
    init (row) {
      // 清除表单验证
      if (this.$refs.dataForm) {
        this.$refs.dataForm.clearValidate()
      }
      if (row && row.id) {
        this.dataForm = row
        // 如果是 Excel 数据源且已有文件，初始化文件列表
        if (row.sourceType === 'excel' && row.config) {
          this.fileList = [{
            name: row.config.name || '已上传文件',
            url: row.config.url
          }]
        }
      } else {
        // 重置表单
        this.dataForm = {
          id: '',
          sourceName: '',
          sourceType: 'mysql',
          driverClassName: 'com.mysql.jdbc.Driver',
          username: '',
          password: '',
          url: 'jdbc:mysql://localhost:3306/db_name?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=utf-8&useSSL=false&useOldAliasMetadataBehavior=true',
          remark: '',
          config: null,
          headerRowNum: 1
        }
        this.fileList = []
      }
    },
    // 名称校验
    validateSourceName (rule, value, callback) {
      checkRepeat({
        id: this.dataForm.id,
        sourceName: this.dataForm.sourceName,
        moduleCode: this.appCode
      }).then(r => {
        if (r) {
          callback(new Error('数据源名称已存在'))
        } else {
          callback()
        }
      })
    },
    // 数据源类型选择
    sourceTypeChange (code) {
      if (!this.dataForm.id && code) {
        const selectedType = this.sourceTypeList.find(item => item.code === code)
        this.dataForm.driverClassName = selectedType?.name || ''
        this.$set(this.dataForm, 'url', selectedType?.url || '')
        
        // 如果是 Excel 类型，重置相关字段
        if (code === 'excel') {
          this.dataForm.username = ''
          this.dataForm.password = ''
          this.dataForm.url = ''
        }
      }
    },
    // 阻止文本域回车换行
    textareaKeydown () {
      const e = window.event || arguments[0]
      if (e.key === 'Enter' || e.code === 'Enter' || e.keyCode === 13) {
        e.returnValue = false
        return false
      }
    },
    // 连接测试
    sourceLinkCheck () {
      let flag = 0
      this.$refs.dataForm.validate((valid) => {
        if (!valid) {
          flag = 1
          return false
        } else {
          if (flag === 0) {
            // Excel 数据源不需要测试连接
            if (this.dataForm.sourceType === 'excel') {
              this.$message.success('Excel 数据源无需测试连接')
              return
            }
            this.sourceLinkTest(this.dataForm)
          }
        }
      })
    },
    sourceLinkTest (row) {
      this.linkLoading = true
      sourceLinkTest(row).then((r) => {
        this.$message.success(r)
        this.linkLoading = false
      }).catch(() => {
        this.linkLoading = false
      })
    },
    // 取消重制
    handleClose () {
      this.$refs.dataForm.resetFields()
      this.dataForm = {
        id: '',
        sourceName: '',
        sourceType: 'mysql',
        driverClassName: 'com.mysql.jdbc.Driver',
        username: '',
        password: '',
        url: 'jdbc:mysql://localhost:3306/db_name?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=utf-8&useSSL=false&useOldAliasMetadataBehavior=true',
        remark: '',
        config: null,
        headerRowNum: 1
      }
      this.fileList = []
      this.setDatasourceVisible = false
    },
    // 保存
    submitForm () {
      this.$refs.dataForm.validate((valid) => {
        if (valid) {
          const formData = {
            ...this.dataForm,
            moduleCode: this.appCode,
            editable: this.appCode ? 1 : 0
          }
          
          // 如果是 Excel 数据源，需要特殊处理
          if (this.dataForm.sourceType === 'excel') {
            if (!this.dataForm.config) {
              this.$message.error('请上传 Excel 文件')
              return
            }
          }
          
          const saveMethod = this.dataForm.id ? update : add
          saveMethod(formData).then(() => {
            this.$message.success('保存成功')
            // 刷新表格
            this.$emit('refreshTable')
            this.handleClose()
          })
        } else {
          return false
        }
      })
    },
    // 文件上传前校验
    beforeUpload (file) {
      const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || 
                     file.type === 'application/vnd.ms-excel'
      const isCsv = file.type === 'text/csv'
      const isLt10M = file.size / 1024 / 1024 < 10
      
      if (!isExcel && !isCsv) {
        this.$message.error('只能上传 Excel 或 CSV 文件')
        return false
      }
      if (!isLt10M) {
        this.$message.error('上传文件大小不能超过 10MB')
        return false
      }
      return true
    },
    // 文件上传成功
    handleUploadSuccess (response, file, fileList) {
      if (response.code !== 200) {
        this.$message.error(response.msg || '上传失败')
        return
      }
      this.dataForm.config = {
        name: file.name,
        url: response.url,
        path: response.fileName
      }
      this.fileList = fileList
    },
    // 文件超出限制
    handleExceed (files, fileList) {
      this.$message.warning(`当前限制选择 1 个文件，本次选择了 ${files.length} 个文件，共选择了 ${files.length + fileList.length} 个文件`)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets//style/bsTheme.scss';

.upload-demo {
  ::v-deep .el-upload {
    width: 100%;
    .el-button {
      width: 100%;
    }
  }
}
</style>
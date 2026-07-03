<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑论文' : '新增论文'"
    :width="responsive.isMobile.value ? '90%' : '700px'"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
    @closed="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" :label-position="responsive.isMobile.value ? 'top' : 'right'" :label-width="responsive.isMobile.value ? '100%' : '110px'">
      <el-form-item label="论文题目" prop="title">
        <el-input v-model="form.title" placeholder="请输入论文题目" />
      </el-form-item>
      <el-row :gutter="20">
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="论文类型" prop="type">
            <el-select v-model="form.type" placeholder="请选择论文类型" :style="{ width: responsive.isMobile.value ? '100%' : '100%' }">
              <el-option label="期刊论文" value="期刊论文" />
              <el-option label="会议论文" value="会议论文" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="期刊/会议名称" prop="journalName">
            <el-input v-model="form.journalName" placeholder="请输入期刊或会议名称" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="responsive.isMobile.value ? 24 : 8">
          <el-form-item label="卷号">
            <el-input v-model="form.volume" placeholder="卷号" />
          </el-form-item>
        </el-col>
        <el-col :span="responsive.isMobile.value ? 24 : 8">
          <el-form-item label="期号">
            <el-input v-model="form.issue" placeholder="期号" />
          </el-form-item>
        </el-col>
        <el-col :span="responsive.isMobile.value ? 24 : 8">
          <el-form-item label="页码">
            <el-input v-model="form.pages" placeholder="页码" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="发表时间" prop="publishDate">
            <el-date-picker v-model="form.publishDate" type="date" placeholder="请选择发表时间" value-format="YYYY-MM-DD" :style="{ width: responsive.isMobile.value ? '100%' : '100%' }" />
          </el-form-item>
        </el-col>
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="作者排序" prop="authorOrder">
            <el-input-number v-model="form.authorOrder" :min="1" :step="1" placeholder="教师为第几作者" :style="{ width: responsive.isMobile.value ? '100%' : '100%' }" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="作者列表">
            <el-input v-model="form.authors" placeholder="多个作者用分号分隔" />
          </el-form-item>
        </el-col>
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="DOI号">
            <el-input v-model="form.doi" placeholder="请输入DOI号" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="收录标签">
        <el-select v-model="form.indexTypes" multiple placeholder="请选择收录标签" :style="{ width: responsive.isMobile.value ? '100%' : '100%' }">
          <el-option label="SCI" value="SCI" />
          <el-option label="SSCI" value="SSCI" />
          <el-option label="EI" value="EI" />
          <el-option label="CSCD" value="CSCD" />
          <el-option label="CSSCI" value="CSSCI" />
          <el-option label="北大核心" value="北大核心" />
          <el-option label="普通期刊" value="普通期刊" />
          <el-option label="其他" value="其他" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div :class="{ 'mobile-dialog-footer': responsive.isMobile.value }">
        <el-button @click="$emit('update:visible', false)">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确 定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createPaper, updatePaper } from '../../api/paper'
import { useResponsive } from '../../composables/useResponsive'

const props = defineProps({ visible: Boolean, editData: Object })
const emit = defineEmits(['update:visible', 'success'])

// 响应式布局检测
const responsive = useResponsive()

const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  title: '', type: '', journalName: '',
  volume: '', issue: '', pages: '',
  publishDate: '', authors: '', authorOrder: 1,
  doi: '', remark: '', indexTypes: []
})

const rules = {
  title: [{ required: true, message: '请输入论文题目', trigger: 'blur' }],
  type: [{ required: true, message: '请选择论文类型', trigger: 'change' }],
  journalName: [{ required: true, message: '请输入期刊/会议名称', trigger: 'blur' }],
  publishDate: [{ required: true, message: '请选择发表时间', trigger: 'change' }],
  authorOrder: [{ required: true, message: '请输入作者排序', trigger: 'blur' }]
}

watch(() => props.visible, (val) => {
  if (val) {
    if (props.editData) {
      isEdit.value = true
      Object.keys(form).forEach(key => {
        const v = props.editData[key]
        if (key === 'indexTypes') {
          form[key] = Array.isArray(v) ? [...v] : []
        } else if (key === 'authorOrder') {
          form[key] = v || 1
        } else {
          form[key] = v !== undefined ? v : ''
        }
      })
    } else {
      isEdit.value = false
      resetForm()
    }
  }
})

function resetForm() {
  form.title = ''; form.type = ''; form.journalName = ''
  form.volume = ''; form.issue = ''; form.pages = ''
  form.publishDate = ''; form.authors = ''; form.authorOrder = 1
  form.doi = ''; form.remark = ''; form.indexTypes = []
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updatePaper(props.editData.id, form)
      ElMessage.success('编辑成功')
    } else {
      await createPaper(form)
      ElMessage.success('新增成功')
    }
    emit('success')
    emit('update:visible', false)
  } finally { submitting.value = false }
}

function handleClose() {
  formRef.value?.resetFields()
  resetForm()
}
</script>

<style scoped>
/* 移动端底部按钮平分宽度 */
.mobile-dialog-footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.mobile-dialog-footer :deep(.el-button) {
  flex: 1;
  margin: 0;
}
</style>

<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑专利' : '新增专利'"
    :width="responsive.isMobile.value ? '90%' : '650px'"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
    @closed="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      :label-width="responsive.isMobile.value ? '100%' : '100px'"
      :label-position="responsive.isMobile.value ? 'top' : 'right'"
    >
      <el-row :gutter="responsive.isMobile.value ? 0 : 20">
        <el-col :span="24">
          <el-form-item label="专利名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入专利名称" :style="responsive.isMobile.value ? 'width:100%' : ''" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="responsive.isMobile.value ? 0 : 20">
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="专利类型" prop="type">
            <el-select v-model="form.type" placeholder="请选择专利类型" style="width:100%">
              <el-option label="发明专利" value="发明专利" />
              <el-option label="实用新型" value="实用新型" />
              <el-option label="外观设计" value="外观设计" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="专利状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择专利状态" style="width:100%">
              <el-option label="申请中" value="申请中" />
              <el-option label="已授权" value="已授权" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="responsive.isMobile.value ? 0 : 20">
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="专利申请号">
            <el-input v-model="form.applicationNo" placeholder="请输入专利申请号" :style="responsive.isMobile.value ? 'width:100%' : ''" />
          </el-form-item>
        </el-col>
        <el-col v-if="form.status === '已授权'" :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="专利授权号">
            <el-input v-model="form.grantNo" placeholder="请输入专利授权号" :style="responsive.isMobile.value ? 'width:100%' : ''" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="responsive.isMobile.value ? 0 : 20">
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="申请日期" prop="applicationDate">
            <el-date-picker v-model="form.applicationDate" type="date" placeholder="请选择申请日期" value-format="YYYY-MM-DD" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col v-if="form.status === '已授权'" :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="授权日期">
            <el-date-picker v-model="form.grantDate" type="date" placeholder="请选择授权日期" value-format="YYYY-MM-DD" style="width:100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="发明人">
        <el-input v-model="form.inventors" placeholder="多个发明人用分号分隔" :style="responsive.isMobile.value ? 'width:100%' : ''" />
      </el-form-item>
      <el-form-item label="专利权人">
        <el-input v-model="form.patentee" placeholder="请输入专利权人" :style="responsive.isMobile.value ? 'width:100%' : ''" />
      </el-form-item>
      <el-row :gutter="responsive.isMobile.value ? 0 : 20">
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="是否计入考核">
            <el-radio-group v-model="form.isCounted">
              <el-radio :label="1">是</el-radio>
              <el-radio :label="0">否</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" :style="responsive.isMobile.value ? 'width:100%' : ''" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div :class="responsive.isMobile.value ? 'mobile-dialog-footer' : ''">
        <el-button @click="$emit('update:visible', false)">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确 定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createPatent, updatePatent } from '../../api/patent'
import { useResponsive } from '../../composables/useResponsive'

const props = defineProps({ visible: Boolean, editData: Object })
const emit = defineEmits(['update:visible', 'success'])

// 响应式布局检测
const responsive = useResponsive()

const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '', type: '', status: '', applicationNo: '', grantNo: '',
  applicationDate: '', grantDate: '', inventors: '', patentee: '', isCounted: 1, remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入专利名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择专利类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择专利状态', trigger: 'change' }],
  applicationDate: [{ required: true, message: '请选择申请日期', trigger: 'change' }]
}

watch(() => props.visible, (val) => {
  if (val) {
    if (props.editData) {
      isEdit.value = true
      Object.keys(form).forEach(key => {
        form[key] = props.editData[key] !== undefined ? props.editData[key] : (key === 'isCounted' ? 1 : '')
      })
    } else {
      isEdit.value = false
      resetForm()
    }
  }
})

function resetForm() {
  form.name = ''; form.type = ''; form.status = ''; form.applicationNo = ''; form.grantNo = ''
  form.applicationDate = ''; form.grantDate = ''; form.inventors = ''; form.patentee = ''
  form.isCounted = 1; form.remark = ''
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updatePatent(props.editData.id, form)
      ElMessage.success('编辑成功')
    } else {
      await createPatent(form)
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
/* 移动端底部按钮样式 */
.mobile-dialog-footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.mobile-dialog-footer :deep(.el-button) {
  flex: 1;
  margin: 0;
}

/* 移动端表单项样式调整 */
:deep(.el-form-item) {
  margin-bottom: 18px;
}

/* 移动端 label 顶部对齐时的样式 */
:deep(.el-form-item__label) {
  text-align: left;
}
</style>
